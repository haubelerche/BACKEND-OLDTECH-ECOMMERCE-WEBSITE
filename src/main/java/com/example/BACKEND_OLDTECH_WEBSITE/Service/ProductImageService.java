package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.ProductImage;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductImageRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ProductImageService.class);
    private final String uploadDir = "uploads/products/";
    private final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB in bytes
    private final int MIN_IMAGES_REQUIRED = 5;
    private final int MAX_IMAGES_ALLOWED = 10;

    @Autowired
    public ProductImageService(ProductImageRepository productImageRepository, 
                             ProductRepository productRepository) {
        this.productImageRepository = productImageRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public List<ProductImage> saveProductImages(Integer productId, List<String> imageUrls) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
        
        List<ProductImage> savedImages = new ArrayList<>();
        
        // Set the first image as primary if no primary image exists
        boolean hasPrimary = productImageRepository.findByProductAndIsPrimaryTrue(product).isPresent();
        
        for (int i = 0; i < imageUrls.size(); i++) {
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setSeller(product.getSeller()); // Set seller from product
            image.setImageUrl(imageUrls.get(i));
            image.setDisplayOrder(i + 1);
            
            // Set the first image as primary if no primary exists
            if (!hasPrimary && i == 0) {
                image.setIsPrimary(true);
                hasPrimary = true;
            } else {
                image.setIsPrimary(false);
            }
            
            savedImages.add(productImageRepository.save(image));
            log.info("Đã lưu ảnh cho sản phẩm ID: {}, URL: {}", productId, imageUrls.get(i));
        }
        
        return savedImages;
    }
    
    @Transactional(readOnly = true)
    public List<ProductImage> getProductImages(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
                
        return productImageRepository.findByProductOrderByDisplayOrderAsc(product);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductImage> getPrimaryProductImage(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
                
        return productImageRepository.findByProductAndIsPrimaryTrue(product);
    }
    
    @Transactional
    public ProductImage setPrimaryImage(Integer productId, Integer imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + productId));
        
        ProductImage newPrimaryImage = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh với ID: " + imageId));
        
        // Check if image belongs to product
        if (!newPrimaryImage.getProduct().equals(product)) {
            throw new IllegalArgumentException("Ảnh không thuộc về sản phẩm này");
        }
        
        // Clear current primary image
        productImageRepository.findByProductAndIsPrimaryTrue(product)
                .ifPresent(image -> {
                    image.setIsPrimary(false);
                    productImageRepository.save(image);
                });
        
        // Set new primary image
        newPrimaryImage.setIsPrimary(true);
        return productImageRepository.save(newPrimaryImage);
    }
    
    @Transactional
    public void deleteProductImage(Integer imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ảnh với ID: " + imageId));
        
        // If this is a primary image, try to set another image as primary
        if (Boolean.TRUE.equals(image.getIsPrimary())) {
            List<ProductImage> otherImages = productImageRepository.findByProductOrderByDisplayOrderAsc(image.getProduct());
            
            for (ProductImage otherImage : otherImages) {
                if (!otherImage.getImageId().equals(imageId)) {
                    otherImage.setIsPrimary(true);
                    productImageRepository.save(otherImage);
                    break;
                }
            }
        }
        
        productImageRepository.delete(image);
        log.info("Đã xóa ảnh với ID: {}", imageId);
    }

    @Transactional
    public List<ProductImage> uploadProductImages(Integer productId, List<MultipartFile> files, Integer thumbnailIndex) 
            throws IllegalArgumentException, IOException {
        
        // Validate product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with ID: " + productId));
        
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
            
       
            ProductImage productImage = new ProductImage();
            productImage.setProduct(product);
            productImage.setSeller(product.getSeller());
            productImage.setImageUrl(fileUrl);
            productImage.setIsPrimary(i == thumbnailIndex);
            productImage.setDisplayOrder(i + 1);
            
            savedImages.add(productImageRepository.save(productImage));
        }
        
        return savedImages;
    }

    public ProductImage getProductThumbnail(Integer productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        return productImageRepository.findByProductAndIsPrimaryTrue(product)
                .orElseThrow(() -> new EntityNotFoundException("Thumbnail not found for product: " + productId));
    }
 
    @Transactional
    public void deleteAllProductImages(Integer productId) {
  
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        

        if (Boolean.TRUE.equals(product.getIsApproved()) || 
                product.getStatus() == ProductStatusEnum.Approved) {
            throw new IllegalStateException("Cannot delete images from an approved product");
        }
        
        List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(product);
        
       
        
        productImageRepository.deleteAll(images);
    }
    
    @Transactional
    public void updateProductThumbnail(Integer productId, Integer imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
       
        List<ProductImage> images = productImageRepository.findByProductOrderByDisplayOrderAsc(product);
        for (ProductImage image : images) {
            image.setIsPrimary(false);
            productImageRepository.save(image);
        }
        
        // Set new thumbnail
        ProductImage newThumbnail = productImageRepository.findById(imageId)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with ID: " + imageId));
        
        if (!newThumbnail.getProduct().equals(product)) {
            throw new IllegalArgumentException("Image does not belong to this product");
        }
        
        newThumbnail.setIsPrimary(true);
        productImageRepository.save(newThumbnail);
    }
}


//fix sau