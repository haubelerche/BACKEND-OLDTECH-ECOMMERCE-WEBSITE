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
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        System.out.println("Cảnh báo: Sử dụng logic thay thế " + userId);
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

    /**
     * Remove a specific product from all users' carts
     * This is called when a product becomes unavailable (hidden, rejected, deleted)
     */
    @Transactional
    public int removeProductFromAllCarts(Integer productId) {
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));
            
            // Find all cart items containing this product
            List<CartItem> cartItemsWithProduct = cartItemRepository.findByProduct(product);
            
            if (!cartItemsWithProduct.isEmpty()) {
                // Delete all cart items containing this product
                cartItemRepository.deleteAll(cartItemsWithProduct);
                System.out.println("Đã xóa sản phẩm ID " + productId + " khỏi " + cartItemsWithProduct.size() + " giỏ hàng");
                return cartItemsWithProduct.size();
            }
            
            return 0;
        } catch (Exception e) {
            System.err.println("Lỗi khi xóa sản phẩm khỏi tất cả giỏ hàng: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Check if a product is available for purchase
     * Returns false if product is hidden, rejected, or not approved
     */
    @Transactional(readOnly = true)
    public boolean isProductAvailable(Integer productId) {
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) {
                return false;
            }
            
            return Boolean.TRUE.equals(product.getIsVisible()) && 
                   Boolean.TRUE.equals(product.getIsApproved()) &&
                   product.getStatus() == com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum.Approved;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate cart items and remove any unavailable products
     * Returns count of removed items
     */
    @Transactional
    public int cleanupUnavailableProducts(Integer userId) {
        try {
            Integer cartId = resolveCartIdForUser(userId);
            List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
            
            int removedCount = 0;
            for (CartItem item : cartItems) {
                if (!isProductAvailable(item.getProduct().getProductId())) {
                    cartItemRepository.delete(item);
                    removedCount++;
                }
            }
            
            if (removedCount > 0) {
                System.out.println("Đã xóa " + removedCount + " sản phẩm không khả dụng khỏi giỏ hàng của người dùng " + userId);
            }
            
            return removedCount;
        } catch (Exception e) {
            System.err.println("Lỗi khi dọn dẹp giỏ hàng: " + e.getMessage());
            return 0;
        }
    }
}