package com.example.audiototext.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpHeaders;
import com.example.audiototext.model.TranscriptionResponse;
import org.springframework.http.HttpMethod;

@Service
public class AssemblyAIService {

	
    private String apiKey=System.getenv("API_KEY");
    

    public String transcribeAudio(MultipartFile file) throws IOException, InterruptedException {
        String uploadUrl = uploadFileToAssemblyAI(file);
        return getTranscription(uploadUrl);
    }
    
    

    private String uploadFileToAssemblyAI(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        System.out.println(apiKey);
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity("https://api.assemblyai.com/v2/upload", entity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("upload_url")) {
            throw new RuntimeException("Failed to upload audio file.");
        }
        return response.getBody().get("upload_url").toString();
    }

    
    

    private String getTranscription(String uploadUrl) throws InterruptedException {
        Map<String, String> request = new HashMap<>();
        request.put("audio_url", uploadUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity("https://api.assemblyai.com/v2/transcript", entity, Map.class);

        String transcriptId = (String) response.getBody().get("id");

        return pollTranscriptionResult(transcriptId);
    }

    
    
    
    private String pollTranscriptionResult(String transcriptId) throws InterruptedException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);

        RestTemplate restTemplate = new RestTemplate();
        String transcriptUrl = "https://api.assemblyai.com/v2/transcript/" + transcriptId;

        int maxAttempts = 12; // Timeout after 1 minute (12 * 5s)
        int attempt = 0;

        while (attempt < maxAttempts) {
            ResponseEntity<Map> response = restTemplate.exchange(transcriptUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            if (response.getBody() == null) {
                return "Failed to fetch transcription.";
            }

            String status = (String) response.getBody().get("status");
            if ("completed".equals(status)) {
                return (String) response.getBody().get("text");
            } else if ("failed".equals(status)) {
                return "Transcription failed.";
            }

            Thread.sleep(5000);
            attempt++;
        }

        return "Transcription timed out.";
    }

}