package com.search;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

public class SearchServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;	
	private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
	
	public Bucket getBucketByIp(String ip) {
        return cache.computeIfAbsent(ip, this::newBucket);
    }	

    private Bucket newBucket(String ip) {
        return Bucket4j.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofSeconds(1))))
                .build();
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		try {
			String queryText = req.getParameter("querytext");		
	        int k = 20;
	        PrintWriter out = res.getWriter();
	        
	        Bucket ipBucket = this.getBucketByIp(req.getRemoteAddr());
			Bucket globalBucket = Bucket4j.builder()
	                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofSeconds(1))))
	                .build();
			
			if (!globalBucket.tryConsume(1)) {
				res.setStatus(429);
				out.print("Rate limit exceeded, please try after sometime.");
				out.flush();	
			}
			else if (!ipBucket.tryConsume(1)) {
				res.setStatus(429);
				out.print("Rate limit exceeded for your IP, please try after sometime.");
				out.flush();
		    } else {	    
				Query q = new Query(queryText, k);
				List<Result> results = q.getResults();
				req.setAttribute("results", results);
				RequestDispatcher rd = req.getRequestDispatcher("result.jsp");
				rd.forward(req, res);
		    }
        } catch (Exception e) {
        	System.out.println("spiderman");
            e.printStackTrace();
        }
	}
}
