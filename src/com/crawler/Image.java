package com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.indexer.Indexer;

public class Image {
	private String url;
	private String title;
	private String alt;
	private String[] preTerms;
	private String[] postTerms;
	private Map<String, Double> termMap;
	
	public Image(Url url, String title, String alt) {
		this.url = url.getUrlString();
		this.title = title;
		this.alt = alt;
		this.termMap = new HashMap<String, Double>();
	}
	
	public void setPreTerms(String[] preTerms) {
		this.preTerms = preTerms;
	}
	
	public void setPostTerms(String[] postTerms) {
		this.postTerms = postTerms;
	}	
	
	public void index(Connection conn, int docId, String docLanguage) throws SQLException {
		// score = lambda * e^(-lambda*distance) + 1 (if present in alt or title)
		// lambda = 1
		
		if (this.preTerms.length > 0) {
			for (int i = 0; i < this.preTerms.length; i++) {
				// insert or improve the term score, always consider shortest distance to image (largeset score)
				double distanceScore = 1 * Math.exp(-1 * i);
				if (!this.preTerms[i].equals("") && (this.termMap.get(this.preTerms[i]) == null || this.termMap.get(this.preTerms[i]) < distanceScore)) {
					if (docLanguage.equals("eng")) {
						this.termMap.put(Indexer.stem_word(this.preTerms[i]), distanceScore);						
					} else {
						this.termMap.put(this.preTerms[i], distanceScore);
					}
				}
			}			
		}
		
		if (this.postTerms.length > 0) {
			for (int i = 0; i < this.postTerms.length; i++) {
				// insert or improve the term score, always consider shortest distance to image (largest score)
				double distanceScore = 1 * Math.exp(-1 * i);
				if (!this.postTerms[i].equals("") && (this.termMap.get(this.postTerms[i]) == null || this.termMap.get(this.postTerms[i]) < distanceScore)) {
					if (docLanguage.equals("eng")) {
						this.termMap.put(Indexer.stem_word(this.postTerms[i]), distanceScore);
					} else {
						this.termMap.put(this.postTerms[i], distanceScore);
					}
				}
			}			
		}
			
		if (this.alt != null) {
			for (String term: this.alt.split("\\s")) {
				if (term.equals("")) {
					continue;
				}
				double score = 0;
				if (this.termMap.get(term) != null) {
					score = this.termMap.get(term);
				}
				
				score += 1;
				if (docLanguage.equals("eng")) {
					this.termMap.put(Indexer.stem_word(term), score);					
				} else {
					this.termMap.put(term, score);					
				}
			}			
		}
		
		if (this.title != null) {
			for (String term: this.title.split("\\s")) {
				if (term.equals("")) {
					continue;
				}
				double score = 0;
				if (this.termMap.get(term) != null) {
					score = this.termMap.get(term);
				}
				
				score += 1;
				if (docLanguage.equals("eng")) {
					this.termMap.put(Indexer.stem_word(term), score);					
				} else {
					this.termMap.put(term, score);					
				}
			}			
		}
		

		for (String term: this.termMap.keySet()) {
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO image_features (url, term, score, docid) VALUES(?,?,?,?)");
			double score = this.termMap.get(term);
			pstmt.setString(1, url);
			pstmt.setString(2, term);
			pstmt.setDouble(3, score);
			pstmt.setInt(4, docId);
			pstmt.executeUpdate();
			conn.commit();
		}
	}
}
