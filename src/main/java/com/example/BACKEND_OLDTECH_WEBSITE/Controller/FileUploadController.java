package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/api/uploads")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private UserService userService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;
    
    @Value("${app.base-url:http://localhost:8080/oldtech}")
    private String baseUrl;

    @PostMapping("/verification-image")
    public ResponseEntity<?> uploadVerificationImage(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "verification-images");
    }
    
    @PostMapping("/profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        return uploadImage(file, "profile-pictures");
    }
    
    @PostMapping("/profile-picture/{userId}")
    public ResponseEntity<?> uploadAndUpdateProfilePicture(
            @RequestParam("file") MultipartFile file, 
            @PathVariable Integer userId) {
        try {
            ResponseEntity<?> uploadResponse = uploadImage(file, "profile-pictures");
            
            // If upload was successful, update the user's avatar_url
            if (uploadResponse.getStatusCode().is2xxSuccessful()) {
                @SuppressWarnings("unchecked")
                Map<String, String> responseBody = (Map<String, String>) uploadResponse.getBody();
                String imageUrl = responseBody.get("imageUrl");
                
                // Update user's avatar_url
                userService.updateProfilePicture(userId, imageUrl);
                
                return ResponseEntity.ok(responseBody);
            } else {
                return uploadResponse;
            }
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to update profile picture: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PostMapping("/product-images")
    public ResponseEntity<?> uploadProductImages(@RequestParam("files") MultipartFile[] files) {
        try {
            List<String> imageUrls = new ArrayList<>();
            
            for (MultipartFile file : files) {
                ResponseEntity<?> uploadResponse = uploadImage(file, "product-images");
                
                if (uploadResponse.getStatusCode().is2xxSuccessful()) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> responseBody = (Map<String, String>) uploadResponse.getBody();
                    imageUrls.add(responseBody.get("imageUrl"));
                } else {
                    return uploadResponse;
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("imageUrls", imageUrls);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Failed to upload product images: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private ResponseEntity<?> uploadImage(MultipartFile file, String subDirectory) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Vui lòng chọn tệp để tải lên");
                return ResponseEntity.badRequest().body(response);
            }

            // Check if file is an image with allowed extensions
            String contentType = file.getContentType();
            if (!isValidImageType(contentType)) {
                Map<String, String> response = new HashMap<>();
                response.put("error", "Chỉ chấp nhận file hình ảnh định dạng PNG, JPEG hoặc JPG");
                return ResponseEntity.badRequest().body(response);
            }

            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate a unique filename
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Save the file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Create response with the file URL
            String fileUrl = baseUrl + "/uploads/" + subDirectory + "/" + newFilename;
            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", fileUrl);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Lỗi khi tải lên tệp: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    private boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.equals("image/png") || 
               contentType.equals("image/jpeg") || 
               contentType.equals("image/jpg");
    }
    
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
} 