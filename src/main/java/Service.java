import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Service extends Remote {
    boolean login(String username, String password) throws RemoteException;
    boolean register(String username, String password) throws RemoteException;
    boolean logout(String username) throws RemoteException;
}
