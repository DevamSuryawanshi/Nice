package com.nice.travel.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import okhttp3.*;

import java.io.IOException;

public class HuggingFaceClient {
    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public String generateSummary(String prompt, String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Not generated";
        }

        try {
            JsonObject payload = new JsonObject();
            payload.addProperty("inputs", prompt);
            
            JsonObject parameters = new JsonObject();
            parameters.addProperty("max_length", 60);
            parameters.addProperty("min_length", 20);
            payload.add("parameters", parameters);

            RequestBody body = RequestBody.create(
                MediaType.parse("application/json"), 
                gson.toJson(payload)
            );

            Request request = new Request.Builder()
                .url(API_URL)
                .addHeader("Authorization", "Bearer " + apiKey)
                .post(body)
                .build();

            Response response = client.newCall(request).execute();
            try {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    JsonArray result = gson.fromJson(responseBody, JsonArray.class);
                    
                    if (result.size() > 0) {
                        return result.get(0).getAsJsonObject().get("summary_text").getAsString();
                    }
                }
            } finally {
                response.close();
            }
        } catch (IOException e) {
            // Fall back to "Not generated"
        }
        
        return "Not generated";
    }
}