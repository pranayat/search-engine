package com.neardup;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Shingling {
		private Connection conn;
			
		public Shingling(Connection conn) {
			this.conn = conn;
		}
		
		public void calculateJaccard() {
			try {
				
				// get a list of all docids and loop through them
				List<Integer> docIds = new ArrayList<Integer>();
				PreparedStatement pstmtdocids = conn.prepareStatement("SELECT DISTINCT docid from kshingles");
				ResultSet rsids = pstmtdocids.executeQuery();
				while(rsids.next()) {
					docIds.add(rsids.getInt("docid"));
				}
				
				//get number such that 10 000 pairs are calculated
				int randnum = (int) 10000/docIds.size();
				int n_minhash = 16;
				CallableStatement cstmt;
				
		
				for (int i = 0; i<docIds.size()-1; i++) {
					List<Integer> randomVals = new ArrayList<Integer>();
					Random rand = new Random();
					for (int n=0; n<randnum; n++) {
						randomVals.add(rand.nextInt(docIds.size()));
					}
					for (int j = 0; j<randomVals.size(); j++) {
						cstmt = conn.prepareCall("select jaccardcalc(?,?)");
					    cstmt.setInt(1, docIds.get(i));
					    cstmt.setInt(2, docIds.get(randomVals.get(j)));
					    cstmt.execute();
					    
					    cstmt = conn.prepareCall("select jaccardapproximationN(?,?,?)");
					    cstmt.setInt(1, docIds.get(i));
					    cstmt.setInt(2, docIds.get(randomVals.get(j)));
					    cstmt.setInt(3, n_minhash);
					    cstmt.execute();
					}
					conn.commit();
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

