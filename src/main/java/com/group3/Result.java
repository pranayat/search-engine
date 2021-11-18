package com.group3;

public class Result {
	private int docid;
	private String url;
	private int score;
	private int rank;
	
	public Result (int docid, String url, int score, int rank) {
		this.docid = docid;
		this.url = url;
		this.score = score;
		this.rank = rank;
	}
	
	public int getDocid () {
		return this.docid;
	}
	
	public String getUrl () {
		return this.url;
	}
	
	public int getScore () {
		return this.score;
	}
	
	public int getRank () {
		return this.rank;
	}
}
