package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//100% ready
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.User.SuspendUserRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
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


@RestController
@RequestMapping("/customer")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


//LẤY TẤT CẢ DANH SÁCH CUSTOMER + SELLER (PUBLIC) THAT ARE ACTIVE
    @GetMapping("/all")
    public ResponseEntity<?> getAllCustomerAndSeller() {
        try {
            List<User> activeCustomersAndSellers = userService.getAllActiveCustomersAndSellers();
            return ResponseEntity.ok(activeCustomersAndSellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }


    // LẤY HỒ SƠ CỦA MỘT NGƯỜI || (SUPER)ADMIN DÙNG
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

    //TÌM THEO TÊN
    @GetMapping("/search/name/{name}")
    public ResponseEntity<?> searchUsersByName(@PathVariable String name) {
        try {
            List<User> users = userService.searchUsersByName(name)
                .stream()
                .filter(user -> user.getAccountStatus() == AccountStatusEnum.Active)
                .filter(user -> user.getRole() == RoleEnum.Customer || user.getRole() == RoleEnum.Seller)
                .toList();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm người dùng theo tên: " + e.getMessage());
        }
    }


    //TÌM THEO EMAIL
    @GetMapping("/search/email/{email}")
    public ResponseEntity<?> searchUsersByEmail(@PathVariable String email) {
        try {
            List<User> users = userService.searchUsersByEmail(email)
                .stream()
                .filter(user -> user.getAccountStatus() == AccountStatusEnum.Active)
                .filter(user -> user.getRole() == RoleEnum.Customer || user.getRole() == RoleEnum.Seller)
                .toList();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm người dùng theo email: " + e.getMessage());
        }
    }


    //TÌM THEO SDT
    @GetMapping("/search/phone/{phoneNumber}")
    public ResponseEntity<?> searchUsersByPhone(@PathVariable String phoneNumber) {
        try {
            List<User> users = userService.searchUsersByPhoneNumber(phoneNumber)
                .stream()
                .filter(user -> user.getAccountStatus() == AccountStatusEnum.Active)
                .filter(user -> user.getRole() == RoleEnum.Customer || user.getRole() == RoleEnum.Seller)
                .toList();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi tìm kiếm người dùng theo số điện thoại: " + e.getMessage());
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


    //VÔ HIỆU HÓA TÀI KHOẢN (for CUSTOMER and seller )
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

//KÍCH HOẠT LẠI TÀI KHOẢN (cho CUSTOMER và SELLER tự kích hoạt lại khi tự vô hiệu hóa)
    @PostMapping("/profile/reactivate/{userId}")
    public ResponseEntity<?> reactivateAccount(@PathVariable Integer userId) {
        try {
            // Check if the user can reactivate their account
            if (!userService.canSelfReactivate(userId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Tài khoản của bạn đã bị vô hiệu hóa bởi Admin. Vui lòng liên hệ quản trị viên để kích hoạt lại.");
            }

            userService.reactivateAccount(userId);
            return ResponseEntity.ok("Tài khoản đã được kích hoạt lại thành công.");
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi kích hoạt lại tài khoản: " + e.getMessage());
        }
    }

}
