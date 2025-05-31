package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReviewRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Creates a review for a seller with a tagged product
     */
    @Transactional
    public Review createSellerReview(Integer sellerId, Review review) {
        // Validate seller exists
        if (!userRepository.existsById(sellerId)) {
            throw new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId);
        }

        // Validate reviewer exists
        if (review.getReviewerId() == null || !userRepository.existsById(review.getReviewerId())) {
            throw new EntityNotFoundException("Không tìm thấy người đánh giá với ID: " + review.getReviewerId());
        }

        // Validate rating is between 1-5
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new IllegalArgumentException("Đánh giá phải có giá trị từ 1 đến 5 sao");
        }

        // Validate product exists if a product ID is provided
        if (review.getProductId() != null) {
            Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + review.getProductId()));

            // Ensure the product belongs to the seller being reviewed
            if (!product.getSellerId().equals(sellerId)) {
                throw new IllegalArgumentException("Sản phẩm được gắn thẻ không thuộc về người bán này");
            }
        }

        review.setSellerId(sellerId);
        review.setReviewTime(Timestamp.from(Instant.now()));
        return reviewRepository.save(review);
    }

    /**
     * Get a review by ID
     */
    public Review getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));
    }

    /**
     * Get all reviews for a seller
     */
    public List<Review> getReviewsBySeller(Integer sellerId) {
        return reviewRepository.findBySellerId(sellerId);
    }

    /**
     * Get all reviews that tagged a specific product
     */
    public List<Review> getReviewsByTaggedProduct(Integer productId) {
        return reviewRepository.findByProductId(productId);
    }

    /**
     * Allow seller to respond to a review
     */
    @Transactional
    public Review respondToReview(Integer sellerId, Integer reviewId, String response) {
        // Validate seller exists and has seller role
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));

        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Chỉ người bán hoặc admin mới có thể phản hồi đánh giá");
        }

        // Get the review
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId));

        // Make sure this review is for this seller
        if (!review.getSellerId().equals(sellerId)) {
            throw new SecurityException("Người bán không được phản hồi đánh giá của người bán khác");
        }

        // Update the response
        review.setSellerResponse(response);
        review.setResponseTime(Timestamp.from(Instant.now()));

        return reviewRepository.save(review);
    }

    /**
     * Delete a review (admin only)
     */
    @Transactional
    public void deleteReview(Integer adminId, Integer reviewId) {
        // Validate admin exists and has admin role
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy admin với ID: " + adminId));

        if (admin.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Chỉ admin mới có thể xóa đánh giá");
        }

        // Verify review exists
        if (!reviewRepository.existsById(reviewId)) {
            throw new EntityNotFoundException("Không tìm thấy đánh giá với ID: " + reviewId);
        }

        reviewRepository.deleteById(reviewId);
    }
}
