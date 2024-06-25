package com.bcs05.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class GTFSGraph {

    public static void main(String[] args) {
        GTFSGraph graph = new GTFSGraph();
        System.out.println("Graph created");
        // Check outgoing edges for 2577952
        Stop stop = new Stop("2577952");
        LinkedList<GTFSWeightedEdge> edges = graph.adjacencyList.get(stop);
        System.out.println("Outgoing edges for stop " + stop.getStopId() + ": ");
        for (GTFSWeightedEdge edge : edges) {
            System.out.println(edge.getStop().getStopId() + " " + edge.getDepartureTime() + " " + edge.getTravelTime());
        }
    }

    private HashMap<Stop, LinkedList<GTFSWeightedEdge>> adjacencyList;
    private static GTFSGraph instance;

    /**
     * Creates an instance of GTFSGraph
     */
    public static void createInstance() {
        instance = new GTFSGraph();
    }

    /**
     * Retrieves the instance of GTFSGraph
     * 
     * @return the instance of GTFSGraph
     */
    public static GTFSGraph getInstance() {
        if (instance == null) {
            instance = new GTFSGraph();
        }
        return instance;
    }

    /**
     * Private Constructor to initialize the graph and populate the adjacency list using data from the database
     */
    private GTFSGraph() {
        adjacencyList = new HashMap<Stop, LinkedList<GTFSWeightedEdge>>();
        createGraph();
    }

    /**
     * Creates the graph by populating the adjacency list from the timetable data in the database
     */
    private void createGraph() {

        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();

            // Query outgoing stops for each stop
            String outGoingStopsSQL = """
                    SELECT
                    	from_stop_id, to_stop_id, departure_time, travel_time, trip_id
                    FROM
                    	timetable
                    ORDER BY
                    	from_stop_id, to_stop_id, departure_time, travel_time;
                                        """;
            PreparedStatement outGoingStopsStatement = connection.prepareStatement(outGoingStopsSQL);
            ResultSet outGoingStops = outGoingStopsStatement.executeQuery();

            // Populate adjacency list
            while (outGoingStops.next()) {
                String fromStopId = outGoingStops.getString("from_stop_id");
                String toStopId = outGoingStops.getString("to_stop_id");
                int travelTime = outGoingStops.getInt("travel_time");
                LocalTime departureTime = outGoingStops.getTime("departure_time").toLocalTime();
                LocalTime arrivalTime = departureTime.plusSeconds(travelTime);
                String tripId = outGoingStops.getString("trip_id");

                Stop fromStop = new Stop(fromStopId);
                Stop toStop = new Stop(toStopId);

                GTFSWeightedEdge edge = new GTFSWeightedEdge(tripId, toStop, departureTime, arrivalTime, travelTime);

                if (!adjacencyList.containsKey(fromStop)) {
                    adjacencyList.put(fromStop, new LinkedList<>());
                }

                adjacencyList.get(fromStop).add(edge);
            }

            addEndingStopsWithNoOutgoingEdges(connection);

            outGoingStops.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    /**
     * Adds ending stops that do not have outgoing edges to the adjacency list
     * 
     * @param connection the database connection
     * @throws SQLException if an error occurs with accessing the database
     */
    private void addEndingStopsWithNoOutgoingEdges(Connection connection) throws SQLException {
        // Get ending stops that don't have outgoing edges
        String endingStopsSQL = """
                SELECT DISTINCT
                	to_stop_id
                FROM
                	timetable
                WHERE
                	to_stop_id NOT IN (SELECT DISTINCT from_stop_id FROM timetable);
                                """;
        PreparedStatement endingStopsStatement = connection.prepareStatement(endingStopsSQL);
        ResultSet endingStops = endingStopsStatement.executeQuery();
        while (endingStops.next()) {
            Stop stop = new Stop(endingStops.getString("to_stop_id"));
            adjacencyList.put(stop, new LinkedList<>());
        }
    }

    /**
     * Retrieves all stops in the graph
     * 
     * @return a LinkedList of all stops in the graph
     */
    public LinkedList<Stop> getStops() {
        return new LinkedList<Stop>(adjacencyList.keySet());
    }

    /**
     * Retrieves neighbouring edges (departures) from a given stop and departure time
     * 
     * @param stop the stop to find the neighbours for
     * @param departureTime the departure time from the stop
     * @return a LinkedList of GTFSWeightedEdge representing neighbouring edges
     */
    public LinkedList<GTFSWeightedEdge> getNeighbours(Stop stop, LocalTime departureTime) {
        LinkedList<GTFSWeightedEdge> neighbours = new LinkedList<GTFSWeightedEdge>();
        LinkedList<GTFSWeightedEdge> edges = adjacencyList.get(stop);

        // sort edges by arrival time
        edges.sort((GTFSWeightedEdge e1, GTFSWeightedEdge e2) -> e1.getArrivalTime().compareTo(e2.getArrivalTime()));

        if (edges == null || edges.isEmpty()) {
            return neighbours;
        }

        ArrayList<Stop> stopsUsed = new ArrayList<Stop>();
        for (GTFSWeightedEdge edge : edges) {
            if (!edge.getDepartureTime().isBefore(departureTime) && !stopsUsed.contains(edge.getStop())) {
                neighbours.add(edge);
                stopsUsed.add(edge.getStop());
            }
        }

        return neighbours;
    }

}
