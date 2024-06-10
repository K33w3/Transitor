package com.bcs05.util;

/**
 * The Coordinates class represents a set of geographic coordinates and a
 * postcode.
 * It provides methods to get and set the latitude, longitude, and postcode.
 */
public class Coordinates {
    private String latitude;
    private String longitude;
    private String postcode;
    private int displayX;
    private int displayY;

    public Coordinates(String latitude, String longitude, String postcode) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.postcode = postcode;
    }

    public Coordinates(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.postcode = null;
    }

    /**
     * Gets the latitude.
     *
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Sets the latitude.
     *
     * @param latitude the latitude to set
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets the display x-coordinate.
     *
     * @return the display x-coordinate
     */
    public int getDisplayX() {
        return displayX;
    }

    /**
     * Sets the display x-coordinate.
     *
     * @param displayX the display x-coordinate to set
     */
    public void setDisplayX(int displayX) {
        this.displayX = displayX;
    }

    /**
     * Gets the display y-coordinate.
     *
     * @return the display y-coordinate
     */
    public int getDisplayY() {
        return displayY;
    }

    /**
     * Sets the display y-coordinate.
     *
     * @param displayY the display y-coordinate to set
     */
    public void setDisplayY(int displayY) {
        this.displayY = displayY;
    }

    /**
     * Gets the longitude.
     *
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Sets the longitude.
     *
     * @param longitude the longitude to set
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets the postcode.
     *
     * @return the postcode
     */
    public String getPostcode() {
        return postcode;
    }

    /**
     * Sets the postcode.
     *
     * @param postcode the postcode to set
     */
    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * Returns a string representation of the Coordinates object.
     *
     * @return a string representation of the object
     */
    @Override
    public String toString() {
        return "Coordinates{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", postcode='" + postcode + '\'' +
                '}';
    }
}
