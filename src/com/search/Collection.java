package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.common.ConnectionManager;

public class Collection implements Comparable<Collection>{
	public int collectionId;
	public float collectionScore;
	
	public Collection(int collectionId, float collectionScore) {
		this.collectionId = collectionId;
		this.collectionScore = collectionScore;
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
    
    public void updateCollectionTermScores(ApiResult result) {
    	
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
