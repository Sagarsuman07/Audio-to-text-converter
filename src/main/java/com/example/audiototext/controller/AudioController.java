package com.example.audiototext.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.audiototext.service.AssemblyAIService;
import com.example.audiototext.service.TranslationService;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AssemblyAIService assemblyAIService;
    
    @Autowired
    private TranslationService translationService;
    
    

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadAudio(
            @RequestParam("file") MultipartFile file,
            @RequestParam("separateSpeakers") boolean separateSpeakers) {  // New parameter

        Map<String, String> response = new HashMap<>();
        try {
            String transcript = assemblyAIService.transcribeAudio(file, separateSpeakers); // Pass parameter
            response.put("transcription", transcript);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error processing audio file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    
    
 // New method to handle translation requests
    @PostMapping("/translate")
    public ResponseEntity<Map<String, String>> translateText(@RequestBody Map<String, String> request) {
        String text = request.get("text");
        String targetLanguage = request.get("targetLanguage");

        try {
            // Call your Translation Service
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
