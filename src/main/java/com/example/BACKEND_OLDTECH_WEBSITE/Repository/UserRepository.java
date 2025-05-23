package com.example.BACKEND_OLDTECH_WEBSITE.Repository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    User findByPhoneNumber(String phoneNumber);
    List<User> findByEmailContainingIgnoreCase(String email);
    List<User> findByPhoneNumberContaining(String phoneNumber);
    List<User> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(String firstName, String lastName);
    List<User> findByAccountStatus(AccountStatusEnum accountStatus);
    List<User> findByAccountStatusAndSuspensionEndTimeBefore(AccountStatusEnum status, LocalDateTime dateTime);
    List<User> findByAccountStatusAndSuspensionEndTimeIsNotNull(AccountStatusEnum status);
}
