package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressResponse {
    private Integer addressId;
    private String city;
    private String district;
    private String ward;
    private String street;
    private String detailedAddress;
    private String addressType;
    private Boolean isDefault;
    private List<AddressResponse> addresses;
    private String createdAt;
    private String updatedAt;
    private String userId;


}
