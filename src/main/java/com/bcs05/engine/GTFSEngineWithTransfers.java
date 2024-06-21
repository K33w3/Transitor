package com.bcs05.engine;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.bcs05.util.BusTransferResult;
import com.bcs05.util.CoordHandler;
import com.bcs05.util.Coordinates;
import com.bcs05.util.DatabaseConnection;
import com.bcs05.util.DijkstraNode;
import com.bcs05.util.DijkstraNodeComparator;
import com.bcs05.util.GTFSGraph;
import com.bcs05.util.GTFSWeightedEdge;
import com.bcs05.util.Path;
import com.bcs05.util.PathStop;
import com.bcs05.util.PathTransfer;
import com.bcs05.util.PathTransferStop;
import com.bcs05.util.Route;
import com.bcs05.util.Stop;
import com.bcs05.util.Utils;
import com.graphhopper.ResponsePath;

public class GTFSEngineWithTransfers {

    public static void main(String[] args) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        engine.findPathWithTransfers("6225GE", "6228JG", 0.3);
    }

    GTFSGraph graph;

    public GTFSEngineWithTransfers() {
        graph = new GTFSGraph();
    }

    public PathTransfer findPathWithTransfers(String fromPostalCode, String toPostalCode, double radius) {
        ArrayList<Stop> startStops = GTFSEngine.getStopsFromPostalCode(fromPostalCode, radius);
        ArrayList<Stop> endStops = GTFSEngine.getStopsFromPostalCode(toPostalCode, radius);

        Coordinates fromPostalCodeCoordinates = CoordHandler.getCoordinates(fromPostalCode);

        ArrayList<BusTransferResult> results = new ArrayList<BusTransferResult>();
        Boolean alreadyFoundResults = false;

        for (Stop startStop : startStops) {
            for (Stop endStop : endStops) {
                // Get walking time to start stop
                ResponsePath walkToFromStopPath = GTFSEngine.walk(fromPostalCodeCoordinates,
                        startStop.getCoordinates());
                int walkTimeInSecondsToFromStop = (int) walkToFromStopPath.getTime() / 1000;

                LocalTime timeArrivingAtStartStop = LocalTime.now().plusSeconds(walkTimeInSecondsToFromStop)
                        .truncatedTo(ChronoUnit.MINUTES);

                BusTransferResult path = findBusPathWithTransfers(startStop, endStop,
                        timeArrivingAtStartStop);

                if (path != null) {
                    results.add(path);
                }

                if (results.size() == 1) {
                    alreadyFoundResults = true;
                    break;
                }

            }

            if (alreadyFoundResults) {
                break;
            }
        }

        if (results.isEmpty()) {
            return null;
        }

        BusTransferResult bestResult = getBestTransferResult(results);
        PathTransfer finalPath = reconstructPathForUI(fromPostalCode, toPostalCode, bestResult);

        return finalPath;
    }

    public BusTransferResult findBusPathWithTransfers(Stop startStop, Stop endStop,
            LocalTime timeArrivingAtStartStop) {
        System.out.println("New iteration started");
        HashMap<Stop, Boolean> explored = new HashMap<Stop, Boolean>();
        HashMap<Stop, Integer> travelTime = new HashMap<Stop, Integer>();
        HashMap<Stop, Stop> previous = new HashMap<Stop, Stop>();
        HashMap<Stop, String> tripId = new HashMap<Stop, String>();
        HashMap<Stop, LocalTime> departureTime = new HashMap<Stop, LocalTime>();

        for (Stop stop : graph.getStops()) {
            explored.put(stop, false);
            travelTime.put(stop, Integer.MAX_VALUE);
            previous.put(stop, null);
            tripId.put(stop, null);
            departureTime.put(stop, null);
        }

        PriorityQueue<DijkstraNode> priorityQueue = new PriorityQueue<DijkstraNode>(
                new DijkstraNodeComparator());

        travelTime.put(startStop, 0);
        priorityQueue.add(new DijkstraNode(startStop, 0));

        LocalTime currentTime = timeArrivingAtStartStop;

        while (!priorityQueue.isEmpty()) {
            Stop currentStop = priorityQueue.poll().getStop();
            LocalTime currentDepartureTime = currentTime.plusSeconds(travelTime.get(currentStop)); // arrive at station

            explored.put(currentStop, true);

            if (currentStop.equals(endStop)) {
                break;
            }

            for (GTFSWeightedEdge neighbourEdge : graph.getNeighbours(currentStop, currentDepartureTime)) {
                Stop neighbourStop = neighbourEdge.getStop();
                int waitingTime = (int) (neighbourEdge.getDepartureTime().toSecondOfDay()
                        - currentDepartureTime.toSecondOfDay());
                int newTravelTime = travelTime.get(currentStop) + waitingTime + neighbourEdge.getTravelTime();
                if (newTravelTime < travelTime.get(neighbourStop)) {
                    travelTime.put(neighbourStop, newTravelTime);
                    previous.put(neighbourStop, currentStop);
                    tripId.put(neighbourStop, neighbourEdge.getTripId());
                    departureTime.put(currentStop, neighbourEdge.getDepartureTime());

                    if (!explored.get(neighbourStop)) {
                        priorityQueue.add(new DijkstraNode(neighbourStop, travelTime.get(neighbourStop)));
                    }
                }

            }
        }

        // Reconstruct path
        if (explored.getOrDefault(endStop, false)) {
            System.out.println("Path found");
            System.out.println("Travel time: " + travelTime.get(endStop));
            System.out.println("Arrival time: " + currentTime.plusSeconds(travelTime.get(endStop)));

            ArrayList<PathTransferStop> pathStops = new ArrayList<PathTransferStop>();

            Stop currentStop = endStop;
            while (currentStop != null) {
                System.out.println(
                        currentStop.getStopId() + " " + currentTime.plusSeconds(travelTime.get(currentStop)) + " "
                                + tripId.get(currentStop) + " " + departureTime.get(currentStop));
                if (departureTime.get(currentStop) == null) {
                    PathTransferStop pathStop = new PathTransferStop(currentStop.getStopId(),
                            currentStop.getCoordinates(),
                            null, null, tripId.get(currentStop));
                    pathStops.add(0, pathStop);
                } else {
                    PathTransferStop pathStop = new PathTransferStop(currentStop.getStopId(),
                            currentStop.getCoordinates(),
                            null, departureTime.get(currentStop).toString(), tripId.get(currentStop));
                    pathStops.add(0, pathStop);
                }

                currentStop = previous.get(currentStop);

            }

            BusTransferResult result = new BusTransferResult(pathStops,
                    currentTime.plusSeconds(travelTime.get(endStop)));

            return result;
        } else
            System.out.println("No path found");

        return null;
    }

    private BusTransferResult getBestTransferResult(ArrayList<BusTransferResult> results) {
        BusTransferResult bestResult = results.get(0);
        for (BusTransferResult result : results) {
            if (result.getArrivalTime().isBefore(bestResult.getArrivalTime())) {
                bestResult = result;
            }
        }

        return bestResult;
    }

    private PathTransfer reconstructPathForUI(String fromPostalCode, String toPostalCode, BusTransferResult result) {
        PathTransfer path = new PathTransfer();

        Stop fromStop = result.getStops().get(0);
        Stop toStop = result.getStops().get(result.getStops().size() - 1);

        // Walk to fromStop
        addWalkPath(path, fromPostalCode, fromStop);

        // Get routes for each stop
        ArrayList<Route> routes = new ArrayList<Route>();
        routes.add(null);
        for (int i = 1; i < result.getStops().size(); i++) {
            PathTransferStop stop = result.getStops().get(i);
            Route route = getRoute(stop.getTripId());
            routes.add(route);
        }

        // Add first stop to path
        ArrayList<PathStop> stops = new ArrayList<PathStop>();
        ArrayList<Route> associatedRoutes = new ArrayList<Route>();
        stops.add(result.getStops().get(0));
        associatedRoutes.add(routes.get(1));

        // Add first stop of each distinct route
        String currentRouteId = routes.get(1).getRouteId();
        for (int i = 2; i < result.getStops().size(); i++) {
            PathTransferStop stop = result.getStops().get(i);
            if (!routes.get(i).getRouteId().equals(currentRouteId)) {
                stops.add(stop);
                associatedRoutes.add(routes.get(i));
                currentRouteId = routes.get(i).getRouteId();
            }
        }

        path.setStops(stops);
        path.setRoutes(associatedRoutes);

        // Divide into tripIds
        ArrayList<PathTransferStop> allStops = result.getStops();
        ArrayList<ArrayList<PathTransferStop>> dividedIntoTripIds = new ArrayList<ArrayList<PathTransferStop>>();
        ArrayList<PathTransferStop> currentTripIds = new ArrayList<PathTransferStop>();
        currentTripIds.add(allStops.get(0));
        currentTripIds.add(allStops.get(1));
        for (int i = 2; i < allStops.size(); i++) {
            if (allStops.get(i).getTripId().equals(allStops.get(i - 1).getTripId())) {
                currentTripIds.add(allStops.get(i));
            } else {
                dividedIntoTripIds.add(currentTripIds);
                currentTripIds = new ArrayList<PathTransferStop>();
                currentTripIds.add(allStops.get(i));
            }
        }
        dividedIntoTripIds.add(currentTripIds);

        // Get shapes coordinates
        for (ArrayList<PathTransferStop> tripStops : dividedIntoTripIds) {
            String tripId = tripStops.get(0).getTripId();
            if (tripId == null) {
                tripId = tripStops.get(1).getTripId();
            }
            PathTransferStop firstStop = tripStops.get(0);
            PathTransferStop lastStop = tripStops.get(tripStops.size() - 1);
            try {
                Connection connection = DatabaseConnection.getConnection();
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
                PreparedStatement getCoordinatesStatement = connection.prepareStatement(getCoordinatesSQL);
                getCoordinatesStatement.setString(1, tripId);
                getCoordinatesStatement.setString(2, tripId);
                getCoordinatesStatement.setString(3, firstStop.getStopId());
                getCoordinatesStatement.setString(4, tripId);
                getCoordinatesStatement.setString(5, lastStop.getStopId());

                ResultSet coordinatesResult = getCoordinatesStatement.executeQuery();
                while (coordinatesResult.next()) {
                    String lat = coordinatesResult.getString("shape_pt_lat");
                    String lon = coordinatesResult.getString("shape_pt_lon");
                    Coordinates c = new Coordinates(lat, lon);
                    path.addCoordinates(c, 1);
                }

            } catch (SQLException e) {
                e.printStackTrace();
            }

        }

        // Walk to toStop
        addWalkPath(path, toPostalCode, toStop);

        return path;
    }

    private void addWalkPath(Path path, String postalCode, Stop stop) {
        ResponsePath walkPath = GTFSEngine.walk(CoordHandler.getCoordinates(postalCode),
                stop.getCoordinates());
        ArrayList<Coordinates> walkCoordinates = Utils.pointListToArrayList(walkPath.getPoints());
        for (Coordinates c : walkCoordinates) {
            path.addCoordinates(c, 0);
        }
    }

    private String getStopName(String stopId) {

        String stopName = null;

        try {
            // Get DB connection
            Connection connection = DatabaseConnection.getConnection();

            // Query stop name
            String stopNameQuerySQL = """
                    SELECT
                        stop_name
                    FROM
                        stops
                    WHERE
                        stop_id = ?
                    """;
            PreparedStatement stopNameStatement = connection.prepareStatement(stopNameQuerySQL);
            stopNameStatement.setString(1, stopId);
            ResultSet stopNameResult = stopNameStatement.executeQuery();
            stopNameResult.next();
            stopName = stopNameResult.getString("stop_name");

            stopNameStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stopName;

    }

    private Route getRoute(String tripId) {

        Route route = null;

        try {
            // Get DB connection
            Connection connection = DatabaseConnection.getConnection();

            // Query stop name
            String routeIdSQL = """
                    SELECT
                        route_id,
                        route_short_name,
                        route_long_name
                    FROM
                        routes
                    WHERE
                        route_id = (
                            SELECT
                                route_id
                            FROM
                                trips
                            WHERE
                                trip_id = ?
                        )
                    """;

            PreparedStatement routeIdStatement = connection.prepareStatement(routeIdSQL);
            routeIdStatement.setString(1, tripId);
            ResultSet routeIdResult = routeIdStatement.executeQuery();
            routeIdResult.next();
            String routeId = routeIdResult.getString("route_id");
            String routeShortName = routeIdResult.getString("route_short_name");
            String routeLongName = routeIdResult.getString("route_long_name");
            route = new Route(routeId, routeShortName, routeLongName);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return route;

    }

}
