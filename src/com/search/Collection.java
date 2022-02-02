package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.common.ConnectionManager;

public class Collection implements Comparable<Collection>{
	public int collectionId;
	public double collectionScore;
	public ApiResult knownTermsApiResult;
	public ApiResult unknownTermsApiResult;
	public static double avgCw = updateAvgCw();
	
	public Collection(int collectionId, float collectionScore) {
		this.collectionId = collectionId;
		this.collectionScore = collectionScore;
	}
	
	public void setKnownTermsApiResult(ApiResult knownTermsApiResult) {
		this.knownTermsApiResult = knownTermsApiResult;
	}
	
	public void setUnknownTermsApiResult(ApiResult unknownTermsApiResult) {
		this.unknownTermsApiResult = unknownTermsApiResult;
	}
		
	public static double updateAvgCw() {
		int sumCw = 0;
		for (int i = 1; i < 4; i++) {
			Query q = new Query("foo", 1, "1", "eng", "web", false);
			ApiResult r;
			try {
				r = q.getResultsFromCollection(i);
				sumCw = sumCw + r.cw;
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
		 		
		return sumCw/3;
	}
	
    public static Map<String, Float> findCollectionTermScores(int collectionId, List<String> queryTerms) throws SQLException {
    	
    	Connection conn = (new ConnectionManager()).getConnection();
    	PreparedStatement pstmt;
		ResultSet rs;
		String stmt = String.format("SELECT term, score from collection_scores WHERE collection_id = ? AND term in (%s)",
                queryTerms.stream()
                .map(v -> "?")
                .collect(Collectors.joining(", ")));
    	pstmt = conn.prepareStatement(stmt);
    	pstmt.setInt(1, collectionId);
    	int i = 2;
    	for (String queryTerm: queryTerms) {
    		pstmt.setString(i, queryTerm);
    		i = i + 1;
    	}
		rs = pstmt.executeQuery();
		
		Map<String, Float> termScoreMap = new HashMap<String, Float>();
		while(rs.next()) {
			termScoreMap.put(rs.getString("term"), rs.getFloat("score"));
		}
		
		conn.close();
		
		return termScoreMap;
    }
    
    public static Float getMapSum(Map<String, Float> scoresMap) {
    	float sum = 0;
    	for (Map.Entry<String, Float> entry : scoresMap.entrySet()) {
    		sum = sum + entry.getValue();
    	}
    	
    	return sum;
    }
    
    public static Map<String, Integer> getTermCfMap(List<String> terms, List<ApiResult> apiResults) {
    	Map<String, Integer> termCfMap = new HashMap<String, Integer>();
    	for (String term: terms) {
    		int cf = 0;
    		termCfMap.put(term, cf);
    		for (ApiResult apiResult: apiResults) {
    			if (isTermInCollection(term, apiResult.stat)) {
        			cf = cf + 1;
        		}
    		}
    		termCfMap.put(term, cf);
    	}
    	
    	return termCfMap;
    }

    private static boolean isTermInCollection(String term, List<Stat> termStats) {
    	boolean found = false;
    	for (Stat termStat: termStats) {
    		if (term.equals(termStat.term)) {
    			found = true;
    			break;
    		}
    	}
    	
    	return found;
    }
    
    private static int getTermDfFromStat(List<Stat> stat, String term) {
    	int df = 0;
    	for(Stat termStat: stat) {
    		if (termStat.term.equals(term)) {
    			df = termStat.df;
    			break;
    		}
    	}
    	
    	return df;
    }
    
    private double computeCollectionScoreForTerm(String term, ApiResult result, Integer cf) {
    	double score;
    	if (!isTermInCollection(term, result.stat)) {
    		score = (double) 0.4;
    	} else {
    		int df = getTermDfFromStat(result.stat, term);
    		int c = 3; // TODO
    		score = 0.4 + (0.6) * (df / (df + 50 + 150 * (result.cw / avgCw))) * (Math.log((c + 0.5) / cf) / Math.log(c + 1));
    	}
    	
    	return score;
    }

    public void insertCollectionTermScores(List<String> terms, ApiResult apiResult, Map<String, Integer> termCfMap) throws SQLException {
    	Connection conn = (new ConnectionManager()).getConnection();
    	for (String term: terms) {
    		int cf = termCfMap.get(term);
    		double score = this.computeCollectionScoreForTerm(term, apiResult, cf);
        	PreparedStatement pstmt = conn.prepareStatement("INSERT INTO collection_scores (collection_id, term, score, cf) VALUES (?,?,?,?)");
        	pstmt.setInt(1, this.collectionId);
        	pstmt.setString(2, term);
        	pstmt.setDouble(3, score);
        	pstmt.setInt(4, cf);
        	pstmt.executeUpdate();
    	}
    	conn.commit();
    	conn.close();
    }

    public void updateCollectionTermScores(List<String> terms, ApiResult apiResult, Map<String, Integer> termCfMap) throws SQLException {
    	Connection conn = (new ConnectionManager()).getConnection();
    	for (String term: terms) {
    		int cf = termCfMap.get(term);
    		double score = this.computeCollectionScoreForTerm(term, apiResult, cf);
        	PreparedStatement pstmt = conn.prepareStatement("UPDATE collection_scores set score = ?, cf = ? WHERE collection_id = ? AND term = ? ");
        	pstmt.setDouble(1, score);
        	pstmt.setInt(2, cf);
        	pstmt.setInt(3, this.collectionId);
        	pstmt.setString(4, term);
        	pstmt.executeUpdate();
    	}
    	conn.commit();
    	conn.close();
    }
    
    private static double getAggregateCollectionScore(int collectionId) throws SQLException {
    	Connection conn = (new ConnectionManager()).getConnection();
    	PreparedStatement pstmt = conn.prepareStatement("SELECT sum(score) as agg_score from collection_scores WHERE collection_id = ?");
    	pstmt.setInt(1, collectionId);
    	ResultSet rs = pstmt.executeQuery();
    	rs.next();
    	conn.close();
    	return rs.getDouble("agg_score");
    }
    
    private static double getMinCollectionScore(int collectionId) throws SQLException {
    	return 0.4;
    }
    
    private static double getMaxCollectionScore(int collectionId) throws SQLException {
    	int c = 3; // TODO
    	Connection conn = (new ConnectionManager()).getConnection();
    	PreparedStatement pstmt = conn.prepareStatement("SELECT cf from collection_scores WHERE collection_id = ?");
    	pstmt.setInt(1, collectionId);
    	ResultSet rs = pstmt.executeQuery();
    	rs.next();
    	conn.close();
    	int cf = rs.getInt("cf");
 
    	return 0.4 + (0.6) * Math.log((c + 0.5) / cf) / Math.log(c + 1);    	
    }
  
	public static List<Result> mergeResults(List<Collection> collections) throws SQLException {
		List<Result> mergedResults = new ArrayList<Result>();
		for (Collection c: collections) {
			double Rmin = getMinCollectionScore(c.collectionId);
			double Rmax = getMaxCollectionScore(c.collectionId);
			double collectionScore = getAggregateCollectionScore(c.collectionId);
			
			if (c.knownTermsApiResult != null) {
				for (Result r: c.knownTermsApiResult.resultList) {
					double score = (r.getScore() + 0.4 * r.getScore() * (collectionScore - Rmin) / (Rmax - Rmin)) / 1.4;
					mergedResults.add(new Result(r.getUrl(), score, c.collectionId));
				}
			}
			if (c.unknownTermsApiResult != null) {
				for (Result r: c.unknownTermsApiResult.resultList) {
					double score = (r.getScore() + 0.4 * r.getScore() * (collectionScore - Rmin) / (Rmax - Rmin)) / 1.4;
					mergedResults.add(new Result(r.getUrl(), score, c.collectionId));
				}
			}
			
		}
		
		Collections.sort(mergedResults);
		
		int i = 1;
		for (Result r: mergedResults) {
			r.setRank(i);
			i = i + 1;
		}
		
		return mergedResults; 
	}

	@Override
	public int compareTo(Collection c) {
		if (this.collectionScore == c.collectionScore) {
			return 0;
		}
		else if (this.collectionScore < c.collectionScore) {
			return 1;
		} else {
			return -1;
		}
	}
}
