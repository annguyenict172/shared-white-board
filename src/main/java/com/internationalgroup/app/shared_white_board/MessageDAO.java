package com.internationalgroup.app.shared_white_board;

import java.util.*;

public class MessageDAO {
	DatabaseTemplate dbTemplate;
	
	public MessageDAO() {
		dbTemplate = new DatabaseTemplate();
	}
	
	public List<Message> getMessageList(Drawing drawing) {
		ArrayList<Message> messages = new ArrayList<Message>();
		
		String query = "SELECT * FROM workspace_messages WHERE drawing_id = " + drawing.getId() + ";";
		List<Map<String, Object>> results = dbTemplate.executeQuery(query);
		
		UserDAO userDAO = new UserDAO();
		for (Map<String, Object> result : results) {
			int senderId = (Integer) result.get("sender_id");
			int timestamp = (Integer) result.get("timestamp");
			String text = (String) result.get("text");
			User sender = userDAO.getUser(senderId);
			Message message = new Message(sender, text, timestamp);
			messages.add(message);
		}
		
		return messages;
	}
	
	public void addMessage(Message message, Drawing drawing) {
		String sqlStatement;
		sqlStatement = "INSERT INTO workspace_messages (sender_id, drawing_id, text, timestamp) VALUES ";
		sqlStatement += "(" + message.getSender().getId() + ", " + drawing.getId() + ", \"" + message.getText() + "\", " + message.getTimestamp() +");";
		dbTemplate.executeUpdate(sqlStatement);
	}
}
