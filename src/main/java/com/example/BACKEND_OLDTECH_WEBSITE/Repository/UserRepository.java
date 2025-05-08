package com.example.BACKEND_OLDTECH_WEBSITE.Repository;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);
    User findByPhoneNumber(String phoneNumber);


}
