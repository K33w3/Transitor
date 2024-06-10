package com.bcs05.util;

/**
 * Represents a set of coordinates along a path, including the shape distance
 * traveled.
 */
public class PathCoordinates extends Coordinates {

    private int shapeDistTraveled;

    /**
     * Constructs a new PathCoordinates object with the specified latitude,
     * longitude, and shape distance traveled.
     *
     * @param latitude          the latitude of the coordinates
     * @param longitude         the longitude of the coordinates
     * @param shapeDistTraveled the shape distance traveled
     */
    public PathCoordinates(String latitude, String longitude, int shapeDistTraveled) {
        super(latitude, longitude);
        this.shapeDistTraveled = shapeDistTraveled;
    }

    /**
     * Gets the shape distance traveled.
     *
     * @return the shape distance traveled
     */
    public int getShapeDistTraveled() {
        return shapeDistTraveled;
    }

}
