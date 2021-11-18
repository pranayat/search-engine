package com.indexer;

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

import com.common.ConnectionManager;

public class Indexer {
	
	private Connection conn;
	
	public Indexer() {
		this.conn = (new ConnectionManager()).getConnection();
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
            System.out.println(stopwords);
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
	            	System.out.println(stemmed_word);
	            	System.out.println(metadata.get(stemmed_word));
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
       System.out.println(metadata);

       try {
    	   String SQLdocuments = "INSERT INTO documents (docid SERIAL PRIMARY KEY, url VARCHAR UNIQUE, "
       	   		+ "crawled_on_date TIMESTAMP NULL)" + "VALUES(?,?,?)";
    	   String SQLfeatures = "INSERT INTO features (docid INT, term VARCHAR, "
    	   		+ "term_frequency BIGINT, tf_idf BIGINT)" + "VALUES(?,?,?,?)";
    	   String SQLlinks = "INSERT INTO links (from_docid INT, to_docid INT) "
    	   		 + "VALUES(?,?)";
    	   
    	   PreparedStatement pstmt0 = conn.prepareStatement(SQLdocuments);
    	   PreparedStatement pstmt1 = conn.prepareStatement(SQLfeatures);
    	   PreparedStatement pstmt2 = conn.prepareStatement(SQLlinks);
    	   
    	   //Insert crawled document (Timestamp automatically?)
    	   pstmt0.setString(1,docURL);
    	   Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	   pstmt0.setTimestamp(2,timestamp);
    	   ResultSet rs0 = pstmt0.executeQuery();
    	   int docid0 = rs0.getInt("docid");
    	   
    	   //Insert documents from outgoing links
    	   List<Integer> outgoingids = new ArrayList<Integer>();
//    	   for (int l=0; l<links.size(); l++) {
//    		   pstmt0.setString(1,links.get(l));
//        	   ResultSet rs = pstmt0.executeQuery();
//        	   outgoingids.add(rs.getInt("docid"));
//    	   }
//    	   
//    	   for (Map.Entry<String,Integer> termPair : metadata.entrySet()) {
//    		   pstmt1.setInt(1,docid0);
//    		   pstmt1.setString(2, termPair.getKey());
//    		   pstmt1.setInt(3,termPair.getValue());
//    		   pstmt1.executeQuery();
//    		   
//    		   TFIDFScoreComputer Scorer = new TFIDFScoreComputer();
//    		   Scorer.recomputeScores(termPair.getKey());
//    	   }
//    	   
//    	   for (int l=0; l<outgoingids.size(); l++) {
//    		   pstmt2.setInt(l, docid0);
//    		   pstmt2.setInt(2, outgoingids.get(l));
//    	   }
    	      
       } catch (SQLException e) {
    	   try {
    		   conn.rollback();
    	   } catch (SQLException e1) {
    		   e1.printStackTrace();
    	   }
       }
   }
    

}

