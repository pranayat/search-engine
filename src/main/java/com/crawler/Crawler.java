package com.crawler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import com.common.ConnectionManager;
import com.indexer.Indexer;

public class Crawler implements Runnable {
	
	private int maxDepth;
	private int depth;
	private int fanOut;
	private Queue<String> pagesAtLevel;
	private Connection conn;
	private int maxDocs;
	private int crawledDocsCount;
	private String rootHost;
	private String rootPath;
	private int threadId;
	
	public Crawler(Boolean newRun, int threadId, int maxDepth, int fanOut, int maxDocs, String rootHost, String rootPath) {
		this.fanOut = fanOut;
		this.maxDepth = maxDepth;
		this.maxDocs = maxDocs;
		this.rootHost = rootHost;
		this.rootPath = rootPath;
		this.threadId = threadId;
		this.conn = (new ConnectionManager()).getConnection();
		
		this.depth = 0;
		this.crawledDocsCount = 0;
		this.pagesAtLevel= new LinkedList<>();
		
		if (newRun) {
			this.resetState();
		}

		this.loadStateFromDB();
	}
	
	@Override
	public void run() {
		this.crawl();
	}
	
	private void loadStateFromDB () {
		PreparedStatement pstmt;
		ResultSet rs;
		try {
			pstmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM crawler_queue WHERE thread_id = ?");
			pstmt.setInt(1, this.threadId);
			rs = pstmt.executeQuery();
			rs.next();
			Boolean isFreshRun = rs.getInt("total") == 0 ? true : false;
			
			if (isFreshRun) {
				this.addToQueue(new String [] {this.rootHost + "/" + this.rootPath, null}, 0);
				System.out.println("Initialzed crawler for a fresh run");
				return;
			}
			
			pstmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM crawler_queue WHERE popped = 'true' AND thread_id = ?");
			pstmt.setInt(1, this.threadId);
			rs = pstmt.executeQuery();
			rs.next();
			this.crawledDocsCount = rs.getInt("total");
			
			pstmt = conn.prepareStatement("SELECT url FROM crawler_queue WHERE popped != 'true' AND thread_id = ?");
			pstmt.setInt(1, this.threadId);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				this.pagesAtLevel.add(rs.getString(1));
			}
			
			pstmt = conn.prepareStatement("SELECT MAX(depth) AS depth FROM crawler_queue WHERE thread_id = ?");
			pstmt.setInt(1, this.threadId);
			rs = pstmt.executeQuery();
			rs.next();
			this.depth = rs.getInt("depth");
			
			System.out.println("Initialized crawler to resume last run");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return;
	}

	public String getPageAtUrl(String targetURL, String urlParameters) throws Exception {
		  HttpURLConnection connection = null;

		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("POST");
		    connection.setRequestProperty("Content-Type", 
		        "application/x-www-form-urlencoded");

		    connection.setRequestProperty("Content-Length", 
		        Integer.toString(urlParameters.getBytes().length));
		    connection.setRequestProperty("Content-Language", "en-US");  

		    connection.setUseCaches(false);
		    connection.setDoOutput(true);

		    //Send request
		    DataOutputStream wr = new DataOutputStream (
		        connection.getOutputStream());
		    wr.writeBytes(urlParameters);
		    wr.close();

		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		      response.append('\r');
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    throw e;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}	

	public Map<String, String> getUrlObj(String url) {

		Map<String, String> urlObj = new HashMap<>();

		if (url.length() >= 2 && url.substring(0, 2).equals("//")) { // scheme relative url
			url = url.substring(2, url.length());
		}
		
		String[] urlSegments = {};
		String host = "", path = url;
		if (url.length() >= 4 && url.substring(0, 4).equals("www.")) {
			urlSegments = url.split("/");
			host = "https://" + urlSegments[0];
			path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}
		else if (url.length() >= 8 && url.substring(0, 8).equals("https://")) {
			urlSegments = url.substring(8, url.length()).split("/");
			host = "https://" + urlSegments[0];
			path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}
		else if(url.length() >= 7 && url.substring(0, 7).equals("http://")) {
			urlSegments = url.substring(7, url.length()).split("/");
			host = "http://" + urlSegments[0];
			path = String.join("/", Arrays.copyOfRange(urlSegments, 1, urlSegments.length));
		}
		
		if (host.length() > 0) {
			host = host.substring(0, host.length() - (host.endsWith("/") ? 1 : 0));			
		}
		
		if (path.length() > 0) {
			path = path.substring(path.startsWith("/") ? 1 : 0, path.length());			
		}

	    urlObj.put("host", host);
	    urlObj.put("path", path);

	    return urlObj;
	}
	
	private void addToQueue(String [] nodes, int depth) {
		for(String node: nodes) {
			try {
				PreparedStatement pstmt = conn.prepareStatement("INSERT INTO crawler_queue (url, popped, depth, thread_id) VALUES (?, 'false', ?, ?)");
				pstmt.setString(1, node);
				pstmt.setInt(2, depth);
				pstmt.setInt(3, this.threadId);
				pstmt.executeUpdate();
				conn.commit();
			} catch (SQLException e) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					e1.printStackTrace();
				}
				
				break;
			}
			
			this.pagesAtLevel.add(node);
		}

		return;
	}
	
	private String popQueueHead() {
		try {
			PreparedStatement pstmt = conn.prepareStatement("UPDATE crawler_queue SET popped = 'true' WHERE id = (SELECT MIN(id) FROM crawler_queue WHERE popped != 'true' AND thread_id = ?);");
			pstmt.setInt(1,  this.threadId);
			pstmt.executeUpdate();
			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
			}
			
			return null;
		}
		
		return this.pagesAtLevel.poll();
	}
	
	private Boolean isUrlAlreadyCrawled(String url) {
		try {
		PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) AS total FROM crawler_queue WHERE url = ?"); // check across threads
		pstmt.setString(1, url);
		ResultSet rs = pstmt.executeQuery();
		rs.next();
		
		return (rs.getInt("total") > 0 ? true : false);
		} catch (SQLException e) {
		}

		return false;
	}
	
	public void resetState () {
		try {
			PreparedStatement pstmt = conn.prepareStatement("DELETE FROM crawler_queue WHERE thread_id = ?");
			pstmt.setInt(1, this.threadId);
			pstmt.execute();
			conn.commit();
		} catch (SQLException e) {
		}
		
		return;
	}
	
	public void crawl() {

		String host;
		String path;
		while (!this.pagesAtLevel.isEmpty()) {
			System.out.println("Checking depth - " + this.depth + " max depth - " + this.maxDepth);
			if (this.depth >= this.maxDepth || this.crawledDocsCount >= this.maxDocs) {
				System.out.println("Ending crawl session");
				this.resetState();
				return;
			}
			
			String urlToHit = this.popQueueHead();
			if (urlToHit == null) {
				this.depth++;
				this.addToQueue(new String [] {null}, this.depth); // push null to end of queue
				continue;
			} else {
				host = this.getUrlObj(urlToHit).get("host");
			}
			
			
			System.out.println("Parent url - " + urlToHit);
			Page page = new Page();
			String response = "";
			try {
				response = this.getPageAtUrl(urlToHit, "");
				this.crawledDocsCount++;
							    
			} catch (Exception e) {
				System.out.println("Error hitting - " + urlToHit);
				continue;
			}
			
			page.setPageSource(response);
			
			List<String> childLinks;
			
			try {
				childLinks = page.getOutgoingLinks();
			} catch (Exception e) {
				System.out.println("Falling back to regex matching to extract links on this page");
				childLinks = page.getOutgoingLinksViaRegex();
			}
			
			try {
				Indexer ind = new Indexer(conn);
				ind.index(urlToHit, page.getPageText(), childLinks);
			} catch (Exception e) {
				System.out.println("Indexing isn't working");
			}
			
			if (childLinks.size() == 0) {
				return;
			}

			if (childLinks.size() >= this.fanOut) {
				childLinks = childLinks.subList(0, this.fanOut);
			}
			for (String childUrl: childLinks) {
				Map<String, String>childUrlObj = this.getUrlObj(childUrl);
				
				String childHost = childUrlObj.get("host").length() > 0 ? childUrlObj.get("host") : host; // get new host or retain parent host
				String childPath = childUrlObj.get("path");
				System.out.println("child path - " + childPath);
				if (this.isUrlAlreadyCrawled(childHost + "/" + childPath)) {
					System.out.println("Successfuly avoided cycle");
					continue;
				}
				
				this.addToQueue(new String[] { childHost + "/" + childPath}, this.depth);
			}
		}
	}
}
