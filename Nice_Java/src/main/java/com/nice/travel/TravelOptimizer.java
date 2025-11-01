package com.nice.travel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TravelOptimizer {
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public static void main(String[] args) {
        if (args.length != 4 || !args[0].equals("--gen_trip_summary") || !args[2].equals("--input")) {
            System.err.println("Usage: java TravelOptimizer --gen_trip_summary true/false --input routes.json");
            System.exit(1);
        }
        
        boolean genSummary = Boolean.parseBoolean(args[1]);
        String inputFile = args[3];
        
        try {
            JsonNode result = optimizeTravel(inputFile, genSummary);
            System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    public static JsonNode optimizeTravel(String inputFile, boolean genSummary) throws IOException {
        JsonNode input = mapper.readTree(new File(inputFile));
        ObjectNode result = mapper.createObjectNode();
        
        JsonNode requests = input.get("requests");
        JsonNode routes = input.get("routes");
        
        for (JsonNode request : requests) {
            String requestId = request.get("request_id").asText();
            String source = request.get("source").asText();
            String destination = request.get("destination").asText();
            String criteria = request.get("criteria").asText();
            
            ObjectNode requestResult = findOptimalRoute(source, destination, criteria, routes, genSummary);
            result.set(requestId, requestResult);
        }
        
        return result;
    }
    
    private static ObjectNode findOptimalRoute(String source, String destination, String criteria, 
                                             JsonNode routes, boolean genSummary) {
        ObjectNode result = mapper.createObjectNode();
        
        // Build graph
        Map<String, List<RouteInfo>> graph = buildGraph(routes);
        
        // Find optimal path using Dijkstra
        PathResult pathResult = dijkstra(graph, source, destination, criteria);
        
        if (pathResult.path.isEmpty()) {
            result.set("schedule", mapper.createArrayNode());
            result.put("criteria", criteria);
            result.put("value", 0);
            result.put("travelSummary", genSummary ? "No routes available" : "Not generated");
        } else {
            ArrayNode schedule = mapper.createArrayNode();
            int totalTime = 0;
            int totalCost = 0;
            
            for (int i = 0; i < pathResult.path.size(); i++) {
                RouteInfo route = pathResult.path.get(i);
                ObjectNode routeNode = mapper.createObjectNode();
                routeNode.put("source", route.source);
                routeNode.put("destination", route.destination);
                routeNode.put("mode", route.mode);
                routeNode.put("departureTime", route.departureTime);
                routeNode.put("arrivalTime", route.arrivalTime);
                routeNode.put("cost", route.cost);
                schedule.add(routeNode);
                
                totalCost += route.cost;
                totalTime += route.duration;
                
                // Add waiting time
                if (i < pathResult.path.size() - 1) {
                    totalTime += calculateWaitingTime(route.arrivalTime, pathResult.path.get(i + 1).departureTime);
                }
            }
            
            result.set("schedule", schedule);
            result.put("criteria", criteria);
            
            int value = criteria.equals("Time") ? totalTime : 
                       criteria.equals("Cost") ? totalCost : pathResult.path.size();
            result.put("value", value);
            
            String summary = genSummary ? generateSummary(pathResult.path, totalTime) : "Not generated";
            result.put("travelSummary", summary);
        }
        
        return result;
    }
    
    private static Map<String, List<RouteInfo>> buildGraph(JsonNode routes) {
        Map<String, List<RouteInfo>> graph = new HashMap<>();
        
        for (JsonNode route : routes) {
            String source = route.get("source").asText();
            String destination = route.get("destination").asText();
            String mode = route.get("mode").asText();
            String departureTime = route.get("departureTime").asText();
            String arrivalTime = route.get("arrivalTime").asText();
            int cost = route.get("cost").asInt();
            
            int duration = calculateDuration(departureTime, arrivalTime);
            RouteInfo routeInfo = new RouteInfo(source, destination, mode, departureTime, arrivalTime, cost, duration);
            
            graph.computeIfAbsent(source, k -> new ArrayList<>()).add(routeInfo);
        }
        
        return graph;
    }
    
    private static PathResult dijkstra(Map<String, List<RouteInfo>> graph, String start, String end, String criteria) {
        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<RouteInfo>> paths = new HashMap<>();
        PriorityQueue<Node> pq = new PriorityQueue<>(Comparator.comparingInt(n -> n.distance));
        
        distances.put(start, 0);
        paths.put(start, new ArrayList<>());
        pq.offer(new Node(start, 0, new ArrayList<>()));
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();
            
            if (current.city.equals(end)) {
                return new PathResult(current.path, current.distance);
            }
            
            if (current.distance > distances.getOrDefault(current.city, Integer.MAX_VALUE)) {
                continue;
            }
            
            for (RouteInfo route : graph.getOrDefault(current.city, new ArrayList<>())) {
                int weight = getWeight(route, criteria);
                int newDistance = current.distance + weight;
                
                if (newDistance < distances.getOrDefault(route.destination, Integer.MAX_VALUE)) {
                    distances.put(route.destination, newDistance);
                    List<RouteInfo> newPath = new ArrayList<>(current.path);
                    newPath.add(route);
                    paths.put(route.destination, newPath);
                    pq.offer(new Node(route.destination, newDistance, newPath));
                }
            }
        }
        
        return new PathResult(new ArrayList<>(), Integer.MAX_VALUE);
    }
    
    private static int getWeight(RouteInfo route, String criteria) {
        switch (criteria) {
            case "Time": return route.duration;
            case "Cost": return route.cost;
            case "Hops": return 1;
            default: return route.duration;
        }
    }
    
    private static int calculateDuration(String departureTime, String arrivalTime) {
        int depMinutes = timeToMinutes(departureTime);
        int arrMinutes = timeToMinutes(arrivalTime);
        return arrMinutes >= depMinutes ? arrMinutes - depMinutes : (24 * 60) - depMinutes + arrMinutes;
    }
    
    private static int calculateWaitingTime(String arrivalTime, String departureTime) {
        int arrMinutes = timeToMinutes(arrivalTime);
        int depMinutes = timeToMinutes(departureTime);
        return depMinutes >= arrMinutes ? depMinutes - arrMinutes : (24 * 60) - arrMinutes + depMinutes;
    }
    
    private static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
    
    private static String generateSummary(List<RouteInfo> path, int totalTime) {
        String apiKey = System.getenv("HUGGINGFACE_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            return "Not generated";
        }
        
        try {
            int hours = totalTime / 60;
            int minutes = totalTime % 60;
            String prompt = String.format("Summarize travel route: %d segments, %dh %dm total", 
                                        path.size(), hours, minutes);
            
            return callHuggingFaceAPI(prompt, apiKey);
        } catch (Exception e) {
            return "Not generated";
        }
    }
    
    private static String callHuggingFaceAPI(String prompt, String apiKey) throws IOException {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost("https://api-inference.huggingface.co/models/facebook/bart-large-cnn");
            post.setHeader("Authorization", "Bearer " + apiKey);
            post.setHeader("Content-Type", "application/json");
            
            ObjectNode payload = mapper.createObjectNode();
            payload.put("inputs", prompt);
            ObjectNode parameters = mapper.createObjectNode();
            parameters.put("max_length", 60);
            parameters.put("min_length", 20);
            payload.set("parameters", parameters);
            
            post.setEntity(new StringEntity(payload.toString()));
            
            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonNode result = mapper.readTree(responseBody);
                
                if (result.isArray() && result.size() > 0) {
                    return result.get(0).get("summary_text").asText();
                }
            }
        }
        return "Not generated";
    }
    
    static class RouteInfo {
        String source, destination, mode, departureTime, arrivalTime;
        int cost, duration;
        
        RouteInfo(String source, String destination, String mode, String departureTime, 
                 String arrivalTime, int cost, int duration) {
            this.source = source;
            this.destination = destination;
            this.mode = mode;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.cost = cost;
            this.duration = duration;
        }
    }
    
    static class Node {
        String city;
        int distance;
        List<RouteInfo> path;
        
        Node(String city, int distance, List<RouteInfo> path) {
            this.city = city;
            this.distance = distance;
            this.path = path;
        }
    }
    
    static class PathResult {
        List<RouteInfo> path;
        int distance;
        
        PathResult(List<RouteInfo> path, int distance) {
            this.path = path;
            this.distance = distance;
        }
    }
}