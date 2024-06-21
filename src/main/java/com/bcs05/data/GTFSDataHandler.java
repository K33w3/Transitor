package com.bcs05.data;

import com.bcs05.util.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

/**
 * Handles GTFS data operations
 */
public class GTFSDataHandler {

    public static void main(String[] args) {
        GTFSDataHandler.createTimeTable();
    }

    /**
     * Creates a timetable based on the GTFS data and stores it in the database
     */
    public static void createTimeTable() {
        try {
            // Open connection to database
            Connection connection = DatabaseConnection.getConnection();
            Statement statement = connection.createStatement();

            // Drop existing timetable table if it exists
            statement.executeUpdate("DROP TABLE IF EXISTS timetable");

            // Create timetable table
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

            // Query trip stops ordered by departure time
            String tripStopsQuerySQL = """
                    SELECT
                        trips.trip_id,
                        stop_times.stop_id,
                        stop_times.departure_time,
                        stop_times.shape_dist_traveled
                    FROM
                        trips
                    JOIN
                        stop_times
                    ON
                    	trips.trip_id = stop_times.trip_id
                    JOIN
                    	calendar_dates
                    ON
                    	trips.service_id = calendar_dates.service_id
                    JOIN
                    	routes
                    ON
                    	trips.route_id = routes.route_id
                    WHERE
                       stop_times.departure_time <= '23:59:59'
                       AND
                       calendar_dates.date = CURRENT_DATE
                    ORDER BY
                        stop_times.trip_id,
                        stop_times.departure_time;
                                        """;

            ResultSet tripStops = statement.executeQuery(tripStopsQuerySQL);

            String currentTripID = "";
            String currentStopID = "";
            Time currentDepartureTime = null;
            int tripSegment = 0;

            while (tripStops.next()) {
                String tripID = tripStops.getString("trip_id");
                String stopID = tripStops.getString("stop_id");
                Time departureTime = tripStops.getTime("departure_time");
                int shapeDistanceTraveled = tripStops.getInt("shape_dist_traveled");

                if (currentTripID.equals(tripID)) {
                    String from_stop_id = currentStopID;
                    String to_stop_id = stopID;
                    int travel_time = (int) (departureTime.getTime() - currentDepartureTime.getTime()) / 1000;

                    // Insert data into timetable table
                    String insertToTimeTableSQL = """
                            INSERT INTO timetable (trip_id, trip_segment, from_stop_id, to_stop_id, departure_time, travel_time, shape_dist_traveled)
                            VALUES (?, ?, ?, ?, ?, ?, ?)
                            """;
                    PreparedStatement preparedStatement = connection.prepareStatement(insertToTimeTableSQL);
                    preparedStatement.setString(1, currentTripID);
                    preparedStatement.setInt(2, tripSegment);
                    preparedStatement.setString(3, from_stop_id);
                    preparedStatement.setString(4, to_stop_id);
                    preparedStatement.setTime(5, currentDepartureTime);
                    preparedStatement.setInt(6, travel_time);
                    preparedStatement.setInt(7, shapeDistanceTraveled);

                    preparedStatement.execute();
                    preparedStatement.close();

                } else
                    tripSegment = 0;

                currentTripID = tripID;
                currentStopID = stopID;
                currentDepartureTime = departureTime;
                tripSegment++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}
