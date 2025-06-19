package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AddressDTO {
    @NotBlank(message = "City cannot be blank")
    private String city;

    @NotBlank(message = "District cannot be blank")
    private String district;

    @NotBlank(message = "Ward cannot be blank")
    private String ward;

    @NotBlank(message = "Street cannot be blank")
    private String street;

    @NotBlank(message = "Full name cannot be blank")
    private String fullName;

    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;

    private String addressDetails;

    private boolean isDefault;
}
