package com.internationalgroup.app.shared_white_board;

import java.sql.*;
import java.util.*;

public class DatabaseTemplate {
	
	Connection connection;
	String dbName = "shared_white_board.db";
	
	public List<Map<String, Object>> executeQuery(String query) {
		List<Map<String, Object>> results = new ArrayList<Map<String, Object>>();
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);
	        ResultSet rs = statement.executeQuery(query);
	        ResultSetMetaData rsMetaData = rs.getMetaData();
            int numberOfColumns = rsMetaData.getColumnCount();
	        while(rs.next()) {
	        	Map<String, Object> item = new HashMap<String, Object>();
	            for (int i = 1; i < numberOfColumns + 1; i++) {
	              String columnName = rsMetaData.getColumnName(i);
	              item.put(columnName, rs.getObject(columnName));

	            }
	        	results.add(item);
	        }
	        return results;
	    } catch(SQLException e) {
	    	System.err.println(e.getMessage());
	    	throw new RuntimeException("SQL Exception");
	    } finally {
	    	try {
	    		if (connection != null) connection.close();
	    	} catch(SQLException e) {
	    		System.err.println(e.getMessage());
		    	throw new RuntimeException("SQL Exception");
	    	}
	    }
	}
	
	public int executeInsert(String insertStatement) {
		try {
			int lastInsertRowId = -1;
			connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);
	        statement.executeUpdate(insertStatement);
	        ResultSet generatedKeys = statement.getGeneratedKeys();
	        if (generatedKeys.next()) {
	            lastInsertRowId = (int) generatedKeys.getLong(1);
	        }
	        return lastInsertRowId;
	    } catch(SQLException e) {
	    	System.err.println(e.getMessage());
	    	throw new RuntimeException("SQL Exception");
	    } finally {
	    	try {
	    		if (connection != null) connection.close();
	    	} catch(SQLException e) {
	    		System.err.println(e.getMessage());
		    	throw new RuntimeException("SQL Exception");
	    	}
	    }
	}
	
	public void executeUpdate(String updateStatement) {
		try {
			connection = DriverManager.getConnection("jdbc:sqlite:" + this.dbName);
	        Statement statement = connection.createStatement();
	        statement.setQueryTimeout(30);
	        statement.executeUpdate(updateStatement);
	    } catch(SQLException e) {
	    	System.err.println(e.getMessage());
	    	throw new RuntimeException("SQL Exception");
	    } finally {
	    	try {
	    		if (connection != null) connection.close();
	    	} catch(SQLException e) {
	    		System.err.println(e.getMessage());
		    	throw new RuntimeException("SQL Exception");
	    	}
	    }
	}
}