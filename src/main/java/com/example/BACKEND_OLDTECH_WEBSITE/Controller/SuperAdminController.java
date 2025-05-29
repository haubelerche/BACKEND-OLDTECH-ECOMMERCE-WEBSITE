package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//90%
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.CreateAdminRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SuperAdminService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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

@RestController
@RequestMapping("/manager")  // Changed to remove trailing slash
@CrossOrigin(origins = "*")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    @Autowired
    public SuperAdminController(SuperAdminService superAdminService) {
        this.superAdminService = superAdminService;
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

    // System Settings Management
    /* 
    @PostMapping("/system/settings")
    public ResponseEntity<?> manageSystemSettings(@RequestParam String key, @RequestParam String value) {
        try {
            superAdminService.manageSystemSettings(key, value);
            return ResponseEntity.ok("Cài đặt hệ thống đã được cập nhật thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật cài đặt hệ thống: " + e.getMessage());
        }
    }

    // System Logs Management
    @GetMapping("/system/logs")
    public ResponseEntity<?> viewSystemLogs(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp endDate) {
        try {
            List<String> logs = superAdminService.viewSystemLogs(startDate, endDate);
            return ResponseEntity.ok(logs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xem nhật ký hệ thống: " + e.getMessage());
        }
    }

    // System Notification Management
    @PostMapping("/system/notifications")
    public ResponseEntity<?> sendSystemNotification(@RequestParam String message, @RequestParam RoleEnum targetAudience) {
        try {
            superAdminService.sendSystemNotification(message, targetAudience);
            return ResponseEntity.ok("Thông báo hệ thống đã được gửi thành công.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gửi thông báo hệ thống: " + e.getMessage());
        }
    }

    // System Report Management
    @GetMapping("/system/reports")
    public ResponseEntity<?> generateSystemReport(
            @RequestParam String reportType,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Timestamp endDate) {
        try {
            Object report = superAdminService.generateSystemReport(reportType, startDate, endDate);
            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi tạo báo cáo hệ thống: " + e.getMessage());
        }
    }

    // System Backup Management
    @PostMapping("/system/backup")
    public ResponseEntity<?> triggerSystemBackup() {
        try {
            String backupStatus = superAdminService.triggerSystemBackup();
            return ResponseEntity.ok(backupStatus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi sao lưu hệ thống: " + e.getMessage());
        }
    }

    // System Restore Management
    @PostMapping("/system/restore")
    public ResponseEntity<?> triggerSystemRestore(@RequestParam String backupId) {
        try {
            String restoreStatus = superAdminService.triggerSystemRestore(backupId);
            return ResponseEntity.ok(restoreStatus);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi khôi phục hệ thống: " + e.getMessage());
        }
    }*/
} 


//temporary will update later

