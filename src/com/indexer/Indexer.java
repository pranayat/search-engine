package com.indexer;

import java.lang.reflect.Array;
// Import the Scanner class to read text files
import java.util.*;
//for timestamps
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.stream.*;

import com.languageclassifier.LanguageClassifier;

public class Indexer{
	
	private Connection conn;
	Set<String> stopwords;
	StopwordRemover sr;
	LanguageClassifier languageClassifier;
	
	public Indexer(Connection conn) {
		this.conn = conn;
		this.sr = new StopwordRemover();
		this.languageClassifier = new LanguageClassifier();
	}
    
    private Map<String, Integer> getCounts(List<String> text, Boolean stem) {

    	Map<String, Integer> countMap = new HashMap<String, Integer>();
    	String token = null;
    	for (String word : text) {
    		if (stem) {
    			token = stem_word(word); 
    		} else {
    			token = word;
    		}
				
			if (countMap.containsKey(token)) {
				countMap.put(token, countMap.get(token)+1);
			} else {
				countMap.put(token,1);
			}
    	}
    	
    	return countMap;
    }
    
    public Map<String, Integer> getTermCounts(List<String> text) {
    	return this.getCounts(text, false);
    }
    
    public Map<String, Integer> getStemCounts(List<String> text) {
    	return this.getCounts(text, true);
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


   public int index (String docURL, String docText, List<String> links, int k) throws SQLException {

	   String docTextLow = docText.toLowerCase();
       
	   String [] textArray = docTextLow.split("\\s+");
	   
	   List<String> textArrayWithoutSpecialChars = new ArrayList<String>();
	   
	   String regex = "([a-zA-Z0-0äüëö]+)";
	   Pattern pattern = Pattern.compile(regex);
	   for(String text: textArray) {
			Matcher matcher = pattern.matcher(text);
			String iText = "";
			while(matcher.find()) {
				iText = iText + matcher.group(1);
			}
			if (iText.length() > 0) {
				textArrayWithoutSpecialChars.add(iText);				
			}
	   }
	   
	   String textLanguage = this.languageClassifier.classify(textArrayWithoutSpecialChars.toArray(new String[0]));
	   
	   List<String> text;
	   if (textLanguage.equals("eng")) {		   
		   text = this.sr.removeStopwords(textArrayWithoutSpecialChars.toArray(new String[0]));
	   } else {
		   text = Arrays.asList(textArrayWithoutSpecialChars.toArray(new String[0]));
	   }
       

       Map <String, Integer> data = null;
       
       if (textLanguage.equals("eng")) {
    	   data = this.getStemCounts(text);
       } else {
    	   data = this.getTermCounts(text);
       }
       
       
       int docid0 = -1;
       try {
    	   
    	   PreparedStatement pstmtwords = conn.prepareStatement("INSERT INTO dbwords (term, language) VALUES(?,?)");
    	   for (String word: text) {
    		   pstmtwords.setString(1, word.toUpperCase());
    		   pstmtwords.setString(2, textLanguage);
    		   pstmtwords.executeUpdate();
    	   }
    	   conn.commit();
  
    	   String SQLdocuments = "INSERT INTO documents (url, crawled_on_date, pagerank, language, doc_text)"
    			   + "VALUES(?,?,?,?,?) RETURNING docid";
    	   String SQLfeatures = "INSERT INTO features (docid, term, "
    	   		+ "term_frequency, df, tf_idf, bm25, combined, language)" + "VALUES(?,?,?,?,?,?,?,?)"; // TODO: check if really needed here
    	   String SQLlinks = "INSERT INTO links (from_docid, to_docid) "
    	   		 + "VALUES(?,?)";
    	   String SQLshingles = "INSERT INTO kshingles (docid, shingle, md5value) VALUES(?,?,?)";
    	   
    	   PreparedStatement pstmtdocuments = conn.prepareStatement(SQLdocuments);
    	   PreparedStatement pstmtfeatures = conn.prepareStatement(SQLfeatures);
    	   PreparedStatement pstmtlinks = conn.prepareStatement(SQLlinks);
    	   PreparedStatement pstmtshingle = conn.prepareStatement(SQLshingles);
    	   CallableStatement cstmt;
    	   
    	   //test if document already in database
    	   PreparedStatement pstmtin = conn.prepareStatement("SELECT docid, crawled_on_date FROM documents WHERE url = ?");
    	   pstmtin.setString(1, docURL);
    	   ResultSet rsin = pstmtin.executeQuery();
    	   
    	   Timestamp timestamp;
    	   Timestamp ts;
    	   boolean alreadyInFeatures = true;
    	   
    	   if (rsin.next()){
    		   docid0 = rsin.getInt("docid");
    		   ts = rsin.getTimestamp("crawled_on_date");
    		   if (ts == null) {
    			   alreadyInFeatures = false;
    		   }
    		   //set real crawled timestamp
    		   PreparedStatement pstmtupdate = conn.prepareStatement("UPDATE documents SET crawled_on_date = ? WHERE docid=?");
    		   timestamp = new Timestamp(System.currentTimeMillis());
        	   pstmtupdate.setTimestamp(1,timestamp);
        	   pstmtupdate.setInt(2, docid0);
        	   pstmtupdate.executeUpdate();
        	   
        	   pstmtupdate = conn.prepareStatement("UPDATE documents SET doc_text = ? WHERE docid=?");
        	   pstmtupdate.setString(1, docText);
        	   pstmtupdate.setInt(2, docid0);
        	   pstmtupdate.executeUpdate();
    
    	   } else {
    		   
    		   //Insert crawled document
        	   pstmtdocuments.setString(1,docURL);
        	   timestamp = new Timestamp(System.currentTimeMillis());
        	   pstmtdocuments.setTimestamp(2,timestamp);
        	   pstmtdocuments.setFloat(3, 0);
        	   pstmtdocuments.setString(4, textLanguage);
        	   pstmtdocuments.setString(5, docText);
        	   ResultSet rs0 = pstmtdocuments.executeQuery();
        	   rs0.next();
        	   docid0 = rs0.getInt("docid");
        	   
    	   }
    	   //insert into features
    	   if (!alreadyInFeatures) {
    		   for (Map.Entry<String,Integer> termPair : data.entrySet()) {

        		   pstmtfeatures.setInt(1,docid0);
        		   pstmtfeatures.setString(2, termPair.getKey());
        		   pstmtfeatures.setInt(3,termPair.getValue());
        		   pstmtfeatures.setInt(4, 1);
        		   pstmtfeatures.setDouble(5, 0);
        		   pstmtfeatures.setDouble(6,0);
        		   pstmtfeatures.setDouble(7,0);
        		   pstmtfeatures.setString(8, textLanguage);
        		   pstmtfeatures.executeUpdate();
      
        	   }
    		   
    		   for (int l = 0; l<(textArrayWithoutSpecialChars.size()-k); l++) {
    			   StringBuilder stringBuilder = new StringBuilder();
    			   for (int shinglelen = 0; shinglelen < k; shinglelen++) {
    				   stringBuilder.append(textArrayWithoutSpecialChars.get(l+shinglelen));
    			   }
    			   String shingle = stringBuilder.toString();
    			   
    			   pstmtshingle.setInt(1,docid0);
    			   pstmtshingle.setString(2, shingle);
    			   pstmtshingle.setInt(3, 0);
    			   pstmtshingle.executeUpdate();
    			   
    			   cstmt = conn.prepareCall("select computeIntMD5(?,?)");
   			       cstmt.setInt(1, docid0);
   			       cstmt.setString(2, shingle);
   			       cstmt.execute();
    			   
    		   }
    	   }
    	   
    	   //Insert documents from outgoing links
    	   int outgoingid;
    	   for (int l=0; l<links.size(); l++) {
    		   
        	   pstmtin.setString(1, links.get(l));
        	   ResultSet rsins = pstmtin.executeQuery();
        	   
        	   if (rsins.next()) {
        		   outgoingid = rsins.getInt("docid");
        	   } else {

        		   pstmtdocuments.setString(1,links.get(l));
        		   //Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // TODO: set to null here and update when docURL received
        		   timestamp = null;
        		   pstmtdocuments.setTimestamp(2,timestamp);
        		   pstmtdocuments.setFloat(3, 0);
        		   pstmtdocuments.setString(4, textLanguage);
        		   pstmtdocuments.setString(5, "");
            	   ResultSet rs = pstmtdocuments.executeQuery();

            	   rs.next();
            	   outgoingid = rs.getInt("docid");
        	   }
        	   
        	   pstmtlinks.setInt(1, docid0);
        	   pstmtlinks.setInt(2, outgoingid);
        	   pstmtlinks.executeUpdate();
    	   }
    	   
    	   conn.commit();
       } catch (SQLException e) {
    	   e.printStackTrace();
    	   try {
    		   conn.rollback();
    	   } catch (SQLException e1) {
    		   e1.printStackTrace();
    	   }
       }
       
	   return docid0;
   }
    

}

