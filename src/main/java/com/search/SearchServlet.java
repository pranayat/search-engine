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

	public void doGet(HttpServletRequest req, HttpServletResponse res) {
		
		String queryText = req.getParameter("querytext");
		String json = req.getParameter("json");
		
        int k = 20;

        try {
        		Query q = new Query(queryText, k);
        		List<Result> results = q.getResults();
        		
        		if (json != null && json.equals("true")) {
        			PrintWriter out = res.getWriter();
        			ObjectMapper objectMapper= new ObjectMapper();
        			String jsonString = objectMapper.writeValueAsString(results);
        			res.setContentType("application/json");
        			res.setCharacterEncoding("UTF-8");//set to unicode so that we can use fuzzystrmatch?
        			out.print(jsonString);
        			out.flush();
        			return;
        		} else {
        			req.setAttribute("results", results);
        			RequestDispatcher rd = req.getRequestDispatcher("result.jsp");
        			rd.forward(req, res);        			
        		}
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
