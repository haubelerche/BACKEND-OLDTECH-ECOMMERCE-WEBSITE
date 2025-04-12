package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCartResponse {
    private Integer cartId;
    private Double totalPrice;
    private List<CartItemResponse> items;
}
