package com.bcs05.util;

import java.time.LocalTime;
import java.util.ArrayList;

public class BusTransferResult {

    // The list of stops in the transfer path
    private ArrayList<PathTransferStop> stops;

    //The arrival time at the final stop
    private LocalTime arrivalTime;

    /**
     * Constructs a new BusTransferResult with the given stops and arrival time
     * 
     * @param stops the list of stops in the transfer path
     * @param arrivalTime the arrival time at the final stop
     */
    public BusTransferResult(ArrayList<PathTransferStop> stops, LocalTime arrivalTime) {
        this.stops = stops;
        this.arrivalTime = arrivalTime;
    }

    /**
     * Gets the list of stops in the transfer path
     * 
     * @return an ArrayList of PathTransferStop obejcts representing the stops
     */
    public ArrayList<PathTransferStop> getStops() {
        return stops;
    }

    /**
     * Gets the arrival time at the final stop
     * 
     * @return the arrival time as a LocalTime object
     */
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }
}
