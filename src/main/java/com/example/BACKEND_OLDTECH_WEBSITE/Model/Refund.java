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
    private Integer refundId;


    private Integer orderId;


    private Integer userId;


    private String reason;

    private RefundStatusEnum status;

    private String adminNotes;

    private Timestamp requestedAt;

    private Timestamp updatedAt;
}