package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//90%
// TODO: only superadmin can CRUD admin accounts, the ordinary admin can only update their own account


import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.CreateAdminRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.SuspendUserRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SuperAdminService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.CreateSuperAdminRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Map;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RestController
@RequestMapping("/manager")  // Changed to remove trailing slash
@CrossOrigin(origins = "*")
public class SuperAdminController {

    private final SuperAdminService superAdminService;
    private final UserService userService;

    @Autowired
    public SuperAdminController(SuperAdminService superAdminService, UserService userService) {
        this.superAdminService = superAdminService;
        this.userService = userService;
    }


//TẠO TÀI KHOẢN SUPERADMIN
    @PostMapping("/superadmins")
    public ResponseEntity<?> createSuperAdminAccount(@Valid @RequestBody CreateSuperAdminRequest request) {
        try {
            User newSuperAdmin = superAdminService.createSuperAdminAccount(request);
            return new ResponseEntity<>(newSuperAdmin, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tạo tài khoản SuperAdmin: " + e.getMessage());
        }
    }



//TẠO TÀI KHOẢN ADMIN
    @PostMapping("/admins")
    @PreAuthorize("hasAuthority('SuperAdmin')")
    public ResponseEntity<?> createAdminAccount(@Valid @RequestBody CreateAdminRequest request) {
        try {
            // Add debug logging
            System.out.println("Creating admin account with email: " + request.getEmail());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current user: " + auth.getName());
            System.out.println("User authorities: " + auth.getAuthorities());

            User newAdmin = superAdminService.createAdminAccount(request);
            return new ResponseEntity<>(newAdmin, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Print the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tạo tài khoản Admin: " + e.getMessage());
        }
    }



//XÓA TÀI KHOẢN ADMIN
    @DeleteMapping("/admins/{adminUserId}")
    @PreAuthorize("hasAuthority('SuperAdmin')")
    public ResponseEntity<?> deleteAdminAccount(@PathVariable Integer adminUserId) {
        try {
            // Add debug logging
            System.out.println("Deleting admin account with ID: " + adminUserId);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Current user: " + auth.getName());
            System.out.println("User authorities: " + auth.getAuthorities());

            superAdminService.deleteAdminAccount(adminUserId);
            return ResponseEntity.ok("Tài khoản Admin đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Print the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa tài khoản Admin: " + e.getMessage());
        }
    }



//CẬP NHẬT TÀI KHOẢN ADMIN
    @PutMapping("/admins/{adminUserId}")
    @PreAuthorize("hasAuthority('SuperAdmin')")
    public ResponseEntity<?> updateAdminAccount(@PathVariable Integer adminUserId, @Valid @RequestBody CreateAdminRequest request) {
        try {
            User updatedAdmin = superAdminService.updateAdminAccount(adminUserId, request);
            return ResponseEntity.ok(updatedAdmin);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật tài khoản Admin: " + e.getMessage());
        }
    }



//LẤY DANH SÁCH TOÀN BỘ ADMIN
    @GetMapping("/admins")
    @PreAuthorize("hasAuthority('SuperAdmin')")
    public ResponseEntity<?> getAllAdminAccounts() {
        try {
            List<User> admins = superAdminService.getAllAdminAccounts();
            return ResponseEntity.ok(admins);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách tài khoản Admin: " + e.getMessage());
        }
    }




//ĐÌNH CHỈ TÀI KHOẢN NGƯỜI DÙNG
    @PostMapping("/users/{userId}/suspend")
    @PreAuthorize("hasAnyAuthority('SuperAdmin', 'Admin')")
    public ResponseEntity<?> suspendUserAccount(@PathVariable Integer userId, @Valid @RequestBody SuspendUserRequest request) {
        try {
            // Log the action
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Admin " + auth.getName() + " suspending user with ID: " + userId);
            System.out.println("Suspension duration: " + request.getDurationInHours() + " hours, reason: " + request.getReason());

            // No need to modify the request - let the service handle null/negative values correctly
            // The service will set null for permanent suspensions

            userService.suspendAccount(userId, request);

            String successMessage;
            if (request.getDurationInHours() == null || request.getDurationInHours() <= 0) {
                successMessage = "Tài khoản người dùng đã bị đình chỉ vĩnh viễn.";
            } else {
                successMessage = "Tài khoản người dùng đã bị đình chỉ tạm thời trong vòng " + request.getDurationInHours() + " giờ với lý do: " + request.getReason();
            }

            return ResponseEntity.ok(successMessage);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đình chỉ tài khoản người dùng: " + e.getMessage());
        }
    }



//KÍCH HOẠT LẠI TÀI KHOẢN NGƯỜI DÙNG
    @PostMapping("/users/{userId}/reactivate")
    @PreAuthorize("hasAnyAuthority('SuperAdmin', 'Admin')")
    public ResponseEntity<?> reactivateUserAccount(@PathVariable Integer userId) {
        try {
            // Log the action
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Admin " + auth.getName() + " reactivating user with ID: " + userId);

            userService.reactivateAccount(userId);
            return ResponseEntity.ok("Tài khoản người dùng đã được kích hoạt lại thành công.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi kích hoạt lại tài khoản người dùng: " + e.getMessage());
        }
    }


//LẤY DANH SÁCH NGƯỜI DÙNG BỊ ĐÌNH CHỈ
    @GetMapping("/users/suspended")
    @PreAuthorize("hasAnyAuthority('SuperAdmin', 'Admin')")
    public ResponseEntity<?> getSuspendedUsers() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("Admin " + auth.getName() + " viewing all suspended users");

            List<Map<String, Object>> suspendedUsersWithDetails = userService.getAllSuspendedUsersWithDetails();
            return ResponseEntity.ok(suspendedUsersWithDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách người dùng bị đình chỉ: " + e.getMessage());
        }
    }
}

