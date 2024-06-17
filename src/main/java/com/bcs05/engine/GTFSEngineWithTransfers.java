package com.bcs05.engine;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.PriorityQueue;

import com.bcs05.util.DijkstraNode;
import com.bcs05.util.DijkstraNodeComparator;
import com.bcs05.util.GTFSGraph;
import com.bcs05.util.GTFSWeightedEdge;
import com.bcs05.util.Path;
import com.bcs05.util.Stop;

public class GTFSEngineWithTransfers {

    public static void main(String[] args) {
        GTFSEngineWithTransfers engine = new GTFSEngineWithTransfers();
        engine.findPathWithTransfers(new Stop("2577952"), new Stop("2578122"));

    }

    GTFSGraph graph;

    public GTFSEngineWithTransfers() {
        graph = new GTFSGraph();
    }

    public Path findPathWithTransfers(Stop startStop, Stop endStop) {

        HashMap<Stop, Boolean> explored = new HashMap<Stop, Boolean>();
        HashMap<Stop, Integer> travelTime = new HashMap<Stop, Integer>();

        for (Stop stop : graph.getStops()) {
            explored.put(stop, false);
            travelTime.put(stop, Integer.MAX_VALUE);
        }

        PriorityQueue<DijkstraNode> priorityQueue = new PriorityQueue<DijkstraNode>(new DijkstraNodeComparator());

        travelTime.put(startStop, 0);
        priorityQueue.add(new DijkstraNode(startStop, 0));

        LocalTime currentTime = LocalTime.now();

        while (!priorityQueue.isEmpty()) {
            Stop currentStop = priorityQueue.poll().getStop();
            LocalTime currentDepartureTime = currentTime.plusSeconds(travelTime.get(currentStop));

            explored.put(currentStop, true);

            for (GTFSWeightedEdge neighbourEdge : graph.getNeighbours(currentStop, currentDepartureTime)) {
                Stop neighbourStop = neighbourEdge.getStop();
                int waitingTime = (int) (neighbourEdge.getDepartureTime().toSecondOfDay()
                        - currentDepartureTime.toSecondOfDay());
                int newTravelTime = travelTime.get(currentStop) + waitingTime + neighbourEdge.getTravelTime();

                if (newTravelTime < travelTime.get(neighbourStop)) {
                    travelTime.put(neighbourStop, newTravelTime);
                }

                if (!explored.get(neighbourStop)) {
                    priorityQueue.add(new DijkstraNode(neighbourStop, travelTime.get(neighbourStop)));
                }
            }

            if (currentStop.equals(endStop)) {
                break;
            }
        }

        if (explored.get(endStop)) {
            System.out.println("Path found");
            System.out.println("Travel time: " + travelTime.get(endStop));
            System.out.println("Arrival time: " + currentTime.plusSeconds(travelTime.get(endStop)));
        } else
            System.out.println("No path found");

        return null;
    }

}
