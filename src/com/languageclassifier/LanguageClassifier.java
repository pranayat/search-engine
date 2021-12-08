package com.languageclassifier;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import com.common.ConnectionManager;

public class LanguageClassifier {
	
	Connection conn;
	private static final HashMap<String, Integer> languageTermCounts = new HashMap<String, Integer>();
	
	static {
		languageTermCounts.put("eng", 57699127);
		languageTermCounts.put("ger", 46387276);		
	}
	
	public LanguageClassifier () {
		this.conn = (new ConnectionManager()).getConnection();
	}
	
	public static void bootstrap() throws NumberFormatException, SQLException, IOException {
		bootstrapClassifierForLanguage("eng");
		bootstrapClassifierForLanguage("ger");
	}
	
	private static void bootstrapClassifierForLanguage(String language) throws SQLException, NumberFormatException, IOException {
		HashMap<String, String> languageFiles = new HashMap<String, String>();

		languageFiles.put("eng", "english_counts2.txt");
		languageFiles.put("ger", "german_counts2.txt");
		
		Connection conn = (new ConnectionManager()).getConnection();
		String s = "CREATE TABLE IF NOT EXISTS " + language + "_term_prob (id SERIAL PRIMARY KEY, term VARCHAR, prob REAL)";
		PreparedStatement pstmt = conn.prepareStatement(s);
		pstmt.execute();
		s = "CREATE INDEX IF NOT EXISTS term_hash ON " + language + "_term_prob USING hash (term)";
		pstmt = conn.prepareStatement(s);
		pstmt.execute();		
		pstmt = conn.prepareStatement("TRUNCATE " + language + "_term_prob");
		pstmt.execute();		
		
        URL url = new URL("http://practicalcryptography.com/media/miscellaneous/files/" + languageFiles.get(language));
        s = "INSERT INTO " + language + "_term_prob (term, prob) VALUES (?, ?)";
		pstmt = conn.prepareStatement(s);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String line;
        String[] tokens;
        double logProb;
        
	    while ((line = in.readLine()) != null) {
	    	tokens = line.split("\\s+");
	    	logProb = Math.log(Double.parseDouble(tokens[1])/languageTermCounts.get(language));
	    	
			pstmt.setString(1, tokens[0]);
			pstmt.setDouble(2, logProb);
			pstmt.executeUpdate();
			conn.commit();
	    }		
	}
	
	public String classify (String [] docTerms) throws SQLException {
		PreparedStatement pstmt;
		ResultSet rs;
		double engProb, gerProb;
		String[] termListClauseArray;
		String termListClause;
		int testTermCount = 10, foundEngTermCount = 1, foundGerTermCount = 1;
		
		if (docTerms.length < 10) {
			testTermCount = docTerms.length;
		}

		termListClauseArray = new String[testTermCount];
		for (int i = 0; i < testTermCount; i++) {
			termListClauseArray[i] = "?";
		}
		termListClause = String.join(",", termListClauseArray);
		
		pstmt = this.conn.prepareStatement("SELECT SUM(prob) FROM eng_term_prob WHERE term IN (" + termListClause + ")");
		for (int i = 0; i < testTermCount; i++) {
			pstmt.setString(i+1, docTerms[i].toUpperCase());
		}
		rs = pstmt.executeQuery();
		rs.next();
		engProb = rs.getDouble(1);
		
		// consider frequency as 1 for unseen terms
		pstmt = this.conn.prepareStatement("SELECT COUNT(*) FROM eng_term_prob WHERE term IN (" + termListClause + ")");
		for (int i = 0; i < testTermCount; i++) {
			pstmt.setString(i+1, docTerms[i].toUpperCase());
		}
		rs = pstmt.executeQuery();
		rs.next();
		foundEngTermCount = rs.getInt(1);
		if (foundEngTermCount < testTermCount) {
			for (int i = 0; i < testTermCount - foundEngTermCount; i ++) {
				engProb = engProb + Math.log((double)1/languageTermCounts.get("eng"));
			}
		}				
		
		pstmt = this.conn.prepareStatement("SELECT SUM(prob) FROM ger_term_prob WHERE term IN (" + termListClause + ")");
		for (int i = 0; i < testTermCount; i++) {
			pstmt.setString(i+1, docTerms[i].toUpperCase());
		}
		rs = pstmt.executeQuery();
		rs.next();
		gerProb = rs.getDouble(1);
		
		// consider frequency as 1 for unseen terms
		pstmt = this.conn.prepareStatement("SELECT COUNT(*) FROM ger_term_prob WHERE term IN (" + termListClause + ")");
		for (int i = 0; i < testTermCount; i++) {
			pstmt.setString(i+1, docTerms[i].toUpperCase());
		}
		rs = pstmt.executeQuery();
		rs.next();
		foundGerTermCount = rs.getInt(1);
		if (foundGerTermCount < testTermCount) {
			for (int i = 0; i < testTermCount - foundGerTermCount; i ++) {
				gerProb = gerProb + Math.log((double)1/languageTermCounts.get("ger"));
			}
		}
		
		if (gerProb <= engProb) {
			return "eng";
		}
		
		return "ger";
				
	}
}
