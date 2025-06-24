package com.example.BACKEND_OLDTECH_WEBSITE.Repository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    List<User> findByRole(RoleEnum role);
    List<User> findByRoleIn(List<RoleEnum> roles);

    // Admin seller filtering methods
    List<User> findByRoleAndIsVerified(RoleEnum role, Boolean isVerified);
    List<User> findByRoleAndAccountStatus(RoleEnum role, AccountStatusEnum accountStatus);
    
    @Query("SELECT u FROM User u WHERE u.role = 'Seller' AND " +
            "(LOWER(u.firstName) LIKE :searchTerm OR LOWER(u.lastName) LIKE :searchTerm OR " +
            "LOWER(u.email) LIKE :searchTerm OR u.phoneNumber LIKE :searchTerm)")
    List<User> findSellersContaining(@Param("searchTerm") String searchTerm);

    // Advanced seller filtering queries
    @Query("SELECT u FROM User u WHERE u.role = :role " +
           "AND (:accountStatus IS NULL OR u.accountStatus = :accountStatus) " +
           "AND (:isVerified IS NULL OR u.isVerified = :isVerified) " +
           "AND (:startDate IS NULL OR u.createdAt >= :startDate) " +
           "AND (:endDate IS NULL OR u.createdAt <= :endDate) " +
           "AND (:searchKeyword IS NULL OR " +
           "     LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
           "     LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
           "     LOWER(u.email) LIKE LOWER(CONCAT('%', :searchKeyword, '%')) OR " +
           "     u.phoneNumber LIKE CONCAT('%', :searchKeyword, '%')) " +
           "AND (:momoAccount IS NULL OR u.refundMomoAccount LIKE CONCAT('%', :momoAccount, '%'))")
    List<User> findSellersWithFilters(
        @Param("role") RoleEnum role,
        @Param("accountStatus") AccountStatusEnum accountStatus,
        @Param("isVerified") Boolean isVerified,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("searchKeyword") String searchKeyword,
        @Param("momoAccount") String momoAccount
    );

    // Count queries for statistics
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") RoleEnum role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.accountStatus = :status")
    Long countByRoleAndAccountStatus(@Param("role") RoleEnum role, @Param("status") AccountStatusEnum status);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.isVerified = :isVerified")
    Long countByRoleAndIsVerified(@Param("role") RoleEnum role, @Param("isVerified") Boolean isVerified);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role AND u.createdAt >= :startDate AND u.createdAt <= :endDate")
    Long countByRoleAndCreatedAtBetween(@Param("role") RoleEnum role, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Specific seller queries for business operations
    @Query("SELECT u FROM User u WHERE u.role = 'Seller' AND u.isVerified = false AND u.accountStatus = 'Active'")
    List<User> findPendingVerificationSellers();

    @Query("SELECT u FROM User u WHERE u.role = 'Seller' AND u.refundMomoAccount = :momoAccount")
    List<User> findSellersByMomoAccount(@Param("momoAccount") String momoAccount);

    @Query("SELECT u FROM User u WHERE u.role = 'Seller' AND u.accountStatus = 'Suspended' AND u.suspensionEndTime IS NOT NULL")
    List<User> findSuspendedSellersWithEndTime();

    // Seller search with enhanced criteria
    @Query("SELECT u FROM User u WHERE u.role = 'Seller' AND " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.phoneNumber LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(u.livingLocation) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "u.refundMomoAccount LIKE CONCAT('%', :keyword, '%'))")
    List<User> findSellersWithEnhancedSearch(@Param("keyword") String keyword);
}