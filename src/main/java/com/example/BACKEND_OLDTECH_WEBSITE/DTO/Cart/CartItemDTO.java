package com.example.BACKEND_OLDTECH_WEBSITE.DTO.Cart;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CartItemDTO {


        private CartItem cartItem;
        private boolean available;

        public CartItemDTO(CartItem cartItem, boolean available) {
            this.cartItem = cartItem;
            this.available = available;

    }}

