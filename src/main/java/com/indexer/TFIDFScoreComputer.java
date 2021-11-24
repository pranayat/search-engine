package main.java.com.indexer;

import java.sql.Connection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.lang.Math;

public class TFIDFScoreComputer {
	
	private Connection conn;
		
	public TFIDFScoreComputer(Connection conn) {
		this.conn = conn;
	}
	
	/*private List<String> loadFeaturesFromDB () {
		PreparedStatement pstmt;
		ResultSet rs;
	}
	
	public void score() {
		
		List<String> = loadFeaturesFromDB();
		
		pstmt = conn.prepareStatement("SELECT term_frequency FROM features WHERE term = ?");
		pstmt.setInt(1, this.threadId);
		rs = pstmt.executeQuery();
	}*/
	
	public void recomputeScores(String term) {
		try {
			List<Integer> docidswithterm = new ArrayList<Integer>();
			List<Integer> featureIds = new ArrayList<Integer>();
			PreparedStatement pstmt;
			PreparedStatement pstmt1;
			PreparedStatement pstmtupdate;
			ResultSet rs;
			ResultSet rs1;
			pstmt = conn.prepareStatement("SELECT docid FROM features WHERE term = ?");
			pstmt.setString(1, term);
			rs = pstmt.executeQuery();
			while(rs.next()) {
				docidswithterm.add(rs.getInt(1));
			}

			pstmt = conn.prepareStatement("SELECT COUNT(*) AS count FROM documents");
			rs = pstmt.executeQuery();
			rs.next();
			int N = rs.getInt("count");

			// for all term doc pairs recompute tf_idf as N has changed
			// for all term docs pairs that contain current term recompute tf_idf as N and df has changed
			
			// get a list of all docids and loop through them
			// pstmt = conn.prepareStatement("SELECT id from features");
			// rs = pstmt.executeQuery();
			// while(rs.next()) {
			// 	featureIds.add(rs.getInt("id"));
			// }
			
//			int df;
			// for (int featureId: featureIds) {
			// 	pstmt = conn.prepareStatement("SELECT term, term_frequency, df FROM features WHERE id = ?");
			// 	pstmt.setInt(1, featureId);
			// 	rs = pstmt.executeQuery();
			// 	rs.next();
				
			// 	df = rs.getInt("df");
			// 	termf = rs.getInt("term_frequency");
				
			// 	if(term.equals(rs.getInt("term"))) {
			// 		df += 1;
			// 	}
			// }


			for (int l=0; l<docidswithterm.size();l++) {
				pstmt = conn.prepareStatement("SELECT term_frequency FROM features WHERE term =? and docid=?");
				pstmt.setString(1,term);
				pstmt.setInt(2, docidswithterm.get(l));
				rs = pstmt.executeQuery();
				rs.next();
				int termf = rs.getInt("term_frequency");
				float tf = 1 + (float)Math.log(termf);
				
				pstmt = conn.prepareStatement("SELECT COUNT(*) AS df FROM features WHERE term =?");
				pstmt.setString(1,term);
				rs = pstmt.executeQuery();
				rs.next();
				int df = rs.getInt("df");

				float idf = (float)Math.log(N/df);
				
				float tf_idf = (float)(tf*idf);
				
				pstmtupdate = conn.prepareStatement("UPDATE features SET df = ?, tf_idf =? WHERE term =? and docid=?");
				pstmtupdate.setInt(1, df);
				pstmtupdate.setDouble(2,tf_idf);
				pstmtupdate.setString(3,term);
				pstmtupdate.setInt(4,docidswithterm.get(l));
				pstmtupdate.executeUpdate();
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
