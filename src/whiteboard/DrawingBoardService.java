package whiteboard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.UUID;


public class DrawingBoardService extends UnicastRemoteObject implements RMIClient {
	private String username = UUID.randomUUID().toString();
	private RMIServer server;
	private DrawingBoard drawingBoard;
	
	public DrawingBoardService(String hostname, int port, String serverName, DrawingBoard drawingBoard) throws RemoteException {
		super();
		this.drawingBoard = drawingBoard;
		try {
			System.out.println("Connecting to server...");
			boolean connected = connect(hostname, port, serverName);
			if (!connected) {
				System.err.println("Cannot connect to server");
			}
		} catch (Exception e) {
			System.err.println("Error when connecting to server: " + e);
		}
		
	}
	
	public String getUsername() throws RemoteException {
		return this.username;
	}
	
	public boolean connect(String host, int port, String serverName) throws IOException, RemoteException {
		Registry registry = LocateRegistry.getRegistry(host, port);
		try {
			server = (RMIServer) registry.lookup(serverName);
			boolean available = server.checkUsernameAvailability("anthailan");
			if (!available) {
				System.err.println("This username has been taken");
				return false;
			}
			server.register(getUsername(), this);
			return true;
		} catch (Exception e) {
			System.err.println(e);
			return false;
		}
	}
	
	public boolean send(String tag, Object data, String dst) throws IOException, RemoteException {
		server.send(dst, this.getUsername(), tag, data);
		return true;
	}
	
	public boolean broadcast(String tag, Object data) throws IOException, RemoteException {
		server.broadcast(this.getUsername(), tag, data);
		return true;
	}
	
	public boolean notify(String tag, Object data, String src) throws IOException, RemoteException {
		drawingBoard.notify(tag, data, src);
		return true;
	}
}

