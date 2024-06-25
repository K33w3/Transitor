package com.bcs05.util;

import java.util.ArrayList;

/**
 * Represents a GeoJSON object with a type, properties and coordinates
 */
public class GeoJSONObject {
    private String type;
    private ArrayList<String> properties = new ArrayList<String>();
    private Coordinates coords;

    /**
     * Constructs a GeoJSONObject with the specified type, properties and coordinates
     * 
     * @param type the type of the GeoJSON object
     * @param properties the properties associated with the GeoJSON object
     * @param coords the coordinates of the GeoJSON object
     */
    public GeoJSONObject(String type, ArrayList<String> properties, Coordinates coords) {
        this.type = type;
        this.properties = properties;
        this.coords = coords;
    }

    /**
     * Retrieves the type of the GeoJson object
     * 
     * @return the type of the GeoJSON object
     */
    public String getType() {
        return type;
    }

    /**
     * Retrieves the properties associated with the GeoJSON object
     * 
     * @return the properties associated with the GeoJSON object
     */
    public ArrayList<String> getProperties() {
        return properties;
    }

    /**
     * Retrieves the coordinates of the GeoJSON object
     * 
     * @return the coordinates of the GeoJSON object
     */
    public Coordinates getCoords() {
        return coords;
    }

    /**
     * Appends a property to the list of properties associated with the GeoJSON object
     * 
     * @param property the property to be appended
     */
    public void appendProperties(String property) {
        properties.add(property);
    }
}