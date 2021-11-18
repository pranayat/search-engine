package com.indexer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class TFIDFScoreComputer {
	
	private Connection conn;
		
	public TFIDFScoreComputer() {
		this.conn = (new ConnectionManager()).getConnection();
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
		List<String> docids = new ArrayList<String>();
		PreparedStatement pstmt;
		PreparedStatement pstmt1;
		ResultSet rs;
		ResultSet rs1;
		pstmt = conn.prepareStatement("SELECT docid FROM features WHERE term = ?");
		pstmt.setString(1, term);
		rs = pstmt.executeQuery();
		while(rs.next()) {
			docids.add(rs.getString(1));
		}
		
		for (int l=0; l<docids.size();l++) {
			pstmt = conn.prepareStatement("SELECT term.frequency AS tf FROM features WHERE term =? and docid=?");
			pstmt.setString(1,term);
			pstmt.setString(2, docids.get(l));
			rs = pstmt.executeQuery();
			int tf = 1 + Math.log(rs.getInt("tf"));
			
			pstmt = conn.prepareStatement("SELECT COUNT(*) AS df FROM features WHERE term =?");
			pstmt.setString(1,term);
			rs = pstmt.executeQuery();
			pstmt1 = conn.prepareStatement("SELECT COUNT(*) AS N FROM features");
			pstmt1.setString(1,term);
			rs1 = pstmt.executeQuery();
			int idf = Math.log(rs1.getInt("N")/rs.getInt("df"));
			
			float tf_idf = tf/idf;
			
		}
	}

}
