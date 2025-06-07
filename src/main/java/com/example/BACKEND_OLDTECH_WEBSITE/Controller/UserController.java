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
import org.springframework.http.converter.HttpMessageNotReadableException;

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
    @GetMapping("/single/{userId}")
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
    @PostMapping("/profile/password/{userId}")
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



//VO HIEU HOA TAI KHOAN (USED BY CUSTOMER AND (SUPER)ADMIN)
    @PostMapping("/profile/deactivate/{userId}") //forever
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



//CẬP NHẬT HỒ SƠ NGƯỜI DÙNG
    @RequestMapping(value = "/profile/update/{userId}", method = {RequestMethod.PUT, RequestMethod.POST})
    public ResponseEntity<?> updateUserProfile(@PathVariable Integer userId, @RequestBody(required = false) Map<String, Object> profileData) {
        try {
            // Check if the request body is empty
            if (profileData == null || profileData.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("status", "error");
                errorResponse.put("message", "Yêu cầu cập nhật thông tin không thể trống. Vui lòng cung cấp dữ liệu JSON để cập nhật.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
            }

            // Log received data for debugging
            System.out.println("Received profile update request for user ID: " + userId);
            System.out.println("Profile data: " + profileData);

            User user = userService.getUserById(userId);
            boolean wasIncomplete = userService.isProfileIncomplete(user);

            // Update profile with provided data
            User updatedUser = userService.updateUserProfile(userId, profileData);
            boolean isNowComplete = !userService.isProfileIncomplete(updatedUser);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("isProfileComplete", isNowComplete);
            response.put("isVerified", updatedUser.getIsVerified());

            // If the profile was incomplete before but is now complete, automatically submit for verification
            if (wasIncomplete && isNowComplete) {
                response.put("verificationStatus", "submitted");
                response.put("message", "Hồ sơ của bạn đã được cập nhật thành công và đã được gửi tới admin để xác thực.");

                // Notify user that their profile has been submitted for verification
                userService.sendProfileSubmittedForVerificationNotification(updatedUser);
            } else if (isNowComplete) {
                response.put("message", "Hồ sơ của bạn đã được cập nhật thành công.");
            } else {
                // Identify what's still missing
                Map<String, Boolean> missingFields = new HashMap<>();
                if (updatedUser.getDob() == null) {
                    missingFields.put("dateOfBirth", true);
                }

                if (updatedUser.getPhoneNumber() == null ||
                        updatedUser.getPhoneNumber().startsWith("placeholder_") ||
                        updatedUser.getPhoneNumber().startsWith("+")) {
                    missingFields.put("phoneNumber", true);
                }

                response.put("missingFields", missingFields);
                response.put("message", "Hồ sơ đã được cập nhật nhưng vẫn chưa hoàn tất. Vui lòng bổ sung thông tin còn thiếu.");
            }

            return ResponseEntity.ok(response);
        } catch (HttpMessageNotReadableException e) {
            // Handle empty or malformed JSON specifically
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Dữ liệu JSON không hợp lệ hoặc bị thiếu. Vui lòng kiểm tra và gửi lại yêu cầu với dữ liệu JSON hợp lệ.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (UsernameNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (IllegalStateException e) {
            // Handle cases where changing restricted fields is attempted
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (IllegalArgumentException e) {
            // Handle validation errors
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // Detailed logging of unexpected errors
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi cập nhật hồ sơ: " + e.getMessage());
            errorResponse.put("exceptionType", e.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

//GUI DON KHIEN NAI TOI ADMIN
    @PostMapping("/profile/complaints/{userId}")
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
    @GetMapping("/profile/notifications/{userId}")
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
    @PostMapping("/profile/notifications/{notificationId}/read/{userId}")
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

