import java.rmi.*;
import java.rmi.server.*;
import java.sql.*;

public class Server extends UnicastRemoteObject implements Service{

    private static final String URL = "jdbc:mysql://localhost:3306/c3358";
    private static final String USER = "c3358";
    private static final String PASSWORD = "c3358PASS";

    public Server() throws RemoteException{
        super();
    }

// Helper Functions: (getConnection, clearOnlineUsers, invalidInput, (insert, update, delete, read) for both table
// ----------------------------------------------------------------------------------
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static void clearOnlineUsers(){
        String sql = "DELETE FROM OnlineUser";

        try (
                Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement()
            ) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.out.println("Error: " + e);
            }
    }

    private boolean invalidInput(String s){
        return s == null || s.isEmpty() || s.contains(" ");
    }
    
    private boolean insertUser(Connection conn, String username, String password) throws SQLException {
        String sql = "INSERT INTO UserInfo (username, password) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        }
    }
    
    private String readUserPassword(Connection conn, String username) throws SQLException {
        String sql = "SELECT password FROM UserInfo WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("password");
                }
                return null;
            }
        }
    }
   
    private boolean updateUserPassword(Connection conn, String username, String newPassword) throws SQLException {
        String sql = "UPDATE UserInfo SET password = ? WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean insertOnlineUser(Connection conn, String username) throws SQLException {
        String sql = "INSERT INTO OnlineUser (username) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }

    private boolean readOnlineUser(Connection conn, String username) throws SQLException {
        String sql = "SELECT username FROM OnlineUser WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean deleteOnlineUser(Connection conn, String username) throws SQLException {
        String sql = "DELETE FROM OnlineUser WHERE username = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        }
    }

// Callable Functions
// ----------------------------------------------------------------------------------

    public boolean login(String username, String password) throws RemoteException {
        if (invalidInput(username) || invalidInput(password)) {
            return false;
        }

        try (Connection conn = getConnection()) {
            if (readOnlineUser(conn, username)) {
                return false;
            }

            String storedPassword = readUserPassword(conn, username);

            if (storedPassword == null) {
                return false;
            }

            if (!password.equals(storedPassword)) {
                return false;
            }

            return insertOnlineUser(conn, username);

        } catch (SQLException e) {
            System.out.println("Error during login: " + e);
            return false;
        }
    }

    public boolean register(String username, String password) throws RemoteException {
        if (invalidInput(username) || invalidInput(password)) {
            return false;
        }

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                boolean userInserted = insertUser(conn, username, password);
                boolean onlineInserted = insertOnlineUser(conn, username);

                if (userInserted && onlineInserted) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }

            } catch (SQLException e) {
                conn.rollback();
                System.out.println("Error during registration: " + e);
                return false;

            } finally {
                conn.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Error during registration: " + e);
            return false;
        }
    }

    public boolean logout(String username) throws RemoteException {
        if (invalidInput(username)) {
            return false;
        }

        try (Connection conn = getConnection()) {
            return deleteOnlineUser(conn, username);

        } catch (SQLException e) {
            System.out.println("Error during logout: " + e);
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
