package com.bcs05.util;

/**
 * Represents a stop in a transportation system.
 */
public class Stop {

    private String stopId;
    private Coordinates coordinates;

    /**
     * Constructs a stop with the specified stop ID.
     *
     * @param stopId the stop ID
     */
    public Stop(String stopId) {
        this.stopId = stopId;
        this.coordinates = null;
    }

    /**
     * Constructs a stop with the specified stop ID and coordinates.
     *
     * @param stopId      the stop ID
     * @param coordinates the coordinates of the stop
     */
    public Stop(String stopId, Coordinates coordinates) {
        this.stopId = stopId;
        this.coordinates = coordinates;
    }

    /**
     * Returns the stop ID.
     *
     * @return the stop ID
     */
    public String getStopId() {
        return stopId;
    }

    /**
     * Returns the coordinates of the stop.
     *
     * @return the coordinates of the stop
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Compares this stop to the specified object. The result is true if and only if
     * the argument is not null and is a
     * Stop object with the same stop ID as this stop.
     *
     * @param o the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null)
            return false;
        Stop stop = (Stop) o;

        return stopId.equals(stop.getStopId());
    }

    /**
     * Returns a string representation of the stop.
     *
     * @return a string representation of the stop
     */
    @Override
    public String toString() {
        return "Stop ID: " + stopId;
    }

    /**
     * Returns the hash code value for the stop.
     *
     * @return the hash code value for the stop
     */
    @Override
    public int hashCode() {
        return stopId.hashCode();
    }

}
