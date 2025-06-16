package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review.ReviewRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Review.ReviewResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Create a review for a seller based on an order and product
     * This endpoint lets the customer submit a review without having to specify productId and orderId
     * These parameters come from the query parameters instead
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Customer')")
    public ResponseEntity<?> createReview(
            @RequestParam Integer reviewerId,
            @RequestParam Integer productId,
            @RequestParam Integer orderId,
            @RequestBody Map<String, Object> reviewData) {

        try {
            // Create ReviewRequest with auto-filled productId and orderId
            ReviewRequest reviewRequest = new ReviewRequest();
            reviewRequest.setProductId(productId);
            reviewRequest.setOrderId(orderId);

            // Extract review text and rating from the request body
            reviewRequest.setReview((String) reviewData.get("review"));
            reviewRequest.setRating(Integer.parseInt(reviewData.get("rating").toString()));

            Review savedReview = reviewService.createReview(reviewRequest, reviewerId);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToReviewResponse(savedReview));
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
    @GetMapping("/get/{reviewId}")
    public ResponseEntity<?> getReviewById(@PathVariable Integer reviewId) {
        try {
            Review review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(convertToReviewResponse(review));
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
    @GetMapping("/list/sellers/{sellerId}")
    public ResponseEntity<?> getReviewsBySeller(@PathVariable Integer sellerId) {
        try {
            List<Review> reviews = reviewService.getReviewsBySeller(sellerId);
            List<ReviewResponse> reviewResponses = reviews.stream()
                .map(this::convertToReviewResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(reviewResponses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách đánh giá về người bán: " + e.getMessage());
        }
    }

    /**
     * Seller response to a review
     */
    @PostMapping("/respond/{reviewId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> respondToReview(
            @PathVariable Integer reviewId,
            @RequestBody String response,
            @RequestParam Integer sellerId) {
        try {
            Review updatedReview = reviewService.respondToReview(sellerId, reviewId, response);
            return ResponseEntity.ok(convertToReviewResponse(updatedReview));
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
    @DeleteMapping("/delete/{reviewId}")
    @PreAuthorize("hasAuthority('Admin')")
    public ResponseEntity<?> deleteReview(@PathVariable Integer reviewId, @RequestParam Integer adminId) {
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

    // Helper method to convert Review entity to ReviewResponse
    private ReviewResponse convertToReviewResponse(Review review) {
        ReviewResponse response = new ReviewResponse();
        response.setReviewId(review.getReviewId());
        response.setOrderId(review.getOrderId());
        response.setReviewerId(review.getReviewerId());
        response.setSellerId(review.getSellerId());
        response.setProductId(review.getProductId());
        response.setRating(review.getRating());
        response.setReview(review.getReview());
        response.setReviewTime(review.getReviewTime());
        response.setSellerResponse(review.getSellerResponse());
        response.setResponseTime(review.getResponseTime());
        return response;
    }
}
