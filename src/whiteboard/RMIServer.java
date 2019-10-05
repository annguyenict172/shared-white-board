package whiteboard;

import java.io.IOException;
import java.util.Vector;
import java.rmi.RemoteException;
import java.rmi.Remote;


public interface RMIServer extends Remote {
	public boolean register(String username, RMIClient client) throws IOException, RemoteException;
	public boolean checkUsernameAvailability(String username) throws RemoteException;
	public boolean removeMember(String username) throws IOException, RemoteException;
	public Vector<String> getMembers() throws RemoteException;
	public boolean send(String to, String from, String mtag, Object data) throws IOException, RemoteException;
	public boolean broadcast(String from, String mtag, Object data) throws IOException, RemoteException;
}
