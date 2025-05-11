package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private Integer cartId;
    private BigDecimal totalPrice;
    private List<CartItem> items;
    private String createdAt;
    private String updatedAt;
    private Integer userId;
}
