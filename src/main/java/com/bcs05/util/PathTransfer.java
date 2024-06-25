package com.bcs05.util;

import java.util.ArrayList;

/**
 * Represents a path with multiple stops and multiple routes for transferring. 
 * Extends the basic path class
 */
public class PathTransfer extends Path {

    // List of routes for transferring
    private ArrayList<Route> routes;

    /**
     * Constructs a new empty PathTransfer object with an empty list of routes 
     */
    public PathTransfer() {

        // Calls the default constructor of the superclass Path
        super();
        this.routes = new ArrayList<Route>();
    }

    /**
     * Constructs a new PathTransfer object with the specified path 
     * coordinates, stops and routes for transferring 
     * 
     * @param path The list of path coordinates
     * @param stops The list of stops along the path
     * @param routes The list of routes for transferring
     */
    public PathTransfer(ArrayList<PathCoordinates> path, ArrayList<PathStop> stops, ArrayList<Route> routes) {

        // Calls the constructor of the superclass Path with path coordinates and stops
        super(path, stops);
        this.routes = routes;
    }

    /**
     * Retrieves the list of routes for transferring
     * 
     * @return the list of routes for transferring
     */
    public ArrayList<Route> getRoutes() {
        return routes;
    }

    /**
     * Sets the list of routes for transferring
     * 
     * @param routes the list of routes for transferring to set
     */
    public void setRoutes(ArrayList<Route> routes) {
        this.routes = routes;
    }

}
