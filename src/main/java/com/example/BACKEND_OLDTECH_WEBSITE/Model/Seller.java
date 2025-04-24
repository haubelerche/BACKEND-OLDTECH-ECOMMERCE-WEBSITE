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

    @Column(name = "seller_id", nullable = false)
    private Integer sellerId;

    @Column(name = "is_approved", columnDefinition = "TINYINT(1)")
    private Boolean isApproved;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status",nullable = false)
    private AccountStatusEnum accountStatus;

    @Column(name = "momo_account", length = 15, nullable = false)
    private String momoAccount;

    @Column(name = "approved_at", nullable = false)
    private Timestamp approvedAt;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
//Referencing column 'seller_id' and referenced column 'user_id' in foreign key constraint 'FK81ec0le2kjk0obbodah7c953d' are incompatible.
// errors come from bigint in seller_id