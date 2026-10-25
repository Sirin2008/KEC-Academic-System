package com.kec.academic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/kec_academic?serverTimezone=Asia/Kolkata";
    private static final String USER = "root";
    // Change this to your own MySQL password.
    private static final String PASSWORD = "Sirin@2008";

    private DBConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
