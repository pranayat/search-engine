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
	
	public Matrix getP() {
		Matrix P;
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
	 	    int[] nonzeros = new int[N];
	 	    Arrays.fill(nonzeros, 0);
	 	    //adjacenz matrix, no docid with 0!!!
	 	    while(rslinks.next()) {
	 	    	from = rslinks.getInt("from_docid") -1;
	 	    	to = rslinks.getInt("to_docid") -1;
	 	    	T.set(from, to, T.get(from,to)+1);
	 	    	nonzeros[from] += 1;
			}
			
	 	    //calculate T
	 	    for (int i=0; i<N; i++) {
//	 	    	VectorProcedure V = new VectorProc(nonzeros[i]);
//	 	    	T.eachNonZeroInColumn(i, V); //here V
	 	    	
	 	    	if (nonzeros[i]==0) {
	 	    		for (int j =0; j<N;j++) {
	 	    			MatrixFunction f = new updateMatrix(N);
	 	    			T.updateAt(i, j, f);
	 	    		}
	 	    	} else {
	 	    		for (int j=0; j<N;j++) {
	 	    			if (T.get(i, j) != 0) {
	 	    				MatrixFunction f = new updateMatrix(nonzeros[i]);
	 	    				T.updateAt(i,j,f);
	 	    			}
	 	    		}
	 	    	}
	 	    }
	 	    //calculate P=T with random jumps
	 	    Matrix Rand = Matrix.zero(N,N);
	 	    Rand.add(1/N);
	 	    P = T.multiply(0.9).add(Rand.multiply(0.1));
	 	    conn.commit();    
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	      return null;
	       }
		return P;
	}
	
	public Vector powerIteration(Matrix P, double stop_crit) {
		double N = (double) 1/P.columns();
		Vector pi_old= Vector.constant(P.columns(),N);
		Vector pi_new = pi_old.multiply(P);
		Vector pi_save;
		
		while (pi_new.subtract(pi_old).norm() > stop_crit) {
			pi_save = pi_new;
			pi_new = pi_save.multiply(P);
			pi_old = pi_save;
		}
		return pi_new;
	}
	
	public void pageRanking() {
		Matrix P = getP();
		Vector rank = powerIteration(P,0.0001);//try good stoping criteria
		
		try {
			//or a new ranking table
			PreparedStatement pstmtupdate = conn.prepareStatement("UPDATE documents SET pagerank = ? WHERE docid=?");
			for (int i=0; i<rank.length(); i++) {
				pstmtupdate.setDouble(1, rank.get(i));
				pstmtupdate.setInt(2,i+1); //attention no zero docid
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
