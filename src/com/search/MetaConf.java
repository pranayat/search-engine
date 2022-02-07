package com.search;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.common.ConnectionManager;

public class MetaConf {

	static List<Engine> getConf() throws SQLException {
    	Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM meta_conf");
		ResultSet rs = pstmt.executeQuery();
	
		List<Engine> engines = new ArrayList<Engine>();
		while(rs.next()) {
			engines.add(new Engine(rs.getInt("id"), rs.getString("url"), rs.getBoolean("enabled")));
		}

		conn.close();
		return engines;
	}
	
	static void addEngine(String url) throws SQLException {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt = conn.prepareStatement("INSERT INTO meta_conf (url, enabled) VALUES (?, true)");
		pstmt.setString(1, url);
		pstmt.executeUpdate();
		
		conn.commit();
		conn.close();
	}
	
	static void deleteEngine(int id) throws SQLException {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt = conn.prepareStatement("DELETE FROM meta_conf WHERE id = ?");
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		
		conn.commit();
		conn.close();
	}
	
	static void toggleEngine(int id) throws SQLException {
		Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt = conn.prepareStatement("UPDATE meta_conf SET enabled = NOT enabled WHERE id = ?");
		pstmt.setInt(1, id);
		pstmt.executeUpdate();
		
		conn.commit();
		conn.close();
	}
	
	static List<Engine>getActiveEngines () throws SQLException {
    	Connection conn = (new ConnectionManager()).getConnection();
		PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM meta_conf WHERE enabled = true");
		ResultSet rs = pstmt.executeQuery();
	
		List<Engine> engines = new ArrayList<Engine>();
		while(rs.next()) {
			engines.add(new Engine(rs.getInt("id"), rs.getString("url"), rs.getBoolean("enabled")));
		}

		conn.close();
		return engines;
	}
}
