package com.CodeClause;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardScreen extends JFrame {

    private JPanel mainPanel;
    private JPanel accountPanel;
    private JPanel transferPanel;
    private JPanel historyPanel;
    
    private JLabel balanceLabel;
    private JTable transactionTable;
    private JTextField transferAmountField, transferToAccountField;

    private JPanel currentPanel; // To keep track of the current panel

    public DashboardScreen() {
        // Set up the frame
        setTitle("Online Banking Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main Panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        // Create the navigation menu
        createMenu();

        // Create account panel (default view)
        createAccountPanel();

        // Create transfer panel
        createTransferPanel();

        // Create history panel
        createHistoryPanel();

        // Add default account panel to center
        mainPanel.add(accountPanel, BorderLayout.CENTER);
        currentPanel = accountPanel;

        // Add main panel to frame
        add(mainPanel);

        // Make the frame visible
        setVisible(true);
    }

    // Method to create the menu on the left side
    private void createMenu() {
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(3, 1));

        JButton accountButton = new JButton("Account Details");
        JButton transferButton = new JButton("Transfer Funds");
        JButton historyButton = new JButton("Transaction History");

        // Add listeners to switch between panels
        accountButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(accountPanel);
            }
        });

        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(transferPanel);
            }
        });

        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchPanel(historyPanel);
            }
        });

        // Add buttons to the menu panel
        menuPanel.add(accountButton);
        menuPanel.add(transferButton);
        menuPanel.add(historyButton);

        // Add the menu panel to the left side of the main panel
        mainPanel.add(menuPanel, BorderLayout.WEST);
    }

    // Method to create the account details panel
    private void createAccountPanel() {
        accountPanel = new JPanel();
        accountPanel.setLayout(new BorderLayout());

        JLabel accountInfoLabel = new JLabel("Account Information", JLabel.CENTER);
        accountInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        balanceLabel = new JLabel("", JLabel.CENTER);
        balanceLabel.setFont(new Font("Arial", Font.PLAIN, 14));

        JPanel accountDetailsPanel = new JPanel();
        accountDetailsPanel.setLayout(new GridLayout(2, 1));
        accountDetailsPanel.add(accountInfoLabel);
        accountDetailsPanel.add(balanceLabel);

        accountPanel.add(accountDetailsPanel, BorderLayout.CENTER);
        updateAccountDetails(); // Update the panel with real data
    }

    // Method to create the transfer funds panel
    private void createTransferPanel() {
        transferPanel = new JPanel();
        transferPanel.setLayout(new GridLayout(4, 2, 10, 10));

        JLabel transferToLabel = new JLabel("Transfer To (Account #): ");
        JLabel amountLabel = new JLabel("Amount (₹): ");
        transferToAccountField = new JTextField();
        transferAmountField = new JTextField();

        JButton transferButton = new JButton("Transfer");
        transferButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performTransfer();
            }
        });

        transferPanel.add(transferToLabel);
        transferPanel.add(transferToAccountField);
        transferPanel.add(amountLabel);
        transferPanel.add(transferAmountField);
        transferPanel.add(new JLabel()); // Empty cell for alignment
        transferPanel.add(transferButton);
    }

    // Method to create the transaction history panel
    private void createHistoryPanel() {
        historyPanel = new JPanel(new BorderLayout());

        JLabel historyLabel = new JLabel("Transaction History", JLabel.CENTER);
        historyLabel.setFont(new Font("Arial", Font.BOLD, 16));

        String[] columnNames = {"Date", "Description", "Amount (₹)"};
        Object[][] data = {}; // Initially empty; populate with real data later

        transactionTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(transactionTable);

        historyPanel.add(historyLabel, BorderLayout.NORTH);
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        updateTransactionHistory(); // Update the panel with real data
    }

    // Method to switch between panels
    private void switchPanel(JPanel newPanel) {
        if (currentPanel != newPanel) {
            mainPanel.remove(currentPanel);
            mainPanel.add(newPanel, BorderLayout.CENTER);
            currentPanel = newPanel;
            mainPanel.revalidate();
            mainPanel.repaint();
        }
    }

    // Method to perform fund transfer
    private void performTransfer() {
        String toAccount = transferToAccountField.getText();
        String amount = transferAmountField.getText();

        if (toAccount.isEmpty() || amount.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both account number and amount.");
            return;
        }

        try {
            double transferAmount = Double.parseDouble(amount);
            // Add logic to perform the transfer and update the database

            JOptionPane.showMessageDialog(this, "Transferred ₹" + transferAmount + " to Account #" + toAccount);
            transferToAccountField.setText("");
            transferAmountField.setText("");
            updateAccountDetails(); // Update balance after transfer
            updateTransactionHistory(); // Update history after transfer

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format. Please enter a valid number.");
        }
    }

    // Method to update account details with real data
    private void updateAccountDetails() {
        Connection conn = DBConnection.getConnection();
        try {
            String query = "SELECT balance FROM accounts WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "your_account_number"); // Use the actual account number
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                balanceLabel.setText("Balance: ₹" + String.format("%.2f", balance));
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Method to update transaction history with real data
    private void updateTransactionHistory() {
        Connection conn = DBConnection.getConnection();
        try {
            String query = "SELECT date, description, amount FROM transactions WHERE account_number = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, "your_account_number"); // Use the actual account number
            ResultSet rs = stmt.executeQuery();
            
            // Use a DefaultTableModel to update the table data
            DefaultTableModel model = (DefaultTableModel) transactionTable.getModel();
            model.setRowCount(0); // Clear existing data
            
            while (rs.next()) {
                String date = rs.getString("date");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                model.addRow(new Object[]{date, description, "₹" + String.format("%.2f", amount)});
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new DashboardScreen();
    }
}




