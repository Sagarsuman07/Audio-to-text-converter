package com.example.audiototext.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.audiototext.model.User;
import com.example.audiototext.service.AuthService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user, HttpSession session) {
        User newUser = authService.registerUser(user);
        session.setAttribute("user", newUser);
        return ResponseEntity.ok("Signup successful");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user, HttpSession session) {
        User existingUser = authService.loginUser(user.getUsername(), user.getPassword());
        if (existingUser != null) {
            session.setAttribute("user", existingUser);
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> currentUser(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user != null) {
            // Return a safe DTO (Data Transfer Object)
            return ResponseEntity.ok(Map.of(
                "id", user.getId(),
                "username", user.getUsername()
            ));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not logged in");
    }


    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logged out successfully");
    }
    
    @GetMapping("/status")
    public ResponseEntity<?> checkLoginStatus(HttpSession session) {
        User user = (User) session.getAttribute("user");
        boolean isLoggedIn = user != null;
        return ResponseEntity.ok().body(java.util.Map.of("loggedIn", isLoggedIn));
    }

}

