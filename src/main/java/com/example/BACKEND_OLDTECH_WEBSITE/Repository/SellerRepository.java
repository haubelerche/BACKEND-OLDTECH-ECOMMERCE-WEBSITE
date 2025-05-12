package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;

import java.util.List;

/**
 * Interface for direct entity manager access
 */
interface EntityManagerProvider {
    EntityManager getEntityManager();
}

@Repository
public interface SellerRepository extends JpaRepository<Seller, Integer>, EntityManagerProvider {
    List<Seller> findByIsApproved(Boolean isApproved);
    List<Seller> findByBusinessStatus(Byte businessStatus);
}