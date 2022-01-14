package com.cli;

import java.util.List;
import java.util.Scanner;

import com.search.ApiResult;
import com.search.Query;
import com.search.Result;

import net.sf.extjwnl.JWNLException;

public class QueryCLI {

	public static void main(String[] args) throws ClassNotFoundException, JWNLException {
		String queryMode = "conjunctive", queryText;
		int choice, k;
		Scanner sc = new Scanner(System.in);
		ApiResult apiResult = null;

		System.out.println("Please select a search mode\n");
		System.out.println("1. Conjunctive\n");
		System.out.println("2. Disjunctive\n");
		choice = sc.nextInt();
		
		if (choice == 1) {
			queryMode = "conjunctive";
		}
		
		if (choice == 2) {
			queryMode = "disjunctive";
		}
		
		System.out.println("Please enter a k value to display top k results\n");
		k = sc.nextInt();
		sc.nextLine(); // read new line which nextInt doesn't
		
		System.out.println("Please enter query terms separated by space\n");
		queryText = sc.nextLine();
		
		String queryTextWithQuotes = queryText;
		if (queryMode.equals("conjunctive")) {
			for (String term: queryText.split("\\s+")) {
				queryText = "\"" + term + "\"";
			}
		}
		
		Query q = new Query(queryTextWithQuotes, k, "tf_idf", "eng", "web");
		apiResult = q.getResults();
		System.out.println("URL   Score   Rank");
		for (Result result: apiResult.resultList) {
			System.out.println(result.getUrl()
				+ "   " + result.getScore() + "   " + result.getRank());
		}
		
		sc.close();
	}

}
