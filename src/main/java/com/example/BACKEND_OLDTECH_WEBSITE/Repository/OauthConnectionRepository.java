package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.OauthConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional; // Use Optional for potentially absent results

@Repository
public interface OauthConnectionRepository extends JpaRepository<OauthConnection, Integer> {


    Optional<OauthConnection> findByProviderAndProviderId(String provider, String providerId);
}