package com.search;

import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

public class SearchApiServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		String queryText = req.getParameter("querytext");
        int k = req.getParameter("k").length() > 0 ? Integer.parseInt(req.getParameter("k")) : 20;
        // TODO: score param
        
        try {
			Query q = new Query(queryText, k);
			List<Result> results = q.getResults();
			PrintWriter out = res.getWriter();
			ObjectMapper objectMapper= new ObjectMapper();
			String jsonString = objectMapper.writeValueAsString(results);
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			out.print(jsonString);
			out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
