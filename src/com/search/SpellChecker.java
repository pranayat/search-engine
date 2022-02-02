package com.search;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

import com.languageclassifier.LanguageClassifier;

public class SpellChecker {
	
	Connection conn;
	
	public SpellChecker(Connection conn) {
		this.conn = conn;
	}	

	public String[][] suggest(String[] query, String selectedLang) throws SQLException{
			String lang;
			if (selectedLang.length() > 0) {
				lang = selectedLang;
			} else {
				LanguageClassifier langclass = new LanguageClassifier();
				lang = langclass.classify(query);
			}
	    
	    String[][] suggestedQuery = new String[query.length][5];
	    
	    Statement stmt = conn.createStatement();
	    ResultSet rs;
	    //could also search in our database and return with the highest freq
	    if (lang.equals("eng")){
	    	int pos = 0;
	    	for (String word : query) {
	    		word = word.toUpperCase();
	    		rs = stmt.executeQuery("SELECT * FROM best_fit_eng('" + word + "')");
	    		
			    int i = 0;
			    while (rs.next()) {
		    		suggestedQuery[pos][i] = rs.getString(1);
		    		i++;
			    }
	    		pos++;
	    	}
	    	
	    } else {
	    	int pos = 0;
	    	for (String word : query) {
	    		word = word.toUpperCase();
	    		rs = stmt.executeQuery("SELECT * FROM best_fit_ger('" + word + "')");
	    		
			    int i = 0;
			    while (rs.next()) {
		    		suggestedQuery[pos][i] = rs.getString(1);
		    		i++;
			    }
	    		pos++;
	    	}
	    	
	    }
		return suggestedQuery;
	}
}
