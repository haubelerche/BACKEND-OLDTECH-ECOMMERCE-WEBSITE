package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "verification_detail")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer verifyId;


    private Integer userId;

    private Boolean isVerified;


    private String nationalId;

    private String frontImageUrl;

    private String backImageUrl;

    private Timestamp verifiedAt;

    private Timestamp createdAt;

    private Timestamp updatedAt;
}