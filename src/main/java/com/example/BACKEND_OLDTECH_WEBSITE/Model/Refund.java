package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;


@Entity
@Table(name = "refund")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Refund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id")
    private Integer refundId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "reason", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String reason;

    @Column(name = "status")
    private RefundStatusEnum status;

    @Column(name = "admin_notes", columnDefinition = "MEDIUMTEXT")
    private String adminNotes;

    @Column(name = "requested_at")
    private Timestamp requestedAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}