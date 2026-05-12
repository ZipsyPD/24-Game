import java.util.ArrayList;
import java.rmi.*;
import java.rmi.server.*;
import java.io.*;

public class Server extends UnicastRemoteObject implements Service{

    private static final String USER_INFO_FILE = "UserInfo.txt";
    private static final String ONLINE_USER_FILE = "OnlineUser.txt";

    public Server() throws RemoteException{
        super();
    }

// Helper Functions: (addOnlineUser, isOnline, isValidInput, clearOnlineUsers)
// ----------------------------------------------------------------------------------
    private static void clearOnlineUsers(){
        try (PrintWriter write = new PrintWriter(new FileWriter(ONLINE_USER_FILE, false))){
        
        }catch (Exception e){
            System.out.println("Error: " + e);
        }
    }

    private boolean addOnlineUser(String username) throws RemoteException{
        // Writing into OnlineUser
        try(PrintWriter write = new PrintWriter(new FileWriter(ONLINE_USER_FILE, true))){
            write.println(username);
            return true;
        }catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }
    }

    private boolean isOnline(String username) throws RemoteException{
        //Check if user is in the OnlineUser file
        try (BufferedReader reader = new BufferedReader(new FileReader(ONLINE_USER_FILE))){
            String line;

            while ((line = reader.readLine()) != null){
                if (username.equals(line)){
                    return true;
                }

            }
        }catch (Exception e){
            System.out.println("Error: " + e);
        }
        return false;
    }

    private boolean invalidInput(String s){
        return s == null || s.isEmpty() || s.contains(" ");
    }
    
// Callable Functions
// ----------------------------------------------------------------------------------

    public boolean login(String username, String password) throws RemoteException{
        // Checking if whitespace in inputs
        if (invalidInput(username) || invalidInput(password)){
            return false;
        }
        // Check if online already
        if (isOnline(username)){
            return false;
        }
        // Reading in the userinfo file to check if user exists
        try(BufferedReader reader = new BufferedReader(new FileReader(USER_INFO_FILE))){
            String line;

            while ((line = reader.readLine()) != null){
                String[] userPass = line.split("\\s+");
                if (userPass.length == 2 && 
                    username.equals(userPass[0]) && 
                    password.equals(userPass[1])){
                    return addOnlineUser(username);
                }
            }
            return false;
        } catch (Exception e){
            System.out.println("Error:" + e);
            return false;
        }
    }

    public boolean register(String username, String password) throws RemoteException{
        // Checking if whitespace exists
        if (invalidInput(username) || invalidInput(password)){
            return false;
        }
        // Check if user already exists in userinfo
        try(BufferedReader reader = new BufferedReader(new FileReader(USER_INFO_FILE))){
            String line;

            while ((line = reader.readLine()) != null){
                String[] userPass = line.split("\\s+");
                if (userPass.length == 2 && username.equals(userPass[0])){
                    return false;
                }
            }
        }catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }

        // Writing into userinfo
        try(PrintWriter write = new PrintWriter(new FileWriter(USER_INFO_FILE, true))){
            write.println(username + " " + password);
            return addOnlineUser(username);
        }catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }
    }

    public boolean logout(String username) throws RemoteException{
        if (!isOnline(username)){
            return false;
        }
        boolean found = false;
        ArrayList<String> remainingUsers = new ArrayList<>();
        try(BufferedReader reader = new BufferedReader(new FileReader(ONLINE_USER_FILE))){
            String line;

            while ((line = reader.readLine()) != null){
                if (username.equals(line)){
                    found = true;
                }else{
                    remainingUsers.add(line);
                }
            }
        }catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }

        if (!found){
            return false;
        }

        try(PrintWriter write = new PrintWriter(new FileWriter(ONLINE_USER_FILE, false))){
            for (String s: remainingUsers){
                write.println(s);
            }
            return true;
        }catch (Exception e){
            System.out.println("Error: " + e);
            return false;
        }
    }

    public static void main(String[] args) {
        try {
            clearOnlineUsers();
            Server app = new Server();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("Service", app);
            System.out.println("Service registered");

        } catch(Exception e){
            System.err.println("Exception thrown: "+e);
        }
    }
}
