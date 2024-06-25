package com.bcs05.util;

/**
 * Represents a stop along a path.
 */
public class PathStop extends Stop {

    private String departureTime;
    private String name;

    /**
     * Constructs a new PathStop object with the specified stop ID, coordinates,
     * name, and departure time.
     *
     * @param stop_id       the ID of the stop
     * @param coordinates   the coordinates of the stop
     * @param name          the name of the stop
     * @param departureTime the departure time of the stop
     */
    public PathStop(String stop_id, Coordinates coordinates, String name, String departureTime) {
        super(stop_id, coordinates);
        this.name = name;
        this.departureTime = departureTime;
    }

    /**
     * Gets the name of the stop.
     *
     * @return the name of the stop
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the departure time of the stop.
     *
     * @return the departure time of the stop
     */
    public String getDepartureTime() {
        return departureTime;
    }

}
