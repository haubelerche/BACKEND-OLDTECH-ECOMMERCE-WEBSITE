package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AddressResponse {
    private Integer addressId;
    private Integer userId;
    private String city;
    private String district;
    private String ward;
    private String street;
    private String detailedAddress;
    private AddressTypeEnum addressType;
    private Boolean isDefault;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
