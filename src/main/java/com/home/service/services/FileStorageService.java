package com.home.service.services;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.config.exceptions.BadRequestException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final String uploadDir = "/opt/uploads/";
    // private final String uploadDir = "uploads/";

    public FileStorageService() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }
    }

    public String storeFile(MultipartFile file) {
        if (file == null) {
            throw new BadRequestException("No file was provided for upload.");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty.");
        }

        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(uploadDir + fileName);

            Files.copy(file.getInputStream(), filePath);
            return fileName; // Return the filename, so it can be used for serving later
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }
}
