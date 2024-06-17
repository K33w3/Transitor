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
import com.graphhopper.isochrone.algorithm.Triangulator.Result;

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
    public ArrayList<Stop> getStopsFromPostalCode(String postalCode, double radiusDistance) {
        // Convert postal code to coordinates
        Coordinates coordinates = CoordHandler.getCoordinates(postalCode);
        if (coordinates == null) {
            return null;
        }
        // Initiate nearest stops list
        ArrayList<Stop> nearestStops = new ArrayList<Stop>();

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
            for (Stop fromStop : fromStops) {
                for (Stop toStop : toStops) {

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
                            select
                                t1.trip_id as trip_id,
                                t1.from_stop_id as from_stop_id,
                                t2.to_stop_id  as to_stop_id,
                                t1.departure_time as departure_time,
                                (t2.departure_time + interval t2.travel_time second + interval ? second) as arrival_time
                            from
                                timetable t1
                            join
                                timetable t2 on t1.trip_id = t2.trip_id
                            join
                                trips tr on t1.trip_id = tr.trip_id
                            join
                                calendar_dates cd on tr.service_id = cd.service_id
                            where
                                t1.from_stop_id = ?
                                and t2.to_stop_id = ?
                                and t1.trip_segment <= t2.trip_segment
                                and t1.departure_time >= (current_time + interval ? second)
                                and cd.date = current_date
                            order by
                                arrival_time
                            limit
                                1;
                                    """;

                    // Prepare the statement
                    PreparedStatement statement = connection.prepareStatement(findShortestPathSQL);
                    statement.setInt(1, walkTimeInSecondsToToPostalCode);
                    statement.setString(2, fromStop.getStopId());
                    statement.setString(3, toStop.getStopId());
                    statement.setInt(4, walkTimeInSecondsToFromStop);

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
                        }
                    }

                    statement.close();

                }
            }

            // Check is a direct trip was not found
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

    public static void main(String[] args) {
        GTFSEngine engine = new GTFSEngine();
        Path path = engine.findShortestPathWithTransportation("6221BA", "6221GE", 0.3);

        if (path != null) {
            System.out.println("Coordinates: " + path.getCoordinates().size());
        } else {
            System.out.println("No path found!");
        }
    }

    public Path findShortestPathWithTransportation(String fromPostalCode, String toPostalCode, double radiusDistance) {
        // Find stops for both postal codes
        ArrayList<Stop> fromStops = getStopsFromPostalCode(fromPostalCode, radiusDistance);
        ArrayList<Stop> toStops = getStopsFromPostalCode(toPostalCode, radiusDistance);

        Path path = new Path();

        try {
            Connection connection = DatabaseConnection.getConnection();

            String findOutgoingTripsSQL = """
                    select
                    	t.route_id,
                    	st.trip_id,
                    	st.departure_time
                    from
                    	stop_times st
                    join
                    	trips t on st.trip_id = t.trip_id
                    join
                    	calendar_dates cd on t.service_id = cd.service_id
                    join
                    	(
                    	select
                            t.route_id,
                            min(st.departure_time) as earliest_departure
                        from
                            stop_times st
                        join
                            trips t on st.trip_id = t.trip_id
                        join
                            calendar_dates cd on t.service_id = cd.service_id
                        where
                            st.stop_id = ? and
                            st.departure_time >= current_time() and
                            cd.date = current_date
                        group by
                            t.route_id
                    	) earliest_trips on t.route_id = earliest_trips.route_id
                    	and st.departure_time = earliest_trips.earliest_departure
                    where
                        st.stop_id = ?
                        and st.departure_time >= current_time
                        and cd.date = current_date;
                    """;

            Boolean transferTripFound = false;
            String firstTripId = null;
            String secondTripId = null;
            Stop transferExit = null;
            Stop transferStart = null;
            Stop fS = null;
            Stop tS = null;

            for (Stop fromStop : fromStops) {

                if (transferTripFound) {
                    break;
                }

                for (Stop toStop : toStops) {

                    if (transferTripFound) {
                        break;
                    }

                    PreparedStatement statement = connection.prepareStatement(findOutgoingTripsSQL);
                    // statement.setString(1, fromStop.getStopId());
                    // statement.setString(2, fromStop.getStopId());
                    statement.setString(1, "2511607");
                    statement.setString(2, "2511607");

                    ResultSet outgoingTripsResults = statement.executeQuery();

                    while (outgoingTripsResults.next()) {
                        String tripId = outgoingTripsResults.getString("trip_id");
                        tripId = "179431363";

                        String checkIfTripExistsSQL = """
                                select
                                	*
                                from
                                	stop_times st
                                where
                                	trip_id = (select to_trip_id from transfers where from_trip_id = ?)
                                	and
                                	stop_id = ?
                                	and
                                	stop_sequence >= (select stop_sequence from stop_times st2
                                						where trip_id = (select to_trip_id from transfers where from_trip_id = ?)
                                						and
                                						stop_id = (select to_stop_id from transfers where from_trip_id = ?))
                                                        """;
                        PreparedStatement checkIfTripExistsStatement = connection
                                .prepareStatement(checkIfTripExistsSQL);
                        checkIfTripExistsStatement.setString(1, tripId);
                        // checkIfTripExistsStatement.setString(2, toStop.getStopId());
                        checkIfTripExistsStatement.setString(2, "2510277");
                        checkIfTripExistsStatement.setString(3, tripId);
                        checkIfTripExistsStatement.setString(4, tripId);

                        ResultSet checkIfTripExistsResults = checkIfTripExistsStatement.executeQuery();
                        if (checkIfTripExistsResults.next()) {
                            System.out.println("Transfer trip found!");
                            transferTripFound = true;
                            firstTripId = tripId;
                            secondTripId = checkIfTripExistsResults.getString("trip_id");

                            // Get transfer stops
                            String getTransferStopsSQL = """
                                    select
                                    	from_stop_id,
                                    	to_stop_id
                                    from
                                    	transfers
                                    where
                                    	from_trip_id = ?
                                    """;
                            PreparedStatement getTransferStopsStatement = connection
                                    .prepareStatement(getTransferStopsSQL);
                            getTransferStopsStatement.setString(1, firstTripId);
                            ResultSet transferStopsResults = getTransferStopsStatement.executeQuery();
                            if (transferStopsResults.next()) {
                                transferExit = new Stop(transferStopsResults.getString("from_stop_id"));
                                transferStart = new Stop(transferStopsResults.getString("to_stop_id"));
                            }

                            fS = fromStop;
                            tS = toStop;

                            break;
                        }
                    }
                    statement.close();
                }
            }

            if (transferTripFound) {
                path = constructPathWithTransfer(fromPostalCode, toPostalCode, fS, tS, transferExit,
                        transferStart, firstTripId, secondTripId, connection);
            } else {
                return null;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return path;
    }

    private ResponsePath walk(Coordinates from, Coordinates to) {
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

    private Path constructPathWithTransfer(String fromPostalCode, String toPostalCode, Stop fromStop, Stop toStop,
            Stop transferExit, Stop transferStart,
            String firstTripId, String secondTripId, Connection connection) throws SQLException {
        Path path = new Path();

        // Walk from fromPostalCode to fromStop
        ResponsePath walkToFromStop = walk(CoordHandler.getCoordinates(fromPostalCode),
                fromStop.getCoordinates());
        ArrayList<Coordinates> walkToFromStopCoordinates = Utils.pointListToArrayList(walkToFromStop.getPoints());
        for (Coordinates c : walkToFromStopCoordinates) {
            path.addCoordinates(c, 0);
        }

        // Coordinates from fromStop to transferExit
        ArrayList<PathCoordinates> coordinatesFromFromStoptoTransferExit = getCoordinatesForDirectTrip(firstTripId,
                fromStop.getStopId(),
                transferExit.getStopId(), connection);
        for (PathCoordinates c : coordinatesFromFromStoptoTransferExit) {
            path.addCoordinates(c, 1);
        }

        // Coordinates from transferStart to toStop
        ArrayList<PathCoordinates> coordinatesFromTransferStartToToStop = getCoordinatesForDirectTrip(secondTripId,
                transferStart.getStopId(),
                toStop.getStopId(), connection);
        for (PathCoordinates c : coordinatesFromTransferStartToToStop) {
            path.addCoordinates(c, 1);
        }

        // Walk from toStop to toPostalCode
        ResponsePath walkToToPostalCode = walk(toStop.getCoordinates(), CoordHandler.getCoordinates(toPostalCode));
        ArrayList<Coordinates> walkToToPostalCodeCoordinates = Utils
                .pointListToArrayList(walkToToPostalCode.getPoints());
        for (Coordinates c : walkToToPostalCodeCoordinates) {
            path.addCoordinates(c, 0);
        }

        return path;

    }

    public ArrayList<PathCoordinates> getCoordinatesForDirectTrip(String tripId, String fromStopId, String toStopId,
            Connection connection) throws SQLException {
        ArrayList<PathCoordinates> coordinates = new ArrayList<PathCoordinates>();

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
        statement.setString(1, tripId);
        statement.setString(2, tripId);
        statement.setString(3, fromStopId);
        statement.setString(4, tripId);
        statement.setString(5, toStopId);

        // Populate the Path instance
        ResultSet coordinatesResultSet = statement.executeQuery();
        while (coordinatesResultSet.next()) {
            String latitute = String.valueOf(coordinatesResultSet.getDouble("shape_pt_lat"));
            String longitude = String.valueOf(coordinatesResultSet.getDouble("shape_pt_lon"));
            int shapeDistTraveled = coordinatesResultSet.getInt("shape_dist_traveled");
            coordinates.add(new PathCoordinates(latitute, longitude, shapeDistTraveled, 1));
        }

        return coordinates;
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
        ArrayList<PathStop> stops = new ArrayList<PathStop>();

        // Get stops SQL query (using a join)
        String getStopsSQL = """
                select
                    s.stop_id,
                    s.stop_name,
                    s.stop_lat,
                    s.stop_lon,
                    st.departure_time
                from
                    stops s
                join
                    stop_times st on s.stop_id = st.stop_id
                where
                    st.trip_id = ?
                    and st.stop_sequence >= (select stop_sequence from stop_times where trip_id = ? and stop_id = ?)
                    and st.stop_sequence <= (select stop_sequence from stop_times where trip_id = ? and stop_id = ?)
                order by
                    st.stop_sequence;
                    """;

        // Prepare statement
        PreparedStatement statement = connection.prepareStatement(getStopsSQL);
        statement.setInt(1, tripId);
        statement.setInt(2, tripId);
        statement.setString(3, start.getStopId());
        statement.setInt(4, tripId);
        statement.setString(5, end.getStopId());

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
