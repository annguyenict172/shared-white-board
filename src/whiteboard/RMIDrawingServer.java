package whiteboard;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;
import java.rmi.RemoteException;
import java.rmi.Remote;


public interface RMIDrawingServer extends Remote {
	public Hashtable<String, String> createDrawing(String username, RMIDrawingClient client) throws ServerError, RemoteException;
	public void joinDrawing(String username, String drawingId, RMIDrawingClient client) throws ServerError, RemoteException;
	public void removeMember(String username, String drawingId, String managerKey) throws ServerError, RemoteException;
	public boolean isManager(String username, String drawingId) throws ServerError, RemoteException;
	public Vector<String> getMembers(String drawingId) throws ServerError, RemoteException;
	public void send(String to, String from, String drawingId, String mtag, Object data) throws ServerError, RemoteException;
	public void broadcast(String from, String drawingId, String mtag, Object data) throws ServerError, RemoteException;
	public void addToDrawing(String username, String drawingId, String managerKey, RMIDrawingClient client) throws ServerError, RemoteException;
	public void declineFromDrawing(String managerKey, RMIDrawingClient client) throws ServerError, RemoteException;
	public void quit(String username, String drawingId) throws ServerError, RemoteException;
}
