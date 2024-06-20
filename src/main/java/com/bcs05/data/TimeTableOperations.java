package com.bcs05.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles operations related to the timetable.
 */
public class TimeTableOperations {
    private static final Logger LOGGER = Logger.getLogger(TimeTableOperations.class.getName());

    /**
     * Drops the existing timetable table if it exists.
     * 
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public static void dropExistingTable(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.executeUpdate("DROP TABLE IF EXISTS timetable");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to drop existing timetable table", e);
            throw e;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close statement", e);
                }
            }
        }
    }

    /**
     * Creates the timetable table.
     * 
     * @param connection the database connection
     * @throws SQLException if a database access error occurs
     */
    public static void createTable(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String createTimeTableTableSQL = """
                    CREATE TABLE timetable (
                        trip_id VARCHAR(255),
                        trip_segment INT,
                        from_stop_id VARCHAR(255),
                        to_stop_id VARCHAR(255),
                        departure_time TIME,
                        travel_time INT,
                        shape_dist_traveled INT
                    )
                    """;
            statement.executeUpdate(createTimeTableTableSQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to create timetable table", e);
            throw e;
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close statement", e);
                }
            }
        }
    }

    /**
     * Executes the query to retrieve trip stops ordered by departure time.
     * 
     * @param connection the database connection
     * @return the result set of the query
     * @throws SQLException if a database access error occurs
     */
    public static ResultSet executeTripStopsQuery(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String tripStopsQuerySQL = """
                    SELECT
                        trips.trip_id,
                        stop_times.stop_id,
                        stop_times.departure_time,
                        stop_times.shape_dist_traveled
                    FROM
                        trips,
                        stop_times
                    WHERE
                        trips.trip_id = stop_times.trip_id
                        AND stop_times.departure_time IS NOT NULL
                    ORDER BY
                        trips.trip_id, stop_times.departure_time
                    """;
            return statement.executeQuery(tripStopsQuerySQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to execute trip stops query", e);
            throw e;
        }
    }

    /**
     * Inserts a timetable entry into the timetable table.
     * 
     * @param connection the database connection
     * @param tripId the trip ID
     * @param tripSegment the trip segment number
     * @param fromStopId the starting stop ID
     * @param toStopId the ending stop ID
     * @param departureTime the departure time
     * @param travelTime the travel time in minutes
     * @param shapeDistTraveled the shape distance traveled
     * @throws SQLException if a database access error occurs
     */
    public static void insertTimetableEntry(Connection connection, String tripId, int tripSegment, String fromStopId, String toStopId, Time departureTime, int travelTime, int shapeDistTraveled) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            String insertSQL = "INSERT INTO timetable (trip_id, trip_segment, from_stop_id, to_stop_id, departure_time, travel_time, shape_dist_traveled) VALUES (?, ?, ?, ?, ?, ?, ?)";
            preparedStatement = connection.prepareStatement(insertSQL);
            preparedStatement.setString(1, tripId);
            preparedStatement.setInt(2, tripSegment);
            preparedStatement.setString(3, fromStopId);
            preparedStatement.setString(4, toStopId);
            preparedStatement.setTime(5, departureTime);
            preparedStatement.setInt(6, travelTime);
            preparedStatement.setInt(7, shapeDistTraveled);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to insert timetable entry", e);
            throw e;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close prepared statement", e);
                }
            }
        }
    }

    /**
     * Retrieves the timetable data.
     * 
     * @param connection the database connection
     * @return the result set of the timetable data
     * @throws SQLException if a database access error occurs
     */
    public static ResultSet getTimetable(Connection connection) throws SQLException {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            String querySQL = "SELECT * FROM timetable";
            return statement.executeQuery(querySQL);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to get timetable", e);
            throw e;
        }
    }
}
