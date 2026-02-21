package fr.am.sae302.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {


    private static final String URL =
        "jdbc:mysql://localhost:3306/sae302"
        + "?useSSL=false"
        + "&serverTimezone=UTC"
        + "&allowPublicKeyRetrieval=true";


    private static final String USER = "YOUR_DB_USER";
    private static final String PASSWORD = "YOUR_DB_PASSWORD";


    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
