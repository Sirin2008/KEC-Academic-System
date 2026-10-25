package com.kec.academic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:mysql://localhost:3306/kec_academic?serverTimezone=Asia/Kolkata";

    private static final String USER = "root";

    // Use your actual MySQL password locally.
    private static final String PASSWORD = "Sirin@2008";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}