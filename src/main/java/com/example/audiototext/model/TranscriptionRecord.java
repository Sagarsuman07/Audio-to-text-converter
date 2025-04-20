package com.example.audiototext.model;

import jakarta.persistence.*;

@Entity
public class TranscriptionRecord {

	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String fileName;

	    @Lob
	    @Column(columnDefinition = "LONGBLOB")
	    private byte[] audioFile;

	    @Lob
	    @Column(columnDefinition = "LONGTEXT")
	    private String transcription;

	    @ManyToOne
	    @JoinColumn(name = "user_id")
	    private User user;

	    // Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTranscription() {
		return transcription;
	}

	public void setTranscription(String transcription) {
		this.transcription = transcription;
	}

	public byte[] getAudioFile() {
		return audioFile;
	}

	public void setAudioFile(byte[] audioFile) {
		this.audioFile = audioFile;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

    
}
