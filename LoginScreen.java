package com.CodeClause;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginScreen extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginScreen() {
        setTitle("Online Banking - Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(10, 10, 80, 25);
        panel.add(usernameLabel);

        usernameField = new JTextField(20);
        usernameField.setBounds(100, 10, 160, 25);
        panel.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 40, 80, 25);
        panel.add(passwordLabel);

        passwordField = new JPasswordField(20);
        passwordField.setBounds(100, 40, 160, 25);
        panel.add(passwordField);

        loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 250, 25);
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                authenticateUser(usernameField.getText(), new String(passwordField.getPassword()));
            }
        });
        panel.add(loginButton);

        add(panel);
    }

    // Method to authenticate the user
    private void authenticateUser(String username, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Establish connection to the database
            conn = DBConnection.getConnection();

            // Prepare the SQL query to authenticate the user
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);

            // Execute the query
            rs = stmt.executeQuery();

            // If the result set is not empty, the user is authenticated
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "Login successful!");
                // Proceed to the dashboard (not implemented here)
                new DashboardScreen().setVisible(true);
                dispose();  // Close the login screen
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error connecting to database.");
        } finally {
            // Close resources
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        LoginScreen loginScreen = new LoginScreen();
        loginScreen.setVisible(true);
    }
}


