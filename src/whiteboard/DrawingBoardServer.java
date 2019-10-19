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
	Hashtable<String, Hashtable<String, RMIDrawingClient>> drawingClients;
	Hashtable<String, String> drawingManagers;
	Hashtable<String, String> drawingKeys;
	Hashtable<String, Vector<String>> drawingChats;
	Hashtable<String, Vector<String>> drawingInstructions;
	
	public DrawingBoardServer() throws RemoteException {
		super();
		drawingClients = new Hashtable<String, Hashtable<String, RMIDrawingClient>>();
		drawingManagers = new Hashtable<String, String>();
		drawingKeys = new Hashtable<String, String>();
		drawingChats = new Hashtable<String, Vector<String>>();
		drawingInstructions = new Hashtable<String, Vector<String>>();
	}
	
	public Hashtable<String, String> createDrawing(String username, RMIDrawingClient client) throws ServerError, RemoteException {
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
		
		// Send the info back to the manager
		Hashtable<String, String> drawingInfo = new Hashtable<String, String>();
		drawingInfo.put("drawingId", drawingId);
		drawingInfo.put("drawingKey", drawingKey);
		
		return drawingInfo;
	}
	
	public void joinDrawing(String username, String drawingId, RMIDrawingClient client) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		String manager = drawingManagers.get(drawingId);
		
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}

		send(manager, username, drawingId, MessageTag.ASK_TO_JOIN, client);
	}
	
	public void addToDrawing(String username, String drawingId, String managerKey, RMIDrawingClient client) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
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
		
		send(username, null, drawingId, MessageTag.MANAGER_APPROVED, null);
		broadcast(null, drawingId, MessageTag.NEW_MEMBER, username);
	}
	
	public void declineFromDrawing(String managerKey, RMIDrawingClient client) throws ServerError, RemoteException {
		client.notify(MessageTag.MANAGER_DECLINED, null, null);
	}
	
	public void removeMember(String username, String drawingId, String managerKey) throws ServerError, RemoteException {
		Hashtable<String, RMIDrawingClient> clients = drawingClients.get(drawingId);
		if (clients == null) {
			throw new ServerError("Drawing does not exist.");
		}
		if (clients.get(username) == null) {
			throw new ServerError("User does not exist.");
		}

		clients.remove(username);
		synchronized (drawingClients) {
			drawingClients.put(drawingId, clients);
		}
		broadcast(null, drawingId, MessageTag.REMOVE_MEMBER, username);
	}
	
	public boolean isManager(String username, String drawingId) throws ServerError, RemoteException {
		String manager = drawingManagers.get(drawingId);
		if (manager != username) {
			return false;
		}
		return true;
	}
	
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
		Vector<String> members = getMembers(drawingId);
		for (String receiver: members) {
			send(receiver, from, drawingId, mtag, data);
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
