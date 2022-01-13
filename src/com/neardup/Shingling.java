package com.neardup;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.scoring.ViewCreator;

public class Shingling {
		private Connection conn;
		List<Integer> minhashparameters;
			
		public Shingling(Connection conn, List<Integer> parameters) {
			this.conn = conn;
			this.minhashparameters = parameters;
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
				int numpairs = (int) 10000/docIds.size();
				CallableStatement cstmt;
				
				for (int i = 0; i<docIds.size()-1; i++) {
					List<Integer> randomVals = new ArrayList<Integer>();
					Random rand = new Random();
					for (int n=0; n<numpairs; n++) {
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
		
		public void calculateapproxJaccard() {
			try {
				CallableStatement cstmt;
				Map<Integer, Integer> shingledocids = new HashMap<Integer, Integer>();
				PreparedStatement pstmtsim = conn.prepareStatement("SELECT docid1, docid2 from docsimilarities");
				ResultSet rsidpairs = pstmtsim.executeQuery();
				while(rsidpairs.next()) {
					shingledocids.put(rsidpairs.getInt("docid1"), rsidpairs.getInt("docid2"));
				}
				System.out.println(shingledocids);
				for (int n_minhash: minhashparameters) {
					for (Map.Entry<Integer,Integer> idPair : shingledocids.entrySet()) {
						cstmt = conn.prepareCall("select jaccardapproximationN(?,?,?)");
					    cstmt.setInt(1, idPair.getKey());
					    cstmt.setInt(2, idPair.getValue());
					    cstmt.setInt(3, n_minhash);
					    cstmt.execute();
					}
					System.out.println("calculating diff");
					ShingleReport sr = new ShingleReport(conn);
					sr.report();
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

