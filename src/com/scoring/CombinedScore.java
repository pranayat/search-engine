package com.scoring;

// match in okapi http://kanungo.com/pubs/trec01-linkrank.pdf
// https://dl.acm.org/doi/pdf/10.1145/860435.860449?casa_token=zpXPUESLioYAAAAA:spv2nngPA_tT5f3VO3mU1871gev28jBvZogmD8WfBgjyRhXOauBVxI--n6mzr2Pz2LnclHOBX7_i
import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import com.common.ConnectionManager;

import java.lang.*;

public class CombinedScore {
	
	private Connection conn;
	
	public CombinedScore() {
		this.conn = (new ConnectionManager()).getConnection();
	}	
	
	public void combinedfunction() {
		try {

			Statement stmt = conn.createStatement();
		      //Query to create a function
		    String query = "CREATE OR REPLACE FUNCTION combined(featureId int, pagerank float)"
		    		+ " RETURNS float"
		    		+ " AS $$"
		    		+ "   declare bm25_score float;"
		    		+ "	  declare result float;"
		    		+ "   declare N int;"
		    		+ "	  BEGIN"
		    		+ "   select count(*) into N from documents;"
		    		+ "	  select bm25 into bm25_score from features where id = featureId;"
		    		+ "	  result = bm25_score^(1-1/log(N)) * pagerank^(1/log(N));"
		    		+ "	  UPDATE features SET combined = result WHERE id=featureId;"
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
		
	
	public void combinedScoring() {
		try {
			// get a list of all docids and loop through them
			List<Integer> featureIds = new ArrayList<Integer>();
			PreparedStatement pstmtfeatureids = conn.prepareStatement("SELECT id from features");
			ResultSet rsids = pstmtfeatureids.executeQuery();
			while(rsids.next()) {
				featureIds.add(rsids.getInt("id"));
			}
				
			this.combinedfunction();
			
			CallableStatement cstmt;
			PreparedStatement pstmt;
			float pagerank;
			
			for (int featureId: featureIds) {
				
				pstmt = conn.prepareStatement("SELECT pagerank FROM documents where"
						+ " documents.docid = (SELECT docid FROM features WHERE id = ?) ");
				pstmt.setInt(1, featureId);
				ResultSet rs = pstmt.executeQuery();
				rs.next();
				pagerank = rs.getFloat("pagerank");
				
				cstmt = conn.prepareCall("select combined(?,?)");
			    cstmt.setInt(1, featureId);
			    cstmt.setFloat(2, pagerank );
			    cstmt.execute();
			    
			    conn.commit();
			}
			
			conn.close();
		} catch (SQLException e) {
	    	   e.printStackTrace();
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	       }
	}
}
