package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.ChangePasswordRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    
    
    
//LAY TOAN BO DANH SACH USER
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



    
//TIM THEO TEN
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


    

//TIM THEO EMAIL
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


//TIM THEO SDT
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


// LAY HO SO CUA MOT NGUOI ((SUPER)ADMIN)
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
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
    


//DOI MAT KHAU TRONG HO SO
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



//VO HIEU HOA TAI KHOAN (CUSTOMER AND (SUPER)ADMIN)
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



//YEU CAU XAC THUC HO SO, GUI TOI ADMIN SAU KHI HOAN TAT THONG TIN CA NHAN
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




//GUI DON KHIEN NAI TOI ADMIN
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



// LAY CAC LOAI THONG BAO
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



// MARK THONG BAO DA DOC
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



    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserAccount(@PathVariable Integer userId) {
        try {
            userService.deleteAccount(userId);
            return ResponseEntity.ok("Tài khoản người dùng " + userId + " đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa tài khoản người dùng: " + e.getMessage());
        }
    }









}

