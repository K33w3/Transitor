package com.bcs05.util;

/**
 * The Transportation enum represents different modes of transportation.
 * It includes options for foot and bike.
 */
public enum Transportation {
    FOOT("foot"),
    BIKE("bike"),
    BUS("bus"),
    AERIAL("aerial");

    public final String label;

    /**
     * Constructs a new Transportation enum with the given label.
     *
     * @param label the label associated with the transportation mode
     */
    private Transportation(String label) {
        this.label = label;
    }
}
