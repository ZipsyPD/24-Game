import java.sql.*;

public class DemoDB{
    private static final String URL = "jdbc:mysql://localhost:3306/c3358";
    private static final String USER = "c3358";
    private static final String PASSWORD = "c3358PASS";

    private static Connection conn;

    static void insert(String name, String birthday){
        try {
            PreparedStatement stmt = 
                conn.prepareStatement(
                    "INSERT INTO c3358_2025 (name, birthday) VALUES (?, ?)"
                );

            stmt.setString(1, name);
            stmt.setDate(2, java.sql.Date.valueOf(birthday));
            stmt.execute();
        } catch (SQLException | IllegalArgumentException e) {
            System.err.println("Error inserting record: " +e);
        }
    }

    static void read(String name){
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT birthday FROM c3358_2025 WHERE name = ?");
            stmt.setString(1, name);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                System.out.println("Birthday of "+name+" is on "+rs.getDate(1).toString());
            } else {
                System.out.println(name+" not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error reading record: "+e);
        }
    }

    static void list(){
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT name, birthday FROM c3358_2025"
                    );
            while (rs.next()) {
                System.out.println("Birthday of "+rs.getString(1)+" is on "+rs.getDate(2).toString());
            }
        } catch (SQLException e) {
            System.err.println("Error listing records: "+e);
        }
    }

    static void update(String name, String birthday) {
        try {
            PreparedStatement stmt = 
                conn.prepareStatement(
                        "UPDATE c3358_2025 SET birthday = ? WHERE name = ?"
                        );
            stmt.setDate(1, java.sql.Date.valueOf(birthday));
            stmt.setString(2, name);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Birthday of "+name+" updated");
            } else {
                System.out.println(name+" not found!");
            }
        } catch (SQLException e) {
            System.err.println("Error reading record: "+e);
        }
    }

    static void delete(String name){
        try {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM c3358_2025 WHERE name = ?");
            stmt.setString(1, name);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Record of "+name+" removed");
            } else {
                System.out.println(name+" not found!");
            }
        } catch (SQLException | IllegalArgumentException e){
            System.err.println("Error inserting record: "+e);
        }
    }

    static void month(int month){
        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT name, birthday FROM c3358_2025 WHERE MONTH(birthday) = ?"
                    );
            
            stmt.setInt(1, month);

            ResultSet rs = stmt.executeQuery();

            boolean found = false;

            while (rs.next()){
                found = true;
                System.out.println(rs.getString("name") + " has birthday on " + rs.getDate("birthday"));
            }
            if (!found) {
                System.out.println("No birthdays in month " + month);
            }
        } catch (SQLException e) {
            System.err.println("Error finding records by month: " + e);
        }
    }

    public static void main(String[] args){
        try{
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            if (args.length == 0) {
                System.out.println("Usage: \n java DemoDB insert <name> <yyyy-mm-dd>");
                System.out.println("java DemoDB read <name> \n java DemoDB list \n java DemoDB update <name> <yyyy-mm-dd>");
                System.out.println("java DemoDB delete <name> \n java DemoDB month <1-12>");
                return;
            }

            String command = args[0];

            if (command.equals("insert") && args.length == 3) {
                insert(args[1], args[2]);
            } else if (command.equals("read") && args.length == 2) {
                read(args[1]);
            } else if (command.equals("list") && args.length == 1) {
                list();
            } else if (command.equals("update") && args.length == 3) {
                update(args[1], args[2]);
            } else if (command.equals("delete") && args.length == 2) {
                delete(args[1]);
            } else if (command.equals("month") && args.length == 2) {
                month(Integer.parseInt(args[1]));
            } else {
                System.out.println("Unknown command: " + command);
            }

            conn.close();
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e);
        }
    }
}
