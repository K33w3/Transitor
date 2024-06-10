package com.bcs05;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import com.bcs05.util.DatabaseConnection;

public class DatabaseConnectionTest {

    @Test
    public void testGetConnection() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            assertNotNull(connection, "Connection should not be null");
            System.out.println("Connection successful!");
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
            fail("Failed to establish a connection: " + e.getMessage());
        }
    }
}
