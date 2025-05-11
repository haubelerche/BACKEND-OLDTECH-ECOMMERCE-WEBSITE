package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

   
    
    public void createSellerReview(Integer sellerId, Review review) {
        review.setSellerId(sellerId);
        review.setReviewTime(Timestamp.from(Instant.now()));
        reviewRepository.save(review);
    }

    public Review getReviewById(Integer reviewId) {
        return reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá với ID: " + reviewId));
    }

    
    
    public List<Review> getReviewsBySeller(Integer sellerId) {
        return reviewRepository.findBySellerId(sellerId);
    }
}