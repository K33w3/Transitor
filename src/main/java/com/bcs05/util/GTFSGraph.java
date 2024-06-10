package com.bcs05.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalTime;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

/**
 * Represents a graph of stops and connections in a GTFS dataset
 * This graph is used for route planning
 */
public class GTFSGraph {

    // adjacency list representing the graph
    private HashMap<Stop, LinkedList<GTFSWeightedEdge>> adjacencyList;

    public static void main(String[] args) {
        GTFSGraph graph = new GTFSGraph();
        Stop stop = new Stop("2578364");
        LinkedList<GTFSWeightedEdge> outgoingStops = graph.getOutgoingStops(stop);

        System.out.println("Outgoing stops for 2578364:");
        for (GTFSWeightedEdge e : outgoingStops) {
            System.out.println(e.getStop());
            System.out.println(e.getTravelTime());
            System.out.println(e.getDepartureTime());
            System.out.println();
        }
    }

    /**
     * Constructs a new GTFSGraph object and creates the graph
     */
    public GTFSGraph() {
        adjacencyList = new HashMap<Stop, LinkedList<GTFSWeightedEdge>>();
        createGraph();
    }

    /**
     * Adds a vertex (stop) to the Graph
     * 
     * @param stop the stop to be added as a vertex
     */
    public void addVertex(Stop stop) {
        adjacencyList.put(stop, new LinkedList<GTFSWeightedEdge>());
    }

    /**
     * Adds an edge (connection) between two stops in the graph
     * 
     * @param fromStop      The starting stop of the edge
     * @param toStop        The ending stop of the edge.
     * @param distance      the distance between the two stops
     * @param departureTime the time that the bus leaves from the starting stop
     */
    public void addEdge(Stop fromStop, Stop toStop, int distance, LocalTime departureTime) {
        GTFSWeightedEdge weightedEdge = new GTFSWeightedEdge(toStop, distance, departureTime);
        adjacencyList.get(fromStop).add(weightedEdge);
    }

    /**
     * Retrieves the list of outgoing stops from the given stop
     * 
     * @param stop The stop for which the outgoing stops are to be retrieved
     * @return the list of all the outgoing Stops from the given stop
     */
    public LinkedList<GTFSWeightedEdge> getOutgoingStops(Stop stop) {
        return adjacencyList.get(stop);
    }

    /**
     * Retrieves all the stops in the graph
     * 
     * @return a set containing all the Stops in the graph
     */
    public Set<Stop> getStops() {
        return adjacencyList.keySet();
    }

    /**
     * Creates the graph based on timetable data fetched from the database
     */
    public void createGraph() {
        try {
            // Get DB connection
            Connection connection = DatabaseConnection.getConnection();

            // Get distinct stops
            String distinctStopsQuerySQL = """
                    SELECT DISTINCT stop_id
                    FROM stops;
                        """;
            PreparedStatement distinctStopsStatement = connection.prepareStatement(distinctStopsQuerySQL);
            ResultSet stops = distinctStopsStatement.executeQuery();

            // Add stops to graph
            while (stops.next()) {
                String stop_id = stops.getString("stop_id");
                Stop stop = new Stop(stop_id);
                addVertex(stop);
            }
            distinctStopsStatement.close();
            // Add edges
            for (Stop stop : adjacencyList.keySet()) {
                // Get outgoing stops
                String outgoingStopsQuerySQL = """
                            SELECT DISTINCT
                            t.to_stop_id,
                            t.travel_time,
                            t.departure_time AS earliest_departure_time
                        FROM
                            timetable t
                        WHERE
                            t.departure_time = (
                                SELECT MIN(tt.departure_time)
                                FROM timetable tt
                                WHERE tt.to_stop_id = t.to_stop_id
                                      AND tt.from_stop_id = ?
                                      AND tt.departure_time >= ?
                            )
                            AND t.from_stop_id = ?
                            AND t.departure_time >= ?;
                        """.formatted(stop.getStopId(), LocalTime.now().toString(), stop.getStopId(),
                        LocalTime.now().toString());

                PreparedStatement outgoingStopsStatement = connection.prepareStatement(outgoingStopsQuerySQL);

                ResultSet outgoingStops = outgoingStopsStatement.executeQuery();

                // Process your ResultSet
                while (outgoingStops.next()) {
                    // Extract data from the ResultSet
                    String toStopId = outgoingStops.getString("to_stop_id");
                    Stop toStop = new Stop(toStopId);
                    String travelTime = outgoingStops.getString("travel_time");
                    String earliestDepartureTime = outgoingStops.getString("earliest_departure_time");
                    // Process/display the data as needed
                    addEdge(stop, toStop, Integer.parseInt(travelTime), LocalTime.parse(earliestDepartureTime));
                }

                outgoingStopsStatement.close();

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<Stop> getKNearestStops(String postalCode, int k) {
        // Convert postal code to coordinates
        Coordinates coordinates = CoordHandler.getCoordinates(postalCode);

        // Initiate nearest stops list
        ArrayList<Stop> nearestStops = new ArrayList<Stop>();

        try {
            // Get DB connection
            Connection connection = DatabaseConnection.getConnection();

            // Get k nearest stops
            String getKNearestStopsSQL = """
                    SELECT
                        stop_id,
                        SQRT(POW((stop_lat - %s), 2) + POW((stop_lon - %s), 2)) AS distance
                    FROM
                        stops
                    ORDER BY
                        distance
                    LIMIT
                        %s;
                            """
                    .formatted(Double.valueOf(coordinates.getLatitude()), Double.valueOf(coordinates.getLongitude()),
                            k);
            PreparedStatement getKNearestStopsStatement = connection.prepareStatement(getKNearestStopsSQL);
            ResultSet kNearestStops = getKNearestStopsStatement.executeQuery();

            // Add nearest stops for list
            while (kNearestStops.next()) {
                String stopId = kNearestStops.getString("stop_id");
                nearestStops.add(new Stop(stopId));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return nearestStops;
    }

}
