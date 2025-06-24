package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Address;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AddressTypeEnum;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {
    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "District cannot be blank")
    private String district;

    @NotBlank(message = "Ward cannot be blank")
    private String ward;

    @NotBlank(message = "Street cannot be blank")
    private String street;

    private String detailedAddress;

    private AddressTypeEnum addressType;

    private Boolean isDefault;


    // Create explicit getter for boolean field to avoid naming conflicts
    public Boolean isIsDefault() {
        return isDefault;
    }

}
