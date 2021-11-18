package com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.common.ConnectionManager;

public class Driver {

	private static void dropTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			pstmt = conn.prepareStatement("DROP TABLE crawler_queue");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE documents");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE features");
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
	
	private static void createTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS crawler_queue (id SERIAL PRIMARY KEY, thread_id INT, url VARCHAR, popped BOOLEAN, depth INT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS documents (docid SERIAL PRIMARY KEY, url VARCHAR UNIQUE, crawled_on_date TIMESTAMP NULL)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS features (docid INT, term_frequency BIGINT, tf_idf BIGINT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS links (from_docid INT, to_docid INT)");

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
		
		dropTables();
		createTables();
		
		Crawler c1 = new Crawler(false, 1, 10, 3, 10, "https://en.wikipedia.org", "");
		Thread crawler1 = new Thread(c1);
		Crawler c2 = new Crawler(false, 2, 10, 3, 10, "https://cs.uni-kl.de", "");
		Thread crawler2 = new Thread(c2);
		Crawler c3 = new Crawler(false, 3, 10, 3, 10, "https://www.uni-kl.de", "");		
		Thread crawler3 = new Thread(c3);
		
		crawler1.start();
		crawler2.start();
		crawler3.start();
		
		try {
			crawler1.join();
			crawler2.join();
			crawler3.join();
			System.out.println("END");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

