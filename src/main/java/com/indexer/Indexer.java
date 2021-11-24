package main.java.com.indexer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// Import the Scanner class to read text files
import java.util.*;
//for timestamps
import java.sql.Timestamp;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Indexer{
	
	private Connection conn;
	Set<String> stopwords;
	StopwordRemover sr;
	
	public Indexer(Connection conn) {
		this.conn = conn;
		this.sr = new StopwordRemover();
	}
    
    public Map<String, Integer> getTermCounts(Set<String> text) {
    	Map<String, Integer> data = new HashMap<String, Integer>();
    	for (String word : text) {
            String stemmed_word = stem_word(word);
            //System.out.println(stemmed_word);
            
            if (data.containsKey(stemmed_word)) {
            	data.put(stemmed_word, data.get(stemmed_word)+1);
            }else {
            	data.put(stemmed_word,1);
            }
    	}
    	return data;
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
	   
	   String docTextLow = docText.toLowerCase();
       
	   String [] textArray = docTextLow.split("\\s+");
	   List<String> textArrayWithoutSpecialChars = new ArrayList<String>();
	   
	   String regex = "([a-zA-Z0-9äöüÄÖÜß]+)";
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
	   
       Set<String> text = this.sr.removeStopwords(textArrayWithoutSpecialChars.toArray(new String[0]));
       
       Map <String, Integer> data = this.getTermCounts(text);

       try {
    	   String SQLinside = "SELECT docid FROM documents WHERE url = ?";
    	   String SQLdocuments = "INSERT INTO documents (url, crawled_on_date)"
    			   + "VALUES(?,?) RETURNING docid";
    	   String SQLfeatures = "INSERT INTO features (docid, term, "
    	   		+ "term_frequency, df, tf_idf)" + "VALUES(?,?,?,?,?)";
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
        		   Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // TODO: set to null here and update when docURL received
            	   pstmt0.setTimestamp(2,timestamp);
            	   ResultSet rs = pstmt0.executeQuery();
            	   rs.next();
            	   outgoingid = rs.getInt("docid");
        	   }
        	   
        	   pstmt2.setInt(1, docid0);
        	   pstmt2.setInt(2, outgoingid);
        	   pstmt2.executeUpdate();
    	   }
    	   
    	   for (Map.Entry<String,Integer> termPair : data.entrySet()) {
    		   
    		   pstmt1.setInt(1,docid0);
    		   pstmt1.setString(2, termPair.getKey());
    		   pstmt1.setInt(3,termPair.getValue());
    		   pstmt1.setInt(4, 1);
    		   pstmt1.setInt(5, 0);
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

