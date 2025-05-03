package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import jakarta.persistence.*;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;
    private Timestamp orderTime;

    private Integer userId;

    private Integer ShippingAddressId;

    private PaymentMethodEnum paymentMethod;

    private BigDecimal totalAmount;

    private OrderStatusEnum status;

    private String notes;

    private Timestamp createdAt;

    private Timestamp updatedAt;


}