package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2RegisterRequest {
    private String phoneNumber;
    private Date dob;
    private String refundMomoAccount;

    private String password;
    private String firstName;
    private String lastName;
    private Boolean isSellerRequest;


}