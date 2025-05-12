package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;


@Entity
@Table(name = "refund")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Integer refundId;


    @Column(name = "order_id")
    private Integer orderId;


    @Column(name = "user_id")
    private Integer userId;


    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RefundStatusEnum status;

    @Column(name = "admin_notes")
    private String adminNotes;

    @Column(name = "requested_at")
    private Timestamp requestedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}