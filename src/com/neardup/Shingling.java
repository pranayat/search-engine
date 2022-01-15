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

import com.common.ConnectionManager;
import com.scoring.ViewCreator;

public class Shingling {
		private Connection conn;
		List<Integer> minhashparameters;
			
		public Shingling(List<Integer> parameters) {
			this.conn = (new ConnectionManager()).getConnection();
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
				
				//get number such that at least 10 000 pairs are calculated
				int numpairs = (int) 15000/docIds.size();
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
				Map<Integer,List<Integer>> shingledocids = new HashMap<Integer,List<Integer>>();
				PreparedStatement pstmtsim = conn.prepareStatement("SELECT docid1, docid2 from docsimilarities");
				ResultSet rsidpairs = pstmtsim.executeQuery();
				while(rsidpairs.next()) {
					if (shingledocids.containsKey(rsidpairs.getInt("docid1"))) {
						List<Integer> docids = shingledocids.get(rsidpairs.getInt("docid1"));
						docids.add(rsidpairs.getInt("docid2"));
						shingledocids.put(rsidpairs.getInt("docid1"), docids);
					} else {
						List<Integer> docids = new ArrayList<Integer>();
						docids.add(rsidpairs.getInt("docid2"));
						shingledocids.put(rsidpairs.getInt("docid1"),docids);
					}
				}
				
				// calculate approx_jaccard for different n
				float[] errors = new float[minhashparameters.size()*4];
				ShingleReport sr = new ShingleReport(conn);
				int count = 0;
				for (int n_minhash: minhashparameters) {
					for (int key : shingledocids.keySet()) {
						List<Integer> pairs = shingledocids.get(key);
						for (int id: pairs) {
							cstmt = conn.prepareCall("select jaccardapproximationN(?,?,?)");
						    cstmt.setInt(1, key);
						    cstmt.setInt(2, id);
						    cstmt.setInt(3, n_minhash);
						    cstmt.execute();
						}
					}
					conn.commit();
					
					System.out.println("calculating diff");
					errors = sr.report(errors, count);
					count+=1;
				}
				sr.plot(errors);
				
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

