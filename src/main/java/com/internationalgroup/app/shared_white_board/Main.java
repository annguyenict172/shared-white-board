package com.internationalgroup.app.shared_white_board;

import java.util.ArrayList;

public class Main {
	public static void main(String[] args) {
		UserDAO userDAO = new UserDAO();
    	DrawingDAO drawingDAO = new DrawingDAO();
    	MessageDAO messageDAO = new MessageDAO();
    	
    	User an2 = userDAO.getUser(2);
    	Drawing drawing = drawingDAO.getDrawing(2);
    	
    	Message message1 = new Message(an2, "hello world", 1000);
    	Message message2 = new Message(an2, "hello world!!!", 1001);
    	
    	messageDAO.addMessage(message1, drawing);
    	messageDAO.addMessage(message2, drawing);
    	
    	ArrayList<Message> messages = (ArrayList<Message>) messageDAO.getMessageList(drawing);
    	
    	for (Message message : messages) {
    		System.out.println(message.getText());
    	}
    }
}
