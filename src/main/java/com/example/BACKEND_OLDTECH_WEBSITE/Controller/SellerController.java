package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SellerService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductImageService;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sellers")
@CrossOrigin(origins = "*")
public class SellerController {

    private final SellerService sellerService;
    private final ProductImageService productImageService;

    @Autowired
    public SellerController(SellerService sellerService, ProductImageService productImageService) {
        this.sellerService = sellerService;
        this.productImageService = productImageService;
    }

    // Product Management Endpoints
    @PostMapping("/{sellerId}/products")
    public ResponseEntity<?> addProduct(@PathVariable Integer sellerId, @RequestBody Product product) {
        try {
            Product addedProduct = sellerService.addProduct(sellerId, product);
            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }

    @PutMapping("/{sellerId}/products/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer sellerId, @PathVariable Integer productId, @RequestBody Product product) {
        try {
            Product updatedProduct = sellerService.updateProduct(sellerId, productId, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }

    @DeleteMapping("/{sellerId}/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer sellerId, @PathVariable Integer productId) {
        try {
            sellerService.deleteProduct(sellerId, productId);
            return ResponseEntity.ok("Sản phẩm đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/{sellerId}/products")
    public ResponseEntity<?> getProducts(@PathVariable Integer sellerId) {
        try {
            List<Product> products = sellerService.getProductsBySeller(sellerId);
            return ResponseEntity.ok(products);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    // Review Management Endpoints
    @PostMapping("/{sellerId}/reviews/{reviewId}/respond")
    public ResponseEntity<?> respondToReview(@PathVariable Integer sellerId, @PathVariable Integer reviewId, @RequestBody String response) {
        try {
            Review review = sellerService.respondToReview(sellerId, reviewId, response);
            return ResponseEntity.ok(review);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi phản hồi đánh giá: " + e.getMessage());
        }
    }

    // Order Management Endpoints
    @GetMapping("/{sellerId}/sales")
    public ResponseEntity<?> getSalesHistory(@PathVariable Integer sellerId) {
        try {
            List<OrderDetail> salesHistory = sellerService.getSalesHistory(sellerId);
            return ResponseEntity.ok(salesHistory);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy lịch sử bán hàng: " + e.getMessage());
        }
    }

    @PutMapping("/{sellerId}/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer sellerId, @PathVariable Integer orderId, @RequestParam String status) {
        try {
            Orders updatedOrder = sellerService.updateOrderStatus(sellerId, orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }

    @PostMapping("/{sellerId}/orders/{orderId}/ship")
    public ResponseEntity<?> confirmOrderShipped(@PathVariable Integer sellerId, @PathVariable Integer orderId) {
        try {
            Orders shippedOrder = sellerService.confirmOrderShipped(sellerId, orderId);
            return ResponseEntity.ok(shippedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác nhận đã giao hàng: " + e.getMessage());
        }
    }

    // Statistics Endpoints
    @GetMapping("/{sellerId}/statistics/revenue")
    public ResponseEntity<?> getRevenueStatistics(@PathVariable Integer sellerId) {
        try {
            Map<String, Object> statistics = sellerService.getRevenueStatistics(sellerId);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
        }
    }

    // Account Management Endpoints
    @PutMapping("/{sellerId}/status")
    public ResponseEntity<?> toggleSellingActive(@PathVariable Integer sellerId, @RequestParam boolean active) {
        try {
            User user = sellerService.toggleSellingActive(sellerId, active);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái người bán: " + e.getMessage());
        }
    }

    @PutMapping("/{sellerId}/business-status")
    public ResponseEntity<?> updateBusinessStatus(@PathVariable Integer sellerId, @RequestParam byte status) {
        try {
            sellerService.updateBusinessStatus(sellerId, status);
            return ResponseEntity.ok("Trạng thái kinh doanh của người bán đã được cập nhật thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái kinh doanh: " + e.getMessage());
        }
    }

    // Product Image Management Endpoints
    @PostMapping(value = "/{sellerId}/products/{productId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadProductImages(
            @PathVariable Integer sellerId, 
            @PathVariable Integer productId,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("thumbnailIndex") Integer thumbnailIndex) {
        
        try {
            // Verify seller owns the product
            List<Product> sellerProducts = sellerService.getProductsBySeller(sellerId);
            boolean productBelongsToSeller = sellerProducts.stream()
                    .anyMatch(p -> p.getProductId().equals(productId));
            
            if (!productBelongsToSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Sản phẩm không thuộc về người bán này");
            }
            
            // Check minimum image requirement
            if (images.size() < 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Cần tải lên ít nhất 5 hình ảnh cho sản phẩm");
            }
            
            // Validate file sizes (max 10MB each)
            for (MultipartFile image : images) {
                if (image.getSize() > 10 * 1024 * 1024) { // 10MB in bytes
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Hình ảnh '" + image.getOriginalFilename() + "' vượt quá kích thước tối đa 10MB");
                }
                
                // Validate file type
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Tệp '" + image.getOriginalFilename() + "' không phải là hình ảnh");
                }
            }
            
            // Process image upload
            List<ProductImage> savedImages = productImageService.uploadProductImages(
                    productId, images, thumbnailIndex);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(savedImages);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xử lý hình ảnh: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tải lên hình ảnh sản phẩm: " + e.getMessage());
        }
    }
    
    @GetMapping("/{sellerId}/products/{productId}/images")
    public ResponseEntity<?> getProductImages(
            @PathVariable Integer sellerId,
            @PathVariable Integer productId) {
        
        try {
            // Verify seller owns the product
            List<Product> sellerProducts = sellerService.getProductsBySeller(sellerId);
            boolean productBelongsToSeller = sellerProducts.stream()
                    .anyMatch(p -> p.getProductId().equals(productId));
            
            if (!productBelongsToSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Sản phẩm không thuộc về người bán này");
            }
            
            List<ProductImage> images = productImageService.getProductImages(productId);
            return ResponseEntity.ok(images);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy hình ảnh sản phẩm: " + e.getMessage());
        }
    }
    
    @PutMapping("/{sellerId}/products/{productId}/images/{imageId}/thumbnail")
    public ResponseEntity<?> setProductThumbnail(
            @PathVariable Integer sellerId,
            @PathVariable Integer productId,
            @PathVariable Integer imageId) {
        
        try {
            // Verify seller owns the product
            List<Product> sellerProducts = sellerService.getProductsBySeller(sellerId);
            boolean productBelongsToSeller = sellerProducts.stream()
                    .anyMatch(p -> p.getProductId().equals(productId));
            
            if (!productBelongsToSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Sản phẩm không thuộc về người bán này");
            }
            
            productImageService.updateProductThumbnail(productId, imageId);
            return ResponseEntity.ok("Đã cập nhật ảnh đại diện cho sản phẩm");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi cập nhật ảnh đại diện: " + e.getMessage());
        }
    }
    
    @DeleteMapping("/{sellerId}/products/{productId}/images/{imageId}")
    public ResponseEntity<?> deleteProductImage(
            @PathVariable Integer sellerId,
            @PathVariable Integer productId,
            @PathVariable Integer imageId) {
        
        try {
            // Verify seller owns the product
            List<Product> sellerProducts = sellerService.getProductsBySeller(sellerId);
            boolean productBelongsToSeller = sellerProducts.stream()
                    .anyMatch(p -> p.getProductId().equals(productId));
            
            if (!productBelongsToSeller) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Sản phẩm không thuộc về người bán này");
            }
            
            // Check if product is approved
            Product product = sellerProducts.stream()
                    .filter(p -> p.getProductId().equals(productId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
            
            if (Boolean.TRUE.equals(product.getIsApproved()) || 
                    product.getStatus() == ProductStatusEnum.Approved) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể xóa hình ảnh của sản phẩm đã được duyệt bởi admin");
            }
            
            // Get current image count to maintain minimum requirement
            List<ProductImage> images = productImageService.getProductImages(productId);
            if (images.size() <= 5) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Không thể xóa hình ảnh này vì sản phẩm phải có ít nhất 5 hình ảnh");
            }
            
            productImageService.deleteProductImage(imageId);
            return ResponseEntity.ok("Đã xóa hình ảnh sản phẩm");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa hình ảnh sản phẩm: " + e.getMessage());
        }
    }
}