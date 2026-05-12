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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 250);

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

        playerInfoButton.addActionListener(e->showInfoWindow());
        leaderboardButton.addActionListener(e-> showLeaderboardWindow());
        gameButton.addActionListener(e->showGameWindow());
        logoutButton.addActionListener(e -> handleLogout());


        navigation.add(playerInfoButton);
        navigation.add(leaderboardButton);
        navigation.add(gameButton);
        navigation.add(logoutButton);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(5, 1));

        infoPanel.add(new JLabel(currUsername));
        infoPanel.add(new JLabel("Number of wins: 67"));
        infoPanel.add(new JLabel("Number of games: 20"));
        infoPanel.add(new JLabel("Average time to win: 4.20s"));
        infoPanel.add(new JLabel("Rank: #21"));

        panel.add(navigation, BorderLayout.NORTH);
        panel.add(infoPanel, BorderLayout.CENTER);

        frame.setContentPane(panel);
        frame.revalidate();
        frame.repaint();
    }

    private void showGameWindow(){
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
        String[][] data = {
            {"1", "Player 4", "20", "35", "10.4s"},
            {"2", "Player 2", "18", "25", "13.2s"},
            {"3", "Player 6", "18", "31", "15.1s"},
            {"4", "Player 8", "16", "30", "12.8s"},
            {"5", currUsername, "10", "20", "12.5s"}
        };

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
