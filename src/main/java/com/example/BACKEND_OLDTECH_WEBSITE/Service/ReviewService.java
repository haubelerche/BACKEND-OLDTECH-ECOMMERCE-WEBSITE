package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review.ReviewRequest;
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
     * Creates a review for a seller based on an order with a specific product
     */
    @Transactional
    public Review createReview(ReviewRequest reviewRequest, Integer reviewerId) {
        // Verify the reviewer exists
        User reviewer = userRepository.findById(reviewerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + reviewerId));

        // Verify the reviewer has Customer role
        if (reviewer.getRole() != RoleEnum.Customer) {
            throw new IllegalArgumentException("Chỉ khách hàng mới có thể đánh giá người bán");
        }

        // Verify the product exists
        Product product = productRepository.findById(reviewRequest.getProductId())
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + reviewRequest.getProductId()));

        // Get seller from product
        Integer sellerId = product.getSellerId();
        if (sellerId == null) {
            throw new IllegalArgumentException("Sản phẩm không có người bán");
        }

        // Check if this reviewer has already reviewed this order/product combination
        List<Review> existingReviews = reviewRepository.findByOrderId(reviewRequest.getOrderId());
        for (Review existingReview : existingReviews) {
            if (existingReview.getReviewerId().equals(reviewerId) &&
                existingReview.getProductId().equals(reviewRequest.getProductId())) {
                throw new IllegalArgumentException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi");
            }
        }

        // Create and save the review
        Review review = new Review();
        review.setOrderId(reviewRequest.getOrderId());
        review.setProductId(reviewRequest.getProductId());
        review.setReviewerId(reviewerId);
        review.setSellerId(sellerId);
        review.setRating(reviewRequest.getRating());
        review.setReview(reviewRequest.getReview());
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
     * Get all reviews for an order
     */
    public List<Review> getReviewsByOrder(Integer orderId) {
        return reviewRepository.findByOrderId(orderId);
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
