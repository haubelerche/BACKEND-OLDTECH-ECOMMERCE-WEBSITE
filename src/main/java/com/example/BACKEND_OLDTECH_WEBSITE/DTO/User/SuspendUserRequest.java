package com.example.BACKEND_OLDTECH_WEBSITE.DTO.User;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;



@Setter
@Getter
@NoArgsConstructor
@Data
public class SuspendUserRequest {
    // Getters and setters
    private Integer durationInHours;
    private String reason;

}