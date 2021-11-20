package main.java.com.crawler;

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

import main.java.com.common.ConnectionManager;
import main.java.com.indexer.Indexer;

public class Crawler implements Runnable {
	
	private int maxDepth;
	private int depth;
	private int fanOut;
	private Queue<String> pagesAtLevel;
	private Connection conn;
	private int maxDocs;
	private int crawledDocsCount;
	private String rootUrlString;
	private int threadId;
	
	public Crawler(Boolean newRun, int threadId, int maxDepth, int fanOut, int maxDocs, String rootUrlString) {
		this.fanOut = fanOut;
		this.maxDepth = maxDepth;
		this.maxDocs = maxDocs;
		this.rootUrlString = rootUrlString;
		this.threadId = threadId;
		this.conn = (new ConnectionManager()).getConnection();
		
		this.depth = 0;
		this.crawledDocsCount = 0;
		this.pagesAtLevel= new LinkedList<>();
		
		if (newRun) {
			this.resetQueueState();
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
				this.addToQueue(new String [] {this.rootUrlString, null}, 0);
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
		    StringBuilder response = new StringBuilder();
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
			
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
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
			e.printStackTrace();
		}

		return true; //consider crawled to be safe and not violate unique constraint
	}
	
	public void resetQueueState () {
		try {
			PreparedStatement pstmt = conn.prepareStatement("DELETE FROM crawler_queue WHERE thread_id = ?");
			pstmt.setInt(1, this.threadId);
			pstmt.execute();
			conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return;
	}
	
	public void crawl() {

		String urlToHit, response = "";
		Page page;
		Url parentUrl = null, childUrl = null;
		Indexer ind;
		List<String> childLinks = new ArrayList<String>(), normalizedChildLinks = new ArrayList<String>();
		
		while (!this.pagesAtLevel.isEmpty()) {
			if (this.depth >= this.maxDepth || this.crawledDocsCount >= this.maxDocs) {
				System.out.println("Ending crawl session for thread = " + this.threadId
						+ " docs crawled = " + this.crawledDocsCount + " till depth = " + this.depth);
				this.resetQueueState();
				return;
			}
			
			urlToHit = this.popQueueHead();
			if (urlToHit == null) {
				this.depth++;
				this.addToQueue(new String [] {null}, this.depth); // push null to end of queue
				continue;
			} else {
				try {
					parentUrl = new Url(urlToHit, null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			System.out.println("Hitting - " + urlToHit);
			try {
				response = this.getPageAtUrl(urlToHit, "");
				this.crawledDocsCount++;
							    
			} catch (Exception e) {
				System.out.println("Error hitting - " + urlToHit);
				e.printStackTrace();
				continue;
			}
			
			page = new Page();
			page.setPageSource(response);
			
			try {
				childLinks = page.getOutgoingLinks();
			} catch (Exception e) {
				System.out.println("Falling back to regex matching to extract links on this page");
				childLinks = page.getOutgoingLinksViaRegex();
			}
			
			if (childLinks.size() == 0) {
				return;
			}

			if (childLinks.size() >= this.fanOut) {
				childLinks = childLinks.subList(0, this.fanOut);
			}

			for (String childUrlString: childLinks) {
				try {
					childUrl = new Url(childUrlString, parentUrl);
					if (this.isUrlAlreadyCrawled(childUrl.getUrlString())) {
						continue;
					}
					
					normalizedChildLinks.add(childUrl.getUrlString());
					this.addToQueue(new String[] { childUrl.getUrlString() }, this.depth);
				} catch (Exception e) {
				}
			}
			
			try {
				ind = new Indexer(conn);
				ind.index(urlToHit, page.getPageText(), normalizedChildLinks);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error in call to indexer");
			}
		}
	}
}