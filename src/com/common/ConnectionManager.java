package com.common;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionManager {
	
	private Connection connection = null;

    public Connection getConnection() {
    	if (connection != null) {
    		return this.connection;
    	}

        String host="localhost";
        String port="5432";
        String db_name="search_engine";
        String username="postgres";
        String password="password";
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+db_name+"", ""+username+"", ""+password+"");
            if (connection != null) {
            	connection.setAutoCommit(false);
            } else {
                System.out.println("Connection Failed");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    	return connection;
    }
}