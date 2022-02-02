package com.search;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.indexer.Indexer;
import com.languageclassifier.LanguageClassifier;

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
			String queryText = req.getParameter("query");		
			int k = 20;
			PrintWriter out = res.getWriter();
			ApiResult apiResultC3 = null;
	        
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
	    		String queryLanguage;
	    		
	    		if (req.getParameter("lang") != null && req.getParameter("lang").length() > 0) {
	    			queryLanguage = req.getParameter("lang");
	    		} else {
	    			LanguageClassifier l = new LanguageClassifier();
	    			String[] queryTerms = queryText.split("\\s+");
	    			queryLanguage = l.classify(queryTerms);
	    		}
		    	
	    		String searchMode = req.getParameter("mode");
	    		
	    		if (searchMode.equals("image")) {
	    			Query q = new Query(queryText, queryLanguage, "image");
	    			apiResultC3 = q.getResults();
	    			req.setAttribute("results", apiResultC3.resultList);
	    			req.setAttribute("suggestedQueries", apiResultC3.suggestedQueries);
	    			req.setAttribute("queryLang", queryLanguage);
	    			RequestDispatcher rd = req.getRequestDispatcher("image_result.jsp");
	    			rd.forward(req, res);
	    			return;
	    		}	
	    		
	    		if (scoreTypeOption.equals("1")) {
	    			scoreType = "tf_idf";
	    		} else if (scoreTypeOption.equals("2")) {
	    			scoreType = "bm25";
	    		} else if (scoreTypeOption.equals("3")) {
	    			scoreType = "combined";
	    		}
	    		
	    		Query q3 = new Query(queryText, k, scoreType, queryLanguage, "web", false);
	    		apiResultC3 = q3.getResultsFromCollection(3);
	    		
	    		req.setAttribute("results", apiResultC3.resultList);
	    		req.setAttribute("suggestedQueries", apiResultC3.suggestedQueries);
	    		req.setAttribute("queryLang", queryLanguage);
	    		RequestDispatcher rd = req.getRequestDispatcher("result.jsp");
	    		rd.forward(req, res);		    				   }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
