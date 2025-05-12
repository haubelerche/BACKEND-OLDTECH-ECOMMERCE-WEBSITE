package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.CreateAdminRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AuthProvider;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SuperAdminService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // For hashing passwords

    @Autowired
    public SuperAdminService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public User createAdminAccount(CreateAdminRequest request) {
        if (!request.getEmail().startsWith("staffotech")) {
            throw new IllegalArgumentException("Admin email must start with 'staffotech'.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already exists: " + request.getPhoneNumber());
        }

        User admin = new User();
        admin.setEmail(request.getEmail());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setRole(RoleEnum.Admin);
        admin.setAccountStatus(AccountStatusEnum.Active);
        admin.setIsVerified(true);
        admin.setCreatedAt(Timestamp.from(Instant.now()));
        admin.setUpdatedAt(Timestamp.from(Instant.now()));
        
        // Set auth provider to local
        admin.setAuthProvider(AuthProvider.local);

        return userRepository.save(admin);
    }

    //delete admin account
    @Transactional
    public void deleteAdminAccount(Integer adminUserId) {
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found with ID: " + adminUserId));
        if (admin.getRole() != RoleEnum.Admin) {
            throw new IllegalArgumentException("User with ID: " + adminUserId + " is not an Admin.");
        }
        userRepository.deleteById(adminUserId);
    }

    //update admin account (basic info, not password or role here)
    @Transactional
    public User updateAdminAccount(Integer adminUserId, CreateAdminRequest request) { // Can create a specific UpdateAdminRequest DTO if fields differ significantly
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new EntityNotFoundException("Admin user not found with ID: " + adminUserId));

        if (admin.getRole() != RoleEnum.Admin) {
            throw new IllegalArgumentException("User with ID: " + adminUserId + " is not an Admin.");
        }

        // Check for email uniqueness if it's being changed
        if (!admin.getEmail().equals(request.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("New email already exists: " + request.getEmail());
            }
            admin.setEmail(request.getEmail());
        }
        
        // Check for phone number uniqueness if it's being changed
        if (!admin.getPhoneNumber().equals(request.getPhoneNumber()) && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
             throw new IllegalArgumentException("Số điện thoại đã tồn tại: " + request.getPhoneNumber());
        }

        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPhoneNumber(request.getPhoneNumber());
        admin.setUpdatedAt(Timestamp.from(Instant.now()));

        return userRepository.save(admin);
    }

    //get all admin accounts
    public List<User> getAllAdminAccounts() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == RoleEnum.Admin)
                .collect(Collectors.toList());
    }

    //set user role
    @Transactional
    public User setUserRole(Integer userId, RoleEnum newRole) {
        User user = userRepository.findById(userId)
                 .orElseThrow(() -> new EntityNotFoundException("Người dùng không tồn tại với ID: " + userId));
        

        // Specific check for SuperAdmin email
        if (newRole == RoleEnum.SuperAdmin) {
            if (user.getEmail() == null || !user.getEmail().startsWith("managerotech")) {
                 throw new IllegalArgumentException("User email ('" + user.getEmail() + "') must start with 'managerotech' to be assigned the SuperAdmin role.");
            }
        } 
        // Check for Admin email prefix when assigning Admin role
        else if (newRole == RoleEnum.Admin) {
            if (user.getEmail() == null || !user.getEmail().startsWith("staffotech")) {
                  throw new IllegalArgumentException("User email ('" + user.getEmail() + "') must start with 'staffotech' to be assigned the Admin role.");
            }
        }

        user.setRole(newRole);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        return userRepository.save(user);
    }

    //manage system settings
    public void manageSystemSettings(String settingKey, String settingValue) {
        System.out.println("SuperAdminService: Managing system setting - Key: " + settingKey + ", Value: " + settingValue);
        //SettingsRepository.save(new SystemSetting(settingKey, settingValue));
    }

    //manage system logs
    public List<String> viewSystemLogs(Timestamp startDate, Timestamp endDate) {

        System.out.println("SuperAdminService: Viewing system logs from " + startDate + " to " + endDate);
        //return LogService.getLogs(startDate, endDate);
        return List.of("Log entry 1 at ...", "Log entry 2 at ..."); // Dummy data
    }

    //manage system notifications
    public void sendSystemNotification(String message, RoleEnum targetAudience) {

        System.out.println("SuperAdminService: Sending system notification to " + targetAudience + ": " + message);
        //NotificationService.sendToRole(targetAudience, message);
    }

    //manage system reports
    public Object generateSystemReport(String reportType, Timestamp startDate, Timestamp endDate) {

        System.out.println("SuperAdminService: Generating system report type '" + reportType + "' from " + startDate + " to " + endDate);
        //return ReportGenerator.createReport(reportType, startDate, endDate);
        return "Report data for " + reportType; // Dummy data
    }

    //manage system backup
    @Transactional
    public String triggerSystemBackup() {
        System.out.println("SuperAdminService: Triggering system backup...");
       //BackupService.startFullBackup();
        return "System backup initiated successfully.";
    }

    //manage system restore
    @Transactional
    public String triggerSystemRestore(String backupId) {
        System.out.println("SuperAdminService: Triggering system restore from backup ID: " + backupId);
        //RestoreService.startRestore(backupId);
        return "System restore from backup " + backupId + " initiated.";
    }
}
