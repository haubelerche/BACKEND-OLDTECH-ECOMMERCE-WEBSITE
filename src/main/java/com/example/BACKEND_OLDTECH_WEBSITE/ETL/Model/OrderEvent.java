package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model sự kiện liên quan đến đơn hàng
 * Sử dụng trong Kafka để xử lý thời gian thực
 */ 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    
    private String eventId;
    private String eventType; // CREATED, UPDATED, STATUS_CHANGED, CANCELLED
    private Integer orderId;
    private Integer userId;
    private String status;
    private BigDecimal totalAmount;
    private String productCategory;
    private Integer sellerId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private Map<String, Object> metadata;
    
    // Factory methods for different event types
    public static OrderEvent createOrderCreatedEvent(Integer orderId, Integer userId, 
                                                   BigDecimal totalAmount, String category, Integer sellerId) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType("CREATED");
        event.setOrderId(orderId);
        event.setUserId(userId);
        event.setTotalAmount(totalAmount);
        event.setProductCategory(category);
        event.setSellerId(sellerId);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }
    
    public static OrderEvent createStatusChangeEvent(Integer orderId, String oldStatus, String newStatus) {
        OrderEvent event = new OrderEvent();
        event.setEventId(generateEventId());
        event.setEventType("STATUS_CHANGED");
        event.setOrderId(orderId);
        event.setStatus(newStatus);
        event.setTimestamp(LocalDateTime.now());
        event.setMetadata(Map.of("oldStatus", oldStatus, "newStatus", newStatus));
        return event;
    }
    
    private static String generateEventId() {
        return "evt_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000);
    }
}
