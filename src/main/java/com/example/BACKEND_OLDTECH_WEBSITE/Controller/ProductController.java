package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductListResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CartItemService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);    @Autowired
    private ProductService productService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CartItemService cartItemService;

    /*--ADMIN OPERATIONS--*/

    /**
     * Get all pending products for admin review
     */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> getPendingProducts() {
        try {
            logger.info("Admin requesting pending products");
            List<Product> pendingProducts = productService.getProductsByStatus(ProductStatusEnum.Pending);
            
            List<ProductListResponse> response = pendingProducts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Lấy danh sách sản phẩm chờ duyệt thành công");
            result.put("products", response);
            result.put("totalCount", response.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting pending products: {}", e.getMessage(), e);
            return handleException("Lỗi khi lấy danh sách sản phẩm chờ duyệt", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Verify a single product
     */
    @PutMapping("/verify/{productId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> verifyProduct(@PathVariable Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            logger.info("Admin verifying product ID: {}", productId);
            productService.verifyProduct(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm " + productId + " đã được xác thực thành công");
            response.put("productId", productId);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Product not found for verification: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error verifying product {}: {}", productId, e.getMessage(), e);
            return handleException("Lỗi khi xác thực sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * Reject a single product
     * Also removes the product from all users' carts
     */
    @PutMapping("/reject/{productId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> rejectProduct(@PathVariable Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            // Get product first to get its name
            Product product = productService.getProductById(productId);
            
            logger.info("Admin rejecting product ID: {} - '{}'", productId, product.getName());
            productService.rejectProduct(productId);
            
            // Remove product from all carts since it's now rejected
            int removedFromCarts = removeProductFromAllCarts(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã bị từ chối và xóa khỏi giỏ hàng của người dùng");
            response.put("productId", productId);
            response.put("productName", product.getName());
            response.put("removedFromCartsCount", removedFromCarts);
            
            logger.info("Product {} rejected successfully and removed from {} carts", productId, removedFromCarts);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Product not found for rejection: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error rejecting product {}: {}", productId, e.getMessage(), e);
            return handleException("Lỗi khi từ chối sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Hide all products from a specific seller (Admin only)
     */
    @PutMapping("/seller/hide-all/{sellerId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> hideAllProductsFromSeller(@PathVariable Integer sellerId) {
        try {
            if (sellerId == null || sellerId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID người bán không hợp lệ"));
            }
            
            logger.info("Admin hiding all products from seller ID: {}", sellerId);
            int count = productService.hideAllProductsFromSeller(sellerId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã ẩn " + count + " sản phẩm của người bán");
            response.put("sellerId", sellerId);
            response.put("hiddenCount", count);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error hiding products from seller {}: {}", sellerId, e.getMessage(), e);
            return handleException("Lỗi khi ẩn sản phẩm của người bán", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Update product category (Admin only)
     */
    @PutMapping("/category/{productId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> setProductCategory(@PathVariable Integer productId, 
                                              @RequestParam Integer categoryId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            if (categoryId == null || categoryId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID danh mục không hợp lệ"));
            }
            
            logger.info("Admin updating category for product {} to category {}", productId, categoryId);
            productService.setProductCategory(productId, categoryId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Danh mục của sản phẩm đã được cập nhật thành công");
            response.put("productId", productId);
            response.put("categoryId", categoryId);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found when updating category: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating product category: {}", e.getMessage(), e);
            return handleException("Lỗi khi cập nhật danh mục sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }







    /*--PUBLIC OPERATIONS--*/

    /**
     * Search products by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchProducts(@RequestParam String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Từ khóa tìm kiếm không được trống"));
            }
            
            logger.info("Searching products with keyword: {}", keyword);
            List<Product> products = productService.searchProducts(keyword.trim());
            
            // Only return visible and approved products for public search
            List<ProductListResponse> response = products.stream()
                    .filter(product -> Boolean.TRUE.equals(product.getIsVisible()) && 
                                     Boolean.TRUE.equals(product.getIsApproved()))
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tìm kiếm thành công");
            result.put("keyword", keyword);
            result.put("products", response);
            result.put("totalCount", response.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error searching products with keyword '{}': {}", keyword, e.getMessage(), e);
            return handleException("Lỗi khi tìm kiếm sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get product details by ID
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductDetails(@PathVariable Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            logger.info("Getting product details for ID: {}", productId);
            Product product = productService.getProductById(productId);
            
            // Check if product is visible to public
            if (!Boolean.TRUE.equals(product.getIsVisible()) || 
                !Boolean.TRUE.equals(product.getIsApproved())) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Sản phẩm không tồn tại hoặc chưa được duyệt"));
            }
            
            ProductResponse response = convertToResponse(product);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Lấy thông tin sản phẩm thành công");
            result.put("product", response);
            
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            logger.error("Product not found with ID: {}", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", "Không tìm thấy sản phẩm"));
        } catch (Exception e) {
            logger.error("Error getting product details for ID {}: {}", productId, e.getMessage(), e);
            return handleException("Lỗi khi lấy thông tin sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get all visible and approved products
     */
    @GetMapping("/list")
    public ResponseEntity<?> getAllProducts() {
        try {
            logger.info("Getting all visible products");
            List<Product> products = productService.getAllProducts();
            
            // Filter only visible and approved products for public access
            List<ProductListResponse> response = products.stream()
                    .filter(product -> Boolean.TRUE.equals(product.getIsVisible()) && 
                                     Boolean.TRUE.equals(product.getIsApproved()))
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Lấy danh sách sản phẩm thành công");
            result.put("products", response);
            result.put("totalCount", response.size());
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting all products: {}", e.getMessage(), e);
            return handleException("Lỗi khi lấy danh sách sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Get products with sorting and filtering options
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getProductsWithFilters(
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String keyword) {
        try {
            logger.info("Getting products with filters - sortBy: {}, sortOrder: {}, minPrice: {}, maxPrice: {}, location: {}, keyword: {}", 
                       sortBy, sortOrder, minPrice, maxPrice, location, keyword);
            
            List<Product> products = productService.getAllProducts();
            
            // Filter only visible and approved products
            List<Product> filteredProducts = products.stream()
                    .filter(product -> Boolean.TRUE.equals(product.getIsVisible()) && 
                                     Boolean.TRUE.equals(product.getIsApproved()))
                    .collect(Collectors.toList());
            
            // Apply keyword filter if provided
            if (keyword != null && !keyword.trim().isEmpty()) {
                filteredProducts = filteredProducts.stream()
                        .filter(product -> 
                            product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                            product.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(Collectors.toList());
            }
            
            // Apply price range filter if provided
            if (minPrice != null && !minPrice.trim().isEmpty()) {
                try {
                    BigDecimal minPriceValue = new BigDecimal(minPrice);
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getPrice().compareTo(minPriceValue) >= 0)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid minPrice format: {}", minPrice);
                }
            }
            
            if (maxPrice != null && !maxPrice.trim().isEmpty()) {
                try {
                    BigDecimal maxPriceValue = new BigDecimal(maxPrice);
                    filteredProducts = filteredProducts.stream()
                            .filter(product -> product.getPrice().compareTo(maxPriceValue) <= 0)
                            .collect(Collectors.toList());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid maxPrice format: {}", maxPrice);
                }
            }
            
            // Apply location filter if provided (filter by seller's location)
            if (location != null && !location.trim().isEmpty()) {
                filteredProducts = productService.filterProductsBySellerLocation(filteredProducts, location);
            }
            
            // Apply sorting
            if (sortBy != null && !sortBy.trim().isEmpty()) {
                switch (sortBy.toLowerCase()) {
                    case "price":
                        if ("desc".equalsIgnoreCase(sortOrder)) {
                            filteredProducts.sort(Comparator.comparing(Product::getPrice).reversed());
                        } else {
                            filteredProducts.sort(Comparator.comparing(Product::getPrice));
                        }
                        break;
                    case "name":
                        if ("desc".equalsIgnoreCase(sortOrder)) {
                            filteredProducts.sort(Comparator.comparing(Product::getName).reversed());
                        } else {
                            filteredProducts.sort(Comparator.comparing(Product::getName));
                        }
                        break;
                    case "created":
                    case "date":
                        if ("desc".equalsIgnoreCase(sortOrder)) {
                            filteredProducts.sort(Comparator.comparing(Product::getCreatedAt).reversed());
                        } else {
                            filteredProducts.sort(Comparator.comparing(Product::getCreatedAt));
                        }
                        break;
                    default:
                        logger.warn("Unknown sort field: {}", sortBy);
                        break;
                }
            }
            
            List<ProductListResponse> response = filteredProducts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Lấy danh sách sản phẩm với bộ lọc thành công");
            result.put("products", response);
            result.put("totalCount", response.size());
            result.put("filters", Map.of(
                "sortBy", sortBy != null ? sortBy : "",
                "sortOrder", sortOrder,
                "minPrice", minPrice != null ? minPrice : "",
                "maxPrice", maxPrice != null ? maxPrice : "",
                "location", location != null ? location : "",
                "keyword", keyword != null ? keyword : ""
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error getting products with filters: {}", e.getMessage(), e);
            return handleException("Lỗi khi lấy sản phẩm với bộ lọc", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Search products near user's location (advanced search)
     */
    @GetMapping("/search-nearby")
    public ResponseEntity<?> searchProductsNearby(
            @RequestParam(required = false) String userLocation,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "50") Integer radiusKm,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
        try {
            logger.info("Searching products nearby - location: {}, keyword: {}, radius: {}km", 
                       userLocation, keyword, radiusKm);
            
            List<Product> products;
            if (keyword != null && !keyword.trim().isEmpty()) {
                products = productService.searchProducts(keyword.trim());
            } else {
                products = productService.getAllProducts();
            }
            
            // Filter only visible and approved products
            List<Product> filteredProducts = products.stream()
                    .filter(product -> Boolean.TRUE.equals(product.getIsVisible()) && 
                                     Boolean.TRUE.equals(product.getIsApproved()))
                    .collect(Collectors.toList());
            
            // Filter by location proximity if userLocation is provided
            if (userLocation != null && !userLocation.trim().isEmpty()) {
                filteredProducts = productService.filterProductsByLocationProximity(
                    filteredProducts, userLocation, radiusKm);
            }
            
            // Apply pagination
            int startIndex = page * size;
            int endIndex = Math.min(startIndex + size, filteredProducts.size());
            List<Product> paginatedProducts = filteredProducts.subList(startIndex, endIndex);
            
            List<ProductListResponse> response = paginatedProducts.stream()
                    .map(this::convertToListResponse)
                    .collect(Collectors.toList());
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Tìm kiếm sản phẩm lân cận thành công");
            result.put("products", response);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("totalCount", filteredProducts.size());
            result.put("totalPages", (int) Math.ceil((double) filteredProducts.size() / size));
            result.put("searchCriteria", Map.of(
                "userLocation", userLocation != null ? userLocation : "",
                "keyword", keyword != null ? keyword : "",
                "radiusKm", radiusKm
            ));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error searching products nearby: {}", e.getMessage(), e);
            return handleException("Lỗi khi tìm kiếm sản phẩm lân cận", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /*--SELLER OPERATIONS--*/    /**
     * Hide a product (Seller or Admin)
     * Also removes the product from all users' carts
     */
    @PutMapping("/{productId}/hide")
    @PreAuthorize("hasAnyAuthority('Seller', 'Admin', 'SuperAdmin')")
    public ResponseEntity<?> hideProduct(@PathVariable Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "ID sản phẩm không hợp lệ"));
            }
            
            // Get product first to verify it exists
            Product product = productService.getProductById(productId);
            
            // For sellers, verify they own the product
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser != null && "Seller".equals(currentUser.getRole().toString())) {
                if (!product.getSellerId().equals(currentUser.getUserId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "message", "Bạn không có quyền ẩn sản phẩm này"));
                }
            }
            
            logger.info("Hiding product ID: {} - '{}'", productId, product.getName());
            
            // Hide the product first
            productService.hideProduct(productId);
            
            // Remove product from all carts
            int removedFromCarts = removeProductFromAllCarts(productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Sản phẩm đã được ẩn thành công và xóa khỏi giỏ hàng của người dùng");
            response.put("productId", productId);
            response.put("productName", product.getName());
            response.put("removedFromCartsCount", removedFromCarts);
            
            logger.info("Product {} hidden successfully and removed from {} carts", productId, removedFromCarts);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Product not found for hiding: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error hiding product {}: {}", productId, e.getMessage(), e);
            return handleException("Lỗi khi ẩn sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Reject multiple products in batch
     * Also removes all rejected products from all users' carts
     */
    @PutMapping("/batch/reject")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> rejectMultipleProducts(@RequestBody List<Integer> productIds) {
        try {
            if (productIds == null || productIds.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Danh sách ID sản phẩm không được trống"));
            }
            
            // Validate all IDs
            boolean hasInvalidId = productIds.stream().anyMatch(id -> id == null || id <= 0);
            if (hasInvalidId) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Có ID sản phẩm không hợp lệ trong danh sách"));
            }
            
            logger.info("Admin rejecting {} products", productIds.size());
            productService.rejectMultipleProducts(productIds);
            
            // Remove all rejected products from carts
            int totalRemovedFromCarts = 0;
            for (Integer productId : productIds) {
                totalRemovedFromCarts += removeProductFromAllCarts(productId);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã từ chối " + productIds.size() + " sản phẩm và xóa khỏi giỏ hàng của người dùng");
            response.put("rejectedCount", productIds.size());
            response.put("productIds", productIds);
            response.put("removedFromCartsCount", totalRemovedFromCarts);
            
            logger.info("Rejected {} products and removed them from {} total cart entries", productIds.size(), totalRemovedFromCarts);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error rejecting multiple products: {}", e.getMessage(), e);
            return handleException("Lỗi khi từ chối nhiều sản phẩm", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }








    // Helper methods

    /**
     * Convert Product entity to ProductResponse DTO
     */
    private ProductResponse convertToResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setProductId(product.getProductId());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus() != null ? product.getStatus().toString() : null);
        response.setIsApproved(product.getIsApproved());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    /**
     * Convert Product entity to ProductListResponse DTO
     */
    private ProductListResponse convertToListResponse(Product product) {
        ProductListResponse response = new ProductListResponse();
        response.setProductId(product.getProductId());
        response.setSellerId(product.getSellerId());
        response.setCategoryId(product.getCategoryId() != null ? product.getCategoryId().intValue() : null);
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStatus(product.getStatus() != null ? product.getStatus().toString() : null);
        return response;
    }

    /**
     * Handle exceptions with consistent error response format
     */
    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message + ": " + e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, status);
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                String userEmail = authentication.getName();
                return userService.findUserByEmail(userEmail);
            }
        } catch (Exception e) {
            logger.warn("Could not get current authenticated user: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Remove a product from all users' carts
     * This is called when a product is hidden, rejected, or deleted
     */
    private int removeProductFromAllCarts(Integer productId) {
        try {
            logger.info("Removing product {} from all carts", productId);
            return cartItemService.removeProductFromAllCarts(productId);
        } catch (Exception e) {
            logger.error("Error removing product {} from carts: {}", productId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Check if current user has authority to access seller functions
     */
    private boolean hasSellerAuthority() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getAuthorities() != null) {
                return authentication.getAuthorities().stream()
                    .anyMatch(authority -> 
                        "Seller".equals(authority.getAuthority()) ||
                        "Admin".equals(authority.getAuthority()) ||
                        "SuperAdmin".equals(authority.getAuthority())
                    );
            }
        } catch (Exception e) {
            logger.warn("Error checking seller authority: {}", e.getMessage());
        }
        return false;
    }
}
