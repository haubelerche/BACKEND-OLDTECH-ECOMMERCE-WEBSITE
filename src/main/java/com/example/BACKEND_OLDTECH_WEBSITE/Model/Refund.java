package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
    @Column(name = "refund_id", nullable = false)
    private Integer refundId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "reason", columnDefinition = "MEDIUMTEXT", nullable = false)
   
    private String reason;

    @Column(name = "status", nullable = false)
    private RefundStatusEnum status;

    @Column(name = "admin_notes", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String adminNotes;

    @Column(name = "requested_at", nullable = false)
    private Timestamp requestedAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}