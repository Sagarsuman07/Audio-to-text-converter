package com.example.audiototext.repository;

import com.example.audiototext.model.TranscriptionRecord;
import com.example.audiototext.model.User;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TranscriptionRecordRepository extends JpaRepository<TranscriptionRecord, Long> {
	List<TranscriptionRecord> findByUser(User user);

}
