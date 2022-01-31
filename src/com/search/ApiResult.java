package com.search;

import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiResult {

	public List<Result> resultList;
	public Map<String, String> query;
	public List<Stat> stats;
	public int cw;
	public String[] suggestedQueries;
	public String[] allsearchterms;
	
	public ApiResult() {
		this.resultList = new ArrayList<Result>();
		this.query = new LinkedHashMap<String, String>();
		this.stats = new ArrayList<Stat>();
		this.cw = 0;
	}

	public ApiResult(List<Result> resultList, Map<String, String> query, List<Stat> stats, int cw, String[] suggestedQueries, String[] allsearchterms) {
		this.resultList = resultList;
		this.query = query;
		this.stats = stats;
		this.cw = cw;
		this.suggestedQueries = suggestedQueries;
		this.allsearchterms = allsearchterms;
	}
}
