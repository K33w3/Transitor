package com.bcs05.util;

import java.util.ArrayList;
import com.bcs05.engine.RoutingEngine;

/**
 * The RouteHandler class provides methods for handling routes and calculating
 * distances.
 */
public class RouteHandler {

    /**
     * Retrieves a list of coordinates for a given route.
     *
     * @param mode the mode of transportation (e.g., "walk", "cycle")
     * @param from the starting coordinates
     * @param to   the destination coordinates
     * @return the list of coordinates for the route
     */
    public static ArrayList<Coordinates> getPointsListRoute(String mode, Coordinates from, Coordinates to) {
        RoutingEngine routingEngine = new RoutingEngine(modeSwitch(mode));
        return routingEngine.getPoints(from, to);
    }

    /**
     * Calculates the distance between two sets of coordinates for a given route.
     *
     * @param mode the mode of transportation (e.g., "walk", "cycle")
     * @param from the starting coordinates
     * @param to   the destination coordinates
     * @return the distance between the coordinates
     */
    public static double getDistanceRoute(String mode, Coordinates from, Coordinates to) {
        RoutingEngine routingEngine = new RoutingEngine(modeSwitch(mode));
        return routingEngine.getDistance(from, to).doubleValue();
    }

    /**
     * Converts the mode of transportation to the corresponding Transportation enum
     * value.
     *
     * @param mode the mode of transportation (e.g., "walk", "cycle")
     * @return the corresponding Transportation enum value
     */
    private static Transportation modeSwitch(String mode) {
        switch (mode) {
            case "walk":
                return Transportation.FOOT;
            case "cycle":
                return Transportation.BIKE;
            default:
                return Transportation.FOOT;
        }
    }
}
