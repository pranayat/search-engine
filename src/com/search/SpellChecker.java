package com.search;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;

import com.languageclassifier.LanguageClassifier;

public class SpellChecker {
	
	Connection conn;
	
	public SpellChecker(Connection conn) {
		this.conn = conn;
	}
	
	public void bestfitfunctioneng() {
		try {
			Statement stmt = conn.createStatement();
		      //Query to create a function
		    String query = "CREATE OR REPLACE FUNCTION bestFiteng(word VARCHAR)"
		    		+ " RETURNS varchar"
		    		+ " AS $$"
		    		+ "	  declare result varchar;"
		    		+ "	  BEGIN"
		    		+ "	  select term into result "
		    		+ "   from eng_term_prob where levenshtein(term, word) <2 "
		    		+ "   ORDER BY levenshtein(term, word) ASC, prob DESC LIMIT 1"
		    		+ " return result;"
		    		+ " END;"
		    		+ " $$ language plpgsql;";
		    stmt.execute(query);
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	       }
	}
	
	public void bestfitfunctionger() {
		try {
			Statement stmt = conn.createStatement();
		      //Query to create a function
		    String query = "CREATE OR REPLACE FUNCTION bestFitger(word VARCHAR)"
		    		+ " RETURNS varchar"
		    		+ " AS $$"
		    		+ "	  declare result varchar;"
		    		+ "	  BEGIN"
		    		+ "	  select term into result "
		    		+ "   from ger_term_prob where levenshtein(term, word) <2 "
		    		+ "   ORDER BY levenshtein(term, word) ASC, prob DESC LIMIT 1"
		    		+ " return result;"
		    		+ " END;"
		    		+ " $$ language plpgsql;";
		    stmt.execute(query);
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	       }
	}
	

	public String[] suggest(String[] query) throws SQLException{
	    LanguageClassifier langclass = new LanguageClassifier();
	    String lang = langclass.classify(query);
	    
	    String[] suggestedQuery = new String[query.length];
	    
	    bestfitfunctioneng();
	    bestfitfunctionger();
	    
	    CallableStatement cstmt;
	    String rsterm;
	    //could also search in our database and return with the highest freq
	    if (lang.equals("eng")){
	    	int pos = 0;
	    	for (String word : query) {
	    		word.toUpperCase();
	    		//find nearest english word with highest probability
	    		cstmt = conn.prepareCall("select bestFiteng(?)");
			    cstmt.setString(1, word);
			    cstmt.execute();
			    rsterm = cstmt.getString(1);
			    conn.commit();
	    		suggestedQuery[pos] = rsterm;
	    		pos++;
	    	}
	    	
	    }else {
	    	int pos = 0;
	    	for (String word : query) {
	    		word.toUpperCase();
	    		//find nearest german word with highest probability
	    		cstmt = conn.prepareCall("select bestFitger(?)");
			    cstmt.setString(1, word);
			    cstmt.execute();
			    rsterm = cstmt.getString(1);
			    conn.commit();
			    suggestedQuery[pos] = rsterm;
	    		pos++;
	    	}
	    	
	    }
		return suggestedQuery;
	}
}
