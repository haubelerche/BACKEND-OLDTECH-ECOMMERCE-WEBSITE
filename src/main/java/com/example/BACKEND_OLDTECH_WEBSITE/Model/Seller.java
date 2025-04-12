package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "seller")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Seller {

    @Id
    @OneToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1)")
    @NotBlank(message = "Thông tin bắt buộc")
    private Boolean isApproved;

    @Column(name = "business_status", columnDefinition = "TINYINT(1)")
    @NotBlank(message = "Thông tin bắt buộc")
    private Boolean businessStatus;

    @Column(name = "momo_account", length = 15)
    @NotBlank(message = "Thông tin bắt buộc")
    private String momoAccount;

    @Column(name = "approved_at")
    private Timestamp approvedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}