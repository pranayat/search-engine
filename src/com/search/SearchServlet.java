package com.search;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		String queryText = req.getParameter("querytext");		
        int k = 20;

        try {
			Query q = new Query(queryText, k);
			List<Result> results = q.getResults();
			req.setAttribute("results", results);
			RequestDispatcher rd = req.getRequestDispatcher("result.jsp");
			rd.forward(req, res);
        } catch (Exception e) {
        	System.out.println("spiderman");
            e.printStackTrace();
        }
	}
}
