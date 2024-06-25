package com.bcs05.data;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles GTFS data operations.
 */
public class GTFSDataHandler {
    private static final Logger LOGGER = Logger.getLogger(GTFSDataHandler.class.getName());

    /**
     * Main method to create the timetable.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GTFSDataHandler handler = new GTFSDataHandler();
        handler.createTimeTable();
    }

    /**
     * Creates a timetable based on the GTFS data and stores it in the database.
     */
    public void createTimeTable() {
        Connection connection = null;
        try {
            // Open connection to database
            connection = DatabaseConnectionManager.getConnection();
            // Drop existing timetable table if it exists
            TimeTableOperations.dropExistingTable(connection);
            // Create timetable table
            TimeTableOperations.createTable(connection);
            // Query trip stops ordered by departure time
            ResultSet rs = TimeTableOperations.executeTripStopsQuery(connection);

            // Variables to track trip segments and previous stop information
            int tripSegment = 0;
            String previousTripId = "";
            String previousStopId = "";
            Time previousDepartureTime = null;
            int previousShapeDistTraveled = 0;

            // Iterate through the result set and insert timetable entries
            while (rs.next()) {
                String tripId = rs.getString("trip_id");
                String stopId = rs.getString("stop_id");
                Time departureTime = rs.getTime("departure_time");
                int shapeDistTraveled = rs.getInt("shape_dist_traveled");

                // Reset trip segment counter if trip ID changes
                if (!tripId.equals(previousTripId)) {
                    tripSegment = 0;
                    previousTripId = tripId;
                } else {
                    tripSegment++;
                }

                // Insert timetable entry for the segment
                if (previousDepartureTime != null) {
                    int travelTime = (int) (departureTime.getTime() - previousDepartureTime.getTime()) / 1000 / 60;
                    int segmentDistance = shapeDistTraveled - previousShapeDistTraveled;

                    TimeTableOperations.insertTimetableEntry(connection, tripId, tripSegment, previousStopId, stopId, previousDepartureTime, travelTime, segmentDistance);
                }

                // Update previous stop information
                previousStopId = stopId;
                previousDepartureTime = departureTime;
                previousShapeDistTraveled = shapeDistTraveled;
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error occurred while creating timetable", e);
        } finally {
            // Close database connection
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Failed to close database connection", e);
                }
            }
        }
    }
}
