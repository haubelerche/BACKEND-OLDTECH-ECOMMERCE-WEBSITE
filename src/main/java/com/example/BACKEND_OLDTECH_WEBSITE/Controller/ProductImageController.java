package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Image.ProductImageRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Image.ProductImageResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductImageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Autowired
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }





    /**
     * THEM ANH ĐƠN LE BANG TAI LEN FILE
     */
    @PostMapping(value = "/{productId}/upload-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Integer productId,
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("thumbnailIndex") Integer thumbnailIndex) {
        try {
            List<ProductImage> savedImages = productImageService.uploadProductImages(productId, files, thumbnailIndex);

            // Map to DTO for clean response
            List<ProductImageResponse> responses = savedImages.stream()
                    .map(img -> {
                        ProductImageResponse response = new ProductImageResponse();
                        response.setImageUrl(img.getImageUrl());
                        return response;
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tải lên hình ảnh: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi khi tải lên hình ảnh sản phẩm: " + e.getMessage()));
        }
    }


    /**
     * Get all images for a specific product
     */
    @GetMapping("/{productId}/images")
    public ResponseEntity<?> getProductImages(@PathVariable Integer productId) {
        try {
            List<ProductImage> images = productImageService.getProductImages(productId);

            // Map to DTO for clean response
            List<ProductImageResponse> responses = images.stream()
                .map(img -> {
                    ProductImageResponse response = new ProductImageResponse();
                    response.setImageUrl(img.getImageUrl());
                    return response;
                })
                .collect(Collectors.toList());

            return ResponseEntity.ok(responses);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi lấy ảnh sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Get the primary/thumbnail image for a product
     */
    @GetMapping("/{productId}/thumbnail")
    public ResponseEntity<?> getProductThumbnail(@PathVariable Integer productId) {
        try {
            ProductImage thumbnail = productImageService.getProductThumbnail(productId);

            ProductImageResponse response = new ProductImageResponse();
            response.setImageUrl(thumbnail.getImageUrl());

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi lấy ảnh chính của sản phẩm: " + e.getMessage()));
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
            ProductImage primaryImage = productImageService.setPrimaryImage(productId, imageId);

            ProductImageResponse response = new ProductImageResponse();
            response.setImageUrl(primaryImage.getImageUrl());

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi đặt ảnh chính: " + e.getMessage()));
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
            // Verify the image belongs to the product before deletion
            productImageService.getProductImages(productId)
                .stream()
                .filter(img -> img.getImageId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Ảnh không thuộc về sản phẩm này"));

            productImageService.deleteProductImage(imageId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa ảnh thành công");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa ảnh sản phẩm: " + e.getMessage()));
        }
    }

    /**
     * Delete all images for a product
     */
    @DeleteMapping("/{productId}/images")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> deleteAllProductImages(@PathVariable Integer productId) {
        try {
            productImageService.deleteAllProductImages(productId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa tất cả ảnh của sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Lỗi khi xóa tất cả ảnh sản phẩm: " + e.getMessage()));
        }
    }
}

