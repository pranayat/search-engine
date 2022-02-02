package com.search;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result implements Comparable<Result> {
	@JsonProperty("url")
	private String url;
	@JsonProperty("snippet")
	private String snippet;
	@JsonProperty("score")
	private double score;
	@JsonProperty("rank")
	private int rank;
	private int sourceCollection;
	
	public Result() {
		super();
	}
	
	public Result (String url, double score, int sourceCollection) {
		this.url = url;
		this.score = score;
		this.sourceCollection = sourceCollection;
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
	
	public int getSourceCollection() {
		return this.sourceCollection;
	}	
	
	public void setRank(int rank) {
		this.rank = rank;
	}

	@Override
	public int compareTo(Result r) {
		if (this.score == r.score) {
			return 0;
		} else if (this.score < r.score) {
			return 1;
		} else {
			return -1;
		}
	}
}