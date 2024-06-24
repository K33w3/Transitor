package com.bcs05.util;

public class PathTransferStop extends PathStop {

    private String tripId;

    public PathTransferStop(String stop_id, Coordinates coordinates, String name, String departureTime, String tripId) {
        super(stop_id, coordinates, name, departureTime);
        this.tripId = tripId;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

}
