package com.example.audiototext.controller;

import com.example.audiototext.model.TranscriptionRecord;
import com.example.audiototext.model.User;
import com.example.audiototext.repository.TranscriptionRecordRepository;
import com.example.audiototext.service.TranscriptionStorageService;

import jakarta.servlet.http.HttpSession;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/transcription")
public class TranscriptionStorageController {

    private final TranscriptionStorageService storageService;
    
    @Autowired
    private TranscriptionRecordRepository transcriptionRepository;

    @Autowired
    public TranscriptionStorageController(TranscriptionStorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveTranscription(
        HttpSession session,
        @RequestParam("transcription") String transcription,
        @RequestParam("file") MultipartFile file
    )
   {  
    	User user = (User) session.getAttribute("user");
    
    if (user == null) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in to save.");
    }

    try {
        storageService.saveTranscriptionForUser(user.getUsername(), transcription, file);
        return ResponseEntity.ok("Transcription saved successfully.");
    } catch (Exception e) {
        return ResponseEntity.status(500).body("Error saving transcription: " + e.getMessage());
    }
   }
    
    
    @GetMapping("/list")
    public ResponseEntity<?> listUserTranscriptions(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
        }

        List<TranscriptionRecord> records = transcriptionRepository.findByUser(user);

        List<Map<String, String>> response = records.stream().map(record -> {
            Map<String, String> map = new HashMap<>();
            map.put("transcription", record.getTranscription());
            map.put("audioBase64", Base64.getEncoder().encodeToString(record.getAudioFile()));
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    
}
