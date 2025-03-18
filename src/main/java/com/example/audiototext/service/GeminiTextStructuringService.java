package com.example.audiototext.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiTextStructuringService {

    @Value("${gemini_api_key}")
    private String apiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String structureTranscription(String rawText) {
        // Construct the request body
        Map<String, Object> requestBody = new HashMap<>();
        
        // Create the parts array with the content
        List<Map<String, Object>> parts = new ArrayList<>();
        parts.add(Map.of("text", "Please analyze this transcribed text and format it with proper " +
                "structure. Create an appropriate title, headings, and subheadings. " +
                "Return the result as HTML with <h1> for the title, <h2> for main headings, " +
                "<h3> for subheadings, and <p> tags for paragraphs. Ensure the structure is logical " +
                "and reflects the content's organization:\n\n" + rawText));
        
        // Add parts to the contents array
        List<Map<String, Object>> contents = new ArrayList<>();
        contents.add(Map.of("parts", parts));
        
        requestBody.put("contents", contents);
        requestBody.put("generationConfig", Map.of(
            "temperature", 0.2,
            "topK", 32,
            "topP", 1,
            "maxOutputTokens", 8192
        ));
        
        // Create the HTTP entity with headers
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Content-Type", "application/json");
        
        org.springframework.http.HttpEntity<Map<String, Object>> entity = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);
        
        // Make the API call
        String response = restTemplate.postForObject(
                "https://generativelanguage.googleapis.com/v1/models/gemini-2.0-flash-001:generateContent?key=" + apiKey,
                entity,
                String.class
        );
        
        // Parse the response to extract the formatted text
        String formattedText = extractFormattedTextFromResponse(response);
        
        return formattedText;
    }
    
    private String extractFormattedTextFromResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return "<p>Error processing the transcription.</p>";
    }
}
