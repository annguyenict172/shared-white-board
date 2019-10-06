package whiteboard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface RMIClient extends Remote {
	public String getUsername() throws RemoteException;
	
	public boolean connect(String host, int port, String serverName) throws IOException, RemoteException;
	
	public boolean send(String tag, Object data, String dst) throws IOException, RemoteException;
	public boolean broadcast(String tag, Object data) throws IOException, RemoteException;
	public boolean notify(String tag, Object data, String src) throws IOException, RemoteException;
}
