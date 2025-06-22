package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order.CreateOrderRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CartItemService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OrderService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderDetailRepository;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartItemController {
    private static final Logger logger = LoggerFactory.getLogger(CartItemController.class);
    @Autowired
    private CartItemService cartItemService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;
    @Autowired
    private MomoPayment momoPayment;

    @Autowired
    private OrderDetailRepository orderDetailRepository;// CUSTOMER - Thêm sản phẩm vào giỏ hàng
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
    }    // CUSTOMER - Lấy tất cả sản phẩm trong giỏ hàng
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
    }

    // CUSTOMER - Thanh toán toàn bộ giỏ hàng
    @PostMapping("/checkout/all/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> checkoutAllItems(@PathVariable Integer userId,
                                            @RequestParam PaymentMethodEnum paymentMethod,
                                            @RequestParam(required = false) Integer shippingAddressId,
                                            @RequestParam(required = false) String notes) {
        try {
            // Lấy tất cả sản phẩm trong giỏ hàng
            List<CartItem> cartItems = cartItemService.getCartItemsByUserId(userId);
            
            if (cartItems.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Giỏ hàng trống, không thể thanh toán");
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }            // Tính tổng tiền
            BigDecimal totalAmount = cartItemService.getTotalPriceByUserId(userId);

            // Tạo đơn hàng
            CreateOrderRequest orderRequest = new CreateOrderRequest();
            orderRequest.setPaymentMethod(paymentMethod);
            orderRequest.setTotalAmount(totalAmount);
            orderRequest.setShippingAddressId(shippingAddressId);
            orderRequest.setNotes(notes);

            Orders newOrder = createOrderFromCart(userId, orderRequest, cartItems);

            // Xử lý theo phương thức thanh toán
            if (paymentMethod == PaymentMethodEnum.Momo) {
                // Đối với MoMo, xóa items khỏi cart sau khi tạo order thành công  
                removeItemsFromCart(userId, cartItems);
                return handleMomoPayment(newOrder, cartItems);
            } else if (paymentMethod == PaymentMethodEnum.CashOnDelivery) {
                return handleCashOnDeliveryPayment(newOrder, cartItems);
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Phương thức thanh toán không được hỗ trợ", "success", false));

        } catch (Exception e) {
            return handleException("Lỗi khi thanh toán toàn bộ giỏ hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // CUSTOMER - Thanh toán sản phẩm được chọn
    @PostMapping("/checkout/selected/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> checkoutSelectedItems(@PathVariable Integer userId,
                                                 @RequestParam PaymentMethodEnum paymentMethod,
                                                 @RequestBody List<Integer> selectedProductIds,
                                                 @RequestParam(required = false) Integer shippingAddressId,
                                                 @RequestParam(required = false) String notes) {
        try {
            if (selectedProductIds == null || selectedProductIds.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Vui lòng chọn ít nhất một sản phẩm để thanh toán");
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }

            // Lấy các sản phẩm được chọn từ giỏ hàng
            List<CartItem> allCartItems = cartItemService.getCartItemsByUserId(userId);
            List<CartItem> selectedItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartItem item : allCartItems) {
                if (selectedProductIds.contains(item.getProduct().getProductId())) {
                    selectedItems.add(item);
                    totalAmount = totalAmount.add(item.getProduct().getPrice());
                }
            }

            if (selectedItems.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Không tìm thấy sản phẩm được chọn trong giỏ hàng");
                response.put("success", false);
                return ResponseEntity.badRequest().body(response);
            }            // Tạo đơn hàng
            CreateOrderRequest orderRequest = new CreateOrderRequest();
            orderRequest.setPaymentMethod(paymentMethod);
            orderRequest.setTotalAmount(totalAmount);
            orderRequest.setShippingAddressId(shippingAddressId);
            orderRequest.setNotes(notes);

            Orders newOrder = createOrderFromCart(userId, orderRequest, selectedItems);

            // Xử lý theo phương thức thanh toán
            if (paymentMethod == PaymentMethodEnum.Momo) {
                // Đối với MoMo, xóa items khỏi cart sau khi tạo order thành công
                removeItemsFromCart(userId, selectedItems);
                return handleMomoPayment(newOrder, selectedItems);
            } else if (paymentMethod == PaymentMethodEnum.CashOnDelivery) {
                return handleCashOnDeliveryPayment(newOrder, selectedItems);
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Phương thức thanh toán không được hỗ trợ", "success", false));

        } catch (Exception e) {
            return handleException("Lỗi khi thanh toán sản phẩm được chọn", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // Xử lý thanh toán Momo
    private ResponseEntity<?> handleMomoPayment(Orders order, List<CartItem> items) {
        try {
            // Tạo payment request với MoMo API thực tế
            MomoCreatePaymentResponseModel momoResponse = momoPayment.createPaymentRequest(
                order.getOrderId().toString(),
                order.getTotalAmount().longValue(),
                "Thanh toán đơn hàng #" + order.getOrderId()
            );
            
            Map<String, Object> response = new HashMap<>();
            
            if (momoResponse.getErrorCode() == 0) {
                response.put("message", "Đang chuyển hướng đến cổng thanh toán MoMo");
                response.put("success", true);
                response.put("paymentMethod", "Momo");
                response.put("orderId", order.getOrderId());
                response.put("totalAmount", order.getTotalAmount());
                response.put("momoPaymentUrl", momoResponse.getPayUrl());
                response.put("qrCodeUrl", momoResponse.getQrCodeUrl());
                response.put("deeplink", momoResponse.getDeeplink());
                
                response.put("items", items.stream().map(item -> {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("productId", item.getProduct().getProductId());
                    itemMap.put("productName", item.getProduct().getName());
                    itemMap.put("price", item.getProduct().getPrice());
                    return itemMap;
                }).toList());
                
                logger.info("Đơn hàng {} đã được tạo và chuyển hướng đến thanh toán MoMo", order.getOrderId());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("message", "Lỗi khi tạo yêu cầu thanh toán MoMo: " + momoResponse.getMessage());
                response.put("success", false);
                response.put("errorCode", momoResponse.getErrorCode());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            return handleException("Lỗi khi xử lý thanh toán MoMo", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Xử lý thanh toán khi nhận hàng
    private ResponseEntity<?> handleCashOnDeliveryPayment(Orders order, List<CartItem> items) {
        try {
            // Tạo hóa đơn ngay lập tức cho COD
            Map<String, Object> invoice = generateInvoice(order, items);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đơn hàng đã được tạo thành công với thanh toán khi nhận hàng");
            response.put("success", true);
            response.put("paymentMethod", "CashOnDelivery");
            response.put("orderId", order.getOrderId());
            response.put("note", "Người bán sẽ cập nhật trạng thái đơn hàng thành 'Completed' sau khi giao hàng thành công");
            response.put("invoice", invoice);
            
            // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
            removeItemsFromCart(order.getUserId(), items);
            
            logger.info("Đơn hàng COD {} đã được tạo thành công cho người dùng {}", order.getOrderId(), order.getUserId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return handleException("Lỗi khi xử lý thanh toán COD", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // Xử lý sau khi thanh toán MoMo thành công/thất bại (callback từ MoMo)
    @PostMapping("/momo/callback")
    public ResponseEntity<?> handleMomoCallback(@RequestParam Map<String, String> response) {
        try {
            logger.info("Received MoMo callback: {}", response);
            
            // Xử lý response từ MoMo
            boolean isValidResponse = momoPayment.verifyPaymentResponse(response);
            
            if (!isValidResponse) {
                logger.error("Invalid MoMo callback signature");
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Invalid callback signature",
                    "success", false
                ));
            }
            
            String orderId = response.get("orderId");
            String resultCode = response.get("resultCode");
            
            if ("0".equals(resultCode)) {
                // Thanh toán thành công
                Orders order = orderService.getOrderById(Integer.parseInt(orderId));
                
                // Cập nhật trạng thái đơn hàng
                order.setStatus(OrderStatusEnum.Processing);
                order.setUpdatedAt(Timestamp.from(Instant.now()));
                orderService.createOrder(order); // Save updated order
                  // Xóa sản phẩm khỏi giỏ hàng
                // Note: Trong trường hợp MoMo callback, chúng ta cần xóa items dựa trên orderId
                // Vì cart items đã được processed khi tạo order, ta chỉ cần log
                logger.info("Order {} payment confirmed, cart items were already processed during order creation", orderId);
                
                logger.info("MoMo payment successful for order {}", orderId);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Payment successful",
                    "success", true,
                    "orderId", orderId
                ));
            } else {
                // Thanh toán thất bại
                logger.warn("MoMo payment failed for order {} with result code: {}", orderId, resultCode);
                
                return ResponseEntity.ok(Map.of(
                    "message", "Payment failed",
                    "success", false,
                    "orderId", orderId,
                    "resultCode", resultCode
                ));
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
            logger.info("Received MoMo return: {}", response);
            
            String orderId = response.get("orderId");
            String resultCode = response.get("resultCode");
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("resultCode", resultCode);
            
            if ("0".equals(resultCode)) {
                result.put("message", "Thanh toán thành công");
                result.put("success", true);
                result.put("status", "success");
            } else {
                result.put("message", "Thanh toán thất bại");
                result.put("success", false);
                result.put("status", "failed");
            }
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Error processing MoMo return", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "Lỗi xử lý kết quả thanh toán",
                "success", false,
                "status", "error"
            ));
        }
    }    // Tạo đơn hàng từ giỏ hàng
    private Orders createOrderFromCart(Integer userId, CreateOrderRequest orderRequest, List<CartItem> items) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setShippingAddressId(orderRequest.getShippingAddressId());
        order.setNotes(orderRequest.getNotes());
        order.setStatus(OrderStatusEnum.Pending);
        order.setOrderTime(Timestamp.from(Instant.now()));
        order.setCreatedAt(Timestamp.from(Instant.now()));
        order.setUpdatedAt(Timestamp.from(Instant.now()));

        Orders savedOrder = orderService.createOrder(order);
        
        // Tạo Order Details từ Cart Items
        createOrderDetails(savedOrder, items);
        
        return savedOrder;
    }// Tạo hóa đơn
    private Map<String, Object> generateInvoice(Orders order, List<CartItem> items) {
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceId", "INV-" + order.getOrderId() + "-" + System.currentTimeMillis());
        invoice.put("orderId", order.getOrderId());
        invoice.put("customerUserId", order.getUserId());
        invoice.put("paymentMethod", order.getPaymentMethod().toString());
        invoice.put("totalAmount", order.getTotalAmount());
        invoice.put("orderTime", order.getOrderTime());        invoice.put("items", items.stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProduct().getProductId());
            itemMap.put("productName", item.getProduct().getName());
            itemMap.put("price", item.getProduct().getPrice());
            itemMap.put("quantity", 1); 
            return itemMap;
        }).toList());
        invoice.put("shippingAddressId", order.getShippingAddressId());
        invoice.put("notes", order.getNotes());
        invoice.put("generatedAt", Timestamp.from(Instant.now()));
        
        return invoice;
    }

    // Tạo Order Details từ Cart Items
    private void createOrderDetails(Orders order, List<CartItem> items) {
        for (CartItem item : items) {
            OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(item.getProduct())
                .priceAtPurchase(item.getProduct().getPrice())
                .build();
            
            orderDetailRepository.save(orderDetail);
        }
    }

    // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
    private void removeItemsFromCart(Integer userId, List<CartItem> items) {
        for (CartItem item : items) {
            try {
                cartItemService.removeItem(userId, item.getProduct().getProductId());
            } catch (Exception e) {
                logger.warn("Không thể xóa sản phẩm {} khỏi giỏ hàng của người dùng {}: {}", 
                           item.getProduct().getProductId(), userId, e.getMessage());
            }
        }
    }

    // Xử lý exception
    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        logger.error(message, e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", message + ": " + e.getMessage());
        response.put("success", false);
        return new ResponseEntity<>(response, status);
    }    /**
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