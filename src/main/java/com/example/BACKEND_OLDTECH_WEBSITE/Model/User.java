package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import jakarta.persistence.*;
import lombok.*;

//import com.fasterxml.jackson.annotation.JsonFormat; if any1 ask how to change the day format
import java.sql.Date;
import java.sql.Timestamp;

@Entity
@Table(name = "user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String email;

    private String password;

    private String phoneNumber;

    private String firstName;

    private String lastName;

    private Date dob;

    private String avatarUrl;

    private RoleEnum role;

    private String refundMomoAccount;

    private AccountStatusEnum accountStatus;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Timestamp lastLogin;
}