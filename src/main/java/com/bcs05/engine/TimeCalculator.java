package com.bcs05.engine;

import java.math.BigDecimal;

/**
 * The TimeCalculator class calculates the time required to travel between two
 * points based on their coordinates.
 */
public class TimeCalculator {
    private BigDecimal lat1;
    private BigDecimal lon1;
    private BigDecimal lat2;
    private BigDecimal lon2;

    /**
     * Constructs a TimeCalculator object with the given coordinates.
     *
     * @param lat1 The latitude of the starting point.
     * @param lon1 The longitude of the starting point.
     * @param lat2 The latitude of the destination point.
     * @param lon2 The longitude of the destination point.
     */
    public TimeCalculator(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        this.lat1 = lat1;
        this.lon1 = lon1;
        this.lat2 = lat2;
        this.lon2 = lon2;
    }

    /**
     * Calculates the time required to walk between two points.
     *
     * @param lat1 The latitude of the starting point.
     * @param lon1 The longitude of the starting point.
     * @param lat2 The latitude of the destination point.
     * @param lon2 The longitude of the destination point.
     * @return The time required to walk between the two points in minutes.
     */
    public static BigDecimal walkTime(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        BigDecimal distance = DistanceCalculator.calculateAerialDistance(lat1, lon1, lat2, lon2);
        BigDecimal timeToWalk = BigDecimal.valueOf(12).multiply(distance); // average walking time of 12 min a kilometer
        return timeToWalk;
    }

    /**
     * Calculates the time required to cycle between two points.
     *
     * @param lat1 The latitude of the starting point.
     * @param lon1 The longitude of the starting point.
     * @param lat2 The latitude of the destination point.
     * @param lon2 The longitude of the destination point.
     * @return The time required to cycle between the two points in minutes.
     */
    public static BigDecimal cycleTime(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
        BigDecimal distance = DistanceCalculator.calculateAerialDistance(lat1, lon1, lat2, lon2);
        BigDecimal timeToCycle = BigDecimal.valueOf(3).multiply(distance); // average cycling time of 3 min a kilometer
        return timeToCycle;
    }
}
