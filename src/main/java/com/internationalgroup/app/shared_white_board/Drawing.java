package com.internationalgroup.app.shared_white_board;

import java.util.*;

public class Drawing {
	private int id;
	private User owner;
	private String fileURL;
	private String name;
	private ArrayList<User> collaborators;
	
	public Drawing(User owner, String fileURL, String name) {
		this.owner = owner;
		this.fileURL = fileURL;
		this.name = name;
		this.collaborators = new ArrayList<User>();
	}
	
	public Drawing(int id, User owner, String fileURL, String name) {
		this.id = id;
		this.owner = owner;
		this.fileURL = fileURL;
		this.name = name;
		this.collaborators = new ArrayList<User>();
	}
	
	public void addCollaborator(User user) {
		this.collaborators.add(user);
	}
	
	public int getId() {
		return this.id;
	}
	
	public User getOwner() {
		return this.owner;
	}
	
	public String getFileURL() {
		return this.fileURL;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<User> getCollaborators() {
		return this.collaborators;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public void setOwner(User user) {
		this.owner = user;
	}
	
	public void setCollaborators(List<User> collaborators) {
		this.collaborators = (ArrayList<User>) collaborators;
	}
	
	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
