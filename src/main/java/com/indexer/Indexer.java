package main.java.com.indexer;

import java.io.*;

import java.io.FileNotFoundException;  // Import this class to handle errors
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Import the Scanner class to read text files
import java.util.*;
//package com.javacodegeeks.snippets.core;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

//for timestamps
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Indexer{
	
	private Connection conn;
	
	public Indexer(Connection conn) {
		this.conn = conn;
	}
    
    public static Set<String> read_stopwords() { 
    	Set<String> stopwords = new HashSet<String>();
        try {
             
            URL url = new URL("http://snowball.tartarus.org/algorithms/english/stop.txt");
            
            // read text returned by server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
		    while ((line = in.readLine()) != null) {
		      String[] parts = line.split("[|]");
		      if (!(parts[0] == null) && !(parts[0].length() == 0)) {
		    	  parts[0].toLowerCase();
		    	  stopwords.add(parts[0].trim());
		      }
		    }
            in.close();
        }
        catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        catch (IOException e) {
            System.out.println("I/O Error: " + e.getMessage());
        }
        return stopwords;
         
    }
    
    public Map<String, Integer> extractmeta(String[] text, Set<String> stopwords) {
    	Map<String, Integer> metadata = new HashMap<String, Integer>();
    	for (int i=0; i<text.length; i++) {
    		if (!stopwords.contains(text[i])){
	            String stemmed_word = stem_word(text[i]);
	            //System.out.println(stemmed_word);
	            
	            if (metadata.containsKey(stemmed_word)) {
	            	metadata.put(stemmed_word, metadata.get(stemmed_word)+1);
	            }else {
	            	metadata.put(stemmed_word,1);
	            }
    		}
    	}
    	return metadata;
    }
    

    public static String stem_word(String word){
        Stemmer stem = new Stemmer();

        char[] data = word.toCharArray();
        for (int j = 0; j<data.length;j++) {
            char c = data[j];
            stem.add(c);
        }
        stem.stem();

        String re = stem.toString();
        return re;
    }

   public void index (String docURL, String docText, List<String> links) {
	   
	   /*String docText = "Hello, my name is distracting me";
	   String docURL = "wikipedia.de";
	   List<String> links = new ArrayList<String>();
	   links.add("wikipedia.de/news");*/
	   
	   String docTextLow = docText.toLowerCase();
	   Set<String> stopwords = read_stopwords();
       
       String[] text = docTextLow.split("\\s+");
       
       Map <String, Integer> metadata = extractmeta(text, stopwords);

       try {
    	   String SQLinside = "SELECT docid FROM documents WHERE url = ?";
    	   String SQLdocuments = "INSERT INTO documents (url, crawled_on_date)"
    			   + "VALUES(?,?) RETURNING docid";
    	   String SQLfeatures = "INSERT INTO features (docid, term, "
    	   		+ "term_frequency, tf_idf)" + "VALUES(?,?,?,?)";
    	   String SQLlinks = "INSERT INTO links (from_docid, to_docid) "
    	   		 + "VALUES(?,?)";
    	   
    	   PreparedStatement pstmt0 = conn.prepareStatement(SQLdocuments);
    	   PreparedStatement pstmt1 = conn.prepareStatement(SQLfeatures);
    	   PreparedStatement pstmt2 = conn.prepareStatement(SQLlinks);
    	   
    	   //test if document already in database
    	   PreparedStatement pstmtin = conn.prepareStatement(SQLinside);
    	   pstmtin.setString(1, docURL);
    	   ResultSet rsin = pstmtin.executeQuery();
    	   
    	   int docid0;
    	   
    	   if (rsin.next()){
    		   docid0 = rsin.getInt("docid");
    	   } else {
    		   //Insert crawled document (Timestamp automatically?)
        	   pstmt0.setString(1,docURL);
        	   Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        	   pstmt0.setTimestamp(2,timestamp);
        	   ResultSet rs0 = pstmt0.executeQuery();
        	   rs0.next();
        	   docid0 = rs0.getInt("docid");
    	   }
    	   
    	   //Insert documents from outgoing links
    	   int outgoingid;
    	   for (int l=0; l<links.size(); l++) {
    		   
        	   pstmtin.setString(1, links.get(l));
        	   ResultSet rsins = pstmtin.executeQuery();
        	   
        	   if (rsins.next()) {
        		   outgoingid = rsins.getInt("docid");
        	   } else {
        		   pstmt0.setString(1,links.get(l));
        		   Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            	   pstmt0.setTimestamp(2,timestamp);
            	   ResultSet rs = pstmt0.executeQuery();
            	   rs.next();
            	   outgoingid = rs.getInt("docid");
        	   }
        	   
        	   pstmt2.setInt(1, docid0);
        	   pstmt2.setInt(2, outgoingid);
        	   pstmt2.executeUpdate();
    	   }
    	   
    	   for (Map.Entry<String,Integer> termPair : metadata.entrySet()) {
    		   
    		   pstmt1.setInt(1,docid0);
    		   pstmt1.setString(2, termPair.getKey());
    		   pstmt1.setInt(3,termPair.getValue());
    		   pstmt1.setInt(4, 0);
    		   pstmt1.executeUpdate();
    		   
    		   TFIDFScoreComputer Scorer = new TFIDFScoreComputer(conn);
    		   Scorer.recomputeScores(termPair.getKey());
    	   }
    	   
    	   conn.commit();
    	      
       } catch (SQLException e) {
    	   System.out.println(e);
    	   try {
    		   conn.rollback();
    	   } catch (SQLException e1) {
    		   e1.printStackTrace();
    	   }
       }
   }
    

}

