package com.scoring;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.common.ConnectionManager;

import java.lang.*;

public class Okapi {

	private Connection conn;
	
	public Okapi() {
		this.conn = (new ConnectionManager()).getConnection();
	}
	
	public void okapifunction() {
		try {

			Statement stmt = conn.createStatement();
		      //Query to create a function
		    String query = "CREATE OR REPLACE FUNCTION okapibm25(featureId int, N int, avgdl float, k1 float, b float)"
		    		+ " RETURNS float"
		    		+ " AS $$"
		    		+ "	  declare fqD int;"
		    		+ "	  declare D int;"
		    		+ "   declare nq int;"
		    		+ "   declare idf float;"
		    		+ "	  declare result float;"
		    		+ "   declare did int;"
		    		+ "	  BEGIN"
		    		+ "	  select docid, term_frequency, df into did, fqD, nq"
		    		+ "   from features where id = featureId;"
		    		+ "   select sum(term_frequency) into D from features where docid = did;" 
		    		+ "   idf = log((N-nq+0.5)/(nq+0.5));"
		    		+ "	  result = idf * ((fqD * (k1 + 1))/(fqD + k1 * (1 - b + b * (D/avgdl))));"
		    		+ "	  UPDATE features SET bm25 = result WHERE id=featureId;"
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
	
	
	public float getAvgDocLen() {
	
		float avgdl;
		try {
			
			PreparedStatement pstmtAVG = conn.prepareStatement("SELECT avg(x.dl) as avgdl from (SELECT sum(term_frequency) as dl FROM"
					+ " features GROUP BY docid) x");
			ResultSet rsAVG = pstmtAVG.executeQuery();
			rsAVG.next();
			avgdl = rsAVG.getFloat("avgdl");
			
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	    	   return avgdl = -1;
	       }
		return avgdl;
	}
		
	
	public void okapiScoring() {
		try {
			// get a list of all docids and loop through them
			List<Integer> featureIds = new ArrayList<Integer>();
			PreparedStatement pstmtfeatureids = conn.prepareStatement("SELECT id from features");
			ResultSet rsids = pstmtfeatureids.executeQuery();
			while(rsids.next()) {
				featureIds.add(rsids.getInt("id"));
			}
				
			PreparedStatement pstmtN = conn.prepareStatement("SELECT COUNT(*) AS count FROM documents");
			ResultSet rsN = pstmtN.executeQuery();
			rsN.next();
			int N = rsN.getInt("count");
				
			float avgdl = this.getAvgDocLen();	
			this.okapifunction();
			
			CallableStatement cstmt;
			
			for (int featureId: featureIds) {
			    cstmt = conn.prepareCall("select okapibm25(?,?,?,?,?)");
			    cstmt.setInt(1, featureId);
			    cstmt.setInt(2, N);
			    cstmt.setFloat(3,avgdl);
			    cstmt.setFloat(4, (float) 1.7);
			    cstmt.setFloat(5, (float) 0.75);
			    cstmt.execute();
			    
			    conn.commit();
			}
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
