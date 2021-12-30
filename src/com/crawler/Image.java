package com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.common.ConnectionManager;

public class Image {
	private String url;
	private String title;
	private String alt;
	private String[] preTerms;
	private String[] postTerms;
	private Map<String, Integer> termMap;
	
	public Image(String url, String title, String alt) {
		this.url = url;
		this.title = title;
		this.alt = alt;
		this.termMap = new HashMap<String, Integer>();
	}
	
	public void setPreTerms(String[] preTerms) {
		this.preTerms = preTerms;
	}
	
	public void setPostTerms(String[] postTerms) {
		this.postTerms = postTerms;
	}	
	
	public void index(Connection conn) throws SQLException {
		for (int i = 0; i < this.preTerms.length; i++) {
			// insert or improve the terms distance, always consider shortest distance to image
			if (this.termMap.get(this.preTerms[i]) == null || this.termMap.get(this.preTerms[i]) > i) {				
				this.termMap.put(this.preTerms[i], i);
			}
		}
		
		for (int i = 0; i < this.postTerms.length; i++) {
			// insert or improve the terms distance, always consider shortest distance to image
			if (this.termMap.get(this.postTerms[i]) == null || this.termMap.get(this.postTerms[i]) > i) {				
				this.termMap.put(this.postTerms[i], i);
			}
		}
			
		for (int i = 0; i < this.preTerms.length; i++) {
			PreparedStatement pstmt = conn.prepareStatement("INSERT INTO image_features (term, term_distance) VALUES(?,?)");
			pstmt.setString(1, this.preTerms[i]);
			pstmt.setInt(2, i);
		}
	}
}
