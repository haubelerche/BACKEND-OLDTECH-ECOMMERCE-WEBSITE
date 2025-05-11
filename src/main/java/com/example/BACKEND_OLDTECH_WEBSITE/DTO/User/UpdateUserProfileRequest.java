package com.example.BACKEND_OLDTECH_WEBSITE.DTO.User;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserProfileRequest {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Date dob; 
    private String avatarUrl;
    private String email;
    private String password;
    private String refundMomoAccount;
} 