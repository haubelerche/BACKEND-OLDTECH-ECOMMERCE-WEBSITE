package com.example.BACKEND_OLDTECH_WEBSITE.Controller;


import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem; // Represents individual items
import com.example.BACKEND_OLDTECH_WEBSITE.Service.CartItemService; // Changed service name
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List; // For returning list of cart items

@RestController
@RequestMapping("/cart")
public class CartItemController {

    @Autowired
    private CartItemService cartItemService;


    @PostMapping("/addItem/{userId}")
    public ResponseEntity<?> addItem(@PathVariable Integer userId, 
                                     @RequestParam Integer productId) {
        cartItemService.addItem(userId, productId);
        return ResponseEntity.ok("Đã thêm sản phẩm vào giỏ hàng.");
    }


    @GetMapping("/getAllItems/{userId}")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable Integer userId) {
        List<CartItem> items = cartItemService.getCartItemsByUserId(userId);
        return ResponseEntity.ok(items);
    }


    @GetMapping("/totalPrice/{userId}")
    public ResponseEntity<BigDecimal> getTotalPrice(@PathVariable Integer userId) {
        BigDecimal total = cartItemService.getTotalPriceByUserId(userId);
        return ResponseEntity.ok(total);
    }


    @DeleteMapping("/removeItem/{userId}") 
    public ResponseEntity<?> removeItem(@PathVariable Integer userId, 
                                        @RequestParam Integer productId) {
        cartItemService.removeItem(userId, productId);
        return ResponseEntity.ok("Đã xóa sản phẩm khỏi giỏ hàng.");
    }


    @DeleteMapping("/clear/{userId}")
    public ResponseEntity<?> clearCart(@PathVariable Integer userId) {
        cartItemService.clearCartByUserId(userId);
        return ResponseEntity.ok("Giỏ hàng đã được dọn.");
    }
} 