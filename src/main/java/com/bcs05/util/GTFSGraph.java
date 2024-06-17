package com.bcs05.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
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

    public GTFSGraph() {
        adjacencyList = new HashMap<Stop, LinkedList<GTFSWeightedEdge>>();
        createGraph();
    }

    public void createGraph() {

        try {
            // Get database connection
            Connection connection = DatabaseConnection.getConnection();

            // Query outgoing stops for each stop
            String outGoingStopsSQL = """
                    SELECT
                    	from_stop_id, to_stop_id, departure_time, travel_time
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
                LocalTime departureTime = outGoingStops.getTime("departure_time").toLocalTime();
                int travelTime = outGoingStops.getInt("travel_time");

                Stop fromStop = new Stop(fromStopId);
                Stop toStop = new Stop(toStopId);

                GTFSWeightedEdge edge = new GTFSWeightedEdge(toStop, departureTime, travelTime);

                if (!adjacencyList.containsKey(fromStop)) {
                    adjacencyList.put(fromStop, new LinkedList<>());
                }

                adjacencyList.get(fromStop).add(edge);
            }

            outGoingStops.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public LinkedList<Stop> getStops() {
        return new LinkedList<Stop>(adjacencyList.keySet());
    }

    public LinkedList<GTFSWeightedEdge> getNeighbours(Stop stop, LocalTime departureTime) {
        LinkedList<GTFSWeightedEdge> neighbours = new LinkedList<GTFSWeightedEdge>();
        LinkedList<GTFSWeightedEdge> edges = adjacencyList.get(stop);
        for (GTFSWeightedEdge edge : edges) {
            if (edge.getDepartureTime().isAfter(departureTime)) {
                neighbours.add(edge);
            }
        }
        return neighbours;
    }

}
