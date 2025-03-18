package com.example.audiototext.controller;

import com.example.audiototext.service.GeminiTextStructuringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/text")
public class GeminiController {

    private final GeminiTextStructuringService geminiService;
    
    @Autowired
    public GeminiController(GeminiTextStructuringService geminiService) {
        this.geminiService = geminiService;
    }
    
    @PostMapping("/format")
    public String formatText(@RequestParam("rawText") String rawText, Model model) {
        try {
            String formattedText = geminiService.structureTranscription(rawText);
            model.addAttribute("formattedContent", formattedText);
            return "formatted-text"; // This refers to the Thymeleaf template name
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error processing your text. Please try again.");
            return "error-page"; // This refers to the error template
        }
    }
}