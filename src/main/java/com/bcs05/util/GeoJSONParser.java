package com.bcs05.util;

import java.util.ArrayList;

public class GeoJSONParser {

    /**
     * Parses GeoJSON content into a list of GeoJSONObject objects
     * 
     * @param geoJsonContent the GeoJSON content to be parsed
     * @return an ArrayList of GeoJSONObject objects parsed from the GeoJSON content
     */
    public static ArrayList<GeoJSONObject> parseGeoJSON(String geoJsonContent) {
        ArrayList<GeoJSONObject> jsonArray = new ArrayList<>();
        ArrayList<String> jsonContent = splitGeoJSON(geoJsonContent);

        // Iterate through the JSON content and create the GeoJSONObject objects
        for (int i = 3; i < jsonContent.size(); i += 3) {
            String json = jsonContent.get(i);

            String type = getType(json);
            ArrayList<String> properties = getProperties(json);
            Coordinates coords = getCoordinates(jsonContent.get(i + 1));

            GeoJSONObject jsonObject = new GeoJSONObject(type, properties, coords); 
            jsonArray.add(jsonObject);
        }
        return jsonArray; 
    }

/**
 * Splits the GeoJSON content into individual JSON strings
 * 
 * @param geoJsonContent the GeoJSON content to split
 * @return an ArrayList containing individual JSON strings
 */
    public static ArrayList<String> splitGeoJSON(String geoJsonContent) {
        geoJsonContent = geoJsonContent.trim();
        geoJsonContent = geoJsonContent.substring(1, geoJsonContent.length() - 1);
        ArrayList<String> jsonContent = new ArrayList<>();
        String[] parts = geoJsonContent.split("},");

        // Process each part and add it to the jsonContent list
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (i == 0) {
                part = part.substring(1); // remove the start "{"
            }
            if (i == part.length() - 1) {
                part = part.substring(0, part.length() - 1); // remove the end "}"
            }
            jsonContent.add(part);
        }
        return jsonContent;
    }

    /**
     * Extracts the type from a JSON String
     * 
     * @param json the JSON string from which the type will be extracted
     * @return the type extracted from the JSON string
     */
    public static String getType(String json) {
        String[] parts = json.split(":");
        String[] parts2 = parts[1].split(",");
        String type = parts2[0].trim();
        return type;
    }

    /**
     * Extracts properties from a JSON string
     * 
     * @param json the JSON string from which the properties will be extracted
     * @return an ArrayList containing properties extracted from the Json String
     */
    public static ArrayList<String> getProperties(String json) {
        ArrayList<String> properties = new ArrayList<>();
        String[] parts = json.split(",");
        parts[1] = parts[1].replace("\"properties\": {","");

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            properties.add(part);
        }

        return properties;
    }

    /**
     * Extracts coordinates from a JSON string and creates a Coordinate object
     * 
     * @param json the JSON string from which the coordinates will be extracted
     * @return a Coordinate object created using the extracted coordinated from the JSON sting
     */
    public static Coordinates getCoordinates(String json) {
        String[] parts = json.split(",");
        String lon = (parts[1].replace("\"coordinates\": [", "")).trim();
        String lat = (parts[2].replace("]", "")).trim();
        Coordinates coords = new Coordinates(lat, lon);
        return coords;
    }
    
}