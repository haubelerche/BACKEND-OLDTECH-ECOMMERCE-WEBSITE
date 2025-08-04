package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart.CartItemDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Exception.ProductAlreadyInCartException;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
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
    @PostMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestParam Integer productId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = null;
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                userId = userService.getUserIdFromAuthentication(authentication);
            }
            // Validate product availability before adding to cart
            if (!cartItemService.isProductAvailable(productId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Sản phẩm này hiện không khả dụng hoặc đã bị ẩn");
                response.put("success", false);
                response.put("productId", productId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (userId != null) {
                cartItemService.addItem(userId, productId);
                BigDecimal newTotal = cartItemService.getTotalPriceByUserId(userId);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Đã thêm sản phẩm vào giỏ hàng thành công");
                response.put("success", true);
                response.put("newTotalPrice", newTotal);
                response.put("productId", productId);
                logger.info("Người dùng {} đã thêm sản phẩm {} vào giỏ hàng. Tổng tiền mới: {}", userId, productId, newTotal);
                return ResponseEntity.ok(response);
            } else {
                // Guest user: inform frontend to handle cart in localStorage or cookies
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Bạn chưa đăng nhập. Vui lòng đăng nhập để lưu giỏ hàng trên hệ thống, hoặc tiếp tục mua sắm với giỏ hàng tạm thời trên trình duyệt.");
                response.put("success", true);
                response.put("guestCart", true);
                response.put("productId", productId);
                return ResponseEntity.ok(response);
            }
        } catch (ProductAlreadyInCartException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("success", false);
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", e.getMessage());
            response.put("success", false);
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Lỗi khi thêm sản phẩm vào giỏ hàng", e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lỗi không xác định khi thêm sản phẩm vào giỏ hàng");
            response.put("success", false);
            response.put("productId", productId);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // CUSTOMER - Lấy tất cả sản phẩm trong giỏ hàng
    @GetMapping("/myCart")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> getCartItems() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = userService.getUserIdFromAuthentication(authentication);
            logger.info("[DEBUG] Lấy giỏ hàng cho userId: {}", userId);
            if (userId == null) {
                logger.error("[ERROR] Không xác định được userId từ token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Không xác định được người dùng từ token",
                    "success", false
                ));
            }
            int unavailableCount = 0;
            try {
                unavailableCount = cartItemService.countUnavailableProductsInCart(userId);
            } catch (Exception e) {
                logger.error("[ERROR] Lỗi khi countUnavailableProductsInCart: {}", e.getMessage(), e);
                return handleException("Lỗi khi kiểm tra sản phẩm không khả dụng", e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            List<CartItemDTO> items;
            try {
                items = cartItemService.getCartItemsByUserId(userId);
            } catch (Exception e) {
                logger.error("[ERROR] Lỗi khi getCartItemsByUserId: {}", e.getMessage(), e);
                return handleException("Lỗi khi lấy danh sách sản phẩm trong giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            BigDecimal totalPrice;
            try {
                totalPrice = cartItemService.getTotalPriceByUserId(userId);
            } catch (Exception e) {
                logger.error("[ERROR] Lỗi khi getTotalPriceByUserId: {}", e.getMessage(), e);
                return handleException("Lỗi khi tính tổng tiền giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("cartItems", items);
            response.put("itemCount", items.size());
            if (unavailableCount > 0) {
                response.put("notice", "Có " + unavailableCount + " sản phẩm không khả dụng trong giỏ hàng");
                response.put("unavailableItems", unavailableCount);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("[ERROR] Lỗi không xác định khi lấy giỏ hàng", e);
            return handleException("Lỗi không xác định khi lấy giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // CUSTOMER - Xóa sản phẩm khỏi giỏ hàng
    @DeleteMapping("/removeItem")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> removeItem(@RequestParam Integer productId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = userService.getUserIdFromAuthentication(authentication);
            cartItemService.removeItem(userId, productId);
            // Tự động tính lại tổng tiền sau khi xóa sản phẩm
            BigDecimal newTotal = cartItemService.getTotalPriceByUserId(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đã xóa sản phẩm khỏi giỏ hàng thành công");
            response.put("success", true);
            response.put("newTotalPrice", newTotal);
            response.put("removedProductId", productId);
            logger.info("Người dùng {} đã xóa sản phẩm {} khỏi giỏ hàng. Tổng tiền mới: {}", userId, productId, newTotal);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return handleException("Không tìm thấy sản phẩm trong giỏ hàng", e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException("Lỗi khi xóa sản phẩm khỏi giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Dọn sạch giỏ hàng
    @DeleteMapping("/clear")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> clearCart() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = userService.getUserIdFromAuthentication(authentication);
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
    @PostMapping("/checkout/all")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> checkoutAllItems(
            @RequestParam PaymentMethodEnum paymentMethod,
            @RequestParam(required = false) Long shippingAddressId,
            @RequestParam(required = false) String notes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer userId = userService.getUserIdFromAuthentication(authentication);
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


// CUSTOMER - Thanh toán sản phẩm trong giỏ hàng theo cartId
@PostMapping( "/checkout")
@PreAuthorize("hasAuthority('Customer')")
public ResponseEntity<?> checkoutCart(
        @RequestParam PaymentMethodEnum paymentMethod,
        @RequestParam Integer cartId,
        @RequestParam(required = false) Long shippingAddressId,
        @RequestParam(required = false) String notes) {
    try {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Integer userId = userService.getUserIdFromAuthentication(authentication);

        Map<String, Object> result = checkoutService.checkoutCart(userId, cartId, paymentMethod, shippingAddressId, notes);

        boolean success = (Boolean) result.get("success");
        if (success) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    } catch (Exception e) {
        return handleException("Lỗi khi thanh toán giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

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






    //KHÔNG ĐỘNG VÀO
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

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(PaymentMethodEnum.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(PaymentMethodEnum.fromString(text));
            }
        });
    }



}
