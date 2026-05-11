import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


import java.rmi.Naming;

public class Client{
    public static void main(String[] args) {
        try {
            Service service = (Service) Naming.lookup("rmi://localhost/Service");

            System.out.println("Connected to RMI service!");

            System.out.println("Register: " + service.register("bob", "123"));
            System.out.println("Logout: " + service.logout("bob"));
            System.out.println("Login: " + service.login("bob", "123"));
            System.out.println("Logout: " + service.logout("bob"));

        } catch (Exception e) {
            System.out.println("Client error: " + e);
            e.printStackTrace();
        }
    }
}
