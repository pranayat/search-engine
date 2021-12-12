package com.crawler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.common.ConnectionManager;
import com.indexer.TFIDFScoreComputer;
import com.languageclassifier.DictionaryBootstrapper;
import com.languageclassifier.LanguageClassifier;

import com.indexer.TFIDFScoreComputer;
import com.scoring.PageRank;
import com.scoring.Okapi;
import com.scoring.ViewCreator;
import com.scoring.CombinedScore;

public class Driver {

	private static ArrayList<String> getSeedUrlsFromDB() {
		PreparedStatement pstmt;
		ResultSet rs;
		ArrayList<String> seedUrls = new ArrayList<String>();
		Connection conn = (new ConnectionManager()).getConnection();

		try {
			pstmt = conn.prepareStatement("SELECT url FROM documents ORDER BY docid DESC LIMIT 5");
			rs = pstmt.executeQuery();

			while(rs.next()) {
				seedUrls.add(rs.getString("url"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return seedUrls;
	}

	private static void dropTables() {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt;

		try {
			
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
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS engterms");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP TABLE IF EXISTS gerterms");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("DROP FUNCTION IF EXISTS best_fit_eng");
			pstmt.execute();

			pstmt = conn.prepareStatement("DROP FUNCTION IF EXISTS best_fit_ger");
			pstmt.execute();

			pstmt = conn.prepareStatement("DROP EXTENSION IF EXISTS fuzzystrmatch");
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
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS crawler_queue (id SERIAL PRIMARY KEY, thread_id INT, url VARCHAR, depth INT)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE INDEX h_url ON crawler_queue USING hash (url)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS documents (docid SERIAL PRIMARY KEY, url VARCHAR UNIQUE, crawled_on_date TIMESTAMP NULL, pagerank FLOAT, language VARCHAR)");
			pstmt.execute();
			
			pstmt = conn.prepareStatement("CREATE EXTENSION IF NOT EXISTS pg_trgm");
			pstmt.execute();

			// create a trigram index on url to allow more forgiving site: filtering
			pstmt = conn.prepareStatement("CREATE INDEX trgm_idx_url ON documents USING gin (url gin_trgm_ops)");
			
			pstmt = conn.prepareStatement("CREATE TABLE IF NOT EXISTS features (id SERIAL, docid INT, term VARCHAR, term_frequency BIGINT, df BIGINT, tf_idf FLOAT, bm25 FLOAT, combined FLOAT, language VARCHAR)");
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
			
			Statement stmt = conn.createStatement();
			stmt.execute("CREATE EXTENSION fuzzystrmatch");

		  String query = "CREATE FUNCTION best_fit_eng(word VARCHAR)"
		  		+ "						RETURNS TABLE ( "
		  		+ "							suggestion VARCHAR "
		  		+ "					)"
		  		+ "					AS $$"
		  		+ "		    		  BEGIN"
		  		+ "		    		  return query select term"
		  		+ "			    		  from eng_term_prob"
		  		+ "                        WHERE levenshtein(term, word) > 0"
		  		+ "			    		  ORDER BY levenshtein(term, word) ASC, prob DESC LIMIT 5;"
		  		+ "		    		END;"
		  		+ "		    		$$ language plpgsql;";

			stmt.execute(query);
			query = "CREATE FUNCTION best_fit_ger(word VARCHAR)"
			  		+ "						RETURNS TABLE ( "
			  		+ "							suggestion VARCHAR "
			  		+ "					)"
			  		+ "					AS $$"
			  		+ "		    		  BEGIN"
			  		+ "		    		  return query select term"
			  		+ "			    		  from ger_term_prob"
			  		+ "                        WHERE levenshtein(term, word) > 0"
			  		+ "			    		  ORDER BY levenshtein(term, word) ASC, prob DESC LIMIT 5;"
			  		+ "		    		END;"
			  		+ "		    		$$ language plpgsql;";	
				
			stmt.execute(query);
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
	
	public static void TFIDFscoring(Connection conn) {
		System.out.println("calling TFIDF scorer");
		TFIDFScoreComputer Scorer = new TFIDFScoreComputer(conn);
		Scorer.computeScores();
	}
	
	public static void PageRankScoring(Connection conn) {
		System.out.println("calling PageRank scorer");
		PageRank pr = new PageRank(conn);
		pr.pageRanking();
	}
	
	public static void OkapiScoring(Connection conn) {
		System.out.println("calling Okapi scorer");
		Okapi ok = new Okapi(conn);
		ok.okapiScoring();
	}
	
	public static void combinedScoring(Connection conn) {
		System.out.println("calling Combined scorer");
		CombinedScore cs = new CombinedScore(conn);
		cs.combinedScoring();
	}
	
	public static void creatingViews(Connection conn) {
		System.out.println("views created");
		ViewCreator vc = new ViewCreator(conn);
		vc.createViews();
	}
	
	public static void main(String[] args) throws NumberFormatException, SQLException, IOException {
		
		int maxDepth = 10, maxDocs = 1000, fanOut = 100;
		String resetIndex = "false", resetDict = "false";
		ArrayList<String> seedUrls = new ArrayList<String>();
		
		Options options = new Options();

		Option maxDocsOpt = new Option("n", "maxDocs", true, "maximum docs");
		maxDocsOpt.setRequired(true);
		options.addOption(maxDocsOpt);

		Option maxDepthOpt = new Option("d", "maxDepth", true, "maximum depth");
		maxDepthOpt.setRequired(true);
		options.addOption(maxDepthOpt);
		
		Option fanoutOpt = new Option("f", "fanOut", true, "fanout");
		fanoutOpt.setRequired(true);
		options.addOption(fanoutOpt);
		
		Option resetIndexOpt = new Option("ri", "resetIndex", true, "reset indexed data");
		resetIndexOpt.setRequired(true);
		options.addOption(resetIndexOpt);
		
		Option resetDictOpt = new Option("rd", "resetDict", true, "reset language dictionary");
		resetDictOpt.setRequired(true);
		options.addOption(resetDictOpt);

		CommandLineParser parser = new DefaultParser();
		HelpFormatter formatter = new HelpFormatter();
		CommandLine cmd = null; 

		try {
				cmd = parser.parse(options, args);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		maxDocs = Integer.parseInt(cmd.getOptionValue("maxDocs"));
		maxDepth = Integer.parseInt(cmd.getOptionValue("maxDepth"));
		fanOut = Integer.parseInt(cmd.getOptionValue("fanOut"));
		resetIndex = cmd.getOptionValue("resetIndex");
		resetDict = cmd.getOptionValue("resetDict");
    		
		System.out.println("maxDepth = " + maxDepth + ", maxDocs = " + maxDocs + ", fanOut = " + fanOut);
		
		Crawler c1 = null, c2 = null, c3 = null, c4 = null, c5 = null;
		Thread crawler1 = null, crawler2 = null, crawler3 = null, crawler4 = null, crawler5 = null;
		if(resetIndex.equals("true")) {
			System.out.println("Deleting old index...");
			seedUrls.add("https://www.cs.uni-kl.de");
			seedUrls.add("https://www.mathematik.uni-kl.de/en");
			seedUrls.add("https://www.mv.uni-kl.de/en");
			seedUrls.add("https://www.architektur.uni-kl.de/en/home/seite");
			seedUrls.add("https://www.eit.uni-kl.de/en/startseite/seite");
			
			seedUrls.add("https://www.asta.uni-kl.de");
			seedUrls.add("https://www.physik.uni-kl.de");
			seedUrls.add("https://www.sowi.uni-kl.de/home");
			seedUrls.add("https://wiwi.uni-kl.de");
			seedUrls.add("https://www.ru.uni-kl.de/startseite");
			
			dropTables();
			createTables();

			System.out.println("Old index deleted");

			c1 = new Crawler(1, maxDepth, maxDocs, fanOut, seedUrls.get(0), true);
			crawler1 = new Thread(c1);
			c2 = new Crawler(2, maxDepth, maxDocs, fanOut, seedUrls.get(1), true);
			crawler2 = new Thread(c2);
			c3 = new Crawler(3, maxDepth, maxDocs, fanOut, seedUrls.get(2), true);		
			crawler3 = new Thread(c3);
			c4 = new Crawler(4, maxDepth, maxDocs, fanOut, seedUrls.get(3), true);		
			crawler4 = new Thread(c4);
			c5 = new Crawler(5, maxDepth, maxDocs, fanOut, seedUrls.get(4), true);		
			crawler5 = new Thread(c5);
		}	else {
			c1 = new Crawler(1, maxDepth, maxDocs, fanOut, "", false);
			crawler1 = new Thread(c1);
			c2 = new Crawler(2, maxDepth, maxDocs, fanOut, "", false);
			crawler2 = new Thread(c2);
			c3 = new Crawler(3, maxDepth, maxDocs, fanOut, "", false);		
			crawler3 = new Thread(c3);
			c4 = new Crawler(4, maxDepth, maxDocs, fanOut, "", false);		
			crawler4 = new Thread(c4);
			c5 = new Crawler(5, maxDepth, maxDocs, fanOut, "", false);		
			crawler5 = new Thread(c5);
		}

		if (resetDict.equals("true")) {
			System.out.println("Bootstrapping language dictionaries...");
			DictionaryBootstrapper db1 = new DictionaryBootstrapper("eng");
			Thread db1Thread = new Thread(db1);
			DictionaryBootstrapper db2 = new DictionaryBootstrapper("ger");
			Thread db2Thread = new Thread(db2);
			
			db1Thread.start();
			db2Thread.start();
			
			try {
				db1Thread.join();
				db2Thread.join();
				System.out.println("Dictionaries created");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("Starting crawl session...");

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

			System.out.println("Crawl session ended");
			
			Connection conn = (new ConnectionManager()).getConnection();
			TFIDFscoring(conn);
			PageRankScoring(conn);
			OkapiScoring(conn);
			combinedScoring(conn);
			creatingViews(conn);
			
			System.out.println("END");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

