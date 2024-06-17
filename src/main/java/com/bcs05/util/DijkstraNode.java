package com.bcs05.util;

/**
 * Represent a node used in the Dijkstra algorithm
 * Each node consists of a stop and the distance from the previous node
 */
public class DijkstraNode {

    // The stop the node represents
    private Stop stop;

    // The travel time from the previous node
    private int travelTime;

    /**
     * Constructs a new DijkstraNode object with the specified stop and travel time
     * 
     * @param stop       The stop represented by this node
     * @param travelTime The travel time from the previous node to this node
     */
    public DijkstraNode(Stop stop, int travelTime) {
        this.stop = stop;
        this.travelTime = travelTime;
    }

    /**
     * Constructs a new DijkstraNode object with the specified weighted edge
     * 
     * @param edge the weighted edge that connects this node to the previous one
     */
    public DijkstraNode(GTFSWeightedEdge edge) {
        stop = edge.getStop();
        travelTime = edge.getTravelTime();
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
     * Retrieves the travel time from the previous node to this node
     * 
     * @return The travel time
     */
    public int getTravelTime() {
        return travelTime;
    }

}
