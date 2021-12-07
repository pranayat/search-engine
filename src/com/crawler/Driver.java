package com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

import com.common.ConnectionManager;
import com.indexer.TFIDFScoreComputer;

public class Driver {

	private static void dropTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			
//			pstmt = conn.prepareStatement("DROP VIEW IF EXISTS features_tfifd");
//			pstmt.execute();
//			
//			pstmt = conn.prepareStatement("DROP VIEW IF EXISTS features_bm25");
//			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS crawler_queue");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS documents");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS features CASCADE");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS links");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS links");
			pstmt.execute();			
			
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
		
	}
	
	private static void createTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS crawler_queue (id SERIAL PRIMARY KEY, thread_id INT, url VARCHAR, popped BOOLEAN, depth INT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE INDEX h_url ON crawler_queue USING hash (url)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS documents (docid SERIAL PRIMARY KEY, url VARCHAR UNIQUE, crawled_on_date TIMESTAMP NULL, pagerank FLOAT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE EXTENSION IF NOT EXISTS pg_trgm");
			pstmt.execute();

			// create a trigram index on url to allow more forgiving site: filtering
			pstmt = conn.prepareStatement("CREATE INDEX trgm_idx_url ON documents USING gin (url gin_trgm_ops)");
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS features (id SERIAL, docid INT, term VARCHAR, term_frequency BIGINT, df BIGINT, tf_idf FLOAT, num_elem BIGINT, bm25 FLOAT, combined FLOAT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS links (from_docid INT, to_docid INT)");
			pstmt.execute();
			
			//indices for making it faster
			pstmt = conn.prepareStatement("CREATE INDEX feat_id ON features USING hash (id)");
			pstmt.execute();
			pstmt = conn.prepareStatement("CREATE INDEX feat_docid ON features USING hash (docid)");
			pstmt.execute();
			pstmt = conn.prepareStatement("CREATE INDEX feat_term ON features USING hash (term)");
			pstmt.execute();
			pstmt = conn.prepareStatement("CREATE INDEX doc_id ON documents USING hash (docid)");
			pstmt.execute();
			
			
			conn.commit();
		} catch (Exception e) {
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) {
		
		int maxDepth = 10, maxDocs = 1000, fanOut = 100;
		Boolean isFreshRun = false;

		if (args.length > 0 && args[0].length() > 0) {
			maxDepth = Integer.parseInt(args[0]);
		}
		
		if (args.length > 0 && args[1].length() > 0) {
			maxDocs = Integer.parseInt(args[1]);
		}
		
		if (args.length > 0 && args[2].length() > 0) {
			fanOut = Integer.parseInt(args[2]);
		}
		
		System.out.println("maxDepth = " + maxDepth + ", maxDocs = " + maxDocs + ", fanOut = " + fanOut);
		
		if (args.length > 0 && args[3].equals("reset")) {
			System.out.println("Rebuilding index from scratch...");
			isFreshRun = true;
			dropTables();
			createTables();
		}		
		
		System.out.println("Starting crawl");

		Crawler c1 = new Crawler(isFreshRun, 1, maxDepth, maxDocs, fanOut, "https://www.cs.uni-kl.de");
		Thread crawler1 = new Thread(c1);
		Crawler c2 = new Crawler(isFreshRun, 2, maxDepth, maxDocs, fanOut, "https://www.asta.uni-kl.de/");
		Thread crawler2 = new Thread(c2);
		Crawler c3 = new Crawler(isFreshRun, 3, maxDepth, maxDocs, fanOut, "https://www.mathematik.uni-kl.de/en/");		
		Thread crawler3 = new Thread(c3);
		Crawler c4 = new Crawler(isFreshRun, 4, maxDepth, maxDocs, fanOut, "https://www.mv.uni-kl.de/en/");		
		Thread crawler4 = new Thread(c4);
		Crawler c5 = new Crawler(isFreshRun, 5, maxDepth, maxDocs, fanOut, "https://www.architektur.uni-kl.de/en/home/seite");		
		Thread crawler5 = new Thread(c5);

		
		crawler1.start();
		crawler2.start();
		crawler3.start();
		crawler4.start();
		crawler5.start();
		
		try {
			crawler1.join();
			crawler2.join();
			crawler3.join();
			crawler4.join();
			crawler5.join();

			System.out.println("END");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

