package com.bcs05.util;

/**
 * Represents a public transport route
 */
public class Route {

    private String routeId;
    private String routeShortName; // bus number
    private String routeLongName;

    /**
     * Constructs a new route object with the specified attributes
     * 
     * @param routeId The unique identifier for the route 
     * @param routeShortName The short name or number of the route 
     * @param routeLongName The long name of the route 
     */
    public Route(String routeId, String routeShortName, String routeLongName) {
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
    }

    /**
     * Retrieves the unique identifier of the route 
     * 
     * @return the Route's unique identifier
     */
    public String getRouteId() {
        return routeId;
    }

    /**
     * Retrieves the short name or number of the route 
     * 
     * @return The route's short name or number 
     */
    public String getRouteShortName() {
        return routeShortName;
    }

    /**
     * Retrieves the long name of the route
     * 
     * @return The route's long name
     */
    public String getRouteLongName() {
        return routeLongName;
    }

    /**
     * Returns a string representation of the route, using its ID
     * 
     * @return The string representation of the route 
     */
    @Override
    public String toString() {
        return routeId;
    }

}
