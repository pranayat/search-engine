package com.search;

import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult {

	@JsonProperty("resultList")
	public List<Result> resultList;
	@JsonProperty("query")
	public Map<String, String> query;
	@JsonProperty("stat")
	public List<Stat> stat;
	@JsonProperty("cw")
	public int cw;
	@JsonProperty("suggestedQueries")
	public String[] suggestedQueries;
	public String[] allsearchterms;
	
	public ApiResult() {
		this.resultList = new ArrayList<Result>();
		this.query = new LinkedHashMap<String, String>();
		this.stat = new ArrayList<Stat>();
		this.cw = 0;
	}

	public ApiResult(List<Result> resultList, Map<String, String> query, List<Stat> stats, int cw, String[] suggestedQueries, String[] allsearchterms) {
		this.resultList = resultList;
		this.query = query;
		this.stat = stats;
		this.cw = cw;
		this.suggestedQueries = suggestedQueries;
		this.allsearchterms = allsearchterms;
	}
}
