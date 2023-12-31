package com.scoring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;

import com.common.ConnectionManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ViewCreator {
	
	private Connection conn;
	
	public ViewCreator() {
		this.conn = (new ConnectionManager()).getConnection();
	}
	
	public void createViews() {
		try {
		    
		    String viewtfidf = "create or replace view features_tfidf as"
		    		+ " select docid, term, tf_idf as score"
		    		+ " from features";
		    Statement statement1 = conn.createStatement();
		    statement1.execute(viewtfidf);
		        
		    String viewbm25 = "create or replace view  features_bm25 as"
		    		+ " select docid, term, bm25 as score"
		    		+ " from features";
		    Statement statement2 = conn.createStatement();
		    statement2.execute(viewbm25);
		    
		    String viewcombined = "create or replace view  features_combined as"
		    		+ " select docid, term, combined as score"
		    		+ " from features";
		    Statement statement3 = conn.createStatement();
		    statement3.execute(viewcombined);
		    
		    conn.commit();
		    
		    conn.close();
			
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
