package com.adplacement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.common.ConnectionManager;

public class AdClickServlet extends HttpServlet{
	
	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		try {
			System.out.println("does this work");
			
			String clickedButton= req.getParameter("adbutton");
			System.out.println(clickedButton);
			Connection conn = (new ConnectionManager()).getConnection();
    		PreparedStatement pstmt = conn.prepareStatement("UPDATE ad SET budget = budget - onclick WHERE url = ?");
    		pstmt.setString(1, clickedButton);
     	    pstmt.executeUpdate();
     	    conn.commit();
     	    conn.close();
     	    res.sendRedirect(clickedButton);
		
		    
        } catch (Exception e) {
            e.printStackTrace();
        }
	}

}
