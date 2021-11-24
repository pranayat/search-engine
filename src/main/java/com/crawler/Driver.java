package main.java.com.crawler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import main.java.com.common.ConnectionManager;

public class Driver {

	private static void dropTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS crawler_queue");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS documents");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS features");
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
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS documents (docid SERIAL PRIMARY KEY, url VARCHAR UNIQUE, crawled_on_date TIMESTAMP NULL)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE EXTENSION IF NOT EXISTS pg_trgm");
			pstmt.execute();

			// create a trigram index on url to allow more forgiving site: filtering
			pstmt = conn.prepareStatement("CREATE INDEX trgm_idx_url ON documents USING gin (url gin_trgm_ops)");
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS features (docid INT, term VARCHAR, term_frequency BIGINT, tf_idf FLOAT)");
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
		
		// Comment these out if this is not the first time runningthis project
		dropTables();
		createTables();
		
		Crawler c1 = new Crawler(false, 1, 10, 100, 100, "https://en.wikipedia.org/wiki/Led_Zeppelin");
		Thread crawler1 = new Thread(c1);
		Crawler c2 = new Crawler(false, 2, 10, 100, 100, "https://cs.uni-kl.de");
		Thread crawler2 = new Thread(c2);
		Crawler c3 = new Crawler(false, 3, 10, 100, 100, "https://www.uni-kl.de");		
		Thread crawler3 = new Thread(c3);
		Crawler c4 = new Crawler(false, 4, 10, 100, 100, "https://www.kaiserslautern.de");		
		Thread crawler4 = new Thread(c4);
		
		crawler1.start();
		crawler2.start();
		crawler3.start();
		crawler4.start();
		
		try {
			crawler1.join();
			crawler2.join();
			crawler3.join();
			crawler4.join();
			System.out.println("END");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

