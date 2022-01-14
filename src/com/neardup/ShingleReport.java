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
import java.util.Collections;

public class ShingleReport {
	
	private Connection conn;
	
	public ShingleReport(Connection conn) {
		this.conn = conn;
	}

	public void report() {
		try {
			//Map<Float, Float> simvalues = new HashMap<Float, Float>();
			List<Float> diffsimvalues = new ArrayList<Float>();
			int numvalues = 0;
			float sumdiffsimvalues = 0;
			float diffsim;
			PreparedStatement pstmtsim = conn.prepareStatement("SELECT jaccard, approx_jaccard from docsimilarities");
			ResultSet rssim = pstmtsim.executeQuery();
			while(rssim.next()) {
				//simvalues.put(rssim.getFloat("jaccard"),rssim.getFloat("approx_jaccard"));
				diffsim = Math.abs(rssim.getFloat("jaccard")-rssim.getFloat("approx_jaccard"));
				sumdiffsimvalues += diffsim;
				diffsimvalues.add(diffsim);
				numvalues+=1;
			}
			
			// average, the median, and the first and third quartile of the observed absolute error
			Collections.sort(diffsimvalues);
			float average = sumdiffsimvalues/numvalues;
			float median = diffsimvalues.get((int)Math.floor(numvalues/2));
			float firstquartile = diffsimvalues.get((int)Math.floor(numvalues/4));
			float thirdquartile = diffsimvalues.get(numvalues - (int)Math.floor(numvalues/4));
			System.out.println(average);
			System.out.println(median);
			System.out.println(firstquartile);
			System.out.println(thirdquartile );
			
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