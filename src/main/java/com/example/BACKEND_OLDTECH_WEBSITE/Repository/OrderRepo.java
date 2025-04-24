// === com/example/BACKEND_OLDTECH_WEBSITE/Repository/OrderRepo.java ===
package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepo extends JpaRepository<Orders, Integer> {
}