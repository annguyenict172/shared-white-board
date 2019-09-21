package com.internationalgroup.app.shared_white_board;

public class Message {
	private User sender;
	private String text;
	private int timestamp;
	
	public Message (User sender, String text, int timestamp) {
		this.sender = sender;
		this.text = text;
		this.timestamp = timestamp;
	}
	
	public User getSender() {
		return this.sender;
	}
	
	public String getText() {
		return this.text;
	}
	
	public int getTimestamp() {
		return this.timestamp;
	}
}
