package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserService userService;

    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    public Product getProductById(Integer productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByStatus(ProductStatusEnum status) {
        return productRepository.findByStatus(status);
    }

    @Transactional
    public void markProductAsPending(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setStatus(ProductStatusEnum.Pending);
        product.setUpdatedAt(Timestamp.from(Instant.now()));
        productRepository.save(product);
    }

    @Transactional
    public void verifyProduct(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setStatus(ProductStatusEnum.Approved);
        product.setIsApproved(true);
        product.setIsVisible(true);
        product.setUpdatedAt(Timestamp.from(Instant.now()));
        productRepository.save(product);
    }

    @Transactional
    public void verifyMultipleProducts(List<Integer> productIds) {
        List<Product> productsToUpdate = new ArrayList<>();
        Timestamp now = Timestamp.from(Instant.now());

        for (Integer productId : productIds) {
            productRepository.findById(productId).ifPresent(product -> {
                product.setStatus(ProductStatusEnum.Approved);
                product.setIsApproved(true);
                product.setIsVisible(true);
                product.setUpdatedAt(now);
                productsToUpdate.add(product);
            });
        }

        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }

    @Transactional
    public void rejectProduct(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setStatus(ProductStatusEnum.Rejected);
        product.setIsApproved(false);
        product.setIsVisible(false);
        product.setUpdatedAt(Timestamp.from(Instant.now()));
        productRepository.save(product);
    }

    @Transactional
    public void rejectMultipleProducts(List<Integer> productIds) {
        List<Product> productsToUpdate = new ArrayList<>();
        Timestamp now = Timestamp.from(Instant.now());

        for (Integer productId : productIds) {
            productRepository.findById(productId).ifPresent(product -> {
                product.setStatus(ProductStatusEnum.Rejected);
                product.setIsApproved(false);
                product.setIsVisible(false);
                product.setUpdatedAt(now);
                productsToUpdate.add(product);
            });
        }

        if (!productsToUpdate.isEmpty()) {
            productRepository.saveAll(productsToUpdate);
        }
    }

    @Transactional
    public void hideProduct(Integer productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));
        product.setIsVisible(false);
        product.setUpdatedAt(Timestamp.from(Instant.now()));
        productRepository.save(product);
    }

    @Transactional
    public int hideAllProductsFromSeller(Integer sellerId) {
        List<Product> sellerProducts = productRepository.findBySellerId(sellerId);
        if (sellerProducts.isEmpty()) {
            return 0;
        }

        Timestamp now = Timestamp.from(Instant.now());
        for (Product product : sellerProducts) {
            product.setIsVisible(false);
            product.setUpdatedAt(now);
        }

        productRepository.saveAll(sellerProducts);
        return sellerProducts.size();
    }

    @Transactional
    public void setProductCategory(Integer productId, Integer categoryId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + productId));

        Category category = categoryRepository.findById(categoryId.longValue())
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với ID: " + categoryId));

        product.setCategoryId(categoryId.longValue());
        product.setUpdatedAt(Timestamp.from(Instant.now()));

        productRepository.save(product);
    }

    /**
     * Filter products by seller's location
     */
    public List<Product> filterProductsBySellerLocation(List<Product> products, String location) {
        if (location == null || location.trim().isEmpty()) {
            return products;
        }
        
        List<Product> filteredProducts = new ArrayList<>();
        String searchLocation = location.toLowerCase().trim();
        
        for (Product product : products) {
            try {
                // Get seller's location from User model
                var seller = userService.findUserById(product.getSellerId());
                if (seller != null && seller.getLivingLocation() != null) {
                    String sellerLocation = seller.getLivingLocation().toLowerCase();
                    if (sellerLocation.contains(searchLocation)) {
                        filteredProducts.add(product);
                    }
                }
            } catch (Exception e) {
                // Log error but continue processing other products
                System.err.println("Error getting seller location for product " + product.getProductId() + ": " + e.getMessage());
            }
        }
        
        return filteredProducts;
    }

    /**
     * Filter products by location proximity (basic implementation)
     * This is a simplified version - in a real application, you'd use proper geolocation APIs
     */
    public List<Product> filterProductsByLocationProximity(List<Product> products, String userLocation, Integer radiusKm) {
        if (userLocation == null || userLocation.trim().isEmpty()) {
            return products;
        }
          List<Product> filteredProducts = new ArrayList<>();
        
        for (Product product : products) {
            try {
                // Get seller's location from User model
                var seller = userService.findUserById(product.getSellerId());
                if (seller != null && seller.getLivingLocation() != null) {
                    String sellerLocation = seller.getLivingLocation().toLowerCase();
                    
                    // Simple proximity check - in reality you'd calculate actual distance
                    // For now, we'll check if locations are in the same city/province
                    if (isLocationNearby(userLocation, sellerLocation, radiusKm)) {
                        filteredProducts.add(product);
                    }
                }
            } catch (Exception e) {
                // Log error but continue processing other products
                System.err.println("Error checking location proximity for product " + product.getProductId() + ": " + e.getMessage());
            }
        }
        
        return filteredProducts;
    }

    /**
     * Simple proximity check - in a real application, you'd use proper geolocation calculation
     * This is just a basic string matching implementation
     */
    private boolean isLocationNearby(String userLocation, String sellerLocation, Integer radiusKm) {
        if (userLocation == null || sellerLocation == null) {
            return false;
        }
        
        String userLoc = userLocation.toLowerCase().trim();
        String sellerLoc = sellerLocation.toLowerCase().trim();
        
        // If locations contain same city/province name, consider them nearby
        String[] userLocationParts = userLoc.split("[,\\s]+");
        String[] sellerLocationParts = sellerLoc.split("[,\\s]+");
        
        for (String userPart : userLocationParts) {
            if (userPart.length() > 2) { // Only check meaningful parts
                for (String sellerPart : sellerLocationParts) {
                    if (sellerPart.contains(userPart) || userPart.contains(sellerPart)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public Page<Product> searchVisibleApprovedProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return productRepository.findByIsVisibleTrueAndIsApprovedTrueAndNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                keyword, keyword, pageable);
        } else {
            return productRepository.findByIsVisibleTrueAndIsApprovedTrue(pageable);
        }
    }

    /**
     * Get a random list of products (size = count) using fast DB query
     */
    public List<Product> getRandomProducts(int count) {
        return productRepository.findRandomVisibleApprovedProducts(count);
    }
}
