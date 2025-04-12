package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "verification_detail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verify_id")
    private Integer verifyId;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User userId;

    @Column(name = "is_verified", columnDefinition = "TINYINT(1)")
    private Boolean isVerified;

    @Column(name = "national_id", length = 20)
    @NotBlank(message = "Thông tin không được để trống")
    private String nationalId;

    @Column(name = "front_image_url", length = 2048)
    @NotBlank(message = "Thông tin không được để trống")
    private String frontImageUrl;

    @Column(name = "back_image_url", length = 2048)
    @NotBlank(message = "Thông tin không được để trống")
    private String backImageUrl;

    @Column(name = "verified_at")
    private Timestamp verifiedAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;
}