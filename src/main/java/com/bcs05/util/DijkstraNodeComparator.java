package com.bcs05.util;

import java.util.Comparator;

/**
 * Comparator for comparing DijkstraNodes based on their distance
 */
public class DijkstraNodeComparator implements Comparator<DijkstraNode> {

    /**
     * Compares the distances of two DijkstraNodes
     * 
     * @param d1 the first Dijkstra Node
     * @param d2 the second Dijkstra Node
     * @return -1 if d1 has a smaller travel time than d2, 1 if it is a larger
     *         travel time, and 0 if they are the same
     */
    @Override
    public int compare(DijkstraNode d1, DijkstraNode d2) {
        if (d1.getTravelTime() < d2.getTravelTime()) {
            return -1;
        } else if (d1.getTravelTime() > d2.getTravelTime()) {
            return 1;
        } else {
            return 0;
        }
    }

}
