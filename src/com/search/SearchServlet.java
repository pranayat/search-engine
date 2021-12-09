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
			ApiResult apiResult = null;
	        
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
		    	String scoreTypeOption = req.getParameter("score");
		    	String scoreType = "tf_idf";
		    	
		    	if (scoreTypeOption == "1") {
		    		scoreType = "tf_idf";
		    	} else if (scoreTypeOption == "2") {
		    		scoreType = "bm25";
		    	} else if (scoreTypeOption == "3") {
		    		scoreType = "combined";
		    	}
		    	
				Query q = new Query(queryText, k, scoreType);
				apiResult = q.getResults();
				req.setAttribute("results", apiResult.resultList);
				RequestDispatcher rd = req.getRequestDispatcher("result.jsp");
				rd.forward(req, res);
		    }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
