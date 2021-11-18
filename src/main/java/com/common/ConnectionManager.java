package com.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
        String password="root";
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://"+host+":"+port+"/"+db_name+"", ""+username+"", ""+password+"");
            if (connection != null) {
            	System.out.println("Connection OK");
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