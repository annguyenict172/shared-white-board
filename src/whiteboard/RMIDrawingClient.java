package whiteboard;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface RMIDrawingClient extends Remote {
	public void notify(String tag, Object data, String src) throws RemoteException;
}
