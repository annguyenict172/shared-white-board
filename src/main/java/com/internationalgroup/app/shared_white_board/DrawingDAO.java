package com.internationalgroup.app.shared_white_board;

import java.util.*;

public class DrawingDAO {
	DatabaseTemplate dbTemplate;
	
	public DrawingDAO() {
		dbTemplate = new DatabaseTemplate();
	}
	
	public Drawing getDrawing(int id) {
		String query = "SELECT * FROM drawings WHERE id = " + id + ";";
		List<Map<String, Object>> results = dbTemplate.executeQuery(query);
		if (results.size() < 1) {
			return null;
		}
		
		Map<String, Object> drawingInfo = results.get(0);
		int ownerId = (Integer) drawingInfo.get("owner_id");
		UserDAO userDAO = new UserDAO();
		User owner = userDAO.getUser(ownerId);
		String fileURL = (String) drawingInfo.get("file_url");
		String name = (String) drawingInfo.get("name");
		
		Drawing drawing = new Drawing(id, owner, fileURL, name);
		
		ArrayList<User> collaborators = new ArrayList<User>();
		query = "SELECT * FROM drawings_collaborators WHERE drawing_id = " + id + ";";
		results = dbTemplate.executeQuery(query);
		for (Map<String, Object> result : results) {
			int collaboratorId = (Integer) result.get("collaborator_id");
			User collaborator = userDAO.getUser(collaboratorId);
			collaborators.add(collaborator);
		}
		
		drawing.setCollaborators(collaborators);
			
		return drawing;
	}
	
	public void addDrawing(Drawing drawing) {
		String insertStatement;
		insertStatement = "INSERT INTO drawings (owner_id, file_url, name) VALUES ";
		insertStatement += "(" + drawing.getOwner().getId() + ", \"" + drawing.getFileURL() + "\", \"" + drawing.getName() + "\");";
		int newId = dbTemplate.executeInsert(insertStatement);
		drawing.setId(newId);
		
		if (drawing.getCollaborators().size() > 0) {
			insertStatement = "INSERT INTO drawings_collaborators (drawing_id, collaborator_id) VALUES ";
			for (User collaborator : drawing.getCollaborators()) {
				insertStatement += "(" + newId + ", " + collaborator.getId() + "),";
			}
			insertStatement = insertStatement.substring(0, insertStatement.length() - 1);
			insertStatement += ";";
			dbTemplate.executeInsert(insertStatement);
		}
	}
	
	public void addCollaborator(Drawing drawing, User collaborator) {
		String sqlStatement;
		sqlStatement = "INSERT INTO drawings_collaborators (collaborator_id, drawing_id) VALUE";
		sqlStatement += "(" + collaborator.getId() + ", " + drawing.getId() + ");";
		dbTemplate.executeUpdate(sqlStatement);
	}
}
