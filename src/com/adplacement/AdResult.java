package com.adplacement;

public class AdResult {
	private String url;
	private String imageurl;
	private String text;
	private int rank;
	
	public AdResult (String url, String imageurl, String text, int rank) {
		this.url = url;
		this.imageurl = imageurl; 
		this.text = text;
		this.rank = rank;
	}	
	
	public AdResult (String url, String adtext, int rank) {
		this.url = url;
		this.text = text;
		this.rank = rank;
	}	
	
	public String getUrl () {
		return this.url;
	}
	
	public String getImageUrl () {
		return this.imageurl;
	}
	
	public String getText () {
		return this.text;
	}
	
	public int getRank () {
		return this.rank;
	}
	
}
