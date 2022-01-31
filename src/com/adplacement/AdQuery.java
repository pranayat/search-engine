package com.adplacement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import com.common.ConnectionManager;
import com.search.Result;


public class AdQuery {
	private String[] queryTerms;
	private int k_ad;
	private String lang;

	public AdQuery(String[] queryTerms, int k_ad, String lang) {
		this.queryTerms = queryTerms ;
		this.k_ad = k_ad;
		this.lang = lang;
	}
	private List<String> subarray(String[] queryTerms) {
		List<String> subarrays = new ArrayList<String>();;
		for (int i = 0; i < queryTerms.length; i++) {
		    String res = "";
		        for (int j = i; j < queryTerms.length; j++) {
		        	if  (j!=i) {
		        		res += " ";
		        	}
		            res += queryTerms[j];
		    subarrays.add(res);
		    }
		}
		return subarrays;
	}
	
	private String buildDisjunctiveClause(String[] queryTerms ) {
		List<String> querySubterms = subarray(queryTerms);
		String clause = "ngram = ";
		for (String term: querySubterms) {
			term = term.replace("'", "''");
			clause += "'" + term + "'" + " OR ngram = ";
		}
		return clause.substring(0, clause.length() - 11); // remove the last OR ngram =
	}
	
	private String buildSearchQuery (String[] queryTerms, int k_ad, String lang) {
		String queryString = "";
		
		queryString = "select fitads.url, fitads.image, fitads.text, sum(fitads.score) as totalscore"
				+ " FROM "
				+ " (SELECT * FROM ad a, ad_ngrams n"
				+ " WHERE a.adid = n.adid AND a.budget > 0 AND a.language = '" + lang +"' AND "+ this.buildDisjunctiveClause(queryTerms) +") as fitads"
				+ " GROUP BY fitads.url, fitads.image, fitads.text"
				+ " ORDER BY totalscore DESC LIMIT " + k_ad + ";";
		System.out.println(queryString);
		return queryString;
	}
	
	public List<AdResult> getAdResults(){
		List<AdResult> resultList = new ArrayList<AdResult>();
		
		try {
			Connection conn = (new ConnectionManager()).getConnection();
    		PreparedStatement pstmt;
    		ResultSet rs;
    		pstmt = conn.prepareStatement(this.buildSearchQuery(this.queryTerms, this.k_ad, this.lang));
			rs = pstmt.executeQuery();
			int i = 0;
			while(rs.next()) {
				++i;
				resultList.add(new AdResult(rs.getString("url"),rs.getString("image"),rs.getString("text"),i));
			}
			conn.close();
		
		} catch (SQLException e) {
        	e.printStackTrace();
        }
		
		return resultList;
	}

}
