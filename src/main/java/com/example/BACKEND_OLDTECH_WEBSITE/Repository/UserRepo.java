package com.example.BACKEND_OLDTECH_WEBSITE.Repository;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    User findByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    User findByPhoneNumber(String phoneNumber);
    boolean existsByUsername(String username);
    User findByUsername(String username);

}
