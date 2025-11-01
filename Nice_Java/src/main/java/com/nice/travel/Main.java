package com.nice.travel;

import com.google.gson.*;
import com.nice.travel.model.Route;
import com.nice.travel.model.TravelRequest;
import com.nice.travel.service.TravelOptimizerService;
import com.nice.travel.util.TimeUtil;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Main {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final TravelOptimizerService optimizerService = new TravelOptimizerService();

    public static void main(String[] args) {
        if (args.length != 4 || !args[0].equals("--gen_trip_summary") || !args[2].equals("--input")) {
            System.err.println("Usage: java Main --gen_trip_summary true/false --input routes.json");
            System.exit(1);
        }

        boolean genSummary = Boolean.parseBoolean(args[1]);
        String inputFile = args[3];

        try {
            Main main = new Main();
            JsonObject result = main.optimizeTravel(inputFile, genSummary);
            System.out.println(gson.toJson(result));
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }

    public JsonObject optimizeTravel(String inputFile, boolean genSummary) throws IOException {
        JsonObject input = gson.fromJson(new FileReader(inputFile), JsonObject.class);
        JsonObject result = new JsonObject();

        JsonArray requests = input.getAsJsonArray("requests");
        JsonArray routes = input.getAsJsonArray("routes");

        Map<String, List<Route>> graph = buildGraph(routes);

        for (JsonElement requestElement : requests) {
            JsonObject requestObj = requestElement.getAsJsonObject();
            TravelRequest request = new TravelRequest(
                requestObj.get("request_id").getAsString(),
                requestObj.get("source").getAsString(),
                requestObj.get("destination").getAsString(),
                requestObj.get("criteria").getAsString()
            );

            JsonObject requestResult = processRequest(request, graph, genSummary);
            result.add(request.getRequestId(), requestResult);
        }

        return result;
    }

    private Map<String, List<Route>> buildGraph(JsonArray routes) {
        Map<String, List<Route>> graph = new HashMap<>();

        for (JsonElement routeElement : routes) {
            JsonObject routeObj = routeElement.getAsJsonObject();
            String source = routeObj.get("source").getAsString();
            String destination = routeObj.get("destination").getAsString();
            String mode = routeObj.get("mode").getAsString();
            String departureTime = routeObj.get("departureTime").getAsString();
            String arrivalTime = routeObj.get("arrivalTime").getAsString();
            int cost = routeObj.get("cost").getAsInt();

            int duration = TimeUtil.calculateDuration(departureTime, arrivalTime);
            Route route = new Route(source, destination, mode, departureTime, arrivalTime, cost, duration);

            graph.computeIfAbsent(source, k -> new ArrayList<>()).add(route);
        }

        return graph;
    }

    private JsonObject processRequest(TravelRequest request, Map<String, List<Route>> graph, boolean genSummary) {
        JsonObject result = new JsonObject();

        List<Route> optimalPath = optimizerService.findOptimalPath(
            graph, request.getSource(), request.getDestination(), request.getCriteria()
        );

        if (optimalPath.isEmpty()) {
            result.add("schedule", new JsonArray());
            result.addProperty("criteria", request.getCriteria());
            result.addProperty("value", 0);
            result.addProperty("travelSummary", genSummary ? "No routes available" : "Not generated");
        } else {
            JsonArray schedule = new JsonArray();
            for (Route route : optimalPath) {
                JsonObject routeObj = new JsonObject();
                routeObj.addProperty("source", route.getSource());
                routeObj.addProperty("destination", route.getDestination());
                routeObj.addProperty("mode", route.getMode());
                routeObj.addProperty("departureTime", route.getDepartureTime());
                routeObj.addProperty("arrivalTime", route.getArrivalTime());
                routeObj.addProperty("cost", route.getCost());
                schedule.add(routeObj);
            }

            int totalTime = optimizerService.calculateTotalTime(optimalPath);
            int totalCost = optimizerService.calculateTotalCost(optimalPath);

            int value = request.getCriteria().equals("Time") ? totalTime :
                       request.getCriteria().equals("Cost") ? totalCost : optimalPath.size();

            result.add("schedule", schedule);
            result.addProperty("criteria", request.getCriteria());
            result.addProperty("value", value);
            result.addProperty("travelSummary", 
                optimizerService.generateTravelSummary(optimalPath, totalTime, genSummary));
        }

        return result;
    }
}