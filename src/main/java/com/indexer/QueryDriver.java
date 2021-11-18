package com.indexer;

import java.util.Arrays;
import java.util.Scanner;

import com.group3.Query;

public class QueryDriver {

	public static void main(String[] args) throws ClassNotFoundException {
		String queryMode = "conjunctive", queryText;
		int choice, k;
		Scanner sc = new Scanner(System.in);

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
		
		Query q = new Query(queryText, k);
		q.getResults();
	}

}
