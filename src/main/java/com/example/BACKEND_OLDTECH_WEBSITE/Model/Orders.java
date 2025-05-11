package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import jakarta.persistence.*;

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
    @Column(name = "order_id", columnDefinition = "INT UNSIGNED")
    private Integer orderId;
    
    private Timestamp orderTime;

    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Integer userId;

    @Column(name = "shipping_address_id", columnDefinition = "INT UNSIGNED")
    private Integer shippingAddressId;

    private PaymentMethodEnum paymentMethod;

    private BigDecimal totalAmount;

    private OrderStatusEnum status;

    private String notes;

    private Timestamp createdAt;

    private Timestamp updatedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", referencedColumnName = "address_id", insertable = false, updatable = false)
    private Address shippingAddress;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false)
    private User user;
}