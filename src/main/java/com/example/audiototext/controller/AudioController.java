package com.example.audiototext.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.audiototext.service.AssemblyAIService;

@RestController
@RequestMapping("/api/audio")
public class AudioController {

    @Autowired
    private AssemblyAIService assemblyAIService;

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        Map<String, String> response = new HashMap<>();
        try {
            String transcript = assemblyAIService.transcribeAudio(file);
            response.put("transcription", transcript);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Error processing audio file.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}