package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductListResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/oldtech/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /*--DÀNH CHO ADMIN--*/

    // LẤY DANH SÁCH SẢN PHẨM ĐANG CHỜ DUYỆT
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<List<Product>> getPendingProducts() {
        try {
            List<Product> pendingProducts = productService.getProductsByStatus(ProductStatusEnum.Pending);
            return ResponseEntity.ok(pendingProducts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    // XÁC THỰC SẢN PHẨM
    @PostMapping("/products/{productId}/verify")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> verifyProduct(@PathVariable Integer productId) {
        try {
            productService.verifyProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã được xác thực thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực sản phẩm: " + e.getMessage());
        }
    }

    // XÁC THỰC NHIỀU SẢN PHẨM
    @PostMapping("/batch/verify")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> verifyMultipleProducts(@RequestBody List<Integer> productIds) {
        try {
            productService.verifyMultipleProducts(productIds);
            return ResponseEntity.ok("Đã xác thực thành công " + productIds.size() + " sản phẩm");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực nhiều sản phẩm: " + e.getMessage());
        }
    }

    // TỪ CHỐI SẢN PHẨM
    @PostMapping("/products/{productId}/reject")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> rejectProduct(@PathVariable Integer productId) {
        try {
            productService.rejectProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã bị từ chối.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi từ chối sản phẩm: " + e.getMessage());
        }
    }

    // TỪ CHỐI NHIỀU SẢN PHẨM
    @PostMapping("/batch/reject")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> rejectMultipleProducts(@RequestBody List<Integer> productIds) {
        try {
            productService.rejectMultipleProducts(productIds);
            return ResponseEntity.ok("Đã từ chối " + productIds.size() + " sản phẩm");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi từ chối nhiều sản phẩm: " + e.getMessage());
        }
    }

    /*--DÀNH CHO NGƯỜI DÙNG NÓI CHUNG--*/

    // TÌM KIẾM SẢN PHẨM BẰNG TỪ KHÓA
    @GetMapping("/search")
    public ResponseEntity<List<ProductListResponse>> searchProducts(@RequestParam String keyword) {
        try {
            List<Product> products = productService.searchProducts(keyword);
            List<ProductListResponse> response = products.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // LẤY THÔNG TIN SẢN PHẨM BẰNG ID
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponse> getProductDetails(@PathVariable Integer productId) {
        try {
            Product product = productService.getProductById(productId);
            ProductResponse response = convertToResponse(product);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    // TÌM KIẾM DANH SÁCH TOÀN BỘ SẢN PHẨM
    @GetMapping("/products")
    public ResponseEntity<List<ProductListResponse>> getAllProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            List<ProductListResponse> response = products.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /*--DÀNH CHO NGƯỜI BÁN HOẶC ADMIN--*/

    // ẨN SẢN PHẨM
    @PostMapping("/products/{productId}/hide")
    @PreAuthorize("hasAnyAuthority('Seller', 'Admin', 'SuperAdmin')")
    public ResponseEntity<?> hideProduct(@PathVariable Integer productId) {
        try {
            productService.hideProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã bị ẩn.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi ẩn sản phẩm: " + e.getMessage());
        }
    }

    // ẨN TOÀN BỘ SẢN PHẨM CỦA MỘT NGƯỜI BÁN
    @PostMapping("/seller/{sellerId}/hide-all")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> hideAllProductsFromSeller(@PathVariable Integer sellerId) {
        try {
            int count = productService.hideAllProductsFromSeller(sellerId);
            return ResponseEntity.ok("Đã ẩn " + count + " sản phẩm của người bán có ID " + sellerId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi ẩn sản phẩm của người bán: " + e.getMessage());
        }
    }

    // CẬP NHẬT DANH MỤC SẢN PHẨM
    @PostMapping("/products/{productId}/category")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> setProductCategory(@PathVariable Integer productId, @RequestParam Integer categoryId) {
        try {
            productService.setProductCategory(productId, categoryId);
            return ResponseEntity.ok("Danh mục của sản phẩm " + productId + " đã được cập nhật thành " + categoryId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật danh mục sản phẩm: " + e.getMessage());
        }
    }

    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId().intValue());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus().toString());
        response.setIsApproved(product.getIsApproved());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    private ProductListResponse convertToListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setProductId(product.getProductId());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId().intValue());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus().toString());
        return response;
    }
}
