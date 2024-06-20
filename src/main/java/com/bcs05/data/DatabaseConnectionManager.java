package com.bcs05.data;

import com.bcs05.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages database connections.
 */
public class DatabaseConnectionManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionManager.class.getName());

    /**
     * Retrieves a connection to the database.
     * 
     * @return the database connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            return DatabaseConnection.getConnection();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get database connection", e);
            throw e;
        }
    }
}

