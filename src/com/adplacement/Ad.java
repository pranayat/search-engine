package com.adplacement;

import java.util.Set;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.common.ConnectionManager;

public class Ad {
	private String adURL;
	private String adText;
	private String ImageURL;
	private float budget;
	private float onclick;
	private Set<String> ngrams;

	public Ad(String adURL, String adText, String ImageURL, float budget, float onclick, Set<String> ngrams) {
		this.adURL = adURL;
		this.adText = adText;
		this.ImageURL = ImageURL;
		this.budget = budget;
		this.onclick = onclick;
		this.ngrams = ngrams;
	}
	
	public Ad(String adURL, String adText, float budget, float onclick, Set<String> ngrams) {
		this.adURL = adURL;
		this.adText = adText;
		this.budget = budget;
		this.onclick = onclick;
		this.ngrams = ngrams;
	}
	
	public void registerAd(int customerid) {
		Connection conn = (new ConnectionManager()).getConnection();
		try {
			   PreparedStatement pstmtad = conn.prepareStatement("INSERT INTO ad (customerid, url, text,"
			   		+ " image, budget, onclick) VALUES(?,?,?,?,?,?) RETURNING adid");
	    	   pstmtad.setInt(1, customerid);
	    	   pstmtad.setString(2, this.adURL);
	    	   pstmtad.setString(3, this.adText);
	    	   pstmtad.setString(4, this.ImageURL);
	    	   pstmtad.setFloat(5, this.budget);
	    	   pstmtad.setFloat(6, this.onclick);
	    	   
	    	   ResultSet rsad = pstmtad.executeQuery();
	    	   int adid = -1;
        	   if (rsad.next()) {
        	       adid = rsad.getInt("adid");
        	   }
    
        	   CallableStatement cstmt;
        	   for (String ngram: this.ngrams) {
        		    cstmt = conn.prepareCall("select updatengrams(?,?,?)");
	   			    cstmt.setString(1, ngram.toLowerCase());
	   			    cstmt.setInt(2, adid);
	   			    cstmt.setInt(3, this.ngrams.size());
	   			    cstmt.execute();
	   			    
	   			    conn.commit();
        	   }
        	   conn.commit();
        	   conn.close();
        	   
		} catch (SQLException e) {
	    	   System.out.println(e);
	    	   try {
	    		   conn.rollback();
	    		   conn.close();
	    	   } catch (SQLException e1) {
	    		   e1.printStackTrace();
	    	   }
	       }
	}
}
