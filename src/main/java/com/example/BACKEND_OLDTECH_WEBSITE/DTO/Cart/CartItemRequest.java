package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;

import lombok.Data;


@Data
public class CartItemRequest {
    private Integer cartId;
   
    private String createdAt; 
    private String updatedAt; 
    private Integer userId;


    private Integer productId; 
    //private Integer quantity;
}