package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.ArrayList;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

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
}
