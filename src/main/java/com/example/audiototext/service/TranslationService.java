package com.example.audiototext.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    public String translateText(String text, String targetLanguage) {
        try {
            // Initialize the Translate service
            Translate translate = TranslateOptions.getDefaultInstance().getService();

            // Perform translation
            Translation translation = translate.translate(
                    text,
                    Translate.TranslateOption.targetLanguage(targetLanguage)
            );

            // Return the translated text
            return translation.getTranslatedText();
        } catch (Exception e) {
            // Handle exceptions
            e.printStackTrace();
            return "Error during translation: " + e.getMessage();
        }
    }
}
