package com.search;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result {
	@JsonProperty("url")
	private String url;
	@JsonProperty("snippet")
	private String snippet;
	@JsonProperty("score")
	private double score;
	@JsonProperty("rank")
	private int rank;
	private double metaScore;
	
	public Result() {
		super();
	}
	
	public Result (String url, String snippet, double score, int rank) {
		this.url = url;
		this.snippet = snippet; 
		this.score = score;
		this.rank = rank;
	}	
	
	public String getUrl () {
		return this.url;
	}
	
	public double getScore () {
		return this.score;
	}
	
	public int getRank () {
		return this.rank;
	}
	
	public String getSnippet() {
		return this.snippet;
	}
	
	public double getMetaScore () {
		return this.metaScore;
	}
}