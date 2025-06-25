package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//100% ok
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Notification;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.ChangePasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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


// LAY HO SO CUA MOT NGUOI

    @GetMapping("/single/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId) {
        try {
            User user = userService.getUserById(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
            response.put("lastName", user.getLastName() != null ? user.getLastName() : "");
            response.put("dob", user.getDob() != null ? user.getDob().toString() : "");
            response.put("phoneNumber", user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
            response.put("avatarUrl", user.getAvatarUrl() != null ? user.getAvatarUrl() : "");
            response.put("email", user.getEmail() != null ? user.getEmail() : "");
            response.put("refund_momo_account", user.getRefundMomoAccount() != null ? user.getRefundMomoAccount() : "");
            return ResponseEntity.ok(response);
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
    @PutMapping("/profile/update/{userId}")
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

            // Check all required fields
            Map<String, Boolean> missingFields = new HashMap<>();

            // Check basic personal info
            if (updatedUser.getFirstName() == null || updatedUser.getFirstName().trim().isEmpty()) {
                missingFields.put("firstName", true);
            }
            if (updatedUser.getLastName() == null || updatedUser.getLastName().trim().isEmpty()) {
                missingFields.put("lastName", true);
            }
            if (updatedUser.getDob() == null) {
                missingFields.put("dateOfBirth", true);
            }

            // Check contact info
            if (updatedUser.getPhoneNumber() == null ||
                    updatedUser.getPhoneNumber().trim().isEmpty() ||
                    updatedUser.getPhoneNumber().startsWith("placeholder_")) {
                missingFields.put("phoneNumber", true);
            }

            // Check verification images
            if (updatedUser.getSelfiePicUrl() == null || updatedUser.getSelfiePicUrl().trim().isEmpty()) {
                missingFields.put("selfiePicUrl", true);
            }
            if (updatedUser.getFrontImageUrl() == null || updatedUser.getFrontImageUrl().trim().isEmpty()) {
                missingFields.put("frontImageUrl", true);
            }
            if (updatedUser.getBackImageUrl() == null || updatedUser.getBackImageUrl().trim().isEmpty()) {
                missingFields.put("backImageUrl", true);
            }            // Check additional required fields
            if (updatedUser.getRefundMomoAccount() == null || updatedUser.getRefundMomoAccount().trim().isEmpty()) {
                missingFields.put("refundMomoAccount", true);
            }
            if (updatedUser.getLivingLocation() == null || updatedUser.getLivingLocation().trim().isEmpty()) {
                missingFields.put("livingLocation", true);
            }

            boolean isNowComplete = missingFields.isEmpty();

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










    


    /*---ADMIN SELLER FILTERING OPERATIONS---*/

    /**
     * Lọc người bán với nhiều tiêu chí (Admin only)
     */
    @GetMapping("/sellers/filter")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> filterSellers(
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) String businessStatus,
            @RequestParam(required = false) Boolean isVerified,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String searchKeyword,
            @RequestParam(required = false) String momoAccount,
            @RequestParam(required = false, defaultValue = "false") Boolean strictMode) {
        try {
            Map<String, Object> response = userService.filterSellers(
                accountStatus, businessStatus, isVerified, startDate, endDate, 
                searchKeyword, momoAccount, strictMode
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi lọc người bán: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Lấy danh sách tất cả người bán (Admin only)
     */
    @GetMapping("/sellers/all")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> getAllSellers() {
        try {
            List<User> sellers = userService.getAllSellers();
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách người bán: " + e.getMessage());
        }
    }

    /**
     * Lấy danh sách người bán đang chờ xác thực (Admin only)
     */
    @GetMapping("/sellers/pending")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> getPendingVerificationSellers() {
        try {
            List<User> pendingSellers = userService.getPendingVerificationSellers();
            return ResponseEntity.ok(pendingSellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách người bán chờ xác thực: " + e.getMessage());
        }
    }

    /**
     * Lọc người bán theo trạng thái tài khoản (Admin only)
     */
    @GetMapping("/sellers/status/{status}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> getSellersByAccountStatus(@PathVariable String status) {
        try {
            List<User> sellers = userService.getSellersByAccountStatus(status);
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lọc người bán theo trạng thái: " + e.getMessage());
        }
    }

    /**
     * Tìm kiếm người bán theo từ khóa (Admin only)
     */
    @GetMapping("/sellers/search")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> searchSellers(@RequestParam String keyword) {
        try {
            List<User> sellers = userService.searchSellers(keyword);
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi tìm kiếm người bán: " + e.getMessage());
        }
    }

    /**
     * Lấy thống kê người bán (Admin only)
     */
    @GetMapping("/sellers/statistics")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> getSellerStatistics() {
        try {
            Map<String, Object> statistics = userService.getSellerStatistics();
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy thống kê người bán: " + e.getMessage());
        }
    }

    /**
     * Cập nhật trạng thái kinh doanh của người bán (Admin only)
     */
    @PutMapping("/sellers/{sellerId}/business-status")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> updateSellerBusinessStatus(
            @PathVariable Integer sellerId, 
            @RequestParam Boolean isActive) {
        try {
            User updatedSeller = userService.updateSellerBusinessStatus(sellerId, isActive);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Trạng thái kinh doanh đã được cập nhật");
            response.put("sellerId", sellerId);
            response.put("businessStatus", isActive ? "Hoạt động" : "Tạm ngưng");
            response.put("seller", updatedSeller);
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", "Lỗi khi cập nhật trạng thái: " + e.getMessage()));
        }
    }

}
