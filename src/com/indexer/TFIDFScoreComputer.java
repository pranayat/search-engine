package com.indexer;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.common.ConnectionManager;


public class TFIDFScoreComputer {
	
	private Connection conn;
		
	public TFIDFScoreComputer() {
		this.conn = (new ConnectionManager()).getConnection();
	}
	
	public void tfidffunction() {
		try {

			Statement stmt = conn.createStatement();
		      //Query to create a function
		    String query = "CREATE OR REPLACE FUNCTION tfidf(featureId int, N int)"
		    		+ " RETURNS float"
		    		+ " AS $$"
		    		+ "	  declare tf int;"
		    		+ "	  declare docf int;"
		    		+ "	  declare result float;"
		    		+ "	  declare actterm character varying;" // TODO: remove this line, not needed
		    		+ "	  BEGIN"
		    		+ "	  select term_frequency, df into tf, docf"
		    		+ "   from features where id = featureId;"
		    		+ "	  result = (1+log(tf))*log(N/docf);"
		    		+ "	  UPDATE features SET tf_idf = result WHERE id=featureId;"
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
	
	
	
	public void computeScores() {
			try {
				List<Integer> featureIds = new ArrayList<Integer>();
				
				// get a list of all docids and loop through them
				PreparedStatement pstmtfeatureids = conn.prepareStatement("SELECT id from features");
				ResultSet rsids = pstmtfeatureids.executeQuery();
				while(rsids.next()) {
					featureIds.add(rsids.getInt("id"));
				}
				
				
				PreparedStatement pstmtN = conn.prepareStatement("SELECT COUNT(*) AS count FROM documents");
				ResultSet rsN = pstmtN.executeQuery();
				rsN.next();
				int N = rsN.getInt("count");
				
				this.tfidffunction();
				
				PreparedStatement pstmtselect;
				PreparedStatement pstmtupdate;
				CallableStatement cstmt;
				ResultSet rs;
				String term;
				int df;
				
				for (int featureId: featureIds) {
	
					pstmtselect = conn.prepareStatement("SELECT term FROM features WHERE id = ?");
					pstmtselect.setInt(1, featureId);
					rs = pstmtselect.executeQuery();
					rs.next();
					term = rs.getString("term");
					
					pstmtselect = conn.prepareStatement("SELECT COUNT(*) AS df FROM features WHERE term =?");
					pstmtselect.setString(1,term);
					rs = pstmtselect.executeQuery();
					rs.next();
					df = rs.getInt("df");
					
					pstmtupdate = conn.prepareStatement("UPDATE features SET df = ? WHERE id=?");
					pstmtupdate.setInt(1, df);
					pstmtupdate.setInt(2,featureId);
					pstmtupdate.executeUpdate();
				
				    cstmt = conn.prepareCall("select tfidf(?,?)");
				    cstmt.setInt(1, featureId);
				    cstmt.setInt(2, N);
				    cstmt.execute();
				    
				    conn.commit();
				}
				conn.close();//close here

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
				
					   