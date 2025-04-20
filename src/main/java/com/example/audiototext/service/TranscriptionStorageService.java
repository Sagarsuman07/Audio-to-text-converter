package com.example.audiototext.service;

import com.example.audiototext.model.TranscriptionRecord;
import com.example.audiototext.model.User;
import com.example.audiototext.repository.TranscriptionRecordRepository;
import com.example.audiototext.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TranscriptionStorageService {

    private final TranscriptionRecordRepository transcriptionRepo;
    private final UserRepository userRepo;

    @Autowired
    public TranscriptionStorageService(TranscriptionRecordRepository transcriptionRepo, UserRepository userRepo) {
        this.transcriptionRepo = transcriptionRepo;
        this.userRepo = userRepo;
    }

    public void saveTranscriptionForUser(String username, String transcription, MultipartFile file) throws Exception {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new Exception("User not found");
        }

        TranscriptionRecord record = new TranscriptionRecord();
        record.setUser(user);
        record.setTranscription(transcription);
        record.setAudioFile(file.getBytes());
        record.setFileName(file.getOriginalFilename());

        transcriptionRepo.save(record);
    }
}
