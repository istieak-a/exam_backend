package com.university.exam.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileStorageService {

    private final Path rootLocation;

    public FileStorageService(@Value("${app.proctoring.video-dir}") String videoDir) {
        this.rootLocation = Paths.get(videoDir).toAbsolutePath().normalize();
    }

    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create proctoring video directory", ex);
        }
    }

    public String store(Long submissionId, MultipartFile file) {
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Cannot store empty file");
            }
            String filename = submissionId + "_" + System.currentTimeMillis() + ".webm";
            Path destination = rootLocation.resolve(filename).normalize();
            if (!destination.startsWith(rootLocation)) {
                throw new RuntimeException("Cannot store file outside of the designated directory");
            }
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("Failed to store proctoring video: " + ex.getMessage(), ex);
        }
    }

    public Path load(String filename) {
        Path file = rootLocation.resolve(filename).normalize();
        if (!file.startsWith(rootLocation)) {
            return null;
        }
        return file;
    }
}
