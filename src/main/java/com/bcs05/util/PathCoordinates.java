package com.bcs05.util;

/**
 * Represents a set of coordinates along a path, including the shape distance
 * traveled.
 */
public class PathCoordinates extends Coordinates {

    private int shapeDistTraveled;
    private int type;
    private Integer busPathColorId;

    /**
     * Constructs a new PathCoordinates object with the specified latitude,
     * longitude, and shape distance traveled.
     *
     * @param latitude          the latitude of the coordinates
     * @param longitude         the longitude of the coordinates
     * @param shapeDistTraveled the shape distance traveled
     */
    public PathCoordinates(String latitude, String longitude, int shapeDistTraveled, int type) {
        super(latitude, longitude);
        this.shapeDistTraveled = shapeDistTraveled;
        this.type = type;
        busPathColorId = null;
    }

    /**
     * Gets the shape distance traveled.
     *
     * @return the shape distance traveled
     */
    public int getShapeDistTraveled() {
        return shapeDistTraveled;
    }

    /**
     * Gets the type of the coordinates
     * 
     * @return the type of the coordinates
     */
    public int getType() {
        return type;
    }

    /**
     * Gets the bus path color ID associated with the coordinates
     * 
     * @return the bus path color ID
     */
    public Integer getBusPathColorId() {
        return busPathColorId;
    }

    /**
     * Sets the bus path color ID associated with the coordinates
     * 
     * @param busPathColorId The bus path color ID to set
     */
    public void setBusPathColorId(Integer busPathColorId) {
        this.busPathColorId = busPathColorId;
    }
}
