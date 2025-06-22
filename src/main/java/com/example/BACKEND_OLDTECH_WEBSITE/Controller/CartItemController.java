package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CartItemService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CheckoutService;
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

@RestController
@RequestMapping("/cart")
public class CartItemController {
    private static final Logger logger = LoggerFactory.getLogger(CartItemController.class);    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private UserService userService;// CUSTOMER - Thêm sản phẩm vào giỏ hàng
    @PostMapping("/addItem/{userId}")
    @PreAuthorize("hasAuthority('Customer') and hasAuthority('Admin')")
    public ResponseEntity<?> addItem(@PathVariable Integer userId,
                                     @RequestParam Integer productId) {
        try {
            // Validate product availability before adding to cart
            if (!cartItemService.isProductAvailable(productId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Sản phẩm này hiện không khả dụng hoặc đã bị ẩn");
                response.put("success", false);
                response.put("productId", productId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            cartItemService.addItem(userId, productId);
            
            // Tự động tính tổng tiền sau khi thêm sản phẩm
            BigDecimal newTotal = cartItemService.getTotalPriceByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã thêm sản phẩm vào giỏ hàng thành công");
            response.put("success", true);
            response.put("newTotalPrice", newTotal);
            response.put("productId", productId);
            
            logger.info("Người dùng {} đã thêm sản phẩm {} vào giỏ hàng. Tổng tiền mới: {}", 
                       userId, productId, newTotal);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return handleException("Không tìm thấy sản phẩm", e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException("Lỗi khi thêm sản phẩm vào giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Lấy tất cả sản phẩm trong giỏ hàng
    @GetMapping("/getAllItems/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> getCartItems(@PathVariable Integer userId) {
        try {
            // Clean up unavailable products first
            int removedCount = cartItemService.cleanupUnavailableProducts(userId);
            
            List<CartItem> items = cartItemService.getCartItemsByUserId(userId);
            BigDecimal totalPrice = cartItemService.getTotalPriceByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", items);
            response.put("totalPrice", totalPrice);
            response.put("itemCount", items.size());
            response.put("success", true);
            
            if (removedCount > 0) {
                response.put("notice", "Đã tự động xóa " + removedCount + " sản phẩm không khả dụng khỏi giỏ hàng");
                response.put("removedUnavailableItems", removedCount);
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException("Lỗi khi lấy danh sách giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Lấy tổng tiền giỏ hàng
    @GetMapping("/totalPrice/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> getTotalPrice(@PathVariable Integer userId) {
        try {
            BigDecimal total = cartItemService.getTotalPriceByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("totalPrice", total);
            response.put("success", true);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException("Lỗi khi tính tổng tiền giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/removeItem/{userId}") 
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> removeItem(@PathVariable Integer userId, 
                                        @RequestParam Integer productId) {
        try {
            cartItemService.removeItem(userId, productId);
            
            // Tự động tính lại tổng tiền sau khi xóa sản phẩm
            BigDecimal newTotal = cartItemService.getTotalPriceByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng thành công");
            response.put("success", true);
            response.put("newTotalPrice", newTotal);
            response.put("removedProductId", productId);
            
            logger.info("Người dùng {} đã xóa sản phẩm {} khỏi giỏ hàng. Tổng tiền mới: {}", 
                       userId, productId, newTotal);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return handleException("Không tìm thấy sản phẩm trong giỏ hàng", e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException("Lỗi khi xóa sản phẩm khỏi giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Dọn sạch giỏ hàng
    @DeleteMapping("/clear/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        try {
            cartItemService.clearCartByUserId(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Giỏ hàng đã được dọn sạch");
            response.put("success", true);
            response.put("totalPrice", BigDecimal.ZERO);
            
            logger.info("Giỏ hàng của người dùng {} đã được dọn sạch", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException("Lỗi khi dọn sạch giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // CUSTOMER - Thanh toán toàn bộ giỏ hàng
    @PostMapping("/checkout/all/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> checkoutAllItems(@PathVariable Integer userId,
                                            @RequestParam PaymentMethodEnum paymentMethod,
                                            @RequestParam(required = false) Integer shippingAddressId,
                                            @RequestParam(required = false) String notes) {
        try {
            Map<String, Object> result = checkoutService.checkoutAllItems(userId, paymentMethod, shippingAddressId, notes);
            
            boolean success = (Boolean) result.get("success");
            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            return handleException("Lỗi khi thanh toán toàn bộ giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // CUSTOMER - Thanh toán sản phẩm được chọn
    @PostMapping("/checkout/selected/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> checkoutSelectedItems(@PathVariable Integer userId,
                                                 @RequestParam PaymentMethodEnum paymentMethod,
                                                 @RequestBody List<Integer> selectedProductIds,
                                                 @RequestParam(required = false) Integer shippingAddressId,
                                                 @RequestParam(required = false) String notes) {
        try {
            Map<String, Object> result = checkoutService.checkoutSelectedItems(userId, paymentMethod, selectedProductIds, shippingAddressId, notes);
            
            boolean success = (Boolean) result.get("success");
            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }

        } catch (Exception e) {
            return handleException("Lỗi khi thanh toán sản phẩm được chọn", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // Xử lý sau khi thanh toán MoMo thành công/thất bại (callback từ MoMo)
    @PostMapping("/momo/callback")
    public ResponseEntity<?> handleMomoCallback(@RequestParam Map<String, String> response) {
        try {
            Map<String, Object> result = checkoutService.handleMomoCallback(response);
            boolean success = (Boolean) result.get("success");
            
            if (success) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
            
        } catch (Exception e) {
            logger.error("Error processing MoMo callback", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Error processing callback",
                "success", false
            ));
        }
    }

    // Endpoint xử lý return URL từ MoMo (cho frontend)
    @GetMapping("/momo/return")
    public ResponseEntity<?> handleMomoReturn(@RequestParam Map<String, String> response) {
        try {
            Map<String, Object> result = checkoutService.handleMomoReturn(response);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing MoMo return", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Lỗi xử lý kết quả thanh toán",
                "success", false,
                "status", "error"
            ));
        }
    }    // Xử lý exception
    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        logger.error(message, e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", message + ": " + e.getMessage());
        response.put("success", false);
        return new ResponseEntity<>(response, status);
    }/**
     * Helper method to get the current authenticated user
     * Used for future authentication needs
     */
    @SuppressWarnings("unused")
    private User getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();
            return userService.findUserByEmail(userEmail);
        } catch (Exception e) {
            logger.error("Error getting current authenticated user: {}", e.getMessage());
            throw new EntityNotFoundException("Không thể xác định người dùng hiện tại");
        }
    }

    // CUSTOMER - Dọn dẹp sản phẩm không khả dụng trong giỏ hàng
    @DeleteMapping("/cleanup/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> cleanupUnavailableProducts(@PathVariable Integer userId) {
        try {
            int removedCount = cartItemService.cleanupUnavailableProducts(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", removedCount > 0 ? 
                "Đã xóa " + removedCount + " sản phẩm không khả dụng khỏi giỏ hàng" : 
                "Không có sản phẩm nào cần xóa");            response.put("success", true);
            response.put("removedCount", removedCount);
            response.put("userId", userId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException("Lỗi khi dọn dẹp giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}