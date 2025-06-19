package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductImageService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductImageController {
    private static final Logger logger = LoggerFactory.getLogger(ProductImageController.class);
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final int MAX_IMAGES_PER_PRODUCT = 10;
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final ProductImageService productImageService;

    @Autowired
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    /**
     * UPLOAD MULTIPLE IMAGES FOR A PRODUCT WITH VALIDATION
     * - Validates file size (max 10MB per file)
     * - Validates file type (images only)
     * - Saves all data to database with proper columns
     * - Sets display order and primary image
     */
    @PostMapping(value = "/{productId}/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Integer productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "thumbnailIndex", required = false, defaultValue = "0") Integer thumbnailIndex) {
        
        logger.info("Starting image upload for product ID: {} with {} files", productId, files.size());
        
        try {
            // Validate inputs
            Map<String, Object> validationResult = validateImageUpload(files, thumbnailIndex);
            if (!(boolean) validationResult.get("valid")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", validationResult.get("message"), "success", false));
            }

            // Check current image count
            List<ProductImage> existingImages = productImageService.getProductImages(productId);
            if (existingImages.size() + files.size() > MAX_IMAGES_PER_PRODUCT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Vượt quá số lượng ảnh tối đa cho sản phẩm (" + MAX_IMAGES_PER_PRODUCT + " ảnh)",
                        "success", false,
                        "currentImageCount", existingImages.size(),
                        "maxAllowed", MAX_IMAGES_PER_PRODUCT
                    ));
            }

            // Upload and save images
            List<ProductImage> savedImages = productImageService.uploadProductImages(productId, files, thumbnailIndex);

            // Prepare detailed response
            List<Map<String, Object>> imageResponses = savedImages.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã tải lên " + savedImages.size() + " ảnh thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("uploadedImages", imageResponses);
            response.put("totalImagesCount", existingImages.size() + savedImages.size());
            response.put("primaryImageIndex", thumbnailIndex);

            logger.info("Successfully uploaded {} images for product ID: {}", savedImages.size(), productId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            logger.error("Validation error during image upload for product {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (EntityNotFoundException e) {
            logger.error("Product not found during image upload: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (IOException e) {
            logger.error("IO error during image upload for product {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi lưu file: " + e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Unexpected error during image upload for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi hệ thống khi tải lên ảnh: " + e.getMessage(), "success", false));
        }
    }

    /**
     * UPLOAD SINGLE IMAGE FOR A PRODUCT
     */
    @PostMapping(value = "/{productId}/upload-single-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> uploadSingleProductImage(
            @PathVariable Integer productId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "setPrimary", required = false, defaultValue = "false") Boolean setPrimary) {
        
        logger.info("Starting single image upload for product ID: {}", productId);
        
        try {
            // Validate single file
            Map<String, Object> validationResult = validateSingleFile(file);
            if (!(boolean) validationResult.get("valid")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", validationResult.get("message"), "success", false));
            }

            // Check image count
            List<ProductImage> existingImages = productImageService.getProductImages(productId);
            if (existingImages.size() >= MAX_IMAGES_PER_PRODUCT) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                        "error", "Đã đạt số lượng ảnh tối đa cho sản phẩm (" + MAX_IMAGES_PER_PRODUCT + " ảnh)",
                        "success", false,
                        "currentImageCount", existingImages.size()
                    ));
            }

            // Upload single image
            List<MultipartFile> singleFileList = List.of(file);
            int thumbnailIndex = setPrimary ? 0 : -1; // -1 means don't set as primary
            List<ProductImage> savedImages = productImageService.uploadProductImages(productId, singleFileList, thumbnailIndex);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã tải lên ảnh thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("uploadedImage", convertToDetailedResponse(savedImages.get(0)));
            response.put("totalImagesCount", existingImages.size() + 1);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exception e) {
            logger.error("Error uploading single image for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi tải lên ảnh: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Get all images for a specific product with detailed information
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Integer productId) {
        try {
            logger.info("Fetching images for product ID: {}", productId);
            List<ProductImage> images = productImageService.getProductImages(productId);

            // Convert to detailed response
            List<Map<String, Object>> imageResponses = images.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productId", productId);
            response.put("images", imageResponses);
            response.put("totalCount", images.size());
            response.put("primaryImage", images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .findFirst()
                .map(this::convertToDetailedResponse)
                .orElse(null));

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Product not found when fetching images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Error fetching images for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi lấy ảnh sản phẩm: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Get the primary/thumbnail image for a product
     */
    @GetMapping("/{productId}/thumbnail")
    public ResponseEntity<?> getProductThumbnail(@PathVariable Integer productId) {
        try {
            logger.info("Fetching thumbnail for product ID: {}", productId);
            ProductImage thumbnail = productImageService.getProductThumbnail(productId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("productId", productId);
            response.put("thumbnail", convertToDetailedResponse(thumbnail));

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Thumbnail not found for product {}: {}", productId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Error fetching thumbnail for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi lấy ảnh chính của sản phẩm: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Set an image as the primary/thumbnail image
     */
    @PutMapping("/{productId}/images/{imageId}/set-primary")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> setPrimaryImage(
            @PathVariable Integer productId,
            @PathVariable Integer imageId) {
        try {
            logger.info("Setting primary image {} for product {}", imageId, productId);
            ProductImage primaryImage = productImageService.setPrimaryImage(productId, imageId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã đặt ảnh làm ảnh chính thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("primaryImage", convertToDetailedResponse(primaryImage));

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Image or product not found when setting primary: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument when setting primary image: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Error setting primary image {} for product {}: {}", imageId, productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi đặt ảnh chính: " + e.getMessage(), "success", false));
        }
    }    /**
     * Update display order of images (Feature coming soon - requires service implementation)
     */
    @PutMapping("/{productId}/images/reorder")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> updateImageOrder(
            @PathVariable Integer productId,
            @RequestBody Map<String, List<Integer>> orderRequest) {
        try {
            // TODO: Implement updateImageOrder method in ProductImageService
            return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
                .body(Map.of(
                    "error", "Tính năng sắp xếp lại thứ tự ảnh đang được phát triển",
                    "success", false,
                    "productId", productId
                ));
            
            /*
            List<Integer> imageIds = orderRequest.get("imageIds");
            if (imageIds == null || imageIds.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Danh sách ID ảnh không được trống", "success", false));
            }

            logger.info("Updating image order for product {} with order: {}", productId, imageIds);
            List<ProductImage> reorderedImages = productImageService.updateImageOrder(productId, imageIds);

            List<Map<String, Object>> imageResponses = reorderedImages.stream()
                .map(this::convertToDetailedResponse)
                .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã cập nhật thứ tự ảnh thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("images", imageResponses);

            return ResponseEntity.ok(response);
            */
        } catch (Exception e) {
            logger.error("Error updating image order for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi cập nhật thứ tự ảnh: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Delete a specific product image
     */
    @DeleteMapping("/{productId}/images/{imageId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable Integer productId,
            @PathVariable Integer imageId) {
        try {
            logger.info("Deleting image {} from product {}", imageId, productId);
            
            // Verify the image belongs to the product before deletion
            List<ProductImage> productImages = productImageService.getProductImages(productId);
            boolean imageExists = productImages.stream()
                .anyMatch(img -> img.getImageId().equals(imageId));
            
            if (!imageExists) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Ảnh không thuộc về sản phẩm này", "success", false));
            }

            productImageService.deleteProductImage(imageId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã xóa ảnh thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("deletedImageId", imageId);
            response.put("remainingImagesCount", productImages.size() - 1);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Image not found when deleting: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Error deleting image {} from product {}: {}", imageId, productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa ảnh sản phẩm: " + e.getMessage(), "success", false));
        }
    }

    /**
     * Delete all images for a product
     */
    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> deleteAllProductImages(@PathVariable Integer productId) {
        try {
            logger.info("Deleting all images for product {}", productId);
            List<ProductImage> existingImages = productImageService.getProductImages(productId);
            int deletedCount = existingImages.size();
            
            productImageService.deleteAllProductImages(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã xóa tất cả " + deletedCount + " ảnh của sản phẩm thành công");
            response.put("success", true);
            response.put("productId", productId);
            response.put("deletedImagesCount", deletedCount);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Product not found when deleting all images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (IllegalStateException e) {
            logger.error("Illegal state when deleting all images: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage(), "success", false));
        } catch (Exception e) {
            logger.error("Error deleting all images for product {}: {}", productId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa tất cả ảnh sản phẩm: " + e.getMessage(), "success", false));
        }
    }

    // Private helper methods

    /**
     * Validate multiple files upload
     */
    private Map<String, Object> validateImageUpload(List<MultipartFile> files, Integer thumbnailIndex) {
        Map<String, Object> result = new HashMap<>();
        
        if (files == null || files.isEmpty()) {
            result.put("valid", false);
            result.put("message", "Vui lòng chọn ít nhất một file ảnh");
            return result;
        }

        if (files.size() > MAX_IMAGES_PER_PRODUCT) {
            result.put("valid", false);
            result.put("message", "Chỉ được upload tối đa " + MAX_IMAGES_PER_PRODUCT + " ảnh cùng lúc");
            return result;
        }

        if (thumbnailIndex != null && (thumbnailIndex < 0 || thumbnailIndex >= files.size())) {
            result.put("valid", false);
            result.put("message", "Chỉ số ảnh chính không hợp lệ (0-" + (files.size() - 1) + ")");
            return result;
        }

        // Validate each file
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            Map<String, Object> fileValidation = validateSingleFile(file);
            if (!(boolean) fileValidation.get("valid")) {
                result.put("valid", false);
                result.put("message", "File " + (i + 1) + ": " + fileValidation.get("message"));
                return result;
            }
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Validate single file
     */
    private Map<String, Object> validateSingleFile(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        if (file == null || file.isEmpty()) {
            result.put("valid", false);
            result.put("message", "File không được trống");
            return result;
        }

        // Check file size (10MB limit)
        if (file.getSize() > MAX_FILE_SIZE) {
            result.put("valid", false);
            result.put("message", "Kích thước file vượt quá 10MB. File hiện tại: " + 
                String.format("%.2f MB", file.getSize() / (1024.0 * 1024.0)));
            return result;
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            result.put("valid", false);
            result.put("message", "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + 
                String.join(", ", ALLOWED_CONTENT_TYPES));
            return result;
        }

        // Check file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !hasValidImageExtension(originalFilename)) {
            result.put("valid", false);
            result.put("message", "Tên file không hợp lệ hoặc không có phần mở rộng ảnh");
            return result;
        }

        result.put("valid", true);
        return result;
    }

    /**
     * Check if filename has valid image extension
     */
    private boolean hasValidImageExtension(String filename) {
        String[] validExtensions = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
        String lowerFilename = filename.toLowerCase();
        for (String ext : validExtensions) {
            if (lowerFilename.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Convert ProductImage to detailed response with all database columns
     */
    private Map<String, Object> convertToDetailedResponse(ProductImage image) {
        Map<String, Object> response = new HashMap<>();
        response.put("imageId", image.getImageId());
        response.put("productId", image.getProduct().getProductId());
        response.put("imageUrl", image.getImageUrl());
        response.put("isPrimary", image.getIsPrimary());
        response.put("displayOrder", image.getDisplayOrder());
        response.put("createdAt", image.getCreatedAt());
        response.put("updatedAt", image.getUpdatedAt());
        response.put("sellerId", image.getSeller() != null ? image.getSeller().getSellerId() : null);
        return response;
    }
}

