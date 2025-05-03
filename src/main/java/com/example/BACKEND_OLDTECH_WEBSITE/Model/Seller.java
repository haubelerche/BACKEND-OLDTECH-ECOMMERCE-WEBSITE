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

    private Integer sellerId;

    private Boolean isApproved;


    private AccountStatusEnum accountStatus;

    private String momoAccount;

    private Timestamp approvedAt;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}
//Referencing column 'seller_id' and referenced column 'user_id' in foreign key constraint 'FK81ec0le2kjk0obbodah7c953d' are incompatible.
// errors come from bigint in seller_id