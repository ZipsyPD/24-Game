import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.rmi.*;
import java.rmi.server.*;
import java.sql.*;
import javax.naming.InitialContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.jms.ConnectionFactory;
import javax.jms.Session;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.Queue;
import javax.jms.JMSException;

public class Server extends UnicastRemoteObject implements Service{

    // These are here to configure docker containers to run 
    private static final String DB_HOST = 
        System.getenv().getOrDefault("DB_HOST", "localhost");

    private static final String DB_NAME = 
        System.getenv().getOrDefault("DB_NAME", "game");

    private static final String DB_USER = 
        System.getenv().getOrDefault("DB_USER", "gameuser");

    private static final String DB_PASSWORD = 
        System.getenv().getOrDefault("DB_PASSWORD", "gamepass");

    private static final String GLASSFISH_HOST = 
        System.getenv().getOrDefault("GLASSFISH_HOST", "localhost");

    private static final String GLASSFISH_PORT = 
        System.getenv().getOrDefault("GLASSFISH_PORT", "3700");

    private static final String URL = 
        "jdbc:mysql://" + DB_HOST + ":3306/" + DB_NAME;

    public Server() throws RemoteException{
        super(1100);
    }

// Helper Functions: (getConnection, clearOnlineUsers, invalidInput, (insert, update, delete, read) for both tables, insertPlayerStats)
// ----------------------------------------------------------------------------------
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
    }

    private static void clearOnlineUsers(){
        String sql = "DELETE FROM OnlineUser";

        try (
                Connection conn = DriverManager.getConnection(URL, DB_USER, DB_PASSWORD);
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

    private boolean insertPlayerStats(Connection conn, String username) throws SQLException {
        String sql = "INSERT INTO PlayerStats (username) VALUES (?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
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
                boolean statsInserted = insertPlayerStats(conn, username);

                if (userInserted && onlineInserted && statsInserted) {
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

    public String[] getPlayerStats(String username) throws RemoteException {
        if (invalidInput(username)) {
            return new String[] { username, "0", "0", "0.00" };
        }

        String sql =
            "SELECT username, games_won, games_played, " +
            "CASE WHEN games_won = 0 THEN 0 ELSE total_winning_time / games_won END AS avg_time " +
            "FROM PlayerStats WHERE username = ?";

        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)
        ) {
            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("username"),
                        String.valueOf(rs.getInt("games_won")),
                        String.valueOf(rs.getInt("games_played")),
                        String.format("%.2f", rs.getDouble("avg_time"))
                    };
                }
            }

        } catch (SQLException e) {
            System.out.println("Error getting player stats: " + e);
        }

        return new String[] { username, "0", "0", "0.00" };
    }

    public String[][] getLeaderboard() throws RemoteException {
        String sql =
            "SELECT username, games_won, games_played, " +
            "CASE WHEN games_won = 0 THEN 0 ELSE total_winning_time / games_won END AS avg_time " +
            "FROM PlayerStats " +
            "ORDER BY games_won DESC, avg_time ASC";

        try (
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()
        ) {
            java.util.ArrayList<String[]> rows = new java.util.ArrayList<>();
            int rank = 1;

            while (rs.next()) {
                rows.add(new String[] {
                    String.valueOf(rank++),
                    rs.getString("username"),
                    String.valueOf(rs.getInt("games_won")),
                    String.valueOf(rs.getInt("games_played")),
                    String.format("%.2f", rs.getDouble("avg_time")) + "s"
                });
            }

            return rows.toArray(new String[0][]);

        } catch (SQLException e) {
            System.out.println("Error getting leaderboard: " + e);
        }

        return new String[0][0];
    }

    public static void main(String[] args) {
        try {
            System.setProperty(
                    "org.omg.CORBA.ORBInitialHost",
                    GLASSFISH_HOST 
                    );

            System.setProperty(
                    "org.omg.CORBA.ORBInitialPort", 
                    GLASSFISH_PORT 
                    );

            clearOnlineUsers();
            Server app = new Server();
            System.setSecurityManager(new SecurityManager());
            Naming.rebind("Service", app);

            //JMS stuff
            app.connectToGameTopic();
            app.startMatchmakingListener();

            System.out.println("JMS listeners started");
            System.out.println("Service registered");

        } catch(Exception e){
            System.err.println("Exception thrown: "+e);
        }
    }

// JMS functions and the Queue
// ----------------------------------------------------------------------------------

    private final List<String> waitingPlayers = new ArrayList<>();
    // queue for matching players into a game
    private javax.jms.Connection matchmakingConnection;
    private Session matchmakingSession;
    private MessageConsumer matchmakingConsumer;

    private javax.jms.Connection topicConnection;
    private Session topicSession;
    private MessageProducer topicProducer;
    private MessageConsumer topicConsumer;

    private Topic gameTopic;

    private int nextGameId = 1;

    //Here is the Gamestate. We need this to update leaderboards and stuff
    private static class GameState {
        int gameId;
        List<String> players;
        int[] numbers;
        long startTime;
        boolean finished;

        GameState(
                int gameId,
                List<String> players,
                int[] numbers
                ){
            this.gameId = gameId;
            this.players = players;
            this.numbers = numbers;
            this.startTime = System.currentTimeMillis();
            this.finished = false;
                }
    }

    private final Map<Integer, GameState> games = new HashMap<>();

    private static final long GAME_TIMEOUT_SECONDS = 120;

    private final ScheduledExecutorService gameTimeoutExecutor = 
        Executors.newSingleThreadScheduledExecutor();

    //Listen to the queue. This will be called whenever a message arrives
    private void startMatchmakingListener() throws Exception {
        System.setProperty("org.omg.CORBA.ORBInitialHost", GLASSFISH_HOST);
        System.setProperty("org.omg.CORBA.ORBInitialPort", GLASSFISH_PORT);

        InitialContext ctx = new InitialContext();

        ConnectionFactory factory = 
            (ConnectionFactory) ctx.lookup(
                    "jms/JPoker24GameConnectionFactory"
                    );
        Queue queue = 
            (Queue) ctx.lookup(
                    "jms/JPoker24GameQueue"
                    );

        matchmakingConnection = factory.createConnection();

        matchmakingSession = 
            matchmakingConnection.createSession(
                    false,
                    Session.AUTO_ACKNOWLEDGE
                    );

        matchmakingConsumer = 
            matchmakingSession.createConsumer(queue);

        matchmakingConsumer.setMessageListener(message -> {
            try {
                System.out.println("SERVER CONSUMED MESSAGE");
                if (message instanceof TextMessage) {
                    String text = 
                        ((TextMessage) message).getText();

                    handleMatchmakingMessage(text);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        });

        matchmakingConnection.start();
    }

    //Processing join messages. We do messages like "JOIN|Alice"
    private synchronized void handleMatchmakingMessage(String text) throws Exception{
        String[] parts = text.split("\\|");

        if (parts.length < 2) {
            return;
        }

        if (!parts[0].equals("JOIN")){
            return;
        }

        String username = parts[1];

        if (waitingPlayers.contains(username)) {
            return;
        }

        waitingPlayers.add(username);
        System.out.println(
                username + " joined matchmaking"
                );

        System.out.println(
                "Players waiting: " + waitingPlayers.size()
                );

        if (waitingPlayers.size() >= 4) {
            createGame();
        }
    }

    // Connect the gameplayy to the topic
    private void connectToGameTopic() throws Exception {
        InitialContext ctx = new InitialContext();

        ConnectionFactory factory = 
            (ConnectionFactory) ctx.lookup(
                    "jms/JPoker24GameConnectionFactory"
                    );

        gameTopic = 
            (Topic) ctx.lookup(
                    "jms/JPoker24GameTopic"
                    );

        topicConnection = factory.createConnection();

        topicSession = 
            topicConnection.createSession(
                    false,
                    Session.AUTO_ACKNOWLEDGE
                    );

        topicProducer = 
            topicSession.createProducer(gameTopic);

        topicConsumer = 
            topicSession.createConsumer(gameTopic);

        topicConsumer.setMessageListener(message -> {
            try{
                if (message instanceof TextMessage) {
                    handleGameplayMessage(
                            ((TextMessage) message).getText()
                            );
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        });

        topicConnection.start();
    }
    // Server can publish to topic and subscribe to the topic
    
    // Here is the method for publishing a gameMessage
    private void publishGameMessage(String text) throws JMSException {

        TextMessage message = topicSession.createTextMessage(text);

        topicProducer.send(message);
        System.out.println("Published: " + text);
    }

    //Here we can create the actual match 
    private synchronized void createGame() throws Exception {

        List<String> players = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            players.add(waitingPlayers.remove(0));
        }

        int gameId = nextGameId++;

        int[] numbers = generate24GameNumbers();

        GameState game = new GameState(gameId, players, numbers);

        games.put(gameId, game);

        String message = 
            "GAME_START|" +
            gameId + "|" +
            String.join(",", players) + "|" +
            numbers[0] + "," +
            numbers[1] + "," +
            numbers[2] + "," +
            numbers[3];

        publishGameMessage(message);

        gameTimeoutExecutor.schedule(
            () -> timeoutGame(gameId),
            GAME_TIMEOUT_SECONDS,
            TimeUnit.SECONDS
        );
    }

    private synchronized void timeoutGame(int gameId) {
        GameState game = games.get(gameId);

        if (game == null || game.finished) {
            return;
        }

        game.finished = true;
        games.remove(gameId);

        try {
            publishGameMessage("GAME_TIMEOUT|" + gameId);
        } catch (JMSException e) {
            e.printStackTrace();
        }

        System.out.println("Game " + gameId + " timed out");
    }

    //handle an answer by parsing
    private synchronized void handleGameplayMessage(String text) throws Exception{

        String[] parts = text.split("\\|", 4);

        if (parts.length < 4) {
            return;
        }

        if (parts[0].equals("ANSWER")) {

            int gameId = Integer.parseInt(parts[1]);

            String username = parts[2];

            String answer = parts[3];

            System.out.println(
                    username + 
                    " answered " + 
                    answer +
                    " in game " +
                    gameId
                    );
            
            GameState game = games.get(gameId);

            if (game == null) {
                return;
            }

            if (game.finished) {
                return;
            }

            if (!game.players.contains(username)) {
                return;
            }

            boolean correct = checkPlayerAnswer(answer, game.numbers);

            if (correct) {
                double winningTime = (System.currentTimeMillis() - game.startTime) / 1000.0;
                updateGameStats(game, username, winningTime);

                game.finished = true;
                games.remove(gameId);

                publishGameMessage(
                        "GAME_OVER|" +
                        gameId + "|" + 
                        username
                        );
            }
        }
    }

    private boolean checkPlayerAnswer(String answer, int[] numbers){
        try {
            if (!usesCorrectNumbers(answer, numbers)) {
                return false;
            }

            double result = ExpressionEvaluator.evaluate(answer);

            return Math.abs(result - 24.0) < 0.00001;
        }catch (Exception e) {
            return false;
        }
    }

    // Here we finally check the game stats and update
    private void updateGameStats(GameState game, String winner, double winningTime) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try {
                String playerSql = 
                    "UPDATE PlayerStats " +
                    "SET games_played = games_played + 1 " +
                    "WHERE username = ?";

                try (PreparedStatement stmt = 
                        conn.prepareStatement(playerSql)) {

                    for (String player : game.players) {
                        stmt.setString(1, player);
                        stmt.addBatch();
                    }

                    stmt.executeBatch();
                        }

                String winnerSql = 
                    "UPDATE PlayerStats " +
                    "SET games_won = games_won + 1, " +
                    "total_winning_time = total_winning_time + ? " +
                    "WHERE username = ?";

                try (PreparedStatement stmt = 
                        conn.prepareStatement(winnerSql)) {
                    stmt.setDouble(1, winningTime);
                    stmt.setString(2, winner);

                    stmt.executeUpdate();
                        }

                conn.commit();

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
// Here we make the 24-game actual algorithm.
// ---------------------------------------------------------------------------------
    private int[] generate24GameNumbers() {
        java.util.Random random = new java.util.Random();

        while(true) {
            int[] numbers = new int[4];

            for (int i = 0; i < 4; i++) {
                numbers[i] = random.nextInt(9) + 1;
            }

            if (check24Numbers(numbers)) {
                return numbers;
            }
        }
    }
    
    // The purpose of this is that it will recursively shrink the arraylist
    private boolean check24Numbers(int[] numbers) {
        if (numbers.length != 4){
            return false;
        }

        List<Double> values = new ArrayList<>();

        for (int number : numbers) {
            values.add((double) number);
        }

        return canMake24(values);
    }

    // The recursive call on the list to shrink to one number
    private boolean canMake24(List<Double> values) {
        // Forgot to account for FPE
        if (values.size() == 1){
            return Math.abs(values.get(0) - 24.0) < 0.000001;
        }
         
        for (int i = 0; i < values.size(); i++) {
            for (int j = 0; j < values.size(); j++) {

                if (i == j) {
                    continue;
                }
                double a = values.get(i);
                double b = values.get(j);

                List<Double> remaining = new ArrayList<>();

                for (int k = 0; k < values.size(); k++) {
                    if (k != i && k != j) {
                        remaining.add(values.get(k));
                    }
                }

                List<Double> results = new ArrayList<>();

                results.add(a+b);
                results.add(a-b);
                results.add(a *b);

                if (Math.abs(b) > 0.000001) {
                    results.add(a /b);
                }

                for (double result : results) {
                    remaining.add(result);

                    if (canMake24(remaining)){
                        return true;
                    }

                    remaining.remove(remaining.size() - 1);
                }
            }
        }
        return false;
    }
    //Check if a given answer is using the right numbers
    private boolean usesCorrectNumbers(String answer, int[] numbers) {
        List<Integer> usedNumbers = new ArrayList<>();

        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < answer.length(); i++) {
            char c = answer.charAt(i);
            if (Character.isDigit(c)) {
                currentNumber.append(c);
            } else {
                if (currentNumber.length() > 0) {
                    usedNumbers.add(
                            Integer.parseInt(currentNumber.toString())
                            );
                    currentNumber.setLength(0);
                }
            }
        }
        if (currentNumber.length() > 0) {
            usedNumbers.add(
                    Integer.parseInt(currentNumber.toString())
                    );
        }

        List<Integer> requiredNumbers = new ArrayList<>();

        for (int n: numbers) {
            requiredNumbers.add(n);
        }

        java.util.Collections.sort(usedNumbers);
        java.util.Collections.sort(requiredNumbers);

        return usedNumbers.equals(requiredNumbers);
    }
    // Putting the evaluate on a separate java file would be great for this
}
