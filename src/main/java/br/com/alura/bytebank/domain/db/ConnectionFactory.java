package br.com.alura.bytebank.domain.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

    public Connection getConnection() {
        try {
            return DriverManager
                    .getConnection("jdbc:sqlserver://localhost:1433;database=byte_bank;trustServerCertificate=true", "user", "banco123");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection closeConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }
}
