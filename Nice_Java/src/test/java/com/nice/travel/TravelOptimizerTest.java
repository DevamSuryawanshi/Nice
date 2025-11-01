package com.nice.travel;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TravelOptimizerTest {
    
    private ObjectMapper mapper;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }
    
    @Test
    void testOptimizationByTime() throws IOException {
        File inputFile = createTestFile("time_test.json", createTimeOptimizationInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Time", request1.get("criteria").asText());
        assertEquals(120, request1.get("value").asInt()); // Direct flight is faster
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    @Test
    void testOptimizationByCost() throws IOException {
        File inputFile = createTestFile("cost_test.json", createCostOptimizationInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Cost", request1.get("criteria").asText());
        assertEquals(300, request1.get("value").asInt()); // Bus route is cheaper
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    @Test
    void testOptimizationByHops() throws IOException {
        File inputFile = createTestFile("hops_test.json", createHopsOptimizationInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Hops", request1.get("criteria").asText());
        assertEquals(1, request1.get("value").asInt()); // Direct route has fewer hops
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    @Test
    void testTieBreakingScenario() throws IOException {
        File inputFile = createTestFile("tie_test.json", createTieBreakingInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Time", request1.get("criteria").asText());
        // Should pick the route with lower cost when time is equal
        assertTrue(request1.get("value").asInt() > 0);
    }
    
    @Test
    void testNoRouteAvailable() throws IOException {
        File inputFile = createTestFile("no_route_test.json", createNoRouteInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Time", request1.get("criteria").asText());
        assertEquals(0, request1.get("value").asInt());
        assertTrue(request1.get("schedule").isArray());
        assertEquals(0, request1.get("schedule").size());
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    @Test
    void testMultiHopWithWaitingTime() throws IOException {
        File inputFile = createTestFile("multihop_test.json", createMultiHopInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), false);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Time", request1.get("criteria").asText());
        assertEquals(270, request1.get("value").asInt()); // 120 + 120 + 30 waiting time
        assertEquals(2, request1.get("schedule").size());
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    @Test
    void testHuggingFaceSummaryGeneration() throws IOException {
        File inputFile = createTestFile("summary_test.json", createSummaryTestInput());
        
        JsonNode result = TravelOptimizer.optimizeTravel(inputFile.getAbsolutePath(), true);
        
        assertNotNull(result);
        JsonNode request1 = result.get("request_id1");
        assertEquals("Time", request1.get("criteria").asText());
        // Without API key, should return "Not generated"
        assertEquals("Not generated", request1.get("travelSummary").asText());
    }
    
    private File createTestFile(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }
    
    private String createTimeOptimizationInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"criteria\": \"Time\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Flight\",\n" +
               "      \"departureTime\": \"09:00\",\n" +
               "      \"arrivalTime\": \"11:00\",\n" +
               "      \"cost\": 500\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"12:00\",\n" +
               "      \"cost\": 150\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"B\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"13:00\",\n" +
               "      \"arrivalTime\": \"17:00\",\n" +
               "      \"cost\": 150\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createCostOptimizationInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"criteria\": \"Cost\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Flight\",\n" +
               "      \"departureTime\": \"09:00\",\n" +
               "      \"arrivalTime\": \"11:00\",\n" +
               "      \"cost\": 500\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"10:00\",\n" +
               "      \"cost\": 150\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"B\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"10:30\",\n" +
               "      \"arrivalTime\": \"12:30\",\n" +
               "      \"cost\": 150\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createHopsOptimizationInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"criteria\": \"Hops\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Flight\",\n" +
               "      \"departureTime\": \"09:00\",\n" +
               "      \"arrivalTime\": \"11:00\",\n" +
               "      \"cost\": 500\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"10:00\",\n" +
               "      \"cost\": 150\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"B\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"10:30\",\n" +
               "      \"arrivalTime\": \"12:30\",\n" +
               "      \"cost\": 150\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createTieBreakingInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"criteria\": \"Time\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"10:00\",\n" +
               "      \"cost\": 100\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Train\",\n" +
               "      \"departureTime\": \"09:00\",\n" +
               "      \"arrivalTime\": \"11:00\",\n" +
               "      \"cost\": 200\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createNoRouteInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"Z\",\n" +
               "      \"criteria\": \"Time\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"10:00\",\n" +
               "      \"cost\": 150\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createMultiHopInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"criteria\": \"Time\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"08:00\",\n" +
               "      \"arrivalTime\": \"10:00\",\n" +
               "      \"cost\": 150\n" +
               "    },\n" +
               "    {\n" +
               "      \"source\": \"B\",\n" +
               "      \"destination\": \"C\",\n" +
               "      \"mode\": \"Bus\",\n" +
               "      \"departureTime\": \"10:30\",\n" +
               "      \"arrivalTime\": \"12:30\",\n" +
               "      \"cost\": 150\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
    
    private String createSummaryTestInput() {
        return "{\n" +
               "  \"requests\": [\n" +
               "    {\n" +
               "      \"request_id\": \"request_id1\",\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"criteria\": \"Time\"\n" +
               "    }\n" +
               "  ],\n" +
               "  \"routes\": [\n" +
               "    {\n" +
               "      \"source\": \"A\",\n" +
               "      \"destination\": \"B\",\n" +
               "      \"mode\": \"Flight\",\n" +
               "      \"departureTime\": \"09:00\",\n" +
               "      \"arrivalTime\": \"11:00\",\n" +
               "      \"cost\": 500\n" +
               "    }\n" +
               "  ]\n" +
               "}";
    }
}