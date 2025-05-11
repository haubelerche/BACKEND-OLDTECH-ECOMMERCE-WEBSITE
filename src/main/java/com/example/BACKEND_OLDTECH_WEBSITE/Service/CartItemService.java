package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
// import com.example.BACKEND_OLDTECH_WEBSITE.Model.User; // User might be needed for userId -> cartId logic
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CartItemRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository; // For user validation
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class CartItemService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private Integer resolveCartIdForUser(Integer userId) {

        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        System.out.println("Warning: Using placeholder logic for resolveCartIdForUser. userId is used as cartId: " + userId);
        return userId;
    }


    @Autowired
    public CartItemService(CartItemRepository cartItemRepository,
                           ProductRepository productRepository,
                           UserRepository userRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void addItem(Integer userId, Integer productId) {

        Integer cartId = resolveCartIdForUser(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));


        CartItem newItem = new CartItem();
        newItem.setCartId(cartId);
        newItem.setProduct(product);

        newItem.setAddedAt(LocalDateTime.now());
        cartItemRepository.save(newItem);

    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByUserId(Integer userId) {
        Integer cartId = resolveCartIdForUser(userId);
        return cartItemRepository.findByCartId(cartId);
    }
    

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsByCartId(Integer cartId) {
        return cartItemRepository.findByCartId(cartId);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPriceByUserId(Integer userId) {
        Integer cartId = resolveCartIdForUser(userId); // Placeholder logic
        List<CartItem> items = cartItemRepository.findByCartId(cartId);
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CartItem item : items) {
            if (item.getProduct() != null && item.getProduct().getPrice() != null) {
                totalPrice = totalPrice.add(item.getProduct().getPrice());
            }
        }
        return totalPrice;
    }

    @Transactional
    public void removeItem(Integer userId, Integer productId) {
        Integer cartId = resolveCartIdForUser(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));

        CartItem itemToRemove = cartItemRepository.findByCartIdAndProduct(cartId, product)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Sản phẩm với ID: " + productId + " không tồn tại trong giỏ hàng với ID: " + cartId));
        
        cartItemRepository.delete(itemToRemove);
    }

    @Transactional
    public void clearCartByUserId(Integer userId) {
        Integer cartId = resolveCartIdForUser(userId);
        cartItemRepository.deleteByCartId(cartId); 
    }
} 