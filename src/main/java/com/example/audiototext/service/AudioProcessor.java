package com.example.audiototext.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class AudioProcessor {

    // Trim the audio file to a maximum duration (in seconds), return as MultipartFile (MP3)
    public static MultipartFile trimAudio(MultipartFile originalMultipart, int maxDurationInSeconds) throws Exception {
        // Save uploaded MultipartFile to a temp file
        File inputFile = File.createTempFile("original_", "_" + originalMultipart.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(inputFile)) {
            fos.write(originalMultipart.getBytes());
        }

        // Prepare output file (trimmed and converted to MP3)
        File trimmedFile = File.createTempFile("trimmed_", ".mp3");

        // Run FFmpeg to trim and convert to mp3
        ProcessBuilder pb = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-i", inputFile.getAbsolutePath(),
                "-t", String.valueOf(maxDurationInSeconds),
                "-vn", // no video
                "-ar", "44100", // audio sample rate
                "-ac", "2",     // audio channels
                "-b:a", "128k", // audio bitrate
                trimmedFile.getAbsolutePath()
        );

        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (InputStream is = process.getInputStream()) {
            is.transferTo(System.out); // Optional: print ffmpeg logs
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("FFmpeg failed to process audio.");
        }

        // Convert trimmed file back to MultipartFile (custom method)
        MultipartFile trimmedMultipart = fileToMultipartFile(trimmedFile, "trimmed_" + originalMultipart.getOriginalFilename());

        // Cleanup temp files
        inputFile.delete();
        trimmedFile.deleteOnExit(); // safer deletion

        return trimmedMultipart;
    }

    // Utility method: convert File to MultipartFile (anonymous class)
    private static MultipartFile fileToMultipartFile(File file, String originalName) throws IOException {
        return new MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return originalName; }
            @Override public String getContentType() { return "audio/mpeg"; } // for MP3
            @Override public boolean isEmpty() { return file.length() == 0; }
            @Override public long getSize() { return file.length(); }
            @Override public byte[] getBytes() throws IOException { return Files.readAllBytes(file.toPath()); }
            @Override public InputStream getInputStream() throws IOException { return new FileInputStream(file); }
            @Override public void transferTo(File dest) throws IOException {
                Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        };
    }
}
