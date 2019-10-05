package whiteboard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.*;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;


public class DrawingBoardServer extends UnicastRemoteObject implements RMIServer {
	Hashtable<String, RMIClient> clients;
	
	public DrawingBoardServer() throws RemoteException {
		super();
		clients = new Hashtable<String, RMIClient>();
	}
	
	public boolean register(String username, RMIClient client) throws IOException, RemoteException {
		System.out.println(clients.get(username));
		if (clients.get(username) != null) {
			return false;
		}
		synchronized (clients) {
			clients.put(username, client);
		}
		broadcast(null, "new_member", username);
		return true;
	}
	
	public boolean checkUsernameAvailability(String username) throws RemoteException {
		return clients.get(username) == null;
	}

	public boolean removeMember(String username) throws IOException, RemoteException { 
		if (clients.get(username) == null) {
			return false;
		}
		synchronized (clients) {
			clients.remove(username);
		}
		broadcast(null, "remove_member", username);
		return true;
	}
	
	public Vector<String> getMembers() throws RemoteException {
		Vector<String> members = new Vector<String>();
		Enumeration names = clients.keys();
		while (names.hasMoreElements()) {
			String name = (String) names.nextElement();
			members.add(name);
		}
		return members;
	}
	
	public boolean send(String to, String from, String mtag, Object data) throws IOException, RemoteException {
		if (clients.get(to) == null) {
			return false;
		}
		RMIClient receiver = (RMIClient) clients.get(to);
		receiver.notify(mtag, data, from);
		
		return true;
	};
	
	public boolean broadcast(String from, String mtag, Object data) throws IOException, RemoteException {
		Vector<String> members = getMembers();
		for (String receiver: members) {
			System.out.println(receiver);
			send(receiver, from, mtag, data);
		}
		return true;
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
