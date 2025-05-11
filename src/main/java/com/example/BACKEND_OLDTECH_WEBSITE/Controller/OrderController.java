package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.OrderService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    // Create a new order
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody @Validated Orders order) {
        try {
            Orders createdOrder = orderService.createOrder(order);
            return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return handleException("Lỗi tạo đơn hàng", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException("Lỗi tạo đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all orders
    @GetMapping
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Orders> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return handleException("Lỗi lấy danh sách đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get order by ID
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable Integer orderId) {
        try {
            Orders order = orderService.getOrderById(orderId);
            return ResponseEntity.ok(order);
        } catch (EntityNotFoundException e) {
            return handleException("Đơn hàng không tồn tại", e, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return handleException("Lỗi lấy thông tin đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get orders by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<Orders> orders = orderService.getOrderHistory(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return handleException("Lỗi lấy lịch sử đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Confirm an order (change status to Processing)
    @PostMapping("/{orderId}/confirm")
    public ResponseEntity<?> confirmOrder(@PathVariable Integer orderId) {
        try {
            orderService.confirmOrder(orderId);
            return ResponseEntity.ok(Map.of(
                "message", "Đơn hàng " + orderId + " đã được xác nhận",
                "success", true
            ));
        } catch (EntityNotFoundException e) {
            return handleException("Đơn hàng không tồn tại", e, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return handleException("Không thể xác nhận đơn hàng", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException("Lỗi xác nhận đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Update order status
    @PutMapping("/{orderId}/status")
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

    // Cancel an order
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable Integer orderId) {
        try {
            orderService.cancelOrder(orderId);
            return ResponseEntity.ok(Map.of(
                "message", "Đơn hàng " + orderId + " đã được hủy",
                "success", true
            ));
        } catch (EntityNotFoundException e) {
            return handleException("Đơn hàng không tồn tại", e, HttpStatus.NOT_FOUND);
        } catch (IllegalStateException e) {
            return handleException("Không thể hủy đơn hàng", e, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return handleException("Lỗi hủy đơn hàng", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        logger.error(message, e);
        Map<String, Object> response = new HashMap<>();
        response.put("message", message + ": " + e.getMessage());
        response.put("success", false);
        return new ResponseEntity<>(response, status);
    }
}