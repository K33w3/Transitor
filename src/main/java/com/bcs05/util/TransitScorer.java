package com.bcs05.util;

public class TransitScorer {

    /**
     * Scores the quality of transfers based on multiple criteria.
     *
     * @param easeOfTransfer An integer representing how easy it is to transfer (e.g., availability of clear signage, short walking distance).
     * @param waitingTime An integer representing the average waiting time for transfers in minutes.
     * @param frequencyOfService An integer representing how frequent the bus or train service is.
     * @param scheduleCoordination An integer representing how well the schedules of different transit lines are coordinated.
     * @return The total score for transfer quality, which is a sum of the input scores.
     */
    public int scoreTransferQuality(int easeOfTransfer, int waitingTime, int frequencyOfService, int scheduleCoordination) {
        return easeOfTransfer + waitingTime + frequencyOfService + scheduleCoordination;
    }

    /**
     * Scores the travel time based on multiple criteria.
     *
     * @param totalTravelTime An integer representing the total travel time in minutes.
     * @param routeDirectness An integer representing how direct the route is (e.g., minimal detours).
     * @param transitSpeed An integer representing the average speed of the transit.
     * @param delayFrequency An integer representing the frequency of delays.
     * @return The total score for travel time, which is a sum of the input scores.
     */
    public int scoreTravelTime(int totalTravelTime, int routeDirectness, int transitSpeed, int delayFrequency) {
        return totalTravelTime + routeDirectness + transitSpeed + delayFrequency;
    }

    /**
     * Evaluates the ease of transfer and returns a score based on predefined criteria.
     *
     * @param clearSignage Boolean indicating if there is clear signage at transfer points.
     * @param shortWalkingDistance Boolean indicating if the walking distance for transfer is short.
     * @param assistanceAvailable Boolean indicating if assistance is available for transfers.
     * @return The score for ease of transfer, with a maximum of 15 points.
     */
    public int getEaseOfTransferScore(boolean clearSignage, boolean shortWalkingDistance, boolean assistanceAvailable) {
        int score = 0;
        if (clearSignage) score += 5;
        if (shortWalkingDistance) score += 5;
        if (assistanceAvailable) score += 5;
        return score;
    }

    /**
     * Evaluates the waiting time for transfers and returns a score based on predefined criteria.
     *
     * @param waitingTimeInMinutes The waiting time in minutes.
     * @return The score for waiting time, with a maximum of 15 points.
     */
    public int getWaitingTimeScore(int waitingTimeInMinutes) {
        if (waitingTimeInMinutes < 5) return 15;
        if (waitingTimeInMinutes <= 10) return 10;
        if (waitingTimeInMinutes <= 15) return 5;
        return 0;
    }

    /**
     * Evaluates the frequency of service and returns a score based on predefined criteria.
     *
     * @param frequencyInMinutes The frequency of service in minutes.
     * @return The score for frequency of service, with a maximum of 10 points.
     */
    public int getFrequencyOfServiceScore(int frequencyInMinutes) {
        if (frequencyInMinutes <= 5) return 10;
        if (frequencyInMinutes <= 10) return 7;
        if (frequencyInMinutes <= 15) return 5;
        return 0;
    }

    /**
     * Evaluates the coordination of schedules and returns a score based on predefined criteria.
     *
     * @param wellCoordinated Boolean indicating if the schedules are well coordinated.
     * @return The score for schedule coordination, with a maximum of 10 points.
     */
    public int getScheduleCoordinationScore(boolean wellCoordinated) {
        return wellCoordinated ? 10 : 0;
    }

    /**
     * Evaluates the total travel time and returns a score based on predefined criteria.
     *
     * @param travelTimeInMinutes The total travel time in minutes.
     * @return The score for total travel time, with a maximum of 20 points.
     */
    public int getTotalTravelTimeScore(int travelTimeInMinutes) {
        if (travelTimeInMinutes < 15) return 20;
        if (travelTimeInMinutes <= 30) return 15;
        if (travelTimeInMinutes <= 45) return 10;
        return 0;
    }

    /**
     * Evaluates the directness of the route and returns a score based on predefined criteria.
     *
     * @param directRoute Boolean indicating if the route is direct.
     * @return The score for route directness, with a maximum of 10 points.
     */
    public int getRouteDirectnessScore(boolean directRoute) {
        return directRoute ? 10 : 0;
    }

    /**
     * Evaluates the average speed of transit and returns a score based on predefined criteria.
     *
     * @param averageSpeed The average speed in km/h.
     * @return The score for transit speed, with a maximum of 10 points.
     */
    public int getTransitSpeedScore(int averageSpeed) {
        if (averageSpeed > 50) return 10;
        if (averageSpeed > 30) return 7;
        if (averageSpeed > 15) return 5;
        return 0;
    }

    /**
     * Evaluates the frequency of delays and returns a score based on predefined criteria.
     *
     * @param delaysPerMonth The number of delays per month.
     * @return The score for delay frequency, with a maximum of 10 points.
     */
    public int getDelayFrequencyScore(int delaysPerMonth) {
        if (delaysPerMonth == 0) return 10;
        if (delaysPerMonth <= 2) return 7;
        if (delaysPerMonth <= 5) return 5;
        return 0;
    }
}
