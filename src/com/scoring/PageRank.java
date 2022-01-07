package com.scoring;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import org.la4j.matrix.SparseMatrix;
import org.la4j.Matrix;
import org.la4j.iterator.VectorIterator;
import org.la4j.Vector;
import org.la4j.vector.functor.VectorProcedure;

import org.la4j.matrix.functor.MatrixFunction;

import java.lang.Object;

public class PageRank {
	
	private Connection conn;
	
	public PageRank(Connection conn) {
		this.conn = conn;
	}
	
	public Matrix getP(Map<Integer,Integer> indices) {
		Matrix P = null;
		Integer N;
		try {
			PreparedStatement pstmtN = conn.prepareStatement("SELECT COUNT(*) AS count FROM documents");
			ResultSet rsN = pstmtN.executeQuery();
			rsN.next();
			N = rsN.getInt("count");
			
			SparseMatrix T = SparseMatrix.zero(N,N); 
			
			PreparedStatement pstmtlinks = conn.prepareStatement("SELECT * FROM links");
	 	    ResultSet rslinks = pstmtlinks.executeQuery();
	 	    int from;
	 	    int to;
	 	    int[] outDegree = new int[N];
	 	    Arrays.fill(outDegree, 0);
	 	    while(rslinks.next()) {
	 	    	from = indices.get(rslinks.getInt("from_docid"));
	 	    	to = indices.get(rslinks.getInt("to_docid"));
	 	    	T.set(from, to, 1);
	 	    	outDegree[from] += 1;
			}
			
	 	    //calculate T
	 	    for (int i=0; i<N; i++) {
	 	    	
	 	    	if (outDegree[i]==0) {
	 	    		for (int j =0; j<N;j++) {
	 	    			MatrixFunction f = new updateMatrix(N);
	 	    			T.updateAt(i, j, f);
	 	    		}
	 	    	} else {
	 	    		for (int j=0; j<N;j++) {
	 	    			if (T.get(i, j) != 0) {
	 	    				MatrixFunction f = new updateMatrix(outDegree[i]);
	 	    				T.updateAt(i,j,f);
	 	    			}
	 	    		}
	 	    	}
	 	    }
	 	    //calculate P=T with random jumps
	 	    Matrix Rand = Matrix.zero(N,N);
	 	    Rand.add(1/N);
	 	    P = T.multiply(0.9).add(Rand.multiply(0.1));
		} catch (SQLException e) {
			e.printStackTrace();
	    }
		
		return P;
	}
	
	public Vector powerIteration(Matrix P, double stop_crit) {
		double N = (double) 1/P.columns();
		Vector pi_old= Vector.constant(P.columns(),N);
		Vector pi_new = pi_old.multiply(P);
		Vector pi_save;
		
		int iterations = 0;
		while ((pi_new.subtract(pi_old).norm() > stop_crit) && (iterations < 100)){
			pi_save = pi_new;
			pi_new = pi_save.multiply(P);
			pi_old = pi_save;
			iterations ++;
		}

		return pi_new;
	}
	
	public void pageRanking() {
		
		try {
			Map<Integer, Integer> indices = new HashMap<Integer, Integer>();
			PreparedStatement pstmt = conn.prepareStatement("SELECT docid FROM documents");
			ResultSet rsids = pstmt.executeQuery();
			int count = 0;
			while(rsids.next()) {
				indices.put(rsids.getInt("docid"),count);
				count++;
			}
			
			Matrix P = getP(indices);
			Vector rank = powerIteration(P,0.0001);//try good stoping criteria
			
			//or a new ranking table
			PreparedStatement pstmtupdate = conn.prepareStatement("UPDATE documents SET pagerank = ? WHERE docid=?");
			int act_docid = -1;
			for (int i=0; i<rank.length(); i++) {
				for (Integer docid : indices.keySet()) {
				    if (indices.get(docid) == i) {
				    	act_docid = docid;
				    	break;
				    }
				}
				pstmtupdate.setDouble(1, rank.get(i));
				pstmtupdate.setInt(2,act_docid); 
				pstmtupdate.executeUpdate();
			}
			
			conn.commit();
			
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
