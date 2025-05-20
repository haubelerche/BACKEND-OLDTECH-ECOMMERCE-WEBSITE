package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Exception.GlobalExceptionHandler.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    // List of allowed file extensions
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "pdf", "doc", "docx", "xls", "xlsx", "txt"
    );

    // Maximum file size in bytes (10MB)
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir)
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Validate file
        validateFile(file);

        // Generate unique filename
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + "." + extension;

        try {
            // Check if the filename contains invalid characters
            if (filename.contains("..")) {
                throw new RuntimeException("Filename contains invalid path sequence " + filename);
            }

            // Copy file to the target location (replacing existing file with the same name)
            Path targetLocation = this.fileStorageLocation.resolve(filename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("File stored successfully: {}", filename);
            return filename;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + filename + ". Please try again!", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new RuntimeException("Failed to store empty file.");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File size exceeds maximum allowed size of 10MB.");
        }

        // Check file extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new RuntimeException("File type not allowed. Allowed types: " +
                    String.join(", ", ALLOWED_EXTENSIONS));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    public Path getFilePath(String filename) {
        return fileStorageLocation.resolve(filename).normalize();
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted successfully: {}", filename);
        } catch (IOException ex) {
            log.error("Error deleting file: {}", filename, ex);
            throw new RuntimeException("Could not delete file " + filename, ex);
        }
    }
}
