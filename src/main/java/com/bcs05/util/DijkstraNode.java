package com.bcs05.util;

/**
 * Represent a node used in the Dijkstra algorithm
 * Each node consists of a stop and the distance from the previous node
 */
public class DijkstraNode {

    // The stop the node represents
    private Stop stop;

    // The distance from the previous node
    private int distance;

    /**
     * Constructs a new DijkstraNode object with the specified stop and distance
     * 
     * @param stop     The stop represented by this node
     * @param distance The distance from the previous node to this node
     */
    public DijkstraNode(Stop stop, int distance) {
        this.stop = stop;
        this.distance = distance;
    }

    /**
     * Constructs a new DijkstraNode object with the specified weighted edge
     * 
     * @param edge the weighted edge that connects this node to the previous one
     */
    public DijkstraNode(GTFSWeightedEdge edge) {
        stop = edge.getStop();
        distance = edge.getTravelTime();
    }

    /**
     * Retrieves the stop represented by this node
     * 
     * @return The Stop
     */
    public Stop getStop() {
        return stop;
    }

    /**
     * Retrieves the distance from the previous node to this node
     * 
     * @return The Distance
     */
    public int getDistance() {
        return distance;
    }

}
