package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", columnDefinition = "INT UNSIGNED")
    private Integer orderId;

    @Column(name = "order_time")
    private Timestamp orderTime;

    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Integer userId;

    @Column(name = "shipping_address_id", columnDefinition = "BIGINT UNSIGNED")
    private Long shippingAddressId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethodEnum paymentMethod;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatusEnum status;    private String notes;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "delivered_at")
    private Timestamp deliveredAt; // Track when order was delivered for auto-completion

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id", referencedColumnName = "address_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Address shippingAddress;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User user;
}
