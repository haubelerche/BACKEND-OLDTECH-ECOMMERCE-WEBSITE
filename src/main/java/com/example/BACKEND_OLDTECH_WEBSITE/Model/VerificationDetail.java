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
    @Column(name = "verify_id", nullable = false)
    private Integer verifyId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User userId;

    @Column(name = "is_verified", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean isVerified;

    @Column(name = "national_id", length = 20, nullable = false)
    
    private String nationalId;

    @Column(name = "front_image_url", length = 2048, nullable = false)
    
    private String frontImageUrl;

    @Column(name = "back_image_url", length = 2048, nullable = false)
    
    private String backImageUrl;

    @Column(name = "verified_at", nullable = false)
    private Timestamp verifiedAt;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}