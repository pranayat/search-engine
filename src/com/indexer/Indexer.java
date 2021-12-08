package com.indexer;

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

   public void index (String docURL, String docText, List<String> links) throws SQLException {
	   System.out.println(links);
	   String docTextLow = docText.toLowerCase();
       
	   String [] textArray = docTextLow.split("\\s+");
	   
	   List<String> textArrayWithoutSpecialChars = new ArrayList<String>();
	   
	   String regex = "([a-zA-Z0-9�������]+)";
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
	   
	   Set<String> text;
	   if (textLanguage.equals("eng")) {		   
		   text = this.sr.removeStopwords(textArrayWithoutSpecialChars.toArray(new String[0]));
	   } else {
		   text = new HashSet<>(Arrays.asList(textArrayWithoutSpecialChars.toArray(new String[0])));
	   }
       
       Map <String, Integer> data = this.getTermCounts(text);

       try {
  
    	   String SQLdocuments = "INSERT INTO documents (url, crawled_on_date, pagerank, language)"
    			   + "VALUES(?,?,?,?) RETURNING docid";
    	   String SQLfeatures = "INSERT INTO features (docid, term, "
    	   		+ "term_frequency, df, tf_idf, num_elem, bm25, combined, language)" + "VALUES(?,?,?,?,?,?,?,?,?,?)"; // TODO: check if really needed here
    	   String SQLlinks = "INSERT INTO links (from_docid, to_docid) "
    	   		 + "VALUES(?,?)";
    	   
    	   PreparedStatement pstmtdocuments = conn.prepareStatement(SQLdocuments);
    	   PreparedStatement pstmtfeatures = conn.prepareStatement(SQLfeatures);
    	   PreparedStatement pstmtlinks = conn.prepareStatement(SQLlinks);
    	   
    	   //test if document already in database
    	   PreparedStatement pstmtin = conn.prepareStatement("SELECT docid FROM documents WHERE url = ?");
    	   pstmtin.setString(1, docURL);
    	   ResultSet rsin = pstmtin.executeQuery();
    	   
    	   int docid0;
    	   Timestamp timestamp;
    	   
    	   if (rsin.next()){
    		   docid0 = rsin.getInt("docid");
    		   //set real crawled timestamp
    		   PreparedStatement pstmtupdate = conn.prepareStatement("UPDATE documents SET crawled_on_date = ? WHERE docid=?");
    		   timestamp = new Timestamp(System.currentTimeMillis());
        	   pstmtupdate.setTimestamp(1,timestamp);
        	   pstmtupdate.setInt(2, docid0);
        	   pstmtupdate.executeUpdate();
    	   } else {
    		   //Insert crawled document
        	   pstmtdocuments.setString(1,docURL);
        	   timestamp = new Timestamp(System.currentTimeMillis());
        	   pstmtdocuments.setTimestamp(2,timestamp);
        	   pstmtdocuments.setFloat(3, 0);
        	   pstmtdocuments.setString(4, textLanguage);
        	   ResultSet rs0 = pstmtdocuments.executeQuery();
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

        		   pstmtdocuments.setString(1,links.get(l));
        		   //Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // TODO: set to null here and update when docURL received
        		   timestamp = null;
        		   pstmtdocuments.setTimestamp(2,timestamp);
        		   pstmtdocuments.setFloat(3, 0);
            	   ResultSet rs = pstmtdocuments.executeQuery();

            	   rs.next();
            	   outgoingid = rs.getInt("docid");
        	   }
        	   
        	   pstmtlinks.setInt(1, docid0);
        	   pstmtlinks.setInt(2, outgoingid);
        	   pstmtlinks.executeUpdate();
    	   }
    	   
    	   for (Map.Entry<String,Integer> termPair : data.entrySet()) {
    		   
    		   pstmtfeatures.setInt(1,docid0);
    		   pstmtfeatures.setString(2, termPair.getKey());
    		   pstmtfeatures.setInt(3,termPair.getValue());
    		   pstmtfeatures.setInt(4, 1);
    		   pstmtfeatures.setDouble(5, 0);
    		   pstmtfeatures.setInt(6, data.size());
    		   pstmtfeatures.setDouble(7,0);
    		   pstmtfeatures.setDouble(8,0);
    		   pstmtfeatures.setString(9, textLanguage);
    		   pstmtfeatures.executeUpdate();
    		   
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

