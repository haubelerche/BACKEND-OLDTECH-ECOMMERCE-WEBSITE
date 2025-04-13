package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class OrderResponse {
    private Integer orderId;
    private Integer userId;
    private Integer shippingAddressId;
    private Date orderTime;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String status;
    private String notes;
    private Date createdAt;
    private Date updatedAt;
}