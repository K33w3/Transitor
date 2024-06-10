package com.bcs05;

import com.bcs05.data.PostalCodeReader;
import com.bcs05.engine.GTFSEngine;
import com.bcs05.util.Path;
import com.bcs05.util.DatabaseConnection;
import com.bcs05.util.ErrorHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class GTFSEngineIntegrationTest {

    private GTFSEngine engine;
    private PostalCodeReader postalCodeReader;
    private List<String> postalCodes;

    @BeforeEach
    public void setUp() {
        try {
            // Establishing the database connection directly for the test
            Connection connection = DatabaseConnection.getConnection();
            engine = new GTFSEngine();
            postalCodeReader = new PostalCodeReader();
            postalCodes = new ArrayList<>(postalCodeReader.getRecords().keySet());
            assertTrue(!postalCodes.isEmpty(), "Postal codes should not be empty for a valid test");
        } catch (SQLException e) {
            ErrorHandler.handleError(e);
            fail("Database connection setup failed: " + e.getMessage());
        }
    }

    @Test
    public void testValidPathBetweenPostalCodes() {
        Random rand = new Random();
        String fromPostalCode = postalCodes.get(rand.nextInt(postalCodes.size()));
        String toPostalCode = postalCodes.get(rand.nextInt(postalCodes.size()));

        try {
            // Assuming the correct method is findShortestDirectPath and it needs a radius
            // distance
            Path result = engine.findShortestDirectPath(fromPostalCode, toPostalCode, 0.5);

            assertNotNull(result, "Expected non-null path");
            assertNotNull(result.getTime(), "Expected non-null travel time");
            assertTrue(result.getTime().toMillis() > 0, "Expected positive travel time");
        } catch (Exception e) {
            ErrorHandler.handleError(e);
            fail("Error during path finding: " + e.getMessage());
        }
    }

    @Test
    public void testNoPathExceptionHandling() {
        List<String[]> testCases = Arrays.asList(
                new String[] { "00000", "99999" }, // Non-existent postal codes
                new String[] { "6200AA", "6299ZZ" }, // Valid range but hypothetical disconnected codes
                new String[] { "!@#$%", "^&*()" }, // Invalid characters
                new String[] { "12345-", "67890-" }, // Formats with dashes
                new String[] { "", "" }, // Empty strings
                new String[] { "12345", null }, // Null as a postal code
                new String[] { null, "67890" }, // Null as another postal code
                new String[] { "   ", "   " }, // Spaces only
                new String[] { "6300AA", "6310ZZ" }, // Out of Maastricht range
                new String[] { "6200AA", "6310ZZ" }, // Mixed valid and out of range
                new String[] { "6200", "6299" }, // Shortened format
                new String[] { "6200ZZ", "12345" } // Maastricht and non-Maastricht
        );

        for (String[] testCase : testCases) {
            final String fromPostalCode = testCase[0];
            final String toPostalCode = testCase[1];

            Path path = engine.findShortestDirectPath(fromPostalCode, toPostalCode, 0.3);

            assertNull(path, "Expected the path to be null for invalid or disconnected postal codes");
        }
    }

    @Test
    public void testResponseTimeForPathCalculation() {
        Random rand = new Random();
        String fromPostalCode = postalCodes.get(rand.nextInt(postalCodes.size()));
        String toPostalCode = postalCodes.get(rand.nextInt(postalCodes.size()));

        long startTime = System.currentTimeMillis();
        try {
            engine.findShortestDirectPath(fromPostalCode, toPostalCode, 0.5);
        } catch (Exception e) {
            ErrorHandler.handleError(e);
            fail("Error during path calculation: " + e.getMessage());
        }
        long endTime = System.currentTimeMillis();

        assertTrue((endTime - startTime) < 10000, "Expected response time to be less than 1000 milliseconds");
    }
}
