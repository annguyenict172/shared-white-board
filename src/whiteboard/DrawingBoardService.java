package whiteboard;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.registry.*;
import java.util.Hashtable;
import java.util.UUID;


public class DrawingBoardService extends UnicastRemoteObject implements RMIDrawingClient {
	public String username = UUID.randomUUID().toString();
	public String drawingId;
	private String drawingKey;
	
	private RMIDrawingServer server;
	private DrawingBoard drawingBoard;
	
	public DrawingBoardService(String hostname, int port, String serverName, DrawingBoard drawingBoard) throws RemoteException, NotBoundException {
		super();
		this.drawingBoard = drawingBoard;
		Registry registry = LocateRegistry.getRegistry(hostname, port);
		server = (RMIDrawingServer) registry.lookup(serverName);
	}
	
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
	
	public void send(String tag, Object data, String dst) {
		try {
			server.send(dst, this.username, this.drawingId, tag, data);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	public void broadcast(String tag, Object data) {
		try {
			server.broadcast(this.username, this.drawingId, tag, data);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	public void addToDrawing(String username, RMIDrawingClient client) {
		try {
			server.addToDrawing(username, this.drawingId, this.drawingKey, client);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	public void declineFromDrawing(RMIDrawingClient client) {
		try {
			server.declineFromDrawing(this.drawingKey, client);
		} catch (RemoteException e) {
			drawingBoard.notifyError("Cannot connect to server");
		} catch (ServerError e) {
			drawingBoard.notifyError(e.getMessage());
		}
	}
	
	public void notify(String tag, Object data, String src) throws RemoteException {
		drawingBoard.notify(tag, data, src);
	}
}

