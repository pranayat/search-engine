package main.java.com.search;

public class Result {
	private int docid;
	private String url;
	private double score;
	private int rank;
	
	public Result (int docid, String url, double score, int rank) {
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
	
	public double getScore () {
		return this.score;
	}
	
	public int getRank () {
		return this.rank;
	}
}
