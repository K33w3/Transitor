package com.bcs05.util;

/**
 * Represents a stop along a transfer path, extending the PathStop class
 * with an additional trip ID field
 */
public class PathTransferStop extends PathStop {

    // The trip ID associated with this transfer stop
    private String tripId;

    /**
     * Constructs a new PathTransferStop object with the specified stop ID, coordinates, name,
     * departure time and trip ID
     * 
     * @param stop_id the ID of the stop
     * @param coordinates The coordinates of the stop
     * @param name The name of the stop
     * @param departureTime The departure time of the stop
     * @param tripId The trip ID associated with the stop
     */
    public PathTransferStop(String stop_id, Coordinates coordinates, String name, String departureTime, String tripId) {

        // Calls the constructor of the superclass PathStop
        super(stop_id, coordinates, name, departureTime);
        this.tripId = tripId;
    }

    /**
     * Retrieves the trip ID associated with this transfer stop
     * 
     * @return The trip ID associated with this transfer stop
     */
    public String getTripId() {
        return tripId;
    }

    /**
     * Sets the trip ID associated with this transfer stop
     * 
     * @param tripId the trip ID to set
     */
    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

}
