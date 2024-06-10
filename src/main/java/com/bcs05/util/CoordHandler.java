package com.bcs05.util;

import com.bcs05.data.PostalCodeReader;
import com.bcs05.engine.DistanceCalculator;
import com.bcs05.engine.TimeCalculator;
import java.math.BigDecimal;

public class CoordHandler {

    private static PostalCodeReader postReader = new PostalCodeReader();

    public static Coordinates getCoordinates(String postcode) {
        return postReader.getCoordinates(postcode);
    }

    /**
     * used to get the time it takes from one postal code to another
     * 
     * @param lat1 The latitude of the starting point.
     * @param lon1 The longitude of the starting point.
     * @param lat2 The latitude of the destination point.
     * @param lon2 The longitude of the destination point.
     * @param mode the type of transportation being calculated for
     * @return the time it takes calculated using the TimeCalculator class
     */
    public static BigDecimal getTime(double lat1, double lon1, double lat2, double lon2, String mode) {
        new TimeCalculator(BigDecimal.valueOf(lat1), BigDecimal.valueOf(lon1), BigDecimal.valueOf(lat2),
                BigDecimal.valueOf(lon2));

        switch (mode) {
            case "Foot":
                return TimeCalculator.walkTime(BigDecimal.valueOf(lat1), BigDecimal.valueOf(lon1),
                        BigDecimal.valueOf(lat2), BigDecimal.valueOf(lon2));
            case "Bike":
                return TimeCalculator.cycleTime(BigDecimal.valueOf(lat1), BigDecimal.valueOf(lon1),
                        BigDecimal.valueOf(lat2), BigDecimal.valueOf(lon2));
            default:
                return BigDecimal.ZERO;
        }
    }

    /**
     * Used to get the distance between two postal codes
     * 
     * @param lat1 The latitude of the starting point.
     * @param lon1 The longitude of the starting point.
     * @param lat2 The latitude of the destination point.
     * @param lon2 The longitude of the destination point.
     * @return the distance between two postal codes using the
     *         DistanceCalculatorClass
     */
    public static BigDecimal getDistance(double lat1, double lon1, double lat2, double lon2) {
        new DistanceCalculator(BigDecimal.valueOf(lat1), BigDecimal.valueOf(lon1), BigDecimal.valueOf(lat2),
                BigDecimal.valueOf(lon2));
        return DistanceCalculator.calculateAerialDistance(BigDecimal.valueOf(lat1), BigDecimal.valueOf(lon1),
                BigDecimal.valueOf(lat2), BigDecimal.valueOf(lon2));
    }

}
