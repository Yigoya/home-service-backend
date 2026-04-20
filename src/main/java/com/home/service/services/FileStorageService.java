package com.home.service.services;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.home.service.config.exceptions.BadRequestException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private final String uploadDir = "/opt/uploads/";
    // private final String uploadDir = "uploads/";

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "svg", "svgz", "html", "htm", "xhtml", "js", "mjs");

    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp");

    public FileStorageService() {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory if it doesn't exist
        }
    }

    public String storeFile(MultipartFile file) {
        return storeFileInternal(file, false);
    }

    public String storeImageFile(MultipartFile file) {
        return storeFileInternal(file, true);
    }

    private String storeFileInternal(MultipartFile file, boolean imageOnly) {
        if (file == null) {
            throw new BadRequestException("No file was provided for upload.");
        }

        if (file.isEmpty()) {
            throw new BadRequestException("Uploaded file is empty.");
        }

        try {
            String originalFilename = StringUtils.cleanPath(
                    Optional.ofNullable(file.getOriginalFilename()).orElse("file"));

            if (originalFilename.contains("..")) {
                throw new BadRequestException("Invalid file name.");
            }

            String extension = getExtension(originalFilename);

            if (DANGEROUS_EXTENSIONS.contains(extension)) {
                throw new BadRequestException("Unsupported file type.");
            }

            if (containsScriptableContent(file)) {
                throw new BadRequestException("Unsupported or unsafe file content.");
            }

            if (imageOnly && !ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
                throw new BadRequestException("Only JPG, JPEG, PNG, GIF, and WEBP images are allowed.");
            }

            String fileName = System.currentTimeMillis() + "_" + UUID.randomUUID()
                    + (extension.isEmpty() ? "" : "." + extension);

            Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = rootPath.resolve(fileName).normalize();

            if (!filePath.startsWith(rootPath)) {
                throw new BadRequestException("Invalid storage path.");
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName; // Return the filename, so it can be used for serving later
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    private String getExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0 || lastDot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }

    private boolean containsScriptableContent(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] sample = inputStream.readNBytes(4096);
            if (sample.length == 0) {
                return false;
            }
            String head = new String(sample, StandardCharsets.UTF_8).toLowerCase(Locale.ROOT);
            return head.contains("<svg")
                    || head.contains("<script")
                    || head.contains("onload=")
                    || head.contains("onerror=")
                    || head.contains("javascript:");
        } catch (IOException e) {
            throw new BadRequestException("Could not inspect uploaded file content.");
        }
    }
}
