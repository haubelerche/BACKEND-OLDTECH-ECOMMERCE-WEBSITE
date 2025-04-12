package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;
import java.util.Date;


@Entity
@Table(name = "user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "email", length = 200)
    @NotBlank(message = "Thông tin không được để trống")
    private String email;

    @Column(name = "password", length = 200)
    @NotBlank(message = "Thông tin không được để trống")
    private String password;

    @Column(name = "phone_number", length = 20)
    @NotBlank(message = "Thông tin không được để trống")
    private String phoneNumber;

    @Column(name = "first_name", length = 100)
    @NotBlank(message = "Thông tin không được để trống")
    private String firstName;

    @Column(name = "last_name", length = 100)
    @NotBlank(message = "Thông tin không được để trống")
    private String lastName;

    @Column(name = "dob")
    @NotBlank(message = "Thông tin không được để trống")
    private Date dob;

    @Column(name = "avatar_url", length = 2048)
    private String avatarUrl;

    @Column(name = "role")
    private RoleEnum role;

    @Column(name = "refund_momo_account", length = 10)
    @NotBlank(message = "Thông tin không được để trống")
    private String refundMomoAccount;

    @Column(name = "is_active", columnDefinition = "TINYINT(1)")
    private Boolean isActive;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "last_login")
    private Timestamp lastLogin;



}