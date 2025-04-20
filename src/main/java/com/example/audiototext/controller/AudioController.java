package com.example.audiototext.controller;

import com.example.audiototext.service.AssemblyAIService;
import com.example.audiototext.service.TranslationService;
import com.example.audiototext.service.AudioProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AssemblyAIService assemblyAIService;

    @Autowired
    private TranslationService translationService;

    private static final int MAX_DURATION_SECONDS = 120;
    
    

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("separateSpeakers") boolean separateSpeakers) {

        Map<String, String> response = new HashMap<>();
        MultipartFile trimmedFile = null;

        try {
            // Trim the audio to max 2 minutes
            trimmedFile = AudioProcessor.trimAudio(file, MAX_DURATION_SECONDS);


            // Transcribe the audio
            String transcript = assemblyAIService.transcribeAudio(trimmedFile, separateSpeakers);
            
         // Convert audio to Base64
            String base64Audio = Base64.getEncoder().encodeToString(trimmedFile.getBytes());
            
            
         // Add both to response
            response.put("transcription", transcript);
            response.put("audioBase64", base64Audio);
            response.put("filename", trimmedFile.getOriginalFilename());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", "Error processing audio file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } 
    }
    
    

    @PostMapping("/translate")
    public ResponseEntity<Map<String, String>> translateText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String targetLanguage = request.get("targetLanguage");

        try {
            String translatedText = translationService.translateText(text, targetLanguage);
            Map<String, String> response = new HashMap<>();
            response.put("translatedText", translatedText);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Error translating text.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 
