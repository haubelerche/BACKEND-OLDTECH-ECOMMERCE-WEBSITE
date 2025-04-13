package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class OrderRequest {
    @NotNull(message = "Shipping address ID cannot be null")
    private Integer shippingAddressId;

    @NotNull(message = "Payment method cannot be null")
    private String paymentMethod;  // Or use an enum here
    private String notes;
}