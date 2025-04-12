package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;

@Entity
@Table(name = "order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;
    @Column(name = "order_time")
    private Timestamp orderTime;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @ManyToOne
    @JoinColumn(name = "shipping_address_id")
    private Address ShippingAddressId;


    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    @NotBlank(message = "Thông tin không được để trống")
    private PaymentMethodEnum paymentMethod;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatusEnum status;

    @Column(name = "notes", columnDefinition = "MEDIUMTEXT")
    private String notes;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;


}