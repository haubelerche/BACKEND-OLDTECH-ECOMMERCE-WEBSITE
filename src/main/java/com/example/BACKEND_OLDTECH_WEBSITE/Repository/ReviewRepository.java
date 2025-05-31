// === com/example/BACKEND_OLDTECH_WEBSITE/Repository/ReviewRepository.java ===
package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findBySellerId(Integer sellerId);
    List<Review> findByProductId(Integer productId);
}