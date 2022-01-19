package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


import com.indexer.StopwordRemover;
import com.common.CharacterSanitizer;
import com.common.ConnectionManager;
import com.indexer.Indexer;
import com.indexer.Stemmer;

import net.sf.extjwnl.JWNLException;

class Segment implements Comparable<Segment>{
	private int serial;
	private List<String> terms;
	private List<String> stemmedTerms;
	private float coverage;
	private int tf;
	
	public Segment(int serial, List<String> terms, Set<String> queryTerms, Boolean stem) {
		this.terms = terms;
		this.serial = serial;
		
		this.stemmedTerms = new ArrayList<String>();
		if (stem) {
			for (String term: terms) {
				this.stemmedTerms.add(Indexer.stem_word(term));				
			}
		} else {
			stemmedTerms = terms;
		}
		
		int count = 0;
		for (String term: queryTerms) {
			if(this.stemmedTerms.contains(term)) {
				count += 1;
			}
		}
		this.coverage = count/queryTerms.size();
		
		this.tf = 0;
		for (String term: this.stemmedTerms) {
			if(queryTerms.contains(term)) {
				this.tf += 1;
			}
		}
	}

	public void appendSegment(Segment s) {
		this.terms = Stream.concat(this.terms.stream(), s.terms.stream())
        .collect(Collectors.toList());
	}
	
	public void prependSegment(Segment s) {
		this.terms = Stream.concat(s.terms.stream(), this.terms.stream())
        .collect(Collectors.toList());
	}
	
	
	public List<String> getTerms() {
		return this.terms;
	}
	
	public List<String> getStemmedTerms() {
		return this.stemmedTerms;
	}	

	public float getCoverage() {
		return this.coverage;
	}
	
	public int getTf() {
		return this.tf;
	}
	
	public int getSerial() {
		return this.serial;
	}

	@Override
	public int compareTo(Segment s) {
		if (this.coverage < s.coverage) {
			return 1;
		}
		
		if (this.coverage > s.coverage) {
			return -1;
		}
		
		if (this.coverage == s.coverage && this.tf < s.tf) {
			return 1;
		}
		
		if (this.coverage == s.coverage && this.tf > s.tf) {
			return -1;
		}
		
		if (this.coverage == s.coverage && this.tf == s.tf) {
			return 0;
		}
		
		else {
			return -1;
		}
	}
	
	public static List<Segment> getBestSegments(List<Segment> segments, Set<String> queryTerms, Query q) {
		Collections.sort(segments);
		List<Segment> diverseSegments = new ArrayList<Segment>();
		for (Segment s: segments) {
			Boolean found = false;
			List<String> termsToRemove = new ArrayList<String>();
			for (String queryTerm: queryTerms) {
				if (s.getStemmedTerms().contains(queryTerm)) {
					termsToRemove.add(queryTerm);
					found = true; // don't break since this segment might contain more query terms
				}
			}
			
			if (found) {
				diverseSegments.add(s);
				
				for (String termToRemove: termsToRemove) {
					queryTerms.remove(termToRemove);
				}
			}
		}
		
		q.setTermsNotFound(queryTerms);
		
		// at this point we have done our best to get all query terms covered in segments with highest tf of these terms
		// if we still have space left we can stitch the neihbours of these segments together
		
		// 8 x 4 = 32
		
		List<Segment> finalSegments = new ArrayList<Segment>(diverseSegments);
		for (Segment s: diverseSegments) {
			if (finalSegments.size() < 4) {
				finalSegments.add(segments.stream().filter(seg -> seg.getSerial() == s.getSerial() + 1).findAny().orElse(null));				
			} else {
				break;
			}
		}

		return finalSegments;
	}
}

public class Query {

	private String queryText;
	private int k;
	private String scoreType;
	private String language;
	private String searchMode;
	private Set<String> termsNotFound;

	public Query(String queryText, String language, String searchMode) {
		this.queryText = queryText.toLowerCase();
		this.searchMode = searchMode;
		this.language = language;
	}
	
	public Query(String queryText, int k, String scoreType, String language, String searchMode) {
		this.queryText = queryText.toLowerCase();
		this.k = k;
		this.scoreType = scoreType;
		this.language = language;
		this.searchMode = searchMode;
	}
	
	public void setTermsNotFound(Set<String> termsNotFound) {
		this.termsNotFound = termsNotFound;
	}
	
	public Set<String> getTermsNotFound() {
		return this.termsNotFound;
	}
	
	//	Distinct query term count = N
	//			Seen terms S = 0
	//
	//			1. Break up doc into segments of 8 words each
	//			2. For each distinct word in query
	//			  2.1 Find segments with word
	//			  2.2 For each segment store - 1. coverage 2. sum of term frequency of query terms
	//			3. Sort segments by 1. coverage 2. term frequency of query terms descending
	//			4. Iterate over sorted list, skipping segments that don't bring up S. We want to make sure all terms are covered in the first few segments. Remaining segments just fill up the max 32 word limit.
	//			5. Stitch together contiguous segments into a single segment
	private String generateSnippet(String docText, Set<String> queryTerms) {
		List<String> docTerms = new ArrayList<String>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Segment> finalSegments = new ArrayList<Segment>();
		
		docTerms = Arrays.asList(docText.split("\\s"));
		int serial = 0;
		for (int i = 0; i < docTerms.size(); i = i + 8) {
			if (i + 8 < docTerms.size()) {
				segments.add(new Segment(serial, docTerms.subList(i, i + 8), queryTerms, this.language.equals("eng")));				
			} else {
				segments.add(new Segment(serial, docTerms.subList(i, docTerms.size()), queryTerms, this.language.equals("eng")));
				break;
			}
			
			serial = serial + 1;
		}
		
		
		finalSegments = Segment.getBestSegments(segments, queryTerms, this);
		
	    Collections.sort(finalSegments, (s1, s2) -> ((Segment) s1).getSerial() - ((Segment) s2).getSerial());
		
		String snippet = "";
		Segment prev = null;
		for (Segment s: finalSegments) {
			if (prev == null) {
				snippet = String.join(" ", s.getTerms());
			} else {
				if  (prev.getSerial() + 1 == s.getSerial()) {
					snippet = snippet + " " + String.join(" ", s.getTerms());
				} else {
					snippet = snippet + "..." + String.join(" ", s.getTerms()); 
				}
			}
			
			prev = s;
		}

		if (this.termsNotFound.size() > 0) {
			snippet = snippet + "<br> Not found: " + String.join(",", this.termsNotFound);
		}
		return snippet;
	}

	private String buildDisjunctiveClause(Set<String> terms, Map<String, List<String>> termSynonymMap) {
		String clause = "term = ";
		for (String term: terms) {
			term = term.replace("'", "''");
			clause += "'" + term + "'" + " OR term = ";
			List<String> synonyms = termSynonymMap.get(term);
			if (synonyms == null) {
				continue;
			}

			for (String synonym: synonyms) {
				synonym = synonym.replace("'", "''");
				clause += "'" + synonym + "'" + " OR term = ";
			}			
		}
		
		
		return clause.substring(0, clause.length() - 10); // remove the last OR term =
	}

	private String buildSearchQuery (Set <String> conjunctiveTerms, Set<String> allTerms, Map<String, List<String>> termSynonymMap, int k, String site) {
		String queryString = "";
		String documentQueryString = "";
		
		if (site.length() > 0) {
			documentQueryString = "	(select docid, url, doc_text from documents WHERE url LIKE '%" + site +"%' AND language = '" + this.language + "') as d ";
		} else {
			documentQueryString = "	(select docid, url, doc_text from documents WHERE language = '" + this.language + "') as d ";
		}
		
		if (conjunctiveTerms.size() > 0) {			
			queryString = "select d.docid, d.url, d.doc_text, e.agg_score from "
					+ documentQueryString
					+ "	INNER JOIN"
					+ "	("
					+ "		select a.docid, b.agg_score from"
					+ "		(select * from ("
					+ "		select docid, count(*) as count from features where " + this.buildDisjunctiveClause(conjunctiveTerms, termSynonymMap)
					+ " 	AND language = '" + this.language + "' group by docid"
					+ "		) as t2 WHERE t2.count = " + conjunctiveTerms.size() + ") as a"
					+ "		INNER JOIN"
					+ "		("
					+ "		select docid, sum(" + this.scoreType + ") as agg_score from features where " + this.buildDisjunctiveClause(allTerms, termSynonymMap)
					+ " 	AND language = '" + this.language + "' group by docid"
					+ "		) as b on a.docid = b.docid"
					+ "	) as e "
					+ "on d.docid = e.docid ORDER BY e.agg_score DESC LIMIT " + k + ";";
			
		} else {
			queryString = "select a.docid, d.url, d.doc_text, a.agg_score from "
					+ "	("
					+ "	select docid, sum(" + this.scoreType + ") as agg_score from features where " + this.buildDisjunctiveClause(allTerms, termSynonymMap)
					+ " AND language = '" + this.language + "' group by docid order by agg_score DESC"
					+ "	) as a"
					+ "	INNER JOIN"
					+ documentQueryString
					+ "on a.docid = d.docid ORDER BY a.agg_score DESC LIMIT " + k + ";";
		}
		
		return queryString;
	}
	
	private String buildImageSearchQuery (Set <String> conjunctiveTerms, Set<String> allTerms, Map<String, List<String>> termSynonymMap) {
		String queryString = "";
//		String documentQueryString = "";
		
//		if (site.length() > 0) {
//			documentQueryString = "	(select docid, url from documents WHERE url LIKE '%" + site +"%' AND language = '" + this.language + "') as d ";
//		} else {
//			documentQueryString = "	(select docid, url from documents WHERE language = '" + this.language + "') as d ";
//		}
		
		if (conjunctiveTerms.size() > 0) {			
			queryString = "select a.url, b.agg_score from"
					+ "		(select * from ("
					+ "		select url, count(*) as count from image_features where " + this.buildDisjunctiveClause(conjunctiveTerms, termSynonymMap)
					+ " 	group by url"
					+ "		) as t2 WHERE t2.count = " + conjunctiveTerms.size() + ") as a"
					+ "		INNER JOIN"
					+ "		("
					+ "		select url, sum(score) as agg_score from image_features where " + this.buildDisjunctiveClause(allTerms, termSynonymMap)
					+ " 	group by url"
					+ "		) as b on a.url = b.url ORDER BY b.agg_score DESC";
			
		} else {
			queryString = "	select url, sum(score) as agg_score from image_features where " + this.buildDisjunctiveClause(allTerms, termSynonymMap)
					+ " group by url order by agg_score DESC";
		}
		
		return queryString;
	}	
	
    public ApiResult getResults() throws ClassNotFoundException, JWNLException {
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
    		
    		// extract and remove site operator from query
    		queryTextTerms = this.queryText.split("\\s+");
    		if (this.queryText.startsWith("site:")) {    			
    			site = queryTextTerms[0].substring(5, queryTextTerms[0].length());
    			queryTextTerms = Arrays.copyOfRange(queryTextTerms, 1, queryTextTerms.length); // don't consider site:abc.com as query term
    		}
    		
    		SpellChecker spellcheck = new SpellChecker(conn);
    		String[][] suggestedQueryTerms = spellcheck.suggest(queryTextTerms, this.language);
    		String [] suggestedQueries = new String[5];
    		
    		// create 5 suggestions
    		for (int i = 0; i < 5; i++) {
    			suggestedQueries[i] = "";
    			for (int j = 0; j < suggestedQueryTerms.length; j++) {
    				suggestedQueries[i] += suggestedQueryTerms[j][i].toLowerCase() + " ";
    			}
    		}
    		

    		// remove stopwords for english queries
			List<String> queryTextWithoutStopwords = Arrays.asList(queryTextTerms);
			if (this.language.equals("eng")) {
				queryTextWithoutStopwords = sr.removeStopwords(queryTextTerms);
			}
    		   
			// keep only numbers and english/german alphabets
			List<String> queryWithoutSpecialChars = new ArrayList<String>();
			List<String> queryTermsToExpand = new ArrayList<String>();
    		for(String term: queryTextWithoutStopwords) {
    			if (term.startsWith("~")) {
    				queryTermsToExpand.add(CharacterSanitizer.sanitize(term));
    			}

    			queryWithoutSpecialChars.add(CharacterSanitizer.sanitize(term));
    		}
    		   
    		// if query is empty return empty result
    		if (!(queryWithoutSpecialChars.size() > 0)) {
    			return new ApiResult();
    		}
    		
    		// get synonyms for each expandable query term
    		Map<String, List<String>> termSynonymMap = new HashMap<String, List<String>>();
    		Synonym sy = new Synonym();    			
    		for (String term: queryTermsToExpand) {
    			List<String> synonyms = new ArrayList<String>();
    			
    			if (this.language.equals("eng")) {
    				synonyms = sy.getEnglishSynonyms(term);
    				termSynonymMap.put(Indexer.stem_word(term), synonyms);
    			} else {
    				synonyms = sy.getGermanSynonyms(term);
    				termSynonymMap.put(term, synonyms);
    			}
    		}
    		
    		for(String term: queryWithoutSpecialChars) {
					if (this.language.equals("eng")) {
						term = Indexer.stem_word(term);
					}
	            
					if (term.startsWith("\"") && term.endsWith("\"")) {
						conjunctiveTerms.add(term.split("\"")[1]);
					} else {
						nonConjunctiveTerms.add(term);
					}
    		}

    		allTerms = Stream.concat(conjunctiveTerms.stream(), nonConjunctiveTerms.stream()).collect(Collectors.toSet());
    		
    		if (this.searchMode.equals("image")) {
        		pstmt = conn.prepareStatement(this.buildImageSearchQuery(conjunctiveTerms, allTerms, termSynonymMap));
    			rs = pstmt.executeQuery();
    			
    			int i = 0;
    			while(rs.next()) {
    				++i;
    				
    			resultList.add(new Result(
    					rs.getString("url").trim(),
    					null,
    					Double.parseDouble(rs.getString("agg_score").trim()),
    					i));
    			}
    		} else {
    			pstmt = conn.prepareStatement(this.buildSearchQuery(conjunctiveTerms, allTerms, termSynonymMap, this.k, site));
    			rs = pstmt.executeQuery();
    			
    			int i = 0;
    			while(rs.next()) {
    				++i;
    				
    			resultList.add(new Result(
    					rs.getString("url").trim(),
    					this.generateSnippet(rs.getString("doc_text"), allTerms.stream().collect(Collectors.toSet())),
    					Double.parseDouble(rs.getString("agg_score").trim()),
    					i));
    			}
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
			
			apiResult = new ApiResult(resultList, query, stats, rs.getInt("cw"), suggestedQueries);
						
        } catch (SQLException e) {
        	e.printStackTrace();
        }
        

        return apiResult ;
    }
}
