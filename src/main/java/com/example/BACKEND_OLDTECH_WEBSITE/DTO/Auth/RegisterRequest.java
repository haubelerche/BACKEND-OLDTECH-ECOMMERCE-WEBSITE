package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String email;
    private String username;
    private String password;
    private String phone_number;
    private String first_name;
    private String last_name;
    private Date dob;
    private String avatarUrl;
}
