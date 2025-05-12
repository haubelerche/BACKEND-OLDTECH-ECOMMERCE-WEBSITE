package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductImageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/product-images")
@CrossOrigin(origins = "*")
public class ProductImageController {

    private final ProductImageService productImageService;

    @Autowired
    public ProductImageController(ProductImageService productImageService) {
        this.productImageService = productImageService;
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductImages(@PathVariable Integer productId) {
        try {
            List<ProductImage> images = productImageService.getProductImages(productId);
            return ResponseEntity.ok(images);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy ảnh sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/{productId}")
    public ResponseEntity<?> addProductImages(@PathVariable Integer productId, @RequestBody List<String> imageUrls) {
        try {
            List<ProductImage> savedImages = productImageService.saveProductImages(productId, imageUrls);
            return ResponseEntity.ok(savedImages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi thêm ảnh sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/{productId}/primary/{imageId}")
    public ResponseEntity<?> setPrimaryImage(@PathVariable Integer productId, @PathVariable Integer imageId) {
        try {
            ProductImage primaryImage = productImageService.setPrimaryImage(productId, imageId);
            return ResponseEntity.ok(primaryImage);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đặt ảnh chính: " + e.getMessage());
        }
    }

    @DeleteMapping("/{imageId}")
    public ResponseEntity<?> deleteProductImage(@PathVariable Integer imageId) {
        try {
            productImageService.deleteProductImage(imageId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa ảnh thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa ảnh sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<?> deleteAllProductImages(@PathVariable Integer productId) {
        try {
            productImageService.deleteAllProductImages(productId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đã xóa tất cả ảnh của sản phẩm thành công");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa tất cả ảnh sản phẩm: " + e.getMessage());
        }
    }
} 