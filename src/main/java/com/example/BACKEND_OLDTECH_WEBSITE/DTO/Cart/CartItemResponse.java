package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import lombok.Data;
@Data
public class CartItemResponse {
    private Integer cartItemId;
    private Integer productId;
    private Double itemTotal;

}
