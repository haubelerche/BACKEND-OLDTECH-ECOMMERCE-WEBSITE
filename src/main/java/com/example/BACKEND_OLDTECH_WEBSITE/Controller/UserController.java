package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.ChangePasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }



//LẤY TẤT CẢ DANH SÁCH USER
    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }




//TÌM THEO TÊN
    @GetMapping("/search/name")
    public ResponseEntity<?> searchUsersByName(@RequestParam String name) {
        try {
            List<User> users = userService.searchUsersByName(name);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tìm kiếm người dùng theo tên: " + e.getMessage());
        }
    }




//TÌM THEO EMAIL
    @GetMapping("/search/email")
    public ResponseEntity<?> searchUsersByEmail(@RequestParam String email) {
        try {
            List<User> users = userService.searchUsersByEmail(email);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tìm kiếm người dùng theo email: " + e.getMessage());
        }
    }


//TÌM THEO SDT
    @GetMapping("/search/phone")
    public ResponseEntity<?> searchUsersByPhone(@RequestParam String phoneNumber) {
        try {
            List<User> users = userService.searchUsersByPhoneNumber(phoneNumber);
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tìm kiếm người dùng theo số điện thoại: " + e.getMessage());
        }
    }
    


// LẤY HỒ SƠ CỦA MỘT NGƯỜI (SUPER)ADMIN
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@RequestParam Integer userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy thông tin người dùng: " + e.getMessage());
        }
    }
    


//ĐỔI MẬT KHẨU TRONG HỒ SƠ
    @PostMapping("/profile/{userId}/password")
    public ResponseEntity<?> changePassword(@PathVariable Integer userId, @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userId, request);
            return ResponseEntity.ok("Mật khẩu đã được thay đổi thành công.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thay đổi mật khẩu: " + e.getMessage());
        }
    }



//VÔ HIỆU HÓA TÀI KHOẢN (for CUSTOMER)
    @PostMapping("/profile/{userId}/deactivate") //forever
    public ResponseEntity<?> deactivateAccount(@PathVariable Integer userId) {
        try {
            userService.deactivateAccount(userId);
            return ResponseEntity.ok("Tài khoản đã được vô hiệu hóa thành công.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi vô hiệu hóa tài khoản: " + e.getMessage());
        }
    }


 //ĐÌNH CHỈ TÀI KHOẢN NGƯỜI DÙNG (ĐÌNH CHỈ TỪ TÀI KHOẢN GỐC LÀ CUSTOMER THÌ CHỨC NĂNG SELLER CŨNG BỊ ĐÌNH CHỈ LUÔN)
    @PostMapping("/admin/users/{userId}/suspend")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> adminSuspendAccount(@PathVariable Integer userId) {
        try {
            userService.deactivateAccount(userId, true);
            return ResponseEntity.ok("Tài khoản người dùng đã bị đình chỉ bởi quản trị viên.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi đình chỉ tài khoản: " + e.getMessage());
        }
    }



// TỰ KÍCH HOẠT LẠI TÀI KHOẢN (FOR CUSTOMER NẾU HỌ TẠM THỜI VÔ HIỆU HÓA TÀI KHOẢN CỦA MÌNH)
    @PostMapping("/profile/{userId}/reactivate")
    public ResponseEntity<?> reactivateAccount(@PathVariable Integer userId) {
        try {
            boolean canReactivate = userService.canSelfReactivate(userId);
            if (!canReactivate) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Tài khoản của bạn đã bị đình chỉ bởi quản trị viên. Vui lòng liên hệ quản trị viên để kích hoạt lại.");
            }

            userService.reactivateAccount(userId);
            return ResponseEntity.ok("Tài khoản đã được kích hoạt lại thành công.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi kích hoạt lại tài khoản: " + e.getMessage());
        }
    }

// KÍCH HOẠT LẠI TÀI KHOẢN BỞI ADMIN SAU ĐÌNH CHỈ
    @PostMapping("/admin/users/{userId}/reactivate")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> adminReactivateAccount(@PathVariable Integer userId) {
        try {
            userService.reactivateAccount(userId);
            return ResponseEntity.ok("Tài khoản người dùng đã được kích hoạt lại bởi quản trị viên.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi kích hoạt lại tài khoản: " + e.getMessage());
        }
    }

//YÊU CẦU XÁC THỰC HỒ SƠ, GỬI TỚI ADMIN SAU KHI HOÀN TẤT THÔNG TIN CÁ NHÂN
    @PostMapping("/profile/{userId}/verification")
    public ResponseEntity<?> requestVerification(@PathVariable Integer userId, @RequestBody VerificationRequest verificationRequest) {
        try {
            userService.submitVerificationDocuments(userId, verificationRequest);
            return ResponseEntity.ok("Yêu cầu xác thực đã được gửi thành công. Vui lòng chờ quản trị viên xem xét.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gửi yêu cầu xác thực: " + e.getMessage());
        }
    }




//GỬI ĐƠN KHIẾU NẠI TỚI ADMIN
    @PostMapping("/profile/{userId}/complaints")
    public ResponseEntity<?> fileComplaint(@PathVariable Integer userId, @RequestBody String complaint) {
        try {
            userService.fileComplaint(userId, complaint);
            return ResponseEntity.ok("Đơn khiếu nại đã được gửi thành công.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gửi đơn khiếu nại: " + e.getMessage());
        }
    }



// LẤY CÁC LOẠI THÔNG BÁO
    @GetMapping("/profile/{userId}/notifications")
    public ResponseEntity<?> getNotifications(@PathVariable Integer userId) {
        try {
            List<Notification> notifications = userService.getNotifications(userId);
            return ResponseEntity.ok(notifications);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy thông báo: " + e.getMessage());
        }
    }



// ĐÁNH DẤU THÔNG BÁO ĐÃ ĐỌC
    @PostMapping("/profile/{userId}/notifications/{notificationId}/read")
    public ResponseEntity<?> markNotificationRead(@PathVariable Integer userId, @PathVariable Long notificationId) {
        try {
            userService.markNotificationRead(userId, notificationId);
            return ResponseEntity.ok("Thông báo đã được đánh dấu là đã đọc.");
        } catch (UsernameNotFoundException | EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi đánh dấu thông báo đã đọc: " + e.getMessage());
        }
    }

















    
    //==============================================
    // PUBLIC
    //==============================================

    // Check if email exists
    @GetMapping("/public/check-email/{email}")
    public ResponseEntity<?> checkEmailExists(@PathVariable String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi kiểm tra email: " + e.getMessage());
        }
    }


    @GetMapping("/public/check-phone/{phoneNumber}")
    public ResponseEntity<?> checkPhoneExists(@PathVariable String phoneNumber) {
        try {
            boolean exists = userService.existsByPhoneNumber(phoneNumber);
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", exists);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi kiểm tra số điện thoại: " + e.getMessage());
        }
    }
}