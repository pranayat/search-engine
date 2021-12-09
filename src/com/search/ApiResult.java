package com.search;

import java.util.List;
import java.util.Map;

public class ApiResult {

	public List<Result> resultList;
	public Map<String, String> query;
	public List<Stat> stats;
	public int cw;
	
	public ApiResult(List<Result> resultList, Map<String, String> query, List<Stat> stats, int cw) {
		this.resultList = resultList;
		this.query = query;
		this.stats = stats;
		this.cw = cw;
	}
}
