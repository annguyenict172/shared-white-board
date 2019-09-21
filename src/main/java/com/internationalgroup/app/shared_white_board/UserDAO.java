package com.internationalgroup.app.shared_white_board;

import java.util.*;

public class UserDAO {
	DatabaseTemplate dbTemplate;
	
	public UserDAO() {
		dbTemplate = new DatabaseTemplate();
	}
	
	public User getUser(int id) {
		String query = "SELECT * FROM users WHERE id = " + id + ";";
		List<Map<String, Object>> results = dbTemplate.executeQuery(query);
		if (results.size() < 1) {
			return null;
		}
		
		Map<String, Object> userInfo = results.get(0);
		String username = (String) userInfo.get("username");
		String password = (String) userInfo.get("password");
		
		return new User(id, username, password);	
	}
	
	public User getUser(String username) {
		String query = "SELECT * FROM users WHERE username = " + username + ";";
		List<Map<String, Object>> results = dbTemplate.executeQuery(query);
		if (results.size() < 1) {
			return null;
		}
		
		Map<String, Object> userInfo = results.get(0);
		int userId = Integer.parseInt((String) userInfo.get("id"));
		String password = (String) userInfo.get("password");
		
		return new User(userId, username, password);
	}
	
	public void addUser(User user) {
		String insertStatement;
		insertStatement = "INSERT INTO users (username, password) VALUES ";
		insertStatement += "(\"" + user.getUsername() + "\", \"" + user.getPassword() + "\");";
		System.out.println(insertStatement);
		int newId = dbTemplate.executeInsert(insertStatement);
		user.setId(newId);
	}

	public void updateUser(User user) {
		String updateStatement;
		updateStatement = "UPDATE users ";
		updateStatement += "SET username = \"" + user.getUsername() + "\", password = \"" + user.getPassword() + "\"";
		updateStatement += " WHERE id = " + user.getId() + ";";
		dbTemplate.executeUpdate(updateStatement);
	}
}
