package com.bcs05.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GeoJSONParser {
    public static void parseGeoJSON(String geoJsonContent) {
        List<String> jsonContent = splitGeoJSON(geoJsonContent);
        for (int i = 3; i < jsonContent.size(); i += 3) {
            String json = jsonContent.get(i);
            String type = getType(json);
            System.out.println("Type: " + type);

            List<String> properties = getProperties(json);
            System.out.println("Properties: " + properties);

            Coordinates coords = getCoordinates(jsonContent.get(i + 1));
            System.out.println("Coordinates: " + coords.getLatitude() + " " + coords.getLongitude());
        }
    }

    public static List<String> splitGeoJSON(String geoJsonContent) {
        geoJsonContent = geoJsonContent.trim();
        geoJsonContent = geoJsonContent.substring(1, geoJsonContent.length() - 1);
        List<String> jsonContent = new ArrayList<>();
        String[] parts = geoJsonContent.split("},");
        System.out.println(parts.length);
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (i == 0) {
                part = part.substring(1); //remove the start {
            }
            if (i == part.length() - 1) {
                part = part.substring(0, part.length() - 1); //remove the end }
            }
            jsonContent.add(part);
        }
        return jsonContent;
    }

    public static String getType(String json) {
        String[] parts = json.split(":");
        String[] parts2 = parts[1].split(",");
        String type = parts2[0].trim();
        return type;
    }

    public static List<String> getProperties(String json) {
        List<String> properties = new ArrayList<>();
        String[] parts = json.split(",");
        parts[1] = parts[1].replace("\"properties\": {","");

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i].trim();
            properties.add(part);
        }

        return properties;
    }

    public static Coordinates getCoordinates(String json) {
        String[] parts = json.split(",");
        String lon = (parts[1].replace("\"coordinates\": [", "")).trim();
        String lat = (parts[2].replace("]", "")).trim();
        Coordinates coords = new Coordinates(lat, lon);
        return coords;
    }
    
}