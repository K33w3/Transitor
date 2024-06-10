package com.bcs05.util;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Represents a path consisting of multiple stops
 * It contains the list of stops along the path, the total time to travel the
 * entire path and the distance in meters
 */
public class Path {

    private ArrayList<PathStop> stops;
    private ArrayList<PathCoordinates> coordinates;
    private Duration time;

    // Total distance of the path in meters
    private int distanceInMeters;

    /**
     * Constructs a new path object with an empty list of stops and
     */
    public Path() {
        this.coordinates = new ArrayList<PathCoordinates>();
        this.time = null;
        this.distanceInMeters = 0;
    }

    public Path(ArrayList<PathCoordinates> path, ArrayList<PathStop> stops) {
        this.coordinates = path;
        this.time = null;
        this.stops = stops;
    }

    /**
     * Adds a coordinate to the path.
     * 
     * @param coordinate The coordinate to add to the path.
     */
    public void addCoordinates(Coordinates coordinate) {
        PathCoordinates pathCoordinate = new PathCoordinates(coordinate.getLatitude(), coordinate.getLongitude(), 0);
        coordinates.add(pathCoordinate);
    }

    /**
     * Adds a coordinate to the start of the path.
     * 
     * @param coordinate
     */
    public void addCoordinatesToStart(Coordinates coordinate) {
        PathCoordinates pathCoordinate = new PathCoordinates(coordinate.getLatitude(), coordinate.getLongitude(), 0);
        coordinates.add(0, pathCoordinate);
    }

    /**
     * Adds a coordinate to the end of the path.
     * 
     * @param coordinate
     */
    public void addCoordinates(PathCoordinates coordinate) {
        coordinates.add(coordinate);
    }

    /**
     * Retrieves the list of coordinates along the path.
     * 
     * @return The list of coordiantes along the path.
     */
    public ArrayList<PathCoordinates> getCoordinates() {
        return coordinates;
    }

    /**
     * Retrieves the list of stops along the path.
     * 
     * @return The list of stops along the path.
     */
    public ArrayList<PathStop> getStops() {
        return stops;
    }

    /**
     * Sets the list of stops along the path.
     * 
     * @param stops The list of stops along the path.
     */
    public void setStops(ArrayList<PathStop> stops) {
        this.stops = stops;
    }

    /**
     * Retrieves the total time to travel the entire path.
     * 
     * @return The time it takes to travel the entire path.
     */
    public Duration getTime() {
        return time;
    }

    /**
     * Sets the total time to travel the entire path.
     * 
     * @param time The time it takes to travel the entire path.
     */
    public void setTime(Duration time) {
        this.time = time;
    }

    /**
     * Retrieves the total distance of the path in meters.
     * 
     * @return The total distance of the path in meters.
     */
    public int getDistance() {
        return distanceInMeters;
    }

    /**
     * Sets the total distance of the path in meters.
     * 
     * @param distance The total distance of the path in meters.
     */
    public void setDistance(int distance) {
        distanceInMeters = distance;
    }

    /**
     * Prints out each coordinate in the path.
     */
    public void printPath() {
        for (Coordinates s : coordinates) {
            System.out.println(s);
        }
    }

}
