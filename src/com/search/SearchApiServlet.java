package com.search;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

public class SearchApiServlet extends HttpServlet {
	
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
			PrintWriter out = res.getWriter();
			ObjectMapper objectMapper= new ObjectMapper();
			String jsonString;
			
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
		    	String queryText = req.getParameter("query");
		    	int k = req.getParameter("k").length() > 0 ? Integer.parseInt(req.getParameter("k")) : 20;		    	
		    	String scoreTypeOption = req.getParameter("score");
		    	String scoreType = "tf_idf";
		    	String queryLanguage = "eng";
		    	
		    	if (req.getParameter("lang") != null && req.getParameter("lang").length() > 0) {
		    		queryLanguage = req.getParameter("lang");
		    	}
		    	
		    	if (scoreTypeOption.equals("1")) {
		    		scoreType = "tf_idf";
		    	} else if (scoreTypeOption.equals("2")) {
		    		scoreType = "bm25";
		    	} else if (scoreTypeOption.equals("3")) {
		    		scoreType = "combined";
		    	}
		    	
		    			    	
	    		Query q = new Query(queryText, k, scoreType, queryLanguage);
				
	    		jsonString = objectMapper.writeValueAsString(q.getResults());
	    		res.setContentType("application/json");
	    		res.setCharacterEncoding("UTF-8");
	    		out.print(jsonString);
	    		out.flush();
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}	    	
	}
}
