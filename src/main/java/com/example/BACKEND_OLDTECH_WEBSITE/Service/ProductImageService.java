package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductImageRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private final String uploadDir = "uploads/products/";
    private final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private final int MIN_IMAGES_REQUIRED = 5;
    private final int MAX_IMAGES_ALLOWED = 10;

    @Autowired
    public ProductImageService(ProductImageRepository productImageRepository, ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }


    @Transactional
    public List<ProductImage> uploadProductImages(Integer productId, List<MultipartFile> files, Integer thumbnailIndex) 
            throws IllegalArgumentException, IOException {
        
        // Validate product exists
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // Validate number of images
        if (files == null || files.size() < MIN_IMAGES_REQUIRED) {
            throw new IllegalArgumentException("At least " + MIN_IMAGES_REQUIRED + " images are required");
        }
        
        if (files.size() > MAX_IMAGES_ALLOWED) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES_ALLOWED + " images are allowed");
        }
        
        // Validate thumbnail index
        if (thumbnailIndex == null || thumbnailIndex < 0 || thumbnailIndex >= files.size()) {
            throw new IllegalArgumentException("Invalid thumbnail index");
        }
        
        List<ProductImage> savedImages = new ArrayList<>();
        
        // Process each file
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            
            // Validate file size
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() + "' exceeds maximum size of 10MB");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File '" + file.getOriginalFilename() + "' is not an image");
            }
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            String fileUrl = uploadDir + filename;
            
            // TODO: Actual file saving logic (using cloud storage or local filesystem)
            // For now, we'll just save the URL in the database
            
            // Create and save ProductImage entity
            ProductImage productImage = new ProductImage();
            productImage.setProductId(productId);
            productImage.setImageUrl(fileUrl);
            productImage.setIsThumbnail(i == thumbnailIndex);
            productImage.setCreatedAt(Timestamp.from(Instant.now()));
            
            savedImages.add(productImageRepository.save(productImage));
        }
        
        return savedImages;
    }

    public List<ProductImage> getProductImages(Integer productId) {
        return productImageRepository.findByProductId(productId);
    }
    

    public ProductImage getProductThumbnail(Integer productId) {
        return productImageRepository.findByProductIdAndIsThumbnail(productId, true)
                .orElseThrow(() -> new EntityNotFoundException("Thumbnail not found for product: " + productId));
    }
    

    @Transactional
    public void deleteProductImage(Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with ID: " + imageId));
        
        // Get product and check if it's approved
        Product product = productRepository.findById(image.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found for image: " + imageId));
        
        // Check if product is approved or has approved status
        if (Boolean.TRUE.equals(product.getIsApproved()) || 
                product.getStatus() == ProductStatusEnum.Approved) {
            throw new IllegalStateException("Cannot delete images from an approved product");
        }
        
        // TODO: Delete actual file from storage
        
        productImageRepository.delete(image);
    }
    
    /**
     * Delete all images for a product if it's not approved
     */
    @Transactional
    public void deleteAllProductImages(Integer productId) {
        // Check if product is approved
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        
        // Check if product is approved or has approved status
        if (Boolean.TRUE.equals(product.getIsApproved()) || 
                product.getStatus() == ProductStatusEnum.Approved) {
            throw new IllegalStateException("Cannot delete images from an approved product");
        }
        
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        
        // TODO: Delete actual files from storage
        
        productImageRepository.deleteAll(images);
    }
    

    @Transactional
    public void updateProductThumbnail(Integer productId, Integer imageId) {
        // Reset all thumbnails for this product
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        for (ProductImage image : images) {
            image.setIsThumbnail(false);
            productImageRepository.save(image);
        }
        
        // Set new thumbnail
        ProductImage newThumbnail = productImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with ID: " + imageId));
        
        if (!newThumbnail.getProductId().equals(productId)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }
        
        newThumbnail.setIsThumbnail(true);
        productImageRepository.save(newThumbnail);
    }
}


//fix sau