package com.example.BACKEND_OLDTECH_WEBSITE.Model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "oauth_connections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OauthConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;



    private String providerId;

    private String provider;


    private String email;


    private String accessToken;


    private String refreshToken;


    private LocalDateTime tokenExpiry;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}