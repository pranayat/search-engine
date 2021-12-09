package com.search;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	private String scoreType;
	private String language;

	public Query(String queryText, int k, String scoreType, String language) {
		this.queryText = queryText;
		this.k = k;
		this.scoreType = scoreType;
		this.language = language;
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
			documentQueryString = "	(select docid, url from documents WHERE url LIKE '%" + site +"%' AND language = '" + this.language + "') as d ";
		} else {
			documentQueryString = "	(select docid, url from documents WHERE language = '" + this.language + "') as d ";
		}
		
		if (conjunctiveTerms.size() > 0) {			
			queryString = "select d.docid, d.url, e.agg_score from "
					+ documentQueryString
					+ "	INNER JOIN"
					+ "	("
					+ "		select a.docid, b.agg_score from"
					+ "		(select * from ("
					+ "		select docid, count(*) as count from features where " + this.buildDisjunctiveClause(conjunctiveTerms)
					+ " 	AND language = '" + this.language + "' group by docid"
					+ "		) as t2 WHERE t2.count = " + conjunctiveTerms.size() + ") as a"
					+ "		INNER JOIN"
					+ "		("
					+ "		select docid, sum(" + this.scoreType + ") as agg_score from features where " + this.buildDisjunctiveClause(allTerms)
					+ " 	AND language = '" + this.language + "' group by docid"
					+ "		) as b on a.docid = b.docid"
					+ "	) as e "
					+ "on d.docid = e.docid ORDER BY e.agg_score DESC LIMIT " + k + ";";
			
		} else {
			queryString = "select a.docid, d.url, a.agg_score from "
					+ "	("
					+ "	select docid, sum(" + this.scoreType + ") as agg_score from features where " + this.buildDisjunctiveClause(allTerms)
					+ " AND language = '" + this.language + "' group by docid order by agg_score DESC"
					+ "	) as a"
					+ "	INNER JOIN"
					+ documentQueryString
					+ "on a.docid = d.docid ORDER BY a.agg_score DESC LIMIT " + k + ";";
		}
		
		return queryString;
	}
	
    public ApiResult getResults() throws ClassNotFoundException {
        String[] queryTextTerms = null;
        ApiResult apiResult = null;
        List<Result> resultList = new ArrayList<Result>();
        
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
    		
    		queryTextTerms = this.queryText.split("\\s+");
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
//    			return results;
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
				resultList.add(new Result(rs.getString("url").trim(),
						Double.parseDouble(rs.getString("agg_score").trim()),
						i));
			}
			
			String[] allTermsArray = allTerms.toArray(new String[allTerms.size()]);
			String[] termListClauseArray = new String[allTerms.size()];
			String termListClause = "";
			for (int j = 0; j < allTermsArray.length; j++) {
				termListClauseArray[j] = "?";
			}
			termListClause = String.join(",", termListClauseArray);
			
			pstmt = conn.prepareStatement("SELECT DISTINCT ON(term) term, df FROM features WHERE term IN (" + termListClause + ")");
			for (int j = 0; j < allTermsArray.length; j++) {
				pstmt.setString(j+1, allTermsArray[j]);
			}
			rs = pstmt.executeQuery();
			
			List<Stat> stats = new ArrayList<Stat>();
			Stat stat;
			
			while(rs.next()) {
				stat = new Stat(rs.getString("term"), rs.getInt("df"));
				stats.add(stat);
			}						
			
			Map<String, String> query = new LinkedHashMap<String, String>();
			query.put("k", String.valueOf(this.k));
			query.put("query", queryText);
			
			pstmt = conn.prepareStatement("SELECT SUM(term_frequency) AS cw FROM features");
			rs = pstmt.executeQuery();
			rs.next();
			
			apiResult = new ApiResult(resultList, query, stats, rs.getInt("cw"));
						
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        

        return apiResult ;
    }
}
