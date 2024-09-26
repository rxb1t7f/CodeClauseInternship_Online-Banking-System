package com.CodeClause;

import javax.swing.*;
import java.util.List;

public class TransactionHistoryUI extends JFrame {
    public TransactionHistoryUI(List<Transaction> transactions) {
        setTitle("Transaction History");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String[] columnNames = {"ID", "Type", "Amount", "Date"};
        Object[][] data = new Object[transactions.size()][4];

        for (int i = 0; i < transactions.size(); i++) {
            data[i][0] = transactions.get(i).getTransactionId();
            data[i][1] = transactions.get(i).getTransactionType();
            data[i][2] = transactions.get(i).getAmount();
            data[i][3] = transactions.get(i).getTransactionDate();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane);
    }
}

