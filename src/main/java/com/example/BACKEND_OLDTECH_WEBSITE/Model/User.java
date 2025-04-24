package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import jakarta.persistence.*;

import lombok.*;

import java.sql.Timestamp;
import java.sql.Date;


@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "username", length = 100, nullable = false, unique = true)
    private String username;

    @Column(name = "email", length = 200, nullable = false)
    private String email;

    @Column(name = "password", length = 200, nullable = false)
    private String password;

    @Column(name = "phone_number", length = 20, nullable = false)

    private String phoneNumber;

    @Column(name = "first_name", length = 100, nullable = false)

    private String firstName;

    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "dob")
    private Date dob;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private RoleEnum role;

    @Column(name = "refund_momo_account", length = 10, nullable = false)
    private String refundMomoAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status",nullable = false)
    private AccountStatusEnum accountStatus;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    @Column(name = "last_login", nullable = false)
    private Timestamp lastLogin;


}