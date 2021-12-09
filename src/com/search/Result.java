package com.search;

import java.util.List;
import java.util.Map;

public class Result {
	private String url;
	private double score;
	private int rank;
	
	public Result (String url, double score, int rank) {
		this.url = url;
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
}