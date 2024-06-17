package com.bcs05.util;

import java.time.LocalTime;

/**
 * Represents a weighted edge in a GTFS graph, connecting two stops.
 * the weight of the edge is the travel time between the stops, and it includes
 * the departure time
 */
public class GTFSWeightedEdge {

    // The stop that the edge is associated with
    private Stop stop;

    // The time that the bus leaves from the stop associated with this edge
    private LocalTime departureTime;

    // The time it takes to travel the distane of the edge
    private int travelTime;

    /**
     * Constructs a new GTFSWeightedEdge object witht the specific stop, travel time
     * and departure time
     * 
     * @param stop          The Stop that the edge is associated with
     * @param travelTime    The Time it takes to travel the distance of the edge
     * @param departureTime The Time that the bus leaves from the Stop
     */
    public GTFSWeightedEdge(Stop stop, LocalTime departureTime, int travelTime) {
        this.stop = stop;
        this.travelTime = travelTime;
        this.departureTime = departureTime;
    }

    /**
     * Retrieves the stop associated with the edge
     * 
     * @return the Stop associated with the edge
     */
    public Stop getStop() {
        return stop;
    }

    /**
     * Retrieves the departure time from the stop associated with this edge
     * 
     * @return the time that the Bus leaves the stop assoicated with this edge
     */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /**
     * Retrieves the travel time for the distance of the edge
     * 
     * @return the time it takes to travel the distance of the edge
     */
    public int getTravelTime() {
        return travelTime;
    }

}
