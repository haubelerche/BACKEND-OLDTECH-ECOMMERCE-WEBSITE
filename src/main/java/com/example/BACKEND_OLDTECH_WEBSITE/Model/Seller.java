 package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "seller")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id", columnDefinition = "INT UNSIGNED", nullable = false)
    private Integer sellerId;

    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @Column(name = "shop_name")
    private String shopName;

    @Column(name = "description")
    private String description;

    @Column(name = "is_approved")
    private Boolean isApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AccountStatusEnum status;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "business_certificate_url")
    private String businessCertificateUrl;    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "momo_account")
    private String momoAccount;

    @Column(name = "business_status")
    private Boolean businessStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    private AccountStatusEnum accountStatus;
}
