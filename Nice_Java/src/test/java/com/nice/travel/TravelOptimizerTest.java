package com.nice.travel;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TravelOptimizerTest {

    private Main main;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        main = new Main();
    }

    @Test
    void testOptimizeByTime() throws IOException {
        String inputFile = createTestFile("time_test.json", createTimeOptimizationInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Time", request1.get("criteria").getAsString());
        assertEquals(120, request1.get("value").getAsInt());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    @Test
    void testOptimizeByCost() throws IOException {
        String inputFile = createTestFile("cost_test.json", createCostOptimizationInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Cost", request1.get("criteria").getAsString());
        assertEquals(300, request1.get("value").getAsInt());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    @Test
    void testOptimizeByHops() throws IOException {
        String inputFile = createTestFile("hops_test.json", createHopsOptimizationInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Hops", request1.get("criteria").getAsString());
        assertEquals(1, request1.get("value").getAsInt());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    @Test
    void testTieBreaking() throws IOException {
        String inputFile = createTestFile("tie_test.json", createTieBreakingInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Time", request1.get("criteria").getAsString());
        assertTrue(request1.get("value").getAsInt() > 0);
    }

    @Test
    void testNoRouteAvailable() throws IOException {
        String inputFile = createTestFile("no_route_test.json", createNoRouteInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Time", request1.get("criteria").getAsString());
        assertEquals(0, request1.get("value").getAsInt());
        assertEquals(0, request1.getAsJsonArray("schedule").size());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    @Test
    void testMultiHopWithWaitingTime() throws IOException {
        String inputFile = createTestFile("multihop_test.json", createMultiHopInput());
        
        JsonObject result = main.optimizeTravel(inputFile, false);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Time", request1.get("criteria").getAsString());
        assertEquals(270, request1.get("value").getAsInt());
        assertEquals(2, request1.getAsJsonArray("schedule").size());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    @Test
    void testHuggingFaceSummaryGeneration() throws IOException {
        String inputFile = createTestFile("summary_test.json", createSummaryTestInput());
        
        JsonObject result = main.optimizeTravel(inputFile, true);
        
        assertNotNull(result);
        JsonObject request1 = result.getAsJsonObject("request_id1");
        assertEquals("Time", request1.get("criteria").getAsString());
        assertEquals("Not generated", request1.get("travelSummary").getAsString());
    }

    private String createTestFile(String filename, String content) throws IOException {
        Path file = tempDir.resolve(filename);
        try (FileWriter writer = new FileWriter(file.toFile())) {
            writer.write(content);
        }
        return file.toString();
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