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

import java.lang.Object;

public class PageRank {
	
	private Connection conn;
	
	public PageRank(Connection conn) {
		this.conn = conn;
	}
	
	public Matrix getP() {
		Matrix P;
		try {
			PreparedStatement pstmtN = conn.prepareStatement("SELECT COUNT(*) AS count FROM documents");
			ResultSet rsN = pstmtN.executeQuery();
			rsN.next();
			int N = rsN.getInt("count");
			
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
	 	    	nonzeros[to] += 1;
			}
			
	 	    VectorIterator I;
	 	    //calculate T
	 	    for (int i=0; i<N; i++) {
	 	    	//T.eachNonZeroInColumn(i, 1/nonzeros[i]); don't understand vectorfunction
	 	    	if (nonzeros[i]==0) {
	 	    		for (int j =0; j<N;j++) {
	 	    			T.set(i, j, 1/N);
	 	    		}
	 	    	} else {
	 	    		I =  T.nonZeroIteratorOfColumn(i);
		 	    	I.set(1/nonzeros[i]);
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
		Vector pi_alt = Vector.constant(P.columns(),1/P.columns());
		Vector pi_neu = pi_alt.multiply(P);
		Vector pi_save;
		
		while (pi_neu.subtract(pi_alt).norm() > stop_crit) {
			pi_save = pi_neu;
			pi_neu = pi_save.multiply(P);
			pi_alt = pi_save;
		}
		
		return pi_neu;
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
