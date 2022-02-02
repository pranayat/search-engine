package com.adplacement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

import com.common.ConnectionManager;

public class AdCustomer {
	private String firstname;
	private String lastname;

	public AdCustomer(String firstname, String lastname) {
		this.firstname = firstname;
		this.lastname = lastname;
	}
	
	public int registerCustomer() {
		Connection conn = (new ConnectionManager()).getConnection();
		int customerid = -1;
		
		try {
			   PreparedStatement pstmtc = conn.prepareStatement("INSERT INTO ad_customer (lastname, firstname)"
			   		+ " VALUES(?,?) RETURNING customerid");
	    	   pstmtc.setString(1, this.lastname);
	    	   pstmtc.setString(2, this.firstname);
        	   
        	   ResultSet rsc =  pstmtc.executeQuery();
        	   if (rsc.next()) {
        	       customerid = rsc.getInt("customerid");
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
		return customerid;
	}
}
