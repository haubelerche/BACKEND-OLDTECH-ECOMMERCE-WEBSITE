package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ReviewService;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a review for a seller with optional product tagging
     * The product tag helps identify which product the customer purchased from this seller
     */
    @PostMapping("/sellers/{sellerId}")
    public ResponseEntity<?> createSellerReview(@PathVariable Integer sellerId, @RequestBody Review review) {
        try {
            Review savedReview = reviewService.createSellerReview(sellerId, review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tạo đánh giá người bán: " + e.getMessage());
        }
    }

    /**
     * Get a specific review by ID
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Integer reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy thông tin đánh giá: " + e.getMessage());
        }
    }

    /**
     * Get all reviews for a seller
     */
    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<?> getReviewsBySeller(@PathVariable Integer sellerId) {
        try {
            List<Review> reviews = reviewService.getReviewsBySeller(sellerId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách đánh giá về người bán: " + e.getMessage());
        }
    }

    /**
     * Get all reviews that tagged a specific product
     * Note: These are still seller reviews, just filtered by the tagged product
     */
    @GetMapping("/tagged-products/{productId}")
    public ResponseEntity<?> getReviewsByTaggedProduct(@PathVariable Integer productId) {
        try {
            List<Review> reviews = reviewService.getReviewsByTaggedProduct(productId);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách đánh giá có gắn thẻ sản phẩm: " + e.getMessage());
        }
    }

    /**
     * Seller response to a review
     */
    @PostMapping("/sellers/{sellerId}/respond/{reviewId}")
    public ResponseEntity<?> respondToReview(
            @PathVariable Integer sellerId,
            @PathVariable Integer reviewId,
            @RequestBody String response) {
        try {
            Review updatedReview = reviewService.respondToReview(sellerId, reviewId, response);
            return ResponseEntity.ok(updatedReview);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi phản hồi đánh giá: " + e.getMessage());
        }
    }

    /**
     * Admin delete a review
     */
    @DeleteMapping("/admin/{adminId}/delete/{reviewId}")
    public ResponseEntity<?> deleteReview(@PathVariable Integer adminId, @PathVariable Integer reviewId) {
        try {
            reviewService.deleteReview(adminId, reviewId);
            return ResponseEntity.ok("Đánh giá đã được xóa thành công");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi xóa đánh giá: " + e.getMessage());
        }
    }
}

