package com.internationalgroup.app.shared_white_board;

public class CreateDatabase {
	public static void main(String[] args) {
		DatabaseTemplate dbTemplate = new DatabaseTemplate();
		String sqlStatement = "";

		sqlStatement += "CREATE TABLE drawings_collaborators";
		sqlStatement += "(";
	    sqlStatement +=	"collaborator_id INTEGER REFERENCES drawings (id),";
	    sqlStatement += "drawing_id INTEGER";
	    sqlStatement += ");";

	    sqlStatement += "CREATE TABLE users";
	    sqlStatement += "(";
	    sqlStatement += "id INTEGER PRIMARY KEY,";
	    sqlStatement += "username CHAR(30),";
	    sqlStatement += "password CHAR(30)";
	    sqlStatement += ");";

	    sqlStatement += "CREATE TABLE drawings";
	    sqlStatement += "(";
	    sqlStatement += "id INTEGER PRIMARY KEY,";
	    sqlStatement += "file_url CHAR(100),";
	    sqlStatement += "owner_id INTEGER REFERENCES users (id),";
	    sqlStatement += "name CHAR(30)";
	    sqlStatement += ");";

	    sqlStatement += "CREATE TABLE workspace_messages";
	    sqlStatement += "(";
	    sqlStatement += "sender_id INTEGER REFERENCES users (id),";
	    sqlStatement += "drawing_id INTEGER REFERENCES drawings (id),";
	    sqlStatement += "text TEXT";
	    sqlStatement += "timestamp INTEGER";
	    sqlStatement += ");";

	    dbTemplate.executeUpdate(sqlStatement);
	}
}
