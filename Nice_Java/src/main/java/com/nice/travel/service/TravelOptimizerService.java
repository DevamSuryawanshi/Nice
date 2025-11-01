package com.nice.travel.service;

import com.nice.travel.api.HuggingFaceClient;
import com.nice.travel.model.Route;
import com.nice.travel.util.TimeUtil;

import java.util.*;

public class TravelOptimizerService {
    private final HuggingFaceClient huggingFaceClient = new HuggingFaceClient();

    public List<Route> findOptimalPath(Map<String, List<Route>> graph, String start, String end, String criteria) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<Route>> paths = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));

        distances.put(start, 0);
        paths.put(start, new ArrayList<>());
        pq.offer(new Node(start, 0, new ArrayList<>()));

        while (!pq.isEmpty()) {
            Node current = pq.poll();

            if (current.city.equals(end)) {
                return current.path;
            }

            if (current.distance > distances.getOrDefault(current.city, Integer.MAX_VALUE)) {
                continue;
            }

            for (Route route : graph.getOrDefault(current.city, new ArrayList<>())) {
                int weight = getWeight(route, criteria);
                int newDistance = current.distance + weight;

                if (newDistance < distances.getOrDefault(route.getDestination(), Integer.MAX_VALUE)) {
                    distances.put(route.getDestination(), newDistance);
                    List<Route> newPath = new ArrayList<>(current.path);
                    newPath.add(route);
                    pq.offer(new Node(route.getDestination(), newDistance, newPath));
                }
            }
        }

        return new ArrayList<>();
    }

    private int getWeight(Route route, String criteria) {
        switch (criteria) {
            case "Time": return route.getDuration();
            case "Cost": return route.getCost();
            case "Hops": return 1;
            default: return route.getDuration();
        }
    }

    public String generateTravelSummary(List<Route> path, int totalTime, boolean genSummary) {
        if (!genSummary) {
            return "Not generated";
        }

        if (path.isEmpty()) {
            return "No routes available";
        }

        String apiKey = System.getenv("HUGGINGFACE_API_KEY");
        int hours = totalTime / 60;
        int minutes = totalTime % 60;
        String prompt = String.format("Summarize travel route: %d segments, %dh %dm total", 
                                    path.size(), hours, minutes);

        return huggingFaceClient.generateSummary(prompt, apiKey);
    }

    public int calculateTotalTime(List<Route> path) {
        int totalTime = 0;
        for (int i = 0; i < path.size(); i++) {
            totalTime += path.get(i).getDuration();
            if (i < path.size() - 1) {
                totalTime += TimeUtil.calculateWaitingTime(
                    path.get(i).getArrivalTime(), 
                    path.get(i + 1).getDepartureTime()
                );
            }
        }
        return totalTime;
    }

    public int calculateTotalCost(List<Route> path) {
        return path.stream().mapToInt(Route::getCost).sum();
    }

    static class Node {
        String city;
        int distance;
        List<Route> path;

        Node(String city, int distance, List<Route> path) {
            this.city = city;
            this.distance = distance;
            this.path = path;
        }
    }
}