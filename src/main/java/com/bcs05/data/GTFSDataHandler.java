// package com.bcs05.data;

// import com.bcs05.util.DatabaseConnection;
// import java.sql.Connection;
// import java.sql.PreparedStatement;
// import java.sql.ResultSet;
// import java.sql.SQLException;
// import java.sql.Statement;
// import java.sql.Time;

// /**
//  * Handles GTFS data operations
//  */
// public class GTFSDataHandler {

//     public static void main(String[] args) {
//         GTFSDataHandler.createTimeTable();
//     }

//     /**
//      * Creates a timetable based on the GTFS data and stores it in the database
//      */
//     public static void createTimeTable() {
//         try {
//             // Open connection to database
//             Connection connection = DatabaseConnection.getConnection();
//             Statement statement = connection.createStatement();

//             // Drop existing timetable table if it exists
//             statement.executeUpdate("DROP TABLE IF EXISTS timetable");

//             // Create timetable table
//             String createTimeTableTableSQL = """
//                     CREATE TABLE timetable (
//                         trip_id VARCHAR(255),
//                         trip_segment INT,
//                         from_stop_id VARCHAR(255),
//                         to_stop_id VARCHAR(255),
//                         departure_time TIME,
//                         travel_time INT,
//                         shape_dist_traveled INT
//                     )
//                     """;

//             statement.executeUpdate(createTimeTableTableSQL);

//             // Query trip stops ordered by departure time
//             String tripStopsQuerySQL = """
//                         SELECT
//                             trips.trip_id,
//                             stop_times.stop_id,
//                             stop_times.departure_time,
//                             stop_times.shape_dist_traveled
//                         FROM
//                             trips,
//                             stop_times
//                         WHERE
//                             trips.trip_id = stop_times.trip_id
//                             AND stop_times.stop_id IN (
//                                 SELECT stop_id
//                                 FROM stops
//                             )
//                             AND trips.route_id IN (
//                                 SELECT route_id
//                                 FROM routes
//                                 WHERE route_type = 3
//                             )
//                             AND stop_times.departure_time <= '23:59:59'
//                         ORDER BY
//                             stop_times.trip_id,
//                             stop_times.departure_time;
//                     """;

//             ResultSet resultSet = statement.executeQuery(tripStopsQuerySQL);

//             String currentTripID = "";
//             String currentStopID = "";
//             Time currentDepartureTime = null;
//             int count = 0;

//             while (resultSet.next()) {
//                 String tripID = resultSet.getString("trip_id");
//                 String stopID = resultSet.getString("stop_id");
//                 Time departureTime = resultSet.getTime("departure_time");
//                 int shapeDistanceTraveled = resultSet.getInt("shape_dist_traveled");

//                 if (currentTripID.equals(tripID)) {
//                     String from_stop_id = currentStopID;
//                     String to_stop_id = stopID;
//                     int travel_time = (int) (departureTime.getTime() - currentDepartureTime.getTime()) / 1000;

//                     // Insert data into timetable table
//                     String insertToTimeTableSQL = """
//                             INSERT INTO timetable (trip_id, trip_segment, from_stop_id, to_stop_id, departure_time, travel_time, shape_dist_traveled)
//                             VALUES (?, ?, ?, ?, ?, ?, ?)
//                             """;
//                     PreparedStatement preparedStatement = connection.prepareStatement(insertToTimeTableSQL);
//                     preparedStatement.setString(1, currentTripID);
//                     preparedStatement.setInt(2, count);
//                     preparedStatement.setString(3, from_stop_id);
//                     preparedStatement.setString(4, to_stop_id);
//                     preparedStatement.setTime(5, currentDepartureTime);
//                     preparedStatement.setInt(6, travel_time);
//                     preparedStatement.setInt(7, shapeDistanceTraveled);

//                     preparedStatement.execute();
//                     preparedStatement.close();

//                 } else
//                     count = 0;

//                 currentTripID = tripID;
//                 currentStopID = stopID;
//                 currentDepartureTime = departureTime;
//                 count++;
//             }

//         } catch (SQLException e) {
//             e.printStackTrace();
//         }

//     }

// }
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
