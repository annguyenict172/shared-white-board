package whiteboard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import java.util.UUID;


public class DrawingBoardServer extends UnicastRemoteObject implements RMIDrawingServer {
	Hashtable<String, Hashtable<String, RMIDrawingClient>> drawingClients;	// Store the RMIDrawingClient objects for each drawing
	Hashtable<String, String> drawingManagers;	// Store the managers for each drawing
	Hashtable<String, String> drawingKeys;	// Store the manager keys for each drawing
	Hashtable<String, Vector<String>> drawingChats;
	Hashtable<String, Vector<Hashtable<String, Object>>> drawingInstructions;
	
	public DrawingBoardServer() throws RemoteException {
		super();
		drawingClients = new Hashtable<String, Hashtable<String, RMIDrawingClient>>();
		drawingManagers = new Hashtable<String, String>();
		drawingKeys = new Hashtable<String, String>();
		drawingChats = new Hashtable<String, Vector<String>>();
		drawingInstructions = new Hashtable<String, Vector<Hashtable<String, Object>>>();
	}
	
	// Create a new drawing
	public Hashtable<String, String> createDrawing(String username, RMIDrawingClient client) throws ServerError, RemoteException {
		// Generate unique drawing ID and manager key
		String drawingId = UUID.randomUUID().toString();
		String drawingKey = UUID.randomUUID().toString();
		Hashtable<String, RMIDrawingClient> clients = new Hashtable<String, RMIDrawingClient>();
		clients.put(username, client);
		
		// Update all lookup table
		synchronized (drawingClients) {
			drawingClients.put(drawingId, clients);
		}
		synchronized (drawingManagers) {
			drawingManagers.put(drawingId, username);
		}
		synchronized (drawingKeys) {
			drawingKeys.put(drawingId, drawingKey);
		}
		synchronized (drawingInstructions) {
			drawingInstructions.put(drawingId, new Vector<Hashtable<String, Object>>());
		}
		synchronized (drawingChats) {
			drawingChats.put(drawingId, new Vector<String>());
		}
		
		// Send the info back to the manager
		Hashtable<String, String> drawingInfo = new Hashtable<String, String>();
		drawingInfo.put("drawingId", drawingId);
		drawingInfo.put("drawingKey", drawingKey);
		
		return drawingInfo;
	}
	
	// Request to join a specific drawing
	// Send the request to the manager
	public void joinDrawing(String username, String drawingId, RMIDrawingClient client) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		String manager = drawingManagers.get(drawingId);
		
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		
		if (clients.get(username) != null) {
			throw new ServerError("Your username has already been chosen by someone else. Please choose another name.");
		}

		send(manager, username, drawingId, MessageTag.ASK_TO_JOIN, client);
	}
	
	// Used by the manager to add a new member to the current drawing
	public void addToDrawing(String username, String drawingId, String managerKey, RMIDrawingClient client) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		
		if (clients.get(username) != null) {
			String message = "Your username has already been chosen by someone else. Please choose another name.";
			send(username, null, drawingId, MessageTag.USERNAME_EXISTED, null);
			return;
		}
		
		String key = drawingKeys.get(drawingId);
		if (key.compareTo(managerKey) != 0) {
			System.out.println("Real key: " + key);
			System.out.println("Received key: " + managerKey);
			throw new ServerError("You are not the manager.");
		}
		
		clients.put(username, client);
		System.out.println("Add client " + username + " successfully!");
		
		synchronized (drawingClients) {
			drawingClients.put(drawingId, clients);
		}
		
		// Notify the user that he has been approved
		send(username, null, drawingId, MessageTag.MANAGER_APPROVED, null);
		
		Vector<Hashtable<String, Object>> instructions = drawingInstructions.get(drawingId);
		send(username, null, drawingId, MessageTag.CURRENT_DRAWING_INSTRUCTIONS, instructions);
		
		Vector<String> chats = drawingChats.get(drawingId);
		send(username, null, drawingId, MessageTag.CURRENT_CHATS, chats);
		
		// Notify all the other users that we have a new member
		broadcast(null, drawingId, MessageTag.NEW_MEMBER, username);
	}
	
	// Used by the manager to decline a request to join from other member
	public void declineFromDrawing(String managerKey, RMIDrawingClient client) throws ServerError, RemoteException {
		client.notify(MessageTag.MANAGER_DECLINED, null, null);
	}
	
	// Used by the manager to kick a member from the current drawing
	public void removeMember(String username, String drawingId, String managerKey) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		
		if (clients.get(username) == null) {
			throw new ServerError("User does not exist.");
		}
		
		if (isManager(username, drawingId)) {
			throw new ServerError("You cannot kick yourself.");
		}
		
		send(username, null, drawingId, MessageTag.KICK_BY_MANAGER, username);

		clients.remove(username);
		synchronized (drawingClients) {
			drawingClients.put(drawingId, clients);
		}
		broadcast(null, drawingId, MessageTag.REMOVE_MEMBER, username);
	}
	
	// Check if this user is the manager of the current drawing
	public boolean isManager(String username, String drawingId) throws ServerError, RemoteException {
		String manager = drawingManagers.get(drawingId);
		if (manager.compareTo(username) != 0) {
			return false;
		}
		return true;
	}
	
	// Get all members from the current drawing
	public Vector<String> getMembers(String drawingId) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		if (clients == null) {
			return null;
		}
		Vector<String> members = new Vector<String>();
		Enumeration names = clients.keys();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			members.add(name);
		}
		return members;
	}
	
	public void send(String to, String from, String drawingId, String mtag, Object data) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		if (clients.get(to) == null) {
			throw new ServerError("The receiver does not exist.");
		}
		RMIDrawingClient receiver = (RMIDrawingClient) clients.get(to);
		receiver.notify(mtag, data, from);
	};
	
	public void broadcast(String from, String drawingId, String mtag, Object data) throws ServerError, RemoteException {
		// Store the drawing and chat
		if (mtag.compareTo(MessageTag.DRAW) == 0) {
			Vector<Hashtable<String, Object>> instructions = (Vector<Hashtable<String, Object>>) drawingInstructions.get(drawingId);
			if (instructions == null) {
				throw new ServerError("Drawing does not exist.");
			}
			instructions.add((Hashtable<String, Object>) data);
			synchronized (drawingInstructions) {
				drawingInstructions.put(drawingId, instructions);
			}
		} else if (mtag.compareTo(MessageTag.CHAT) == 0) {
			Vector<String> chats = (Vector<String>) drawingChats.get(drawingId);
			if (chats == null) {
				throw new ServerError("Drawing does not exist.");
			}
			chats.add(from + ": " + (String) data);
			synchronized (drawingChats) {
				drawingChats.put(drawingId, chats);
			}
		}
		
		Vector<String> members = getMembers(drawingId);
		for (String receiver: members) {
			send(receiver, from, drawingId, mtag, data);
		}
	}
	
	public void quit(String username, String drawingId) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		if (clients.get(username) == null) {
			throw new ServerError("The user does not exist.");
		}
		
		clients.remove(username);
		synchronized (drawingClients) {
			drawingClients.put(drawingId, clients);
		}

		if (isManager(username, drawingId)) {
			broadcast(null, drawingId, MessageTag.MANAGER_QUIT, username);
			
			// Clear all look up tables for the drawing
			drawingClients.remove(drawingId);
			drawingManagers.remove(drawingId);
			drawingKeys.remove(drawingId);
			drawingChats.remove(drawingId);
			drawingInstructions.remove(drawingId);
		} else {
			broadcast(null, drawingId, MessageTag.MEMBER_QUIT, username);
		}
	}
	
	public static void main(String[] args) {
		try {
			int port = Integer.parseInt(args[0]);
			Registry registry = LocateRegistry.createRegistry(port);
			System.out.println("Locate registry successfully...");
			DrawingBoardServer server = new DrawingBoardServer();
			System.out.println("Binding server...");
			registry.rebind("WhiteBoard", server);
			System.out.println("Binding server successfully!");
		} catch (Exception e) {
			System.err.println("Caught exception when registering: " + e);
		}

	}
}
