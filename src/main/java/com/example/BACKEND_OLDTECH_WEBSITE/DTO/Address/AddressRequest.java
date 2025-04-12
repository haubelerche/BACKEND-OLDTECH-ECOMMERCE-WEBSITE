package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    private String city;
    private String district;
    private String ward;
    private String street;
    private String addressType;
    private Boolean isDefault;
}
