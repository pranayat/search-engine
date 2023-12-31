package com.search;

import java.io.PrintWriter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
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

public class MetaSearchServlet extends HttpServlet {
	
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
    
    private String getQueryTextFromTerms(List<String> queryTerms, String queryLanguage, Map<String, String> stemTermMap) {
    	String queryText = "";
    	if (queryLanguage.equals("ger")) {
    		queryText = String.join(" ", queryTerms.toArray(new String[0]));
    	} else {
    		for (String term: queryTerms) {
    			queryText = queryText + " " + stemTermMap.get(term);
    		}
    	}
    	
    	return queryText;
    }

	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		try {
			String queryText = req.getParameter("query");		
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
	    		String scoreTypeOption = req.getParameter("score");
	    		
	    		String queryLanguage;
	    		
	    		if (req.getParameter("lang") != null && req.getParameter("lang").length() > 0) {
	    			queryLanguage = req.getParameter("lang");
	    		} else {
	    			LanguageClassifier l = new LanguageClassifier();
	    			String[] queryTerms = queryText.split("\\s+");
	    			queryLanguage = l.classify(queryTerms);
	    		}
		    	
		    	List<String> queryTerms = Arrays.asList(queryText.split("\\s+"));
		    	List<String> stemmedQueryTerms = new ArrayList<String>();
		    	Map<String, String> stemTermMap = new HashMap<String, String>();
		    	if (queryLanguage.equals("eng")) {
		    		for (String term: queryTerms) {
		    			String stemmedTerm = Indexer.stem_word(term);
		    			stemTermMap.put(stemmedTerm, term);
		    			stemmedQueryTerms.add(stemmedTerm);
		    		}
		    		
		    		queryTerms = stemmedQueryTerms;
		    	}
		    	
		    	List<Collection> sortedCollectionList = new ArrayList<Collection>();
		    	
		    	List<Map<String, Float>> cTermScoresList = new ArrayList<Map<String, Float>>();
		    	
		    	List<Engine> engines = MetaConf.getActiveEngines();

		    	for (Engine activeEngine: engines) {
		    		Map<String, Float> cTermScores = Collection.findCollectionTermScores(activeEngine.url, queryTerms);
		    		cTermScoresList.add(cTermScores);
		    		Collection c = new Collection(activeEngine.url, Collection.getMapSum(cTermScores));
		    		sortedCollectionList.add(c);
		    	}
		    			    	
		    	Collections.sort(sortedCollectionList);
		    	
		    	List<String> unknownTerms = new ArrayList<String>();
		    	List<String> knownTerms = new ArrayList<String>();
		    	
		    	for (String term: queryTerms) {
		    		boolean found = false;
		    		for (Map<String, Float> cTermScores: cTermScoresList) {
		    			if (cTermScores.get(term) != null) {
		    				found = true;
		    				break;
		    			}
		    		}

		    		if (!found) {
		    			unknownTerms.add(term);
		    		} else {
		    			knownTerms.add(term);
		    		}
		    	}
	    		
		    	// route query to top 2 collections for known terms
		    	if (knownTerms.size() > 0) {
		    		String knownQueryText = this.getQueryTextFromTerms(knownTerms, queryLanguage, stemTermMap);
		    		List<ApiResult> knownApiResults = new ArrayList<ApiResult>();
		    		List<CompletableFuture<ApiResult>> futures = new ArrayList<CompletableFuture<ApiResult>>();
		    		
		    		// take max top N
		    		int topN = sortedCollectionList.size();
		    		if (req.getParameter("topn") != null) {
		    			topN = Integer.parseInt(req.getParameter("topn"));
		    			if (sortedCollectionList.size() < topN) {
		    				topN = 1;
		    			}
		    		}

		    		for(Collection c: sortedCollectionList.subList(0, topN)) {
		    			Query q = new Query(knownQueryText, k, scoreTypeOption, queryLanguage, "web", false);
		    			
		    			CompletableFuture<ApiResult> completableFuture
		    		      = CompletableFuture.supplyAsync(() -> {
							try {
								return q.getResultsFromCollection(c.collectionUrl);
							} catch (Exception e) {
								return null;
							}
						});
		    			
		    			futures.add(completableFuture);
		    		}

		    		int i = 0;
		    		for(Collection c: sortedCollectionList.subList(0, topN)) {
		    			ApiResult r = futures.get(i).get();
		    			knownApiResults.add(r);
		    			c.setKnownTermsApiResult(r);
		    		}		    		
		    		
		    		Map<String, Integer> termCfMap = Collection.getTermCfMap(knownTerms, knownApiResults);
		    		for(Collection c: sortedCollectionList.subList(0, topN)) {
		    			c.updateCollectionTermScores(knownTerms, c.knownTermsApiResult, termCfMap);
		    		}
		    	}
		    	
		    	// search all collections for unknown terms
		    	if (unknownTerms.size() > 0) {
		    		String unknownQueryText = this.getQueryTextFromTerms(unknownTerms, queryLanguage, stemTermMap);
		    		List<ApiResult> unknownApiResults = new ArrayList<ApiResult>();
		    		List<CompletableFuture<ApiResult>> futures = new ArrayList<CompletableFuture<ApiResult>>();

		    		for(Collection c: sortedCollectionList) {
		    			Query q = new Query(unknownQueryText, k, scoreTypeOption, queryLanguage, "web", false);
		    			
		    			CompletableFuture<ApiResult> completableFuture
		    		      = CompletableFuture.supplyAsync(() -> {
							try {
								return q.getResultsFromCollection(c.collectionUrl);
							} catch (Exception e) {
								return null;
							}
						});
		    			
		    			futures.add(completableFuture);
		    		}

		    		int i = 0;
		    		for(Collection c: sortedCollectionList) {
		    			ApiResult r = futures.get(i).get();
		    			unknownApiResults.add(r);
		    			c.setUnknownTermsApiResult(r);
		    		}
		    		
		    		Map<String, Integer> termCfMap = Collection.getTermCfMap(unknownTerms, unknownApiResults);
		    		for(Collection c: sortedCollectionList) {
		    			c.insertCollectionTermScores(unknownTerms, c.unknownTermsApiResult, termCfMap);
		    		}
		    	}
		    	
		    	List<Result> mergedResults = Collection.mergeResults(sortedCollectionList);
		    	req.setAttribute("results", mergedResults);
	    		RequestDispatcher rd = req.getRequestDispatcher("meta_result.jsp");
	    		rd.forward(req, res);		    		
		    }		    	
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
}
