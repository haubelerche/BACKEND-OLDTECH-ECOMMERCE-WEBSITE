package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import jakarta.persistence.*;

import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "seller")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Integer sellerId;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatusEnum accountStatus;

    @Column(name = "momo_account", length = 255)
    private String momoAccount;

    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Builder.Default
    @Column(name = "business_status", columnDefinition = "TINYINT(1) DEFAULT '1'")
    private Byte businessStatus = 1;  }
