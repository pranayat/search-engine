package com.search;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Stat {
	@JsonProperty("term")
	public String term;
	@JsonProperty("df")
	public int df;
	
	public Stat() {
		super();
	}
	
	public Stat(String term, int df) {
		this.term = term;
		this.df = df;
	}
}