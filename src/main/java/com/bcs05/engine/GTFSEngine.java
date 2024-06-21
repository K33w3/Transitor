package com.bcs05.engine;

import com.bcs05.util.CoordHandler;
import com.bcs05.util.Coordinates;
import com.bcs05.util.DatabaseConnection;
import com.bcs05.util.Path;
import com.bcs05.util.PathCoordinates;
import com.bcs05.util.PathStop;
import com.bcs05.util.Stop;
import com.bcs05.util.Transportation;
import com.bcs05.util.Utils;
import com.graphhopper.ResponsePath;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

public class GTFSEngine {

    /**
     * Retrieves a list of stops within radiusDistance km from the given postal
     * code.
     * This method converts the postal code to geographic coordinates,
     * queries the database to find nearby stops, and returns a list of stop IDs.
     *
     * @param postalCode the postal code to find nearby stops for
     * @return a list of stops within radiusDistance km of the given postal code
     */
    public static ArrayList<Stop> getStopsFromPostalCode(String postalCode, double radiusDistance) {
        // Convert postal code to coordinates
        Coordinates coordinates = CoordHandler.getCoordinates(postalCode);
        if (coordinates == null) {
            return null;
        }
        // Initiate nearest stops list
        ArrayList<Stop> nearestStops = new ArrayList<>();

        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();

            // Get nearest stops SQL query
            String getNearPostalCodesSQL = """
                    select
                        stop_id,
                        stop_lat,
                        stop_lon,
                        distance_in_km
                    from
                        (
                            select
                                stop_id,
                                stop_lat,
                                stop_lon,
                                2 * 6371 * ASIN(SQRT(
                                    POWER(SIN((RADIANS(stop_lat) - RADIANS(?)) / 2), 2) +
                                    COS(RADIANS(?)) * COS(RADIANS(stop_lat)) *
                                    POWER(SIN((RADIANS(stop_lon) - RADIANS(?)) / 2), 2)
                                )) as distance_in_km
                            from
                                stops
                        ) as distances
                    where
                        distance_in_km < ?
                    order by
                        distance_in_km;
                    """;

            // Prepare the statement
            PreparedStatement getNearestStopsStatement = connection.prepareStatement(getNearPostalCodesSQL);
            getNearestStopsStatement.setString(1, coordinates.getLatitude());
            getNearestStopsStatement.setString(2, coordinates.getLatitude());
            getNearestStopsStatement.setString(3, coordinates.getLongitude());
            getNearestStopsStatement.setDouble(4, radiusDistance);

            // Execute query and add results to array list
            ResultSet nearestStopsResultSet = getNearestStopsStatement.executeQuery();
            while (nearestStopsResultSet.next()) {
                String stopId = nearestStopsResultSet.getString("stop_id");
                Coordinates stopCoordinates = new Coordinates(nearestStopsResultSet.getString("stop_lat"),
                        nearestStopsResultSet.getString("stop_lon"));
                nearestStops.add(new Stop(stopId, stopCoordinates));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nearestStops;
    }

    /**
     * Finds the shortest direct path between two postal codes.
     *
     * @param fromPostalCode the starting postal code
     * @param toPostalCode   the destination postal code
     * @return the shortest path found or null if no path is found
     */
    public Path findShortestDirectPath(String fromPostalCode, String toPostalCode, double radiusDistance) {
        // Find stops for both postal codes
        ArrayList<Stop> fromStops = getStopsFromPostalCode(fromPostalCode, radiusDistance);
        ArrayList<Stop> toStops = getStopsFromPostalCode(toPostalCode, radiusDistance);

        if (fromStops == null || toStops == null) {
            return null;
        }
        System.out.println("Size of fromStops " + fromStops.size());
        System.out.println("Size of toStops " + toStops.size());

        // Set outer variables to store the shortest path
        int bestTripId = -1;
        LocalTime bestDepartureTime = null;
        LocalTime bestArrivalTime = null;
        Stop bestFromStop = null;
        Stop bestToStop = null;
        Path path = new Path();

        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();

            // Find shortest trip
            boolean found = false;
            for (Stop fromStop : fromStops) {
                if (found) break;
                for (Stop toStop : toStops) {
                    if (found) break;

                    // Calculate walk time from postal code to stop
                    ResponsePath walkToFromStopPath = walk(CoordHandler.getCoordinates(fromPostalCode),
                            fromStop.getCoordinates());
                    int walkTimeInSecondsToFromStop = (int) walkToFromStopPath.getTime() / 1000;

                    // Calculate walk time from to stop to to postal code
                    ResponsePath walkToToPostalCode = walk(toStop.getCoordinates(),
                            CoordHandler.getCoordinates(toPostalCode));
                    int walkTimeInSecondsToToPostalCode = (int) walkToToPostalCode.getTime() / 1000;

                    // SQL query to find shortest trip from stop to stop
                    String findShortestPathSQL = """
                        SELECT
                            t1.trip_id AS trip_id,
                            t1.from_stop_id AS from_stop_id,
                            t2.to_stop_id AS to_stop_id,
                            t1.departure_time AS departure_time,
                            (t2.departure_time + INTERVAL t2.travel_time SECOND + INTERVAL ? SECOND) AS arrival_time
                        FROM
                            timetable t1
                        JOIN
                            timetable t2 ON t1.trip_id = t2.trip_id
                        WHERE
                            t1.from_stop_id = ?
                            AND t2.to_stop_id = ?
                            AND EXISTS (
                                SELECT 1
                                FROM trips tr
                                WHERE tr.trip_id = t1.trip_id
                                AND EXISTS (
                                    SELECT 1
                                    FROM calendar_dates cd
                                    WHERE cd.service_id = tr.service_id
                                    AND cd.date = CURRENT_DATE
                                )
                            )
                            AND t2.trip_segment >= t1.trip_segment
                            AND t1.departure_time >= (CURRENT_TIME + INTERVAL ? SECOND)
                        ORDER BY
                            arrival_time
                        LIMIT 1;
                    """;
                    
                    // Prepare the statement
                    PreparedStatement statement = connection.prepareStatement(findShortestPathSQL);
                    statement.setInt(1, walkTimeInSecondsToToPostalCode);  // For the interval addition in arrival_time calculation
                    statement.setString(2, fromStop.getStopId());
                    statement.setString(3, toStop.getStopId());
                    statement.setInt(4, walkTimeInSecondsToFromStop);  // For the departure_time condition                    

                    // Check if this path is better than previous ones
                    ResultSet newPossibleBestPath = statement.executeQuery();
                    if (newPossibleBestPath.next()) {
                        int tripId = newPossibleBestPath.getInt("trip_id");
                        LocalTime departureTime = newPossibleBestPath.getTime("departure_time").toLocalTime();
                        LocalTime arrivalTime = newPossibleBestPath.getTime("arrival_time").toLocalTime();

                        if (bestArrivalTime == null || arrivalTime.isBefore(bestArrivalTime)) {
                            bestTripId = tripId;
                            bestDepartureTime = departureTime;
                            bestArrivalTime = arrivalTime;
                            bestFromStop = fromStop;
                            bestToStop = toStop;
                            found = true;
                        }
                    }

                    statement.close();

                }
            }

            // Check if a direct trip was not found
            if (bestTripId == -1)
                return null;

            // Construct path using shapes
            path = constructPath(fromPostalCode, toPostalCode, bestTripId, bestFromStop, bestToStop, bestDepartureTime,
                    bestArrivalTime, connection);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return path;
    }

    public static ResponsePath walk(Coordinates from, Coordinates to) {
        RoutingEngine routingEngine = new RoutingEngine(Transportation.FOOT);
        return routingEngine.routing(from, to);
    }

    /**
     * Constructs a path between two stops.
     *
     * @param tripId        the trip ID
     * @param fromStop      the starting stop
     * @param toStop        the destination stop
     * @param departureTime the departure time
     * @param arrivalTime   the arrival time
     * @param connection    the database connection
     * @return the path between the two stops
     * @throws SQLException if an error occurs while querying the database
     */
    private Path constructPath(String fromPostalCode, String toPostalCode, int tripId, Stop fromStop, Stop toStop,
            LocalTime departureTime, LocalTime arrivalTime,
            Connection connection) throws SQLException {
        Path path = new Path();

        // Get coordinates SQL query
        String getCoordinatesSQL = """
                select
                    shape_pt_lat,
                    shape_pt_lon,
                    shape_dist_traveled
                from
                    shapes
                where
                    shape_id = (select shape_id from trips where trip_id = ?)
                    and shape_dist_traveled >= (select shape_dist_traveled from stop_times where trip_id = ? and stop_id = ?)
                    and shape_dist_traveled <= (select shape_dist_traveled from stop_times where trip_id = ? and stop_id = ?);
                        """;

        // Prepare statement
        PreparedStatement statement = connection.prepareStatement(getCoordinatesSQL);
        statement.setInt(1, tripId);
        statement.setInt(2, tripId);
        statement.setString(3, fromStop.getStopId());
        statement.setInt(4, tripId);
        statement.setString(5, toStop.getStopId());

        // Populate the Path instance
        ResultSet coordinates = statement.executeQuery();
        while (coordinates.next()) {
            String latitute = String.valueOf(coordinates.getDouble("shape_pt_lat"));
            String longitude = String.valueOf(coordinates.getDouble("shape_pt_lon"));
            int shapeDistTraveled = coordinates.getInt("shape_dist_traveled");
            path.addCoordinates(new PathCoordinates(latitute, longitude, shapeDistTraveled, 1));
        }

        // Path from postal code to stop
        ResponsePath walkToFromStopPath = walk(CoordHandler.getCoordinates(fromPostalCode), fromStop.getCoordinates());
        ArrayList<Coordinates> walkToFromStopCoordinates = Utils.pointListToArrayList(walkToFromStopPath.getPoints());

        // Path from stop to postal code
        ResponsePath walkToToPostalCode = walk(toStop.getCoordinates(), CoordHandler.getCoordinates(toPostalCode));
        ArrayList<Coordinates> walkToToPostalCodeCoordinates = Utils
                .pointListToArrayList(walkToToPostalCode.getPoints());

        // Set time of path
        Duration timeOfPath = computeTime(arrivalTime);
        path.setTime(timeOfPath);

        // Set distance of path
        int distance = computeDistance(path.getCoordinates(), (int) walkToFromStopPath.getDistance(),
                (int) walkToToPostalCode.getDistance());
        path.setDistance(distance);

        // Add walk to from stop path
        for (int i = walkToFromStopCoordinates.size() - 1; i >= 0; i--) {
            path.addCoordinatesToStart(walkToFromStopCoordinates.get(i), 0);
        }

        // Add walk to to postal code path
        for (Coordinates c : walkToToPostalCodeCoordinates) {
            path.addCoordinates(c, 0);
        }

        // Get trip stops
        ArrayList<PathStop> tripStops = getTripStops(tripId, fromStop, toStop, connection);
        path.setStops(tripStops);

        return path;
    }

    /**
     * Computes the time between two LocalTime instances.
     *
     * //* @param departureTime the departure time
     * 
     * @param arrivalTime the arrival time
     * @return the time between the two instances
     */
    private Duration computeTime(LocalTime arrivalTime) {
        return Duration.between(LocalTime.now(), arrivalTime);
    }

    /**
     * Computes the distance given a set of PathCoordinates.
     *
     * @param coordinates the coordinates to compute the distance between
     * @return the distance between the coordinates
     */
    private int computeDistance(ArrayList<PathCoordinates> coordinates, int walkToFromStopDistance,
            int walkToToPostalCodeDistance) {
        PathCoordinates first = coordinates.get(0);
        PathCoordinates last = coordinates.get(coordinates.size() - 1);
        int busDistance = last.getShapeDistTraveled() - first.getShapeDistTraveled();
        int totalDistance = walkToFromStopDistance + busDistance + walkToToPostalCodeDistance;
        return totalDistance;

    }

    private ArrayList<PathStop> getTripStops(int tripId, Stop start, Stop end, Connection connection)
            throws SQLException {
        ArrayList<PathStop> stops = new ArrayList<>();

        // Get stops SQL query (using a join)
        String getStopsSQL = """
            select
                s.stop_id,
                s.stop_name,
                s.stop_lat,
                s.stop_lon,
                (select st.departure_time from stop_times st where st.stop_id = s.stop_id and st.trip_id = ?) as departure_time
            from
                stops s
            where
                s.stop_id in (
                    select stop_id
                    from stop_times
                    where trip_id = ?
                    and stop_sequence >= (select stop_sequence from stop_times where trip_id = ? and stop_id = ?)
                    and stop_sequence <= (select stop_sequence from stop_times where trip_id = ? and stop_id = ?)
                )
            order by
                (select stop_sequence from stop_times st where st.stop_id = s.stop_id and st.trip_id = ?);
            """;
    
        // Prepare statement
        PreparedStatement statement = connection.prepareStatement(getStopsSQL);
        statement.setInt(1, tripId); // For departure_time subquery
        statement.setInt(2, tripId); // For the main subquery
        statement.setInt(3, tripId); // For start stop_sequence subquery
        statement.setString(4, start.getStopId()); // For start stop_sequence subquery
        statement.setInt(5, tripId); // For end stop_sequence subquery
        statement.setString(6, end.getStopId()); // For end stop_sequence subquery
        statement.setInt(7, tripId); // For stop_sequence order subquery
        

        // Populate the stops list
        ResultSet stopResults = statement.executeQuery();
        while (stopResults.next()) {
            String stopId = stopResults.getString("stop_id");
            String stopName = stopResults.getString("stop_name");
            Coordinates coordinates = new Coordinates(stopResults.getString("stop_lat"),
                    stopResults.getString("stop_lon"));
            String departureTime = stopResults.getTime("departure_time").toString();
            stops.add(new PathStop(stopId, coordinates, stopName, departureTime));
        }

        return stops;
    }

}
