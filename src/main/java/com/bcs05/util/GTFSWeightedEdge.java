package com.bcs05.util;

import java.time.LocalTime;

/**
 * Represents a weighted edge in a GTFS graph, connecting two stops.
 * the weight of the edge is the travel time between the stops, and it includes
 * the departure time
 */
public class GTFSWeightedEdge {

    // The trip that the edge is associated with
    private String tripId;

    // The stop that the edge is associated with
    private Stop stop;

    // The time that the bus leaves from the stop associated with this edge
    private LocalTime departureTime;

    // The time that the bus arrives at the stop associated with this edge
    private LocalTime arrivalTime;

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
    public GTFSWeightedEdge(String tripId, Stop stop, LocalTime departureTime, LocalTime arrivalTime, int travelTime) {
        this.tripId = tripId;
        this.stop = stop;
        this.travelTime = travelTime;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
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
     * Retrieves the arrival time at the stop associated with this edge
     * 
     * @return the time that the Bus arrives at the stop assoicated with this edge
     */
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Retrieves the travel time for the distance of the edge
     * 
     * @return the time it takes to travel the distance of the edge
     */
    public int getTravelTime() {
        return travelTime;
    }

    /**
     * Retrieves the trip id associated with the edge
     * 
     * @return the trip id associated with the edge
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * Returns a string representation of the GTFSWeightedEdge object
     * 
     * @return a String representation containing trip ID, stop ID, departure time and travel time
     */
    @Override
    public String toString() {
        return "Trip: " + tripId + " Stop: " + stop.getStopId() + " Dep. " + departureTime + " tT: " + travelTime
                + "\n";
    }

}
