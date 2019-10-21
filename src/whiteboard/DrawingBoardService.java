package whiteboard;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Hashtable;
import java.util.UUID;
import java.util.Vector;


public class DrawingBoardService extends UnicastRemoteObject implements RMIDrawingClient {
	public String username;
	public String drawingId;
	private String drawingKey;
	
	private RMIDrawingServer server;
	private DrawingBoard drawingBoard;
	
	// Connect to the server right after the initialization of this object
	public DrawingBoardService(String hostname, int port, String serverName, DrawingBoard drawingBoard) throws RemoteException, NotBoundException {
		super();
		this.drawingBoard = drawingBoard;
		Registry registry = LocateRegistry.getRegistry(hostname, port);
		server = (RMIDrawingServer) registry.lookup(serverName);
	}
	
	// Check if the current user is the manager
	public boolean isManager() {
		try {
			return server.isManager(username, drawingId);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
			return false;
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
			return false;
		}
	}
	
	// Create a new drawing, and receive the drawing info from server
	// If the user calls this method, he will become the manager
	public void createDrawing() {
		try {
			Hashtable<String, String> drawingInfo = server.createDrawing(this.username, this);
			drawingId = drawingInfo.get("drawingId");
			drawingKey = drawingInfo.get("drawingKey");
			System.out.println("Drawing ID: " + drawingId);
			System.out.println("Drawing Key: " + drawingKey);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Ask to join a drawing
	// Still have to wait for approval from the manager
	public void joinDrawing(String drawingId) {
		try {
			server.joinDrawing(this.username, drawingId, this);
			this.drawingId = drawingId;
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Send a message to another user in the same drawing
	public void send(String tag, Object data, String dst) {
		try {
			server.send(dst, this.username, this.drawingId, tag, data);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Broadcast a message to all users in the same drawing
	public void broadcast(String tag, Object data) {
		try {
			server.broadcast(this.username, this.drawingId, tag, data);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
			System.err.println(e);
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Add a user to the current drawing
	// This method is used by the manager, since only he has the drawing key
	public void addToDrawing(String username, RMIDrawingClient client) {
		try {
			server.addToDrawing(username, this.drawingId, this.drawingKey, client);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Decline a request to join the current drawing
	// This method is used by the manager, since only he has the drawing key
	public void declineFromDrawing(RMIDrawingClient client) {
		try {
			server.declineFromDrawing(this.drawingKey, client);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// Kick a member from the current drawing
	// This method is used by the manager, since only he has the drawing key
	public void removeMember(String username) {
		try {
			server.removeMember(username, this.drawingId, this.drawingKey);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	// This method is used by the DrawingBoardServer object to notify the client
	public void notify(String tag, Object data, String src) throws RemoteException {
		drawingBoard.notify(tag, data, src);
	}
	
	// Get the list of members from the current drawing
	public Vector<String> getMembers() {
		try {
			return server.getMembers(this.drawingId);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
			return null;
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
			return null;
		}
	}
	
	// Quit the drawing
	public void quit() {
		try {
			server.quit(this.username, this.drawingId);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
}

