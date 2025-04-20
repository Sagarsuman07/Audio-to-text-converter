package com.example.audiototext.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password; // For demonstration only — ideally, this should be hashed.

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<TranscriptionRecord> transcriptions;

    
 // Getters and setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<TranscriptionRecord> getTranscriptions() {
		return transcriptions;
	}

	public void setTranscriptions(List<TranscriptionRecord> transcriptions) {
		this.transcriptions = transcriptions;
	}

    
}
