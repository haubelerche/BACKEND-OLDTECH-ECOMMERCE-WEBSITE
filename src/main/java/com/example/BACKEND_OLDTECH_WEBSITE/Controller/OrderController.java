package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order.CreateOrderRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order.OrderResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Address;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.AddressService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OrderService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;


    /*---ADMIN---*/


    //ADMIN LẤY DANH SÁCH TẤT CẢ ĐƠN HÀNG TRÊN NỀN TẢNG
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Orders> orders = orderService.getAllOrders();

            // Convert each order to OrderResponse DTO
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.ok(orderResponses);
        } catch (Exception e) {
            return handleException("Lỗi lấy danh sách đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    // ADMIN - Lọc đơn hàng với nhiều tiêu chí
    @GetMapping("/multi-filter")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersWithMultipleFilters(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, defaultValue = "false") Boolean strictMode) {
        try {
            logger.info("Multi-filter request: status={}, payment={}, user={}, dates={}-{}, amounts={}-{}",
                    status, paymentMethod, userId, startDate, endDate, minAmount, maxAmount);

            List<Orders> orders = orderService.getAllOrders();
            Map<String, Object> appliedFilters = new HashMap<>();
            List<String> filterSteps = new ArrayList<>();
            int originalCount = orders.size();

            // 1. Lọc theo trạng thái - chỉ khi có giá trị hợp lệ
            if (isValidFilterValue(status)) {
                try {
                    OrderStatusEnum statusEnum = OrderStatusEnum.valueOf(status.toUpperCase().trim());
                    orders = orders.stream()
                            .filter(order -> order.getStatus() == statusEnum)
                            .collect(Collectors.toList());
                    appliedFilters.put("status", status.toUpperCase());
                    filterSteps.add("Lọc theo trạng thái '" + status.toUpperCase() + "': " + orders.size() + " đơn hàng");
                } catch (IllegalArgumentException e) {
                    if (strictMode) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Trạng thái không hợp lệ: " + status,
                                "validStatuses", Arrays.stream(OrderStatusEnum.values())
                                        .map(Enum::name).collect(Collectors.toList())
                        ));
                    }
                    appliedFilters.put("status", "IGNORED (không hợp lệ: " + status + ")");
                }
            }

            // 2. Lọc theo phương thức thanh toán - chỉ khi có giá trị hợp lệ
            if (isValidFilterValue(paymentMethod)) {
                try {
                    PaymentMethodEnum paymentEnum = PaymentMethodEnum.valueOf(paymentMethod.toUpperCase().trim());
                    orders = orders.stream()
                            .filter(order -> order.getPaymentMethod() == paymentEnum)
                            .collect(Collectors.toList());
                    appliedFilters.put("paymentMethod", paymentMethod.toUpperCase());
                    filterSteps.add("Lọc theo thanh toán '" + paymentMethod.toUpperCase() + "': " + orders.size() + " đơn hàng");
                } catch (IllegalArgumentException e) {
                    if (strictMode) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Phương thức thanh toán không hợp lệ: " + paymentMethod,
                                "validMethods", Arrays.stream(PaymentMethodEnum.values())
                                        .map(Enum::name).collect(Collectors.toList())
                        ));
                    }
                    appliedFilters.put("paymentMethod", "IGNORED (không hợp lệ: " + paymentMethod + ")");
                }
            }

            // 3. Lọc theo người dùng
            if (userId != null && userId > 0) {
                orders = orders.stream()
                        .filter(order -> order.getUserId().equals(userId))
                        .collect(Collectors.toList());
                appliedFilters.put("userId", userId);
                filterSteps.add("Lọc theo user ID " + userId + ": " + orders.size() + " đơn hàng");
            }
            // 4. Lọc theo ngày bắt đầu - chỉ khi có giá trị hợp lệ
            if (isValidFilterValue(startDate)) {
                try {
                    Timestamp startTimestamp = Timestamp.valueOf(startDate + " 00:00:00");
                    orders = orders.stream()
                            .filter(order -> !order.getOrderTime().before(startTimestamp))
                            .collect(Collectors.toList());
                    appliedFilters.put("startDate", startDate);
                    filterSteps.add("Lọc từ ngày " + startDate + ": " + orders.size() + " đơn hàng");
                } catch (Exception e) {
                    if (strictMode) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Định dạng ngày bắt đầu không hợp lệ: " + startDate,
                                "format", "yyyy-MM-dd"
                        ));
                    }
                    appliedFilters.put("startDate", "IGNORED (không hợp lệ: " + startDate + ")");
                }
            }

            // 5. Lọc theo ngày kết thúc - chỉ khi có giá trị hợp lệ
            if (isValidFilterValue(endDate)) {
                try {
                    Timestamp endTimestamp = Timestamp.valueOf(endDate + " 23:59:59");
                    orders = orders.stream()
                            .filter(order -> !order.getOrderTime().after(endTimestamp))
                            .collect(Collectors.toList());
                    appliedFilters.put("endDate", endDate);
                    filterSteps.add("Lọc đến ngày " + endDate + ": " + orders.size() + " đơn hàng");
                } catch (Exception e) {
                    if (strictMode) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Định dạng ngày kết thúc không hợp lệ: " + endDate,
                                "format", "yyyy-MM-dd"
                        ));
                    }
                    appliedFilters.put("endDate", "IGNORED (không hợp lệ: " + endDate + ")");
                }
            }

            // 6. Lọc theo giá tối thiểu
            if (minAmount != null) {
                orders = orders.stream()
                        .filter(order -> order.getTotalAmount().compareTo(minAmount) >= 0)
                        .collect(Collectors.toList());
                appliedFilters.put("minAmount", minAmount);
                filterSteps.add("Lọc giá >= " + minAmount + ": " + orders.size() + " đơn hàng");
            }

            // 7. Lọc theo giá tối đa
            if (maxAmount != null) {
                orders = orders.stream()
                        .filter(order -> order.getTotalAmount().compareTo(maxAmount) <= 0)
                        .collect(Collectors.toList());
                appliedFilters.put("maxAmount", maxAmount);
                filterSteps.add("Lọc giá <= " + maxAmount + ": " + orders.size() + " đơn hàng");
            }

            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Multi-filter completed: {} -> {} orders", originalCount, orderResponses.size());

            // Tạo response chi tiết
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("summary", Map.of(
                    "totalFound", orderResponses.size(),
                    "originalTotal", originalCount,
                    "filtersApplied", appliedFilters.size(),
                    "strictMode", strictMode
            ));
            response.put("appliedFilters", appliedFilters);
            response.put("filterSteps", filterSteps);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in multi-filter: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đa tiêu chí", e, HttpStatus.INTERNAL_SERVER_ERROR);        }
    }
    /**
     * Helper method to validate filter values and ignore placeholder values
     */
    private boolean isValidFilterValue(String value) {
        if (value == null || value.trim().isEmpty()) return false;

        // Ignore placeholder values from testing tools
        if (value.contains("{{") || value.contains("}}") ||
                value.contains("$random") || value.contains("$placeholder") ||
                value.equals("{{$placeholder}}") || value.contains("alphanumeric") ||
                value.contains("integer(")) {
            return false;
        }

        return true;
    }


// XEM TẤT CẢ ĐƠN HÀNG MỘT NGƯỜI ĐÃ ĐẶT
    @GetMapping("/get/history/{userId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<Orders> orders = orderService.getOrderHistory(userId);

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(orderResponses);
        } catch (Exception e) {
            return handleException("Lỗi lấy lịch sử đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    // ADMIN - Lọc đơn hàngtheo trạng thái
    @GetMapping("/simple-filter")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByStatusSimple(@RequestParam String status) {
        try {
            logger.info("Filtering orders by status: {}", status);

            // Validate status
            if (status == null || status.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Trạng thái không được để trống",
                        "validStatuses", Arrays.stream(OrderStatusEnum.values())
                                .map(Enum::name)
                                .collect(Collectors.toList())
                ));
            }
            OrderStatusEnum statusEnum;
            try {
                // Convert to proper case: first letter uppercase, rest lowercase
                String properStatus = status.trim().substring(0, 1).toUpperCase() +
                        status.trim().substring(1).toLowerCase();
                statusEnum = OrderStatusEnum.valueOf(properStatus);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Trạng thái không hợp lệ: " + status,
                        "validStatuses", Arrays.stream(OrderStatusEnum.values())
                                .map(Enum::name)
                                .collect(Collectors.toList())
                ));
            }

            // Get and filter orders
            List<Orders> orders = orderService.getAllOrders();
            List<Orders> filteredOrders = orders.stream()
                    .filter(order -> order.getStatus() == statusEnum)
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<OrderResponse> orderResponses = filteredOrders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders with status {}", orderResponses.size(), status);
            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("status", statusEnum.name());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering orders by status: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đơn hàng theo trạng thái", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADMIN - Lọc đơn hàng theo ngày
    @GetMapping("/filter-by-date")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByDateRange(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            logger.info("Filtering orders by date range: {} to {}", startDate, endDate);

            List<Orders> orders = orderService.getAllOrders();
            // Lọc theo ngày bắt đầu
            if (startDate != null && !startDate.trim().isEmpty()) {
                try {
                    // Trim the date string to remove any leading/trailing spaces
                    String trimmedStartDate = startDate.trim();
                    // Ensure the date format is correct by validating it first
                    if (!trimmedStartDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Định dạng ngày bắt đầu không hợp lệ: " + startDate,
                                "format", "yyyy-MM-dd (Ví dụ: 2025-06-18)"
                        ));
                    }
                    Timestamp startTimestamp = Timestamp.valueOf(trimmedStartDate + " 00:00:00");
                    orders = orders.stream()
                            .filter(order -> !order.getOrderTime().before(startTimestamp))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Định dạng ngày bắt đầu không hợp lệ: " + startDate,
                            "format", "yyyy-MM-dd (Ví dụ: 2025-06-18)"
                    ));
                }
            }
            // Lọc theo ngày kết thúc
            if (endDate != null && !endDate.trim().isEmpty()) {
                try {
                    // Trim the date string to remove any leading/trailing spaces
                    String trimmedEndDate = endDate.trim();
                    // Ensure the date format is correct by validating it first
                    if (!trimmedEndDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        return ResponseEntity.badRequest().body(Map.of(
                                "error", "Định dạng ngày kết thúc không hợp lệ: " + endDate,
                                "format", "yyyy-MM-dd (Ví dụ: 2025-06-18)"
                        ));
                    }
                    Timestamp endTimestamp = Timestamp.valueOf(trimmedEndDate + " 23:59:59");
                    orders = orders.stream()
                            .filter(order -> !order.getOrderTime().after(endTimestamp))
                            .collect(Collectors.toList());
                } catch (Exception e) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Định dạng ngày kết thúc không hợp lệ: " + endDate,
                            "format", "yyyy-MM-dd (Ví dụ: 2025-06-18)"
                    ));
                }
            }

            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders in date range", orderResponses.size());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("dateRange", Map.of(
                    "startDate", startDate != null ? startDate.trim() : "không giới hạn",
                    "endDate", endDate != null ? endDate.trim() : "không giới hạn"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering orders by date: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đơn hàng theo ngày", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADMIN - Lọc đơn hàng theo khoảng giá
    @GetMapping("/filter-by-amount")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByAmountRange(
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount) {
        try {
            logger.info("Filtering orders by amount range: {} to {}", minAmount, maxAmount);

            List<Orders> orders = orderService.getAllOrders();

            // Lọc theo giá tối thiểu
            if (minAmount != null) {
                orders = orders.stream()
                        .filter(order -> order.getTotalAmount().compareTo(minAmount) >= 0)
                        .collect(Collectors.toList());
            }

            // Lọc theo giá tối đa
            if (maxAmount != null) {
                orders = orders.stream()
                        .filter(order -> order.getTotalAmount().compareTo(maxAmount) <= 0)
                        .collect(Collectors.toList());
            }

            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders in amount range", orderResponses.size());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("amountRange", Map.of(
                    "minAmount", minAmount != null ? minAmount : "không giới hạn",
                    "maxAmount", maxAmount != null ? maxAmount : "không giới hạn"
            ));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering orders by amount: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đơn hàng theo giá", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADMIN - Lọc đơn hàng theo phương thức thanh toán
    @GetMapping("/filter-by-payment")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByPaymentMethod(@RequestParam String paymentMethod) {
        try {
            logger.info("Filtering orders by payment method: {}", paymentMethod);            // Validate payment method with case-insensitive matching
            PaymentMethodEnum paymentEnum = null;
            String inputMethod = paymentMethod.trim();

            // Try to find matching enum value (case-insensitive)
            for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
                if (method.name().equalsIgnoreCase(inputMethod)) {
                    paymentEnum = method;
                    break;
                }
            }

            if (paymentEnum == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Phương thức thanh toán không hợp lệ: " + paymentMethod,
                        "validMethods", Arrays.stream(PaymentMethodEnum.values())
                                .map(Enum::name)
                                .collect(Collectors.toList())
                ));
            }

            final PaymentMethodEnum finalPaymentEnum = paymentEnum;

            List<Orders> orders = orderService.getAllOrders();
            List<Orders> filteredOrders = orders.stream()
                    .filter(order -> order.getPaymentMethod() == finalPaymentEnum)
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<OrderResponse> orderResponses = filteredOrders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders with payment method {}", orderResponses.size(), paymentMethod);

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("paymentMethod", paymentMethod.toUpperCase());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering orders by payment method: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đơn hàng theo phương thức thanh toán", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ADMIN - Lọc đơn hàng theo người dùng
    @GetMapping("/filter-by-user")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> getOrdersByUser(@RequestParam Integer userId) {
        try {
            logger.info("Filtering orders by user ID: {}", userId);

            if (userId == null || userId <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "User ID không hợp lệ: " + userId,
                        "requirement", "User ID phải là số nguyên dương"
                ));
            }

            List<Orders> orders = orderService.getAllOrders();
            List<Orders> filteredOrders = orders.stream()
                    .filter(order -> order.getUserId().equals(userId))
                    .collect(Collectors.toList());

            // Convert to DTOs
            List<OrderResponse> orderResponses = filteredOrders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders for user {}", orderResponses.size(), userId);

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("userId", userId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error filtering orders by user: {}", e.getMessage(), e);
            return handleException("Lỗi lọc đơn hàng theo người dùng", e, HttpStatus.INTERNAL_SERVER_ERROR);        }
    }

    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        logger.error(message, e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", message + ": " + e.getMessage());
        response.put("success", false);
        return new ResponseEntity<>(response, status);
    }



/*---CUSTOMER---*/

    // CUSTOMER TẠO ĐƠN HÀNG MỚI
    @PostMapping("/create/{userId}")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> createOrder(@RequestBody @Valid CreateOrderRequest orderRequest, @PathVariable String userId) {
        try {
            // Get authenticated user automatically from JWT token
            User authenticatedUser = getCurrentAuthenticatedUser();

            if (authenticatedUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("success", false, "message", "Người dùng chưa đăng nhập hoặc token không hợp lệ"));
            }

            logger.info("Creating order for user: {} (ID: {})", authenticatedUser.getEmail(), authenticatedUser.getUserId());

            // Validate payment method - this is crucial for orders_chk_1 constraint
            if (orderRequest.getPaymentMethod() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phương thức thanh toán không được để trống"));
            }

            // Only allow valid payment methods that match the database constraint
            PaymentMethodEnum paymentMethod = orderRequest.getPaymentMethod();
            if (paymentMethod != PaymentMethodEnum.CashOnDelivery && paymentMethod != PaymentMethodEnum.Momo) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Phương thức thanh toán không hợp lệ. Chỉ chấp nhận: CashOnDelivery hoặc Momo"));
            }

            // Check if totalAmount is null
            if (orderRequest.getTotalAmount() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Tổng tiền không được để trống"));
            }

            // Check if totalAmount is negative or zero
            if (orderRequest.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Tổng tiền phải lớn hơn 0"));
            }

            // Check if totalAmount exceeds maximum allowed value (assuming 1 billion is the limit)
            if (orderRequest.getTotalAmount().compareTo(new BigDecimal("1000000000")) > 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Tổng tiền quá lớn, không thể vượt quá 1 tỷ"));
            }

            // Create Orders entity from request
            Orders order = Orders.builder()
                    .orderId(null)
                    .orderTime(Timestamp.from(Instant.now()))
                    .userId(authenticatedUser.getUserId())
                    .paymentMethod(orderRequest.getPaymentMethod())
                    // Format totalAmount to ensure it meets database constraints (2 decimal places)
                    .totalAmount(orderRequest.getTotalAmount().setScale(2, java.math.RoundingMode.HALF_UP))
                    .status(orderRequest.getStatus() != null ? orderRequest.getStatus() : OrderStatusEnum.Pending)
                    .notes(orderRequest.getNotes())
                    .createdAt(Timestamp.from(Instant.now()))
                    .updatedAt(Timestamp.from(Instant.now()))
                    .build();

            // Handle shipping address
            if (orderRequest.getShippingAddressId() != null && orderRequest.getShippingAddressId() > 0) {
                // Use provided address ID
                order.setShippingAddressId(orderRequest.getShippingAddressId());
            } else {
                // Find user's default or first address
                Integer addressId = findUserDefaultAddress(authenticatedUser.getUserId());
                if (addressId != null) {
                    order.setShippingAddressId(addressId);
                } else {
                    return handleException("Địa chỉ giao hàng không được cung cấp và không tìm thấy địa chỉ cho người dùng",
                            new IllegalArgumentException("Shipping address is required"), HttpStatus.BAD_REQUEST);
                }
            }            Orders createdOrder = orderService.createOrder(order);

            // Convert to response DTO
            OrderResponse orderResponse = convertToOrderResponse(createdOrder);

            return new ResponseEntity<>(orderResponse, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return handleException("Lỗi tạo đơn hàng", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException("Lỗi tạo đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }    // Helper method to find a default address for the user
    private Integer findUserDefaultAddress(Integer userId) {
        try {
            List<Address> userAddresses = addressService.getAllAddressesByUserId(userId);
            if (userAddresses != null && !userAddresses.isEmpty()) {
                // First, try to find a default address
                Address defaultAddress = userAddresses.stream()
                        .filter(address -> Boolean.TRUE.equals(address.getIsDefault()))
                        .findFirst()
                        .orElse(null);

                if (defaultAddress != null) {
                    return defaultAddress.getAddressId();
                }

                // If no default address found, use the first address
                return userAddresses.get(0).getAddressId();
            }
            return null;
        } catch (Exception e) {
            logger.error("Error finding address for user {}: {}", userId, e.getMessage());
            return null;
        }
    }



//CUSTOMER XEM CÁC ĐƠN HÀNG HIỆN TẠI CỦA MÌNH
    @GetMapping("/my-orders")
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> getMyOrders() {
        try {
            // Get the authenticated user's email
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String userEmail = authentication.getName();

            // Get user by email
            User user = userService.findUserByEmail(userEmail);

            // Get user's orders
            List<Orders> orders = orderService.getOrderHistory(user.getUserId());

            // Convert to DTOs
            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(orderResponses);        } catch (Exception e) {
            return handleException("Lỗi lấy lịch sử đơn hàng của bạn", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



// CUSTOMER - Cập nhật trạng thái đơn hàng (chỉ cho phép: Completed, Cancelled, Returned)
    @PutMapping("/customer-update-status/{orderId}")
    @PreAuthorize("hasAuthority('Seller') or hasAuthority('Customer')")
    public ResponseEntity<?> updateOrderStatusByCustomer(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        logger.info("=== CUSTOMER UPDATE STATUS ENDPOINT CALLED - OrderID: {}, Status: {} ===", orderId, status);
        try {
            // Get current authenticated user
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Không thể xác thực người dùng"));
            }

            // Get the order to verify ownership and status
            Orders order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Không tìm thấy đơn hàng với ID: " + orderId));
            }

            // Verify that the order belongs to the current user
            if (!order.getUserId().equals(currentUser.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Bạn không có quyền cập nhật đơn hàng này"));
            }

            // Validate requested status
            OrderStatusEnum newStatus;
            try {
                newStatus = OrderStatusEnum.valueOf(status);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Trạng thái không hợp lệ: " + status,
                        "allowedStatuses", Arrays.asList("Completed", "Cancelled", "Returned")
                ));
            }

            // Check if customer is allowed to set this status
            if (newStatus != OrderStatusEnum.Completed &&
                    newStatus != OrderStatusEnum.Cancelled &&
                    newStatus != OrderStatusEnum.Returned) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Customer chỉ được phép cập nhật trạng thái: Completed, Cancelled, Returned",
                        "requestedStatus", status,
                        "allowedStatuses", Arrays.asList("Completed", "Cancelled", "Returned")
                ));
            }

            // Apply business rules for each status
            String validationResult = validateCustomerStatusChange(order, newStatus);
            if (validationResult != null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", validationResult,
                        "currentStatus", order.getStatus().name(),
                        "requestedStatus", status
                ));
            }

            // Save previous status for response
            String previousStatus = order.getStatus().name();

            // Update order status
            orderService.updateOrderStatus(orderId, status);

            // Get updated order for response
            Orders updatedOrder = orderService.getOrderById(orderId);
            OrderResponse orderResponse = convertToOrderResponse(updatedOrder);

            logger.info("User {} updated order {} status from {} to {}",
                    currentUser.getEmail(), orderId, previousStatus, status);

            Map<String, Object> response = new HashMap<>();
            response.put("message", getSuccessMessage(newStatus));
            response.put("success", true);
            response.put("orderId", orderId);
            response.put("previousStatus", previousStatus);
            response.put("newStatus", status);
            response.put("order", orderResponse);

            return ResponseEntity.ok(response);

        } catch (EntityNotFoundException e) {
            logger.error("Order not found: {}", orderId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Đơn hàng không tồn tại"));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid status transition for order {}: {}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Lỗi cập nhật trạng thái: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating order status by customer: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống khi cập nhật trạng thái đơn hàng"));
        }
    }

    /**
     * Validate if customer can change order status according to business rules
     */
    private String validateCustomerStatusChange(Orders order, OrderStatusEnum newStatus) {
        OrderStatusEnum currentStatus = order.getStatus();

        switch (newStatus) {
            case Completed:
                if (currentStatus != OrderStatusEnum.Delivered) {
                    return "Chỉ có thể chuyển sang 'Completed' khi đơn hàng ở trạng thái 'Delivered'";
                }
                break;

            case Cancelled:
                if (currentStatus != OrderStatusEnum.Pending) {
                    return "Chỉ có thể hủy đơn hàng khi còn ở trạng thái 'Pending'";
                }
                break;

            case Returned:
                if (currentStatus != OrderStatusEnum.Delivered && currentStatus != OrderStatusEnum.Completed) {
                    return "Chỉ có thể trả hàng khi đơn hàng ở trạng thái 'Delivered' hoặc 'Completed'";
                }
                break;

            default:
                return "Customer không được phép cập nhật trạng thái này: " + newStatus.name();
        }

        return null; // No validation error
    }

    /**
     * Get success message based on status change
     */
    private String getSuccessMessage(OrderStatusEnum status) {
        switch (status) {
            case Completed:
                return "Cảm ơn bạn đã xác nhận nhận hàng thành công!";
            case Cancelled:
                return "Đơn hàng đã được hủy thành công!";
            case Returned:
                return "Yêu cầu trả hàng đã được ghi nhận!";
            default:
                return "Cập nhật trạng thái đơn hàng thành công!";
        }
    }



/*---SELLER---*/


// SELLER - Lấy đơn hàng theo trạng thái (dành cho Seller)
    @GetMapping("/seller/orders/{status}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> getOrdersByStatusForSeller(@PathVariable String status) {
        try {
            logger.info("Seller getting orders by status: {}", status);

            List<Orders> orders = orderService.getAllOrders();

            // Validate and parse status
            OrderStatusEnum statusEnum;
            try {
                // Try exact match first
                statusEnum = OrderStatusEnum.valueOf(status);
            } catch (IllegalArgumentException e) {
                // Try uppercase match
                try {
                    statusEnum = OrderStatusEnum.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e2) {
                    // Try capitalize first letter
                    String capitalizedStatus = status.substring(0, 1).toUpperCase() +
                            status.substring(1).toLowerCase();
                    statusEnum = OrderStatusEnum.valueOf(capitalizedStatus);
                }
            }

            final OrderStatusEnum finalStatusEnum = statusEnum;
            List<Orders> filteredOrders = orders.stream()
                    .filter(order -> order.getStatus() == finalStatusEnum)
                    .collect(Collectors.toList());

            List<OrderResponse> orderResponses = filteredOrders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            logger.info("Found {} orders with status {}", orderResponses.size(), status);

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalFound", orderResponses.size());
            response.put("status", statusEnum.name());
            response.put("message", "Tìm thấy " + orderResponses.size() + " đơn hàng với trạng thái " + statusEnum.name());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Trạng thái đơn hàng không hợp lệ: " + status,
                    "validStatuses", Arrays.stream(OrderStatusEnum.values())
                            .map(Enum::name)
                            .collect(Collectors.toList())
            ));
        } catch (Exception e) {
            logger.error("Error getting orders by status for seller: {}", e.getMessage(), e);
            return handleException("Lỗi lấy đơn hàng theo trạng thái", e, HttpStatus.INTERNAL_SERVER_ERROR);        }
    }





// SELLER - xem tất cả đơn hàng hiện co (dành cho Seller)
    @GetMapping("/seller/all")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> getAllOrdersForSeller() {
        try {
            logger.info("Seller getting all orders");

            List<Orders> orders = orderService.getAllOrders();

            List<OrderResponse> orderResponses = orders.stream()
                    .map(this::convertToOrderResponse)
                    .collect(Collectors.toList());

            // Group orders by status for statistics
            Map<String, Long> statusCounts = orders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getStatus().name(),
                            Collectors.counting()
                    ));

            logger.info("Seller retrieved {} total orders", orderResponses.size());

            Map<String, Object> response = new HashMap<>();
            response.put("orders", orderResponses);
            response.put("totalOrders", orderResponses.size());
            response.put("statusBreakdown", statusCounts);
            response.put("message", "Lấy thành công " + orderResponses.size() + " đơn hàng");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error getting all orders for seller: {}", e.getMessage(), e);
            return handleException("Lỗi lấy danh sách đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



// UPDATE TRẠNG THÁI ĐƠN HÀNG
    @PutMapping("/status/{orderId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Integer orderId,
            @RequestParam String status) {
        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok(Map.of(
                    "message", "Trạng thái đơn hàng " + orderId + " đã được cập nhật thành " + status,
                    "success", true
            ));
        } catch (EntityNotFoundException e) {
            return handleException("Đơn hàng không tồn tại", e, HttpStatus.NOT_FOUND);
        } catch (IllegalArgumentException e) {
            return handleException("Trạng thái đơn hàng không hợp lệ", e, HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            return handleException("Không thể cập nhật trạng thái đơn hàng", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException("Lỗi cập nhật trạng thái đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }












    /**
     * Convert Orders entity to OrderResponse DTO
     */
    private OrderResponse convertToOrderResponse(Orders order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderTime(order.getOrderTime());
        response.setUserId(order.getUserId());
        response.setShippingAddressId(order.getShippingAddressId());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Optionally add user and address details if available
        try {
            if (order.getUser() != null) {
                response.setUserEmail(order.getUser().getEmail());
                response.setUserFullName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
            }

            if (order.getShippingAddress() != null) {
                Address address = order.getShippingAddress();
                String addressDetail = String.format("%s, %s, %s, %s, %s",
                        address.getDetailedAddress(),
                        address.getStreet(),
                        address.getWard(),
                        address.getDistrict(),
                        address.getCity());
                response.setShippingAddressDetail(addressDetail);
            }
        } catch (Exception e) {
            // If lazy loading fails, just continue without the extra details
            logger.debug("Could not load user/address details for order {}: {}", order.getOrderId(), e.getMessage());
        }

        return response;
    }

    /**
     * Helper method to get the current authenticated user
     * Automatically extracts user information from JWT token
     */
    private User getCurrentAuthenticatedUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("No authentication found in security context");
                return null;
            }            String email = authentication.getName();
            if (email == null || email.trim().isEmpty()) {
                logger.warn("No email found in authentication principal");                return null;
            }

            User user = userService.findUserByEmail(email);
            if (user == null) {
                logger.warn("No user found with email: {}", email);
            }

            return user;
        } catch (Exception e) {
            logger.error("Error getting authenticated user: {}", e.getMessage());
            return null;
        }
    }

    /*--ORDER COMPLETION ENDPOINTS--*/

    /**
     * Complete order manually (buyer for Momo, seller for COD)
     */
    @PutMapping("/complete/{orderId}")
    @PreAuthorize("hasAnyAuthority('Customer', 'Seller')")
    public ResponseEntity<Map<String, Object>> completeOrder(@PathVariable Integer orderId) {
        try {
            User currentUser = getCurrentAuthenticatedUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false, 
                        "message", "Không thể xác thực người dùng"
                    ));
            }

            logger.info("User {} requesting to complete order {}", currentUser.getUserId(), orderId);
            
            orderService.completeOrder(orderId, currentUser.getUserId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đơn hàng đã được hoàn thành thành công");
            response.put("orderId", orderId);
            response.put("completedBy", currentUser.getUserId());
            response.put("completedAt", Timestamp.from(Instant.now()));
            
            return ResponseEntity.ok(response);
            
        } catch (EntityNotFoundException e) {
            logger.error("Order not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false, 
                    "message", e.getMessage()
                ));
        } catch (IllegalStateException e) {
            logger.error("Invalid order completion attempt: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false, 
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Error completing order: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false, 
                    "message", "Lỗi hệ thống khi hoàn thành đơn hàng: " + e.getMessage()
                ));
        }
    }

    /**
     * Get orders pending auto-completion (for monitoring)
     */
    @GetMapping("/pending-auto-completion")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<Map<String, Object>> getOrdersPendingAutoCompletion() {
        try {
            List<Orders> pendingOrders = orderService.getOrdersPendingAutoCompletion();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách đơn hàng chờ tự động hoàn thành thành công");
            response.put("orders", pendingOrders);
            response.put("totalCount", pendingOrders.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting orders pending auto-completion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false, 
                    "message", "Lỗi hệ thống: " + e.getMessage()
                ));
        }
    }

    /**
     * Manually trigger auto-completion check (Admin only)
     */
    @PostMapping("/trigger-auto-completion")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<Map<String, Object>> triggerAutoCompletion() {
        try {
            logger.info("Manual trigger of auto-completion requested");
            orderService.autoCompleteOrders();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã kích hoạt quá trình tự động hoàn thành đơn hàng");
            response.put("triggeredAt", Timestamp.from(Instant.now()));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error triggering auto-completion: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                    "success", false, 
                    "message", "Lỗi hệ thống: " + e.getMessage()
                ));
        }
    }
}
