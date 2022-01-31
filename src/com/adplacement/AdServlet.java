package com.adplacement;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.search.ApiResult;
import com.search.Query;

public class AdServlet extends HttpServlet{

	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		try {
			
			String firstname = req.getParameter("firstname");
			String lastname = req.getParameter("lastname");
			AdCustomer c = new AdCustomer(firstname,lastname);
			int customerid = c.registerCustomer();
			
			String url = req.getParameter("adurl");
			String text = req.getParameter("adtext");
			String iurl = req.getParameter("imageurl");
			float onclick = Float.parseFloat(req.getParameter("onclick"));
			float budget = Float.parseFloat(req.getParameter("budget"));
			String ngrams = req.getParameter("listngrams");
			String language = req.getParameter("language");
			
			
			
			
			Set<String> setngrams = new HashSet<String>(Arrays.asList(ngrams.split(";")));
			
			if (iurl == null) {
				Ad a = new Ad(url, text, budget, onclick, setngrams, language);
				a.registerAd(customerid);
			}else {
				Ad a = new Ad(url, text, iurl, budget, onclick,  setngrams, language);
				a.registerAd(customerid);
			}
			res.sendRedirect("index.html");
		    
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
