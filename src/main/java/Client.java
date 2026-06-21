import javax.jms.*;
import javax.naming.InitialContext;
import java.awt.GridLayout;
import java.awt.BorderLayout;

import javax.swing.*;
import javax.swing.event.*;
import java.rmi.Naming;

public class Client{
    private JFrame frame;
    private Service service;
    private String currUsername;

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    private JTextField registerUsernameField;
    private JPasswordField registerPasswordField;
    private JPasswordField confirmPasswordField;

    public Client(){
        try {
            service = (Service) Naming.lookup("rmi://localhost/Service");
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not connect to RMI: " + e);
            System.exit(1);
        }

        frame = new JFrame("24 Game");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(400, 250);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (currUsername != null) {
                    try {
                        service.logout(currUsername);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                frame.dispose();
                System.exit(0);
            }
        });

        showLoginWindow();

        frame.setVisible(true);
    }

    private void showLoginWindow(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(4,2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(); 

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        statusLabel = new JLabel("");

        loginButton.addActionListener(e -> handleLogin());
        registerButton.addActionListener(e -> showRegisterWindow());

        panel.add(usernameLabel);
        panel.add(usernameField);

        panel.add(passwordLabel);
        panel.add(passwordField);

        panel.add(loginButton);
        panel.add(registerButton);
        panel.add(statusLabel);
        panel.add(new JLabel(""));

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showErrorWindow(String error){
        JOptionPane.showMessageDialog(
                frame,
                error,
                "Error",
                JOptionPane.ERROR_MESSAGE
                );
    }

    private void showRegisterWindow(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(5, 2));

        JLabel usernameLabel = new JLabel("Username:");
        registerUsernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        registerPasswordField = new JPasswordField();

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordField = new JPasswordField();

        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> showLoginWindow());

        panel.add(usernameLabel);
        panel.add(registerUsernameField);

        panel.add(passwordLabel);
        panel.add(registerPasswordField);

        panel.add(confirmPasswordLabel);
        panel.add(confirmPasswordField);

        panel.add(registerButton);
        panel.add(cancelButton);

        panel.add(new JLabel(""));
        panel.add(new JLabel(""));

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showInfoWindow(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel navigation = new JPanel(new GridLayout(1, 4));

        JButton playerInfoButton = new JButton("Player Info");
        JButton leaderboardButton = new JButton("Leaderboard");
        JButton gameButton = new JButton("Play game");
        JButton logoutButton = new JButton("Logout");

        playerInfoButton.addActionListener(e -> showInfoWindow());
        leaderboardButton.addActionListener(e -> showLeaderboardWindow());
        gameButton.addActionListener(e -> showGameWindow());
        logoutButton.addActionListener(e -> handleLogout());

        navigation.add(playerInfoButton);
        navigation.add(leaderboardButton);
        navigation.add(gameButton);
        navigation.add(logoutButton);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(5, 1));

        String[] stats;

        try {
            stats = service.getPlayerStats(currUsername);
        } catch (Exception e) {
            e.printStackTrace();
            stats = new String[] { currUsername, "0", "0", "0.00" };
            showErrorWindow("Could not load player stats: " + e);
        }

        infoPanel.add(new JLabel("Player: " + stats[0]));
        infoPanel.add(new JLabel("Number of wins: " + stats[1]));
        infoPanel.add(new JLabel("Number of games: " + stats[2]));
        infoPanel.add(new JLabel("Average time to win: " + stats[3] + "s"));
        infoPanel.add(new JLabel(""));

        panel.add(navigation, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showGameWindow(){
        JPanel panel = createMainPanelWithNavigation();

        JPanel gamePanel = new JPanel(new BorderLayout());

        JButton newGameButton = new JButton("New game");

        newGameButton.addActionListener(e -> {
            try {
                sendJoinRequest();
                showWaitingRoomWindow();
            } catch (Exception ex) {
                ex.printStackTrace();
                showErrorWindow("Could not join game queue: " + ex);
            }
        });

        gamePanel.add(newGameButton, BorderLayout.CENTER);

        panel.add(gamePanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showWaitingRoomWindow(){
        JPanel panel = createMainPanelWithNavigation();

        JLabel waitingLabel = new JLabel("Waiting for players...", SwingConstants.CENTER);

        panel.add(waitingLabel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private JPanel createMainPanelWithNavigation() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel navigation = new JPanel(new GridLayout(1, 4));

        JButton playerInfoButton = new JButton("Player Info");
        JButton leaderboardButton = new JButton("Leaderboard");
        JButton gameButton = new JButton("Play game");
        JButton logoutButton = new JButton("Logout");

        playerInfoButton.addActionListener(e -> showInfoWindow());
        leaderboardButton.addActionListener(e -> showLeaderboardWindow());
        gameButton.addActionListener(e -> showGameWindow());
        logoutButton.addActionListener(e -> handleLogout());

        navigation.add(playerInfoButton);
        navigation.add(leaderboardButton);
        navigation.add(gameButton);
        navigation.add(logoutButton);

        panel.add(navigation, BorderLayout.NORTH);

        return panel;
    }

    private void sendJoinRequest() throws Exception {
        System.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
        System.setProperty("org.omg.CORBA.ORBInitialPort", "3700");

        javax.naming.InitialContext ctx = new javax.naming.InitialContext();

        javax.jms.ConnectionFactory factory =
            (javax.jms.ConnectionFactory) ctx.lookup("jms/JPoker24GameConnectionFactory");

        javax.jms.Queue queue =
            (javax.jms.Queue) ctx.lookup("jms/JPoker24GameQueue");

        javax.jms.Connection connection = null;
        javax.jms.Session session = null;
        javax.jms.MessageProducer producer = null;

        try {
            connection = factory.createConnection();
            session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(queue);

            javax.jms.TextMessage message =
                session.createTextMessage("JOIN|" + currUsername);

            producer.send(message);

            System.out.println("Sent join request: JOIN|" + currUsername);

        } finally {
            if (producer != null) producer.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
        }
    }

    

    private void showLeaderboardWindow(){
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel navigation = new JPanel(new GridLayout(1, 4));

        JButton playerInfoButton = new JButton("Player Info");
        JButton leaderboardButton = new JButton("Leaderboard");
        JButton gameButton = new JButton("Play game");
        JButton logoutButton = new JButton("Logout");

        playerInfoButton.addActionListener(e -> showInfoWindow());
        leaderboardButton.addActionListener(e -> showLeaderboardWindow());
        gameButton.addActionListener(e -> showGameWindow());
        logoutButton.addActionListener(e -> handleLogout());

        navigation.add(playerInfoButton);
        navigation.add(leaderboardButton);
        navigation.add(gameButton);
        navigation.add(logoutButton);

        String[] columns = {"Rank", "Player", "Games Won", "Games Played", "Avg Time"};
        String[][] data;

        try {
            data = service.getLeaderboard();
        } catch (Exception e) {
            e.printStackTrace();
            data = new String[0][0];
            showErrorWindow("Could not load leaderboard: " + e);
        }

        JTable table = new JTable(data, columns);
        JScrollPane scrollPane = new JScrollPane(table);

        panel.add(navigation, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void handleLogout(){
        if (currUsername == null) {
            showLoginWindow();
            return;
        }

        try{
            boolean success = service.logout(currUsername);

            if (success) {
                currUsername = null;
                showLoginWindow();
            } else{
                showErrorWindow("Logout failed. User or server may not be online.");
            }
        }catch (Exception e){
            e.printStackTrace();
            showErrorWindow("RMI error during logout: " + e);
        }
    }

    private void handleLogin(){
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            showErrorWindow("Username and password cannot be empty.");
            return;
        }

        try{
            boolean success = service.login(username, password);

            if (success) {
                currUsername = username;
                showInfoWindow();
            }else {
                showErrorWindow("Login failed. Username/password may be wrong, or user is not registered");
            }
        }catch (Exception e){
            e.printStackTrace();
            showErrorWindow("RMI error during login: " + e);
        }
    }

    private void handleRegister(){
        String username = registerUsernameField.getText().trim();
        String password = new String(registerPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showErrorWindow("All fields are required.");
            return;
        }

        if (!password.equals(confirmPassword)){
            showErrorWindow("Passwords do not match.");
            return;
        }

        try{
            boolean success = service.register(username, password);

            if (success){
                currUsername = username;
                showInfoWindow();
            }else{
                showErrorWindow("Registration failed. Username may already exist or invalid username/password.");
            }
        } catch (Exception e){
            e.printStackTrace();
            showErrorWindow("RMI error during registration: " + e);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Client());
    }
}
