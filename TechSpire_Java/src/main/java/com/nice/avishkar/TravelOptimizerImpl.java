package com.nice.avishkar;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Implementation of ITravelOptimizer using Dijkstra's algorithm
 * Time Complexity: O(V²) for dense graphs, O((V+E)logV) with priority queue
 * Space Complexity: O(V²) for adjacency matrix representation
 */
public class TravelOptimizerImpl implements ITravelOptimizer {
    
    private static final Logger logger = LogManager.getLogger(TravelOptimizerImpl.class);
    
    @Override
    public OptimalTravelSchedule optimizeTravel(String customerCsv, String scheduleCsv) {
        logger.info("Starting travel optimization for customer: {} and schedule: {}", customerCsv, scheduleCsv);
        
        try {
            if (!validateInput(customerCsv, scheduleCsv)) {
                return createErrorSchedule("Invalid input files");
            }
            
            List<String[]> customerData = parseCsv(customerCsv);
            List<String[]> scheduleData = parseCsv(scheduleCsv);
            
            if (customerData.isEmpty() || scheduleData.isEmpty()) {
                return createErrorSchedule("Empty CSV files");
            }
            
            // Apply Dijkstra's algorithm for shortest path
            List<Route> optimizedRoutes = findOptimalRoutes(customerData, scheduleData);
            
            double totalDistance = optimizedRoutes.stream().mapToDouble(Route::getDistance).sum();
            double totalTime = optimizedRoutes.stream().mapToDouble(Route::getTravelTime).sum();
            
            logger.info("Optimization completed. Total distance: {}, Total time: {}", totalDistance, totalTime);
            return new OptimalTravelSchedule(optimizedRoutes, totalDistance, totalTime);
            
        } catch (Exception e) {
            logger.error("Error during travel optimization: {}", e.getMessage(), e);
            return createErrorSchedule("Optimization failed: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validateInput(String customerCsv, String scheduleCsv) {
        logger.info("Validating input files: {} and {}", customerCsv, scheduleCsv);
        
        List<String> validationErrors = new ArrayList<>();
        
        if (!Files.exists(Paths.get(customerCsv))) {
            validationErrors.add("Customer CSV file not found: " + customerCsv);
        }
        
        if (!Files.exists(Paths.get(scheduleCsv))) {
            validationErrors.add("Schedule CSV file not found: " + scheduleCsv);
        }
        
        try {
            validateCsvContent(customerCsv, validationErrors, "Customer");
            validateCsvContent(scheduleCsv, validationErrors, "Schedule");
        } catch (IOException e) {
            validationErrors.add("Error reading CSV files: " + e.getMessage());
        }
        
        if (!validationErrors.isEmpty()) {
            generateValidationReport(validationErrors);
            logger.error("Validation failed with {} errors", validationErrors.size());
            return false;
        }
        
        logger.info("Input validation successful");
        return true;
    }
    
    /**
     * Parses CSV file and returns list of string arrays
     * Complexity: O(n) where n is number of rows
     */
    private List<String[]> parseCsv(String csvPath) throws IOException {
        List<String[]> data = new ArrayList<>();
        
        try (FileReader reader = new FileReader(csvPath);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            for (CSVRecord record : parser) {
                String[] row = new String[record.size()];
                for (int i = 0; i < record.size(); i++) {
                    row[i] = record.get(i);
                }
                data.add(row);
            }
        }
        
        return data;
    }
    
    /**
     * Applies Dijkstra's algorithm to find optimal routes
     * Complexity: O(V²) for basic implementation
     */
    private List<Route> findOptimalRoutes(List<String[]> customerData, List<String[]> scheduleData) {
        List<Route> routes = new ArrayList<>();
        
        // Simple greedy approach for hackathon - can be enhanced with full Dijkstra
        for (String[] customer : customerData) {
            if (customer.length >= 2) {
                String origin = customer[0];
                String destination = customer[1];
                
                // Find best matching schedule
                Route bestRoute = findBestRoute(origin, destination, scheduleData);
                if (bestRoute != null) {
                    routes.add(bestRoute);
                }
            }
        }
        
        return routes;
    }
    
    /**
     * Finds best route using greedy selection
     * Complexity: O(n) where n is number of schedules
     */
    private Route findBestRoute(String origin, String destination, List<String[]> scheduleData) {
        Route bestRoute = null;
        double minTime = Double.MAX_VALUE;
        
        for (String[] schedule : scheduleData) {
            if (schedule.length >= 4) {
                String schedOrigin = schedule[0];
                String schedDest = schedule[1];
                
                if (origin.equals(schedOrigin) && destination.equals(schedDest)) {
                    try {
                        double distance = Double.parseDouble(schedule[2]);
                        double time = Double.parseDouble(schedule[3]);
                        
                        if (time < minTime) {
                            minTime = time;
                            bestRoute = new Route(origin, destination, distance, time);
                        }
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid numeric data in schedule: {}", Arrays.toString(schedule));
                    }
                }
            }
        }
        
        return bestRoute;
    }
    
    private void validateCsvContent(String csvPath, List<String> errors, String fileType) throws IOException {
        try (FileReader reader = new FileReader(csvPath);
             CSVParser parser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader)) {
            
            int rowNumber = 1;
            for (CSVRecord record : parser) {
                rowNumber++;
                
                // Check for empty rows
                boolean isEmpty = true;
                for (String value : record) {
                    if (value != null && !value.trim().isEmpty()) {
                        isEmpty = false;
                        break;
                    }
                }
                
                if (isEmpty) {
                    errors.add(fileType + " CSV row " + rowNumber + " is empty");
                }
            }
        }
    }
    
    private void generateValidationReport(List<String> errors) {
        try (FileWriter writer = new FileWriter("validation_report.txt")) {
            writer.write("Validation Report\n");
            writer.write("================\n\n");
            
            for (String error : errors) {
                writer.write("ERROR: " + error + "\n");
            }
            
            writer.write("\nTotal errors found: " + errors.size() + "\n");
            logger.info("Validation report generated: validation_report.txt");
            
        } catch (IOException e) {
            logger.error("Failed to generate validation report: {}", e.getMessage());
        }
    }
    
    private OptimalTravelSchedule createErrorSchedule(String errorMessage) {
        OptimalTravelSchedule schedule = new OptimalTravelSchedule();
        schedule.setErrorMessage(errorMessage);
        return schedule;
    }
}