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

    private HashMap<Stop, LinkedList<GTFSWeightedEdge>> adjacencyList;
    private static GTFSGraph instance;

    public static void createInstance() {
        instance = new GTFSGraph();
    }

    public static GTFSGraph getInstance() {
        if (instance == null) {
            instance = new GTFSGraph();
        }
        return instance;
    }

    private GTFSGraph() {
        adjacencyList = new HashMap<Stop, LinkedList<GTFSWeightedEdge>>();
        createGraph();
    }

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

    public LinkedList<Stop> getStops() {
        return new LinkedList<Stop>(adjacencyList.keySet());
    }

    public LinkedList<GTFSWeightedEdge> getNeighbours(Stop stop, LocalTime departureTime) {
        LinkedList<GTFSWeightedEdge> neighbours = new LinkedList<GTFSWeightedEdge>();
        LinkedList<GTFSWeightedEdge> edges = adjacencyList.get(stop);

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
