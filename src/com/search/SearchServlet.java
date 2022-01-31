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
	    		String queryLanguage = "eng";
	    		
	    		if (req.getParameter("lang") != null && req.getParameter("lang").length() > 0) {
	    			queryLanguage = req.getParameter("lang");
	    		}
		    	
		    	if (req.getParameter("meta") == null) {
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
		    		rd.forward(req, res);		    		
		    	} else {
			    	List<String> queryTerms = Arrays.asList(queryText.split("\\s+"));
			    	Map<String, Float> c1TermScores = Collection.findCollectionTermScores(1, queryTerms);
			    	Collection c1 = new Collection(1, Collection.getMapSum(c1TermScores));
			    	Map<String, Float> c2TermScores = Collection.findCollectionTermScores(2, queryTerms);
			    	Collection c2 = new Collection(2, Collection.getMapSum(c2TermScores));
			    	Map<String, Float> c3TermScores = Collection.findCollectionTermScores(3, queryTerms);
			    	Collection c3 = new Collection(3, Collection.getMapSum(c3TermScores));
			    	
			    	List<Collection> sortedCollectionList = new ArrayList<Collection>();
			    	if (c1.collectionScore > 0) {
			    		sortedCollectionList.add(c1);		    		
			    	}
			    	if (c2.collectionScore > 0) {
			    		sortedCollectionList.add(c2);		    		
			    	}
			    	if (c3.collectionScore > 0) {
			    		sortedCollectionList.add(c3);		    		
			    	}
			    	
			    	Collections.sort(sortedCollectionList);
			    	
			    	List<String> unknownTerms = new ArrayList<String>();
			    	List<String> knownTerms = new ArrayList<String>();
			    	
			    	for (String term: queryTerms) {
			    		if (c1TermScores.get(term) == null && c2TermScores.get(term) == null && c3TermScores.get(term) == null) {
			    			unknownTerms.add(term);
			    		} else {
			    			knownTerms.add(term);
			    		}
			    	}
			    	
			    	List<ApiResult> knownTermResults = new ArrayList<ApiResult>();
			    	// do query routing for known terms
			    	if (knownTerms.size() > 0) {
			    		String knownQueryText = String.join(" ", knownTerms.toArray(new String[0]));
	
			    		// take max top 2
			    		for(Collection c: sortedCollectionList.size() >= 2 ? sortedCollectionList.subList(0, 2) : sortedCollectionList.subList(0, 1)) {
			    			Query q = new Query(queryText, k, scoreType, queryLanguage, "web", false);
			    			ApiResult apiResult = q.getResultsFromCollection(c.collectionId);
			    			apiResult.computeMetaScoresByCollection(c);
			    			c.updateCollectionTermScores(apiResult);
			    			knownTermResults.add(apiResult);
			    		}
			    	}
			    	
			    	List<ApiResult> unknownTermResults = new ArrayList<ApiResult>();
			    	// for unknown terms, send that part of the query to all search engines, this will be the entire query if no terms are known
			    	if (unknownTerms.size() > 0) {
			    		String unknownQueryText = String.join(" ", unknownTerms.toArray(new String[0]));
			    		
			    		Query q1 = new Query(queryText, k, scoreType, queryLanguage, "web", false);
			    		ApiResult apiResultC1 = q1.getResultsFromCollection(1);
			    		apiResultC1.computeMetaScoresByCollection(c1);
			    		c1.updateCollectionTermScores(apiResultC1);
			    		unknownTermResults.add(apiResultC1);
			    		
			    		Query q2 = new Query(queryText, k, scoreType, queryLanguage, "web", false);
			    		ApiResult apiResultC2 = q2.getResultsFromCollection(2);
			    		apiResultC2.computeMetaScoresByCollection(c2);
			    		c2.updateCollectionTermScores(apiResultC2);
			    		unknownTermResults.add(apiResultC2);
			    		
			    		Query q3 = new Query(queryText, k, scoreType, queryLanguage, "web", false);
			    		apiResultC3 = q3.getResultsFromCollection(3);
			    		apiResultC3.computeMetaScoresByCollection(c3);
			    		c3.updateCollectionTermScores(apiResultC3);
			    		unknownTermResults.add(apiResultC3);
			    	}
		    	}		    	
		   }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
