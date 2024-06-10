package com.bcs05.engine;

import java.math.BigDecimal;
import java.math.MathContext;

/**
 * The DistanceCalculator class calculates the aerial distance between two
 * points on Earth
 * using the Haversine formula.
 */
public class DistanceCalculator {
    private static final BigDecimal EARTH_RADIUS_KM = new BigDecimal("6371.0"); // Earth's radius in kilometers
    private BigDecimal lat1;
    private BigDecimal lon1;
    private BigDecimal lat2;
    private BigDecimal lon2;

    /**
     * Constructs a DistanceCalculator object with the specified latitude and
     * longitude coordinates.
     *
     * @param lat1 The latitude of the first point.
     * @param lon1 The longitude of the first point.
     * @param lat2 The latitude of the second point.
     * @param lon2 The longitude of the second point.
     */
    public DistanceCalculator(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
    }

    /**
     * Calculates the aerial distance between two points on Earth using the
     * Haversine formula.
     *
     * @param lat1 The latitude of the first point.
     * @param lon1 The longitude of the first point.
     * @param lat2 The latitude of the second point.
     * @param lon2 The longitude of the second point.
     * @return The aerial distance between the two points in kilometers.
     */
    public static BigDecimal calculateAerialDistance(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2,
            BigDecimal lon2) {
        MathContext mc = MathContext.DECIMAL128;

        // Convert latitude and longitude from degrees to radians
        BigDecimal lat1Rad = lat1.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), mc);
        BigDecimal lon1Rad = lon1.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), mc);
        BigDecimal lat2Rad = lat2.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), mc);
        BigDecimal lon2Rad = lon2.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), mc);

        // Calculate differences in latitude and longitude
        BigDecimal dLat = lat2Rad.subtract(lat1Rad);
        BigDecimal dLon = lon2Rad.subtract(lon1Rad);

        // Apply Haversine formula
        double a = Math.sin(dLat.doubleValue() / 2) * Math.sin(dLat.doubleValue() / 2) +
                Math.cos(lat1Rad.doubleValue()) * Math.cos(lat2Rad.doubleValue()) *
                        Math.sin(dLon.doubleValue() / 2) * Math.sin(dLon.doubleValue() / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        BigDecimal distance = EARTH_RADIUS_KM.multiply(BigDecimal.valueOf(c), mc);

        return distance;
    }
}
