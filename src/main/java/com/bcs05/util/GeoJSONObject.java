package com.bcs05.util;

import java.util.ArrayList;

public class GeoJSONObject {
    private String type;
    private ArrayList<String> properties = new ArrayList<String>();
    private Coordinates coords;

    public GeoJSONObject(String type, ArrayList<String> properties, Coordinates coords) {
        this.type = type;
        this.properties = properties;
        this.coords = coords;
    }

    public String getType() {
        return type;
    }

    public ArrayList<String> getProperties() {
        return properties;
    }

    public Coordinates getCoords() {
        return coords;
    }

    public void appendProperties(String property) {
        properties.add(property);
    }
}