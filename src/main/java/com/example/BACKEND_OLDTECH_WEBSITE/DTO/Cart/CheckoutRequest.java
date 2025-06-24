package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    
    @NotNull(message = "Phương thức thanh toán không được để trống")
    private PaymentMethodEnum paymentMethod;
    
    private Integer shippingAddressId; // Optional, will use user's first address if not provided
    
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String notes; // Optional
    
   private Integer cartId; // Optional, can be null if no coupon is applied
    
    private Boolean checkoutAll = true; // Default to checkout all items
}
