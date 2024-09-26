package com.CodeClause;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BankingService {

    // Method to fetch user balance
    public double getBalance(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double balance = 0.0;

        try {
            conn = DBConnection.getConnection();
            String query = "SELECT balance FROM users WHERE user_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            if (rs.next()) {
                balance = rs.getDouble("balance");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return balance;
    }

    // Method to transfer funds between accounts
    public boolean transferFunds(int senderId, String recipientAccount, double amount) {
        Connection conn = null;
        PreparedStatement senderStmt = null;
        PreparedStatement recipientStmt = null;
        PreparedStatement updateSenderBalance = null;
        PreparedStatement updateRecipientBalance = null;
        ResultSet rs = null;
        boolean success = false;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // Check sender's balance
            String querySender = "SELECT balance FROM users WHERE user_id = ?";
            senderStmt = conn.prepareStatement(querySender);
            senderStmt.setInt(1, senderId);
            rs = senderStmt.executeQuery();

            if (rs.next()) {
                double senderBalance = rs.getDouble("balance");

                // Check if sender has enough funds
                if (senderBalance >= amount) {
                    // Fetch recipient's user_id using account number
                    String queryRecipient = "SELECT user_id, balance FROM users WHERE account_number = ?";
                    recipientStmt = conn.prepareStatement(queryRecipient);
                    recipientStmt.setString(1, recipientAccount);
                    rs = recipientStmt.executeQuery();

                    if (rs.next()) {
                        int recipientId = rs.getInt("user_id");
                        double recipientBalance = rs.getDouble("balance");

                        // Deduct amount from sender's balance
                        String updateSender = "UPDATE users SET balance = balance - ? WHERE user_id = ?";
                        updateSenderBalance = conn.prepareStatement(updateSender);
                        updateSenderBalance.setDouble(1, amount);
                        updateSenderBalance.setInt(2, senderId);
                        updateSenderBalance.executeUpdate();

                        // Add amount to recipient's balance
                        String updateRecipient = "UPDATE users SET balance = balance + ? WHERE user_id = ?";
                        updateRecipientBalance = conn.prepareStatement(updateRecipient);
                        updateRecipientBalance.setDouble(1, amount);
                        updateRecipientBalance.setInt(2, recipientId);
                        updateRecipientBalance.executeUpdate();

                        // Record both transactions
                        recordTransaction(conn, senderId, "Debit", amount);
                        recordTransaction(conn, recipientId, "Credit", amount);

                        conn.commit(); // Commit transaction
                        success = true;
                    } else {
                        System.out.println("Error: Recipient account not found.");
                    }
                } else {
                    System.out.println("Error: Insufficient funds.");
                }
            }
        } catch (SQLException e) {
            try {
                if (conn != null) conn.rollback(); // Rollback transaction on error
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            closeResources(rs, senderStmt, conn);
            closeResources(null, recipientStmt, null);
            closeResources(null, updateSenderBalance, null);
            closeResources(null, updateRecipientBalance, null);
        }

        return success;
    }

    // Method to fetch transaction history for a user
    public List<Transaction> getTransactionHistory(int userId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<Transaction> transactions = new ArrayList<>();

        try {
            conn = DBConnection.getConnection();
            String query = "SELECT * FROM transactions WHERE user_id = ? ORDER BY transaction_date DESC";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(rs.getInt("transaction_id"));
                transaction.setUserId(rs.getInt("user_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getDouble("amount"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }

        return transactions;
    }

    // Method to record a transaction in the database
    private void recordTransaction(Connection conn, int userId, String transactionType, double amount) throws SQLException {
        String insertTransaction = "INSERT INTO transactions (user_id, transaction_type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertTransaction)) {
            stmt.setInt(1, userId);
            stmt.setString(2, transactionType);
            stmt.setDouble(3, amount);
            stmt.executeUpdate();
        }
    }

    // Helper method to close resources
    private void closeResources(ResultSet rs, PreparedStatement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}


