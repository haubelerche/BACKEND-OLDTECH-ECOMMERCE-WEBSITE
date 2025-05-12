package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.LoginRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Auth.RegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<?> testRegister(@RequestBody RegisterRequest registerRequest) {
        try {
            System.out.println("Test Register endpoint called with phone: " + registerRequest.getPhoneNumber());
            
            // Explicitly check for existing phone/email for debugging
            boolean emailExists = userService.existsByEmail(registerRequest.getEmail());
            boolean phoneExists = userService.existsByPhoneNumber(registerRequest.getPhoneNumber());
            
            System.out.println("Email exists: " + emailExists);
            System.out.println("Phone exists: " + phoneExists);
            
            // If phone exists, still continue for testing purposes
            if (phoneExists) {
                System.out.println("WARNING: Phone exists but proceeding anyway for testing");
            }
            
            if (emailExists) {
                return ResponseEntity.badRequest().body("Email đã được sử dụng!");
            }
            
            // Create user anyway, bypassing normal phone validation
            userService.createUser(registerRequest);
            return ResponseEntity.ok("Test registration successful!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Test registration failed: " + e.getMessage());
        }
    }
    
    @GetMapping("/check-phone/{phoneNumber}")
    public ResponseEntity<?> checkPhoneNumber(@PathVariable String phoneNumber) {
        try {
            System.out.println("Checking phone number: " + phoneNumber);
            
            // Use the repository method directly 
            boolean exists = userRepository.existsByPhoneNumber(phoneNumber);
            System.out.println("Repository says phone exists: " + exists);
            
            // List all users to check if there's a matching phone number
            List<User> allUsers = userRepository.findAll();
            System.out.println("Total users in DB: " + allUsers.size());
            
            boolean foundInList = false;
            for (User user : allUsers) {
                System.out.println("User ID: " + user.getUserId() + ", Phone: " + user.getPhoneNumber());
                if (phoneNumber.equals(user.getPhoneNumber())) {
                    foundInList = true;
                    System.out.println("Found matching phone in user ID: " + user.getUserId());
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("exists_by_repository", exists);
            response.put("found_in_list", foundInList);
            response.put("total_users", allUsers.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error checking phone: " + e.getMessage());
        }
    }
    
    @PostMapping("/check-login")
    public ResponseEntity<?> checkLoginCredentials(@RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Checking login credentials for email: " + loginRequest.getEmail());
            
            // Check if user exists
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            if (!userOpt.isPresent()) {
                return ResponseEntity.badRequest().body("User with email " + loginRequest.getEmail() + " does not exist");
            }
            
            User user = userOpt.get();
            
            // Check if password matches
            boolean passwordMatches = passwordEncoder.matches(loginRequest.getPassword(), user.getPassword());
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("email_exists", true);
            response.put("password_matches", passwordMatches);
            response.put("user_id", user.getUserId());
            response.put("user_role", user.getRole());
            response.put("user_email", user.getEmail());
            response.put("user_status", user.getAccountStatus());
            response.put("is_verified", user.getIsVerified());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error checking login: " + e.getMessage());
        }
    }
    
    @GetMapping("/list-users")
    public ResponseEntity<?> listAllUsers() {
        try {
            List<User> allUsers = userRepository.findAll();
            System.out.println("Total users in DB: " + allUsers.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("total_users", allUsers.size());
            
            // List all users with minimal info (to avoid sensitive data)
            Map<Integer, Object> userInfo = new HashMap<>();
            for (User user : allUsers) {
                Map<String, Object> info = new HashMap<>();
                info.put("email", user.getEmail());
                info.put("role", user.getRole());
                info.put("status", user.getAccountStatus());
                info.put("verified", user.getIsVerified());
                
                userInfo.put(user.getUserId(), info);
            }
            response.put("users", userInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error listing users: " + e.getMessage());
        }
    }

    @GetMapping("/list-users-detailed")
    public ResponseEntity<?> listAllUsersDetailed() {
        try {
            List<User> allUsers = userRepository.findAll();
            System.out.println("Total users in DB: " + allUsers.size());
            
            Map<String, Object> response = new HashMap<>();
            response.put("total_users", allUsers.size());
            
            // Create a formatted list of users
            StringBuilder usersList = new StringBuilder();
            usersList.append("USER LIST:\n");
            usersList.append("====================\n");
            
            for (User user : allUsers) {
                usersList.append("ID: ").append(user.getUserId()).append("\n");
                usersList.append("Email: ").append(user.getEmail()).append("\n");
                usersList.append("Name: ").append(user.getFirstName()).append(" ").append(user.getLastName()).append("\n");
                usersList.append("Role: ").append(user.getRole()).append("\n");
                usersList.append("Status: ").append(user.getAccountStatus()).append("\n");
                usersList.append("Verified: ").append(user.getIsVerified()).append("\n");
                usersList.append("Phone: ").append(user.getPhoneNumber()).append("\n");
                usersList.append("====================\n");
            }
            
            response.put("users_formatted", usersList.toString());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error listing users: " + e.getMessage());
        }
    }

    @GetMapping("/list-admins")
    public ResponseEntity<?> listAdmins() {
        try {
            List<User> allUsers = userRepository.findAll();
            List<User> adminUsers = allUsers.stream()
                .filter(user -> user.getRole() == RoleEnum.Admin)
                .collect(Collectors.toList());
            
            System.out.println("Total admin users: " + adminUsers.size());
            
            List<Map<String, Object>> adminList = new ArrayList<>();
            for (User admin : adminUsers) {
                Map<String, Object> adminInfo = new HashMap<>();
                adminInfo.put("id", admin.getUserId());
                adminInfo.put("email", admin.getEmail());
                adminInfo.put("name", admin.getFirstName() + " " + admin.getLastName());
                adminList.add(adminInfo);
            }
            
            return ResponseEntity.ok(adminList);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error listing admins: " + e.getMessage());
        }
    }
} 