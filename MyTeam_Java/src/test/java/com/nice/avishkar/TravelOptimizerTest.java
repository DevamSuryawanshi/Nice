package com.nice.avishkar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test class for TravelOptimizer
 * Covers all 6 test cases including valid and invalid inputs
 */
public class TravelOptimizerTest {
    
    private ITravelOptimizer optimizer;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        optimizer = new TravelOptimizerImpl();
    }
    
    @Test
    void testCase1_ValidInputs() throws IOException {
        // Create valid test CSV files
        Path customerCsv = createValidCustomerCsv();
        Path scheduleCsv = createValidScheduleCsv();
        
        OptimalTravelSchedule result = optimizer.optimizeTravel(
            customerCsv.toString(), 
            scheduleCsv.toString()
        );
        
        assertNotNull(result);
        assertTrue(result.isValid());
        assertNotNull(result.getRoutes());
        assertFalse(result.getRoutes().isEmpty());
        assertTrue(result.getTotalDistance() > 0);
        assertTrue(result.getTotalTravelTime() > 0);
    }
    
    @Test
    void testCase2_EmptyCustomerFile() throws IOException {
        Path customerCsv = createEmptyCustomerCsv();
        Path scheduleCsv = createValidScheduleCsv();
        
        OptimalTravelSchedule result = optimizer.optimizeTravel(
            customerCsv.toString(), 
            scheduleCsv.toString()
        );
        
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testCase3_EmptyScheduleFile() throws IOException {
        Path customerCsv = createValidCustomerCsv();
        Path scheduleCsv = createEmptyScheduleCsv();
        
        OptimalTravelSchedule result = optimizer.optimizeTravel(
            customerCsv.toString(), 
            scheduleCsv.toString()
        );
        
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testCase4_NonExistentFiles() {
        String nonExistentCustomer = tempDir.resolve("nonexistent_customer.csv").toString();
        String nonExistentSchedule = tempDir.resolve("nonexistent_schedule.csv").toString();
        
        OptimalTravelSchedule result = optimizer.optimizeTravel(nonExistentCustomer, nonExistentSchedule);
        
        assertNotNull(result);
        assertFalse(result.isValid());
        assertNotNull(result.getErrorMessage());
    }
    
    @Test
    void testCase5_InvalidDataFormat() throws IOException {
        Path customerCsv = createInvalidCustomerCsv();
        Path scheduleCsv = createInvalidScheduleCsv();
        
        OptimalTravelSchedule result = optimizer.optimizeTravel(
            customerCsv.toString(), 
            scheduleCsv.toString()
        );
        
        assertNotNull(result);
        // Should handle gracefully even with invalid data
    }
    
    @Test
    void testCase6_ValidationMethod() throws IOException {
        Path validCustomer = createValidCustomerCsv();
        Path validSchedule = createValidScheduleCsv();
        
        boolean validResult = optimizer.validateInput(
            validCustomer.toString(), 
            validSchedule.toString()
        );
        assertTrue(validResult);
        
        String nonExistentFile = tempDir.resolve("nonexistent.csv").toString();
        boolean invalidResult = optimizer.validateInput(nonExistentFile, nonExistentFile);
        assertFalse(invalidResult);
    }
    
    @Test
    void testOptimalTravelScheduleModel() {
        OptimalTravelSchedule schedule = new OptimalTravelSchedule();
        
        // Test error scenario
        schedule.setErrorMessage("Test error");
        assertFalse(schedule.isValid());
        assertEquals("Test error", schedule.getErrorMessage());
        
        // Test valid scenario
        schedule.setValid(true);
        schedule.setTotalDistance(100.0);
        schedule.setTotalTravelTime(120.0);
        
        assertEquals(100.0, schedule.getTotalDistance());
        assertEquals(120.0, schedule.getTotalTravelTime());
    }
    
    @Test
    void testRouteModel() {
        Route route = new Route("CityA", "CityB", 50.0, 60.0);
        
        assertEquals("CityA", route.getOrigin());
        assertEquals("CityB", route.getDestination());
        assertEquals(50.0, route.getDistance());
        assertEquals(60.0, route.getTravelTime());
        
        assertNotNull(route.toString());
    }
    
    // Helper methods to create test CSV files
    
    private Path createValidCustomerCsv() throws IOException {
        Path csvFile = tempDir.resolve("valid_customer.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Priority\n");
            writer.write("CityA,CityB,High\n");
            writer.write("CityB,CityC,Medium\n");
        }
        return csvFile;
    }
    
    private Path createValidScheduleCsv() throws IOException {
        Path csvFile = tempDir.resolve("valid_schedule.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Distance,TravelTime\n");
            writer.write("CityA,CityB,50.5,60.0\n");
            writer.write("CityB,CityC,75.2,90.0\n");
        }
        return csvFile;
    }
    
    private Path createEmptyCustomerCsv() throws IOException {
        Path csvFile = tempDir.resolve("empty_customer.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Priority\n");
        }
        return csvFile;
    }
    
    private Path createEmptyScheduleCsv() throws IOException {
        Path csvFile = tempDir.resolve("empty_schedule.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Distance,TravelTime\n");
        }
        return csvFile;
    }
    
    private Path createInvalidCustomerCsv() throws IOException {
        Path csvFile = tempDir.resolve("invalid_customer.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Priority\n");
            writer.write(",,\n");
            writer.write("CityA,,High\n");
        }
        return csvFile;
    }
    
    private Path createInvalidScheduleCsv() throws IOException {
        Path csvFile = tempDir.resolve("invalid_schedule.csv");
        try (FileWriter writer = new FileWriter(csvFile.toFile())) {
            writer.write("Origin,Destination,Distance,TravelTime\n");
            writer.write("CityA,CityB,invalid,not_a_number\n");
            writer.write(",,0,0\n");
        }
        return csvFile;
    }
}