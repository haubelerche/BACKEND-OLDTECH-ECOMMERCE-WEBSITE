package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class OrderResponse {
    private Integer orderId;
    private Timestamp orderTime;
    private Integer userId;
    private Integer shippingAddressId;
    private PaymentMethodEnum paymentMethod;
    private BigDecimal totalAmount;
    private OrderStatusEnum status;
    private String notes;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // Optional: Include user and address details for convenience
    private String userEmail;
    private String userFullName;
    private String shippingAddressDetail;
}