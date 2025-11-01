package com.nice.avishkar;

/**
 * Interface for travel optimization functionality
 */
public interface ITravelOptimizer {
    
    /**
     * Optimizes travel schedule based on customer requests and available schedules
     * @param customerCsv Path to customer requests CSV file
     * @param scheduleCsv Path to schedules CSV file
     * @return Optimal travel schedule
     */
    OptimalTravelSchedule optimizeTravel(String customerCsv, String scheduleCsv);
    
    /**
     * Validates input CSV files
     * @param customerCsv Path to customer requests CSV file
     * @param scheduleCsv Path to schedules CSV file
     * @return true if inputs are valid, false otherwise
     */
    boolean validateInput(String customerCsv, String scheduleCsv);
}