package com.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.indexer.StopwordRemover;
import com.common.ConnectionManager;
import com.indexer.Stemmer;


public class Query {

	private String queryText;
	private int k;

	public Query(String queryText, int k) {
		this.queryText = queryText;
		this.k = k;
	}

	private String buildDisjunctiveClause(Set<String> terms) {
		String clause = "term = ";
		for (String term: terms) {
			clause += "'" + term + "'" + " OR term = ";
		}
		
		return clause.substring(0, clause.length() - 10); // remove the last OR term =
	}

	private String buildSearchQuery (Set <String> conjunctiveTerms, Set<String> allTerms, int k, String site) {
		String queryString = "";
		String documentQueryString = "";
		
		if (site.length() > 0) {
			documentQueryString = "	(select docid, url from documents WHERE url LIKE '%" + site +"%') as d ";
		} else {
			documentQueryString = "	(select docid, url from documents) as d ";
		}
		
		if (conjunctiveTerms.size() > 0) {			
			queryString = "select d.docid, d.url, e.agg_score from "
					+ documentQueryString
					+ "	INNER JOIN"
					+ "	("
					+ "		select a.docid, b.agg_score from"
					+ "		(select * from ("
					+ "		select docid, count(*) as count from features where " + this.buildDisjunctiveClause(conjunctiveTerms) + " group by docid"
					+ "		) as t2 WHERE t2.count = " + conjunctiveTerms.size() + ") as a"
					+ "		INNER JOIN"
					+ "		("
					+ "		select docid, sum(tf_idf) as agg_score from features where " + this.buildDisjunctiveClause(allTerms) + " group by docid"
					+ "		) as b on a.docid = b.docid"
					+ "	) as e "
					+ "on d.docid = e.docid ORDER BY e.agg_score DESC LIMIT " + k + ";";
			
		} else {
			queryString = "select a.docid, d.url, a.agg_score from "
					+ "	("
					+ "	select docid, sum(tf_idf) as agg_score from features where " + this.buildDisjunctiveClause(allTerms) + " group by docid order by agg_score DESC"
					+ "	) as a"
					+ "	INNER JOIN"
					+ documentQueryString
					+ "on a.docid = d.docid ORDER BY a.agg_score DESC LIMIT " + k + ";";
		}
		
		return queryString;
	}
	
    public List<Result> getResults() throws ClassNotFoundException {

        List<Result> results = new ArrayList<Result>();
        
        Class.forName("org.postgresql.Driver");

        try {
        	Connection conn = (new ConnectionManager()).getConnection();
    		PreparedStatement pstmt;
    		ResultSet rs;
    		Set<String> allTerms = new HashSet<String>();
    		Set<String> nonConjunctiveTerms = new HashSet<String>();
    		Set<String> conjunctiveTerms = new HashSet<String>();
    		String site = "";
    		
    		StopwordRemover sr = new StopwordRemover();
    		Stemmer s = new Stemmer();
    		
    		String[] queryTextTerms = this.queryText.split("\\s+");
    		if (this.queryText.startsWith("site:")) {    			
    			site = queryTextTerms[0].substring(5, queryTextTerms[0].length());
    			queryTextTerms = Arrays.copyOfRange(queryTextTerms, 1, queryTextTerms.length); // don't consider site:abc.com as query term
    		}
    		
    		Set<String> queryTextWithoutStopwords = sr.removeStopwords(queryTextTerms);
    		Set<String> queryWithoutSpecialChars = new HashSet<String>();
    		   
    		String regex = "([a-zA-Z0-9�������\"]+)";
    		Pattern pattern = Pattern.compile(regex);
    		for(String term: queryTextWithoutStopwords) {
    			Matcher matcher = pattern.matcher(term);
    			String iText = "";
    			while(matcher.find()) {
    				iText = iText + matcher.group(1);
    			}
    			if (iText.length() > 0) {
    				queryWithoutSpecialChars.add(iText);				
    			}
    		}
    		   
    		if (!(queryWithoutSpecialChars.size() > 0)) {
    			return results;
    		}
    				
    		for(String term: queryWithoutSpecialChars) {
    			char[] word = term.toCharArray();
	            for (int j = 0; j<word.length;j++) {
	                char c = word[j];
	                s.add(c);
	            }
	            s.stem();

	            term = s.toString();
	            
	            if (term.startsWith("\"") && term.endsWith("\"")) {
    				conjunctiveTerms.add(term.split("\"")[1]);
    			} else {
    				nonConjunctiveTerms.add(term);
    			}
    			
    		}
    		allTerms = Stream.concat(conjunctiveTerms.stream(), nonConjunctiveTerms.stream()).collect(Collectors.toSet());

    		pstmt = conn.prepareStatement(this.buildSearchQuery(conjunctiveTerms, allTerms, this.k, site));
			rs = pstmt.executeQuery();
			
			int i = 0;
			while(rs.next()) {
				++i;
				results.add(new Result(Integer.parseInt(rs.getString("docid").trim()),
						rs.getString("url").trim(),
						Double.parseDouble(rs.getString("agg_score").trim()),
						i));
			}
			
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        
        return results;
    }
}
