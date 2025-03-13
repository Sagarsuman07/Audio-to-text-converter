package com.example.audiototext.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
    

    public String transcribeAudio(MultipartFile file, boolean separateSpeakers) throws IOException, InterruptedException {
        String uploadUrl = uploadFileToAssemblyAI(file);
        return getTranscription(uploadUrl, separateSpeakers);
    }

    
    

    private String uploadFileToAssemblyAI(MultipartFile file) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity("https://api.assemblyai.com/v2/upload", entity, Map.class);

        if (response.getBody() == null || !response.getBody().containsKey("upload_url")) {
            throw new RuntimeException("Failed to upload audio file.");
        }
        return response.getBody().get("upload_url").toString();
    }

    
    

    private String getTranscription(String uploadUrl, boolean separateSpeakers) throws InterruptedException {
        Map<String, Object> request = new HashMap<>();
        request.put("audio_url", uploadUrl);

        if (separateSpeakers) {
            request.put("speaker_labels", true);  // Enable speaker diarization
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        ResponseEntity<Map> response = new RestTemplate().postForEntity("https://api.assemblyai.com/v2/transcript", entity, Map.class);

        String transcriptId = (String) response.getBody().get("id");
        return pollTranscriptionResult(transcriptId, separateSpeakers);
    }


    
    
    
    private String pollTranscriptionResult(String transcriptId, boolean separateSpeakers) throws InterruptedException {
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
                if (separateSpeakers) {
                    return formatSpeakerText(response.getBody()); // Extract speaker-wise text
                } else {
                    return (String) response.getBody().get("text");
                }
            } else if ("failed".equals(status)) {
                return "Transcription failed.";
            }

            
            if(separateSpeakers)
            {
            	Thread.sleep(10000);
                attempt++;
            }
            else
            {
            	Thread.sleep(5000);
                attempt++;
            }
            
        }

        return "Transcription timed out.";
    }

    
    
    private String formatSpeakerText(Map<String, Object> transcriptionData) {
        List<Map<String, Object>> utterances = (List<Map<String, Object>>) transcriptionData.get("utterances");
        if (utterances == null) {
            return "No speaker data available.";
        }

        StringBuilder formattedText = new StringBuilder();
        for (Map<String, Object> utterance : utterances) {
            String speaker = "Speaker " + utterance.get("speaker");
            String text = (String) utterance.get("text");
            formattedText.append(speaker).append(": ").append(text).append("\n");
        }
        return formattedText.toString();
    }


}