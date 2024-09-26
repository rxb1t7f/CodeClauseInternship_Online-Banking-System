package com.CodeClause;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Singleton connection object
    private static Connection connection;

    public static Connection getConnection() {
        if (connection == null) {
            try {
                String url = "jdbc:mysql://localhost:3306/banking_system"; // Ensure database exists
                String username = "root"; // Replace with your actual MySQL username
                String password = "7386625535"; // Replace with your actual MySQL password

                // Establish connection using DriverManager
                connection = DriverManager.getConnection(url, username, password);
                System.out.println("Database connected successfully!");

            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println("Failed to connect to the database.");
            }
        }
        return connection;
    }
}
