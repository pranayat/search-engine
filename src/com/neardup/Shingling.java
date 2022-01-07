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
				
				//get number such that 100 000 pairs are calculated
				int randnum = (int) 1000/docIds.size();
				
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
					}
					conn.commit();
				}
				
//				cstmt = conn.prepareCall("select jaccardcalc(?,?)");
//			    cstmt.setInt(1, docIds.get(1));
//			    cstmt.setInt(2, docIds.get(2));
//			    cstmt.execute();
//			    
//			    cstmt = conn.prepareCall("select jaccardcalc(?,?)");
//			    cstmt.setInt(1, docIds.get(1));
//			    cstmt.setInt(2, docIds.get(3));
//			    cstmt.execute();
//			    
//			    cstmt = conn.prepareCall("select jaccardcalc(?,?)");
//			    cstmt.setInt(1, docIds.get(50));
//			    cstmt.setInt(2, docIds.get(100));
//			    cstmt.execute();
				
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

