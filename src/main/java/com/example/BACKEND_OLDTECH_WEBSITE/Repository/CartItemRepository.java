package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    Optional<CartItem> findByCartIdAndProduct(Integer cartId, Product product);
    List<CartItem> findByCartId(Integer cartId);
    List<CartItem> findByProduct(Product product);
    void deleteByCartId(Integer cartId);
    void deleteByProduct(Product product);
}