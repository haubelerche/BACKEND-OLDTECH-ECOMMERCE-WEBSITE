package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.VerificationDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.VerificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/verification")
public class VerificationViewController {

    private final UserService userService;
    private final VerificationService verificationService;

    @Autowired
    public VerificationViewController(UserService userService, VerificationService verificationService) {
        this.userService = userService;
        this.verificationService = verificationService;
    }

    @GetMapping("/form/{userId}")
    public String showVerificationForm(@PathVariable Integer userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
            
            // Check if user has any verification details
            Optional<VerificationDetail> verificationDetail = verificationService.getVerificationDetailsByUserId(userId);
            verificationDetail.ifPresent(detail -> model.addAttribute("verificationDetail", detail));
            
            return "verification-form";
        } catch (UsernameNotFoundException e) {
            // Handle user not found error
            return "error/404";
        } catch (Exception e) {
            // Handle general errors
            return "error/500";
        }
    }
    
    @GetMapping("/status/{userId}")
    public String showVerificationStatus(@PathVariable Integer userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);
            
            // Get verification status
            model.addAttribute("verificationStatus", userService.getVerificationStatus(userId));
            
            // Get verification details if available
            Optional<VerificationDetail> verificationDetail = verificationService.getVerificationDetailsByUserId(userId);
            if (verificationDetail.isPresent()) {
                model.addAttribute("verificationDetail", verificationDetail.get());
            }
            
            return "verification-status";
        } catch (UsernameNotFoundException e) {
            // Handle user not found error
            return "error/404";
        } catch (Exception e) {
            // Handle general errors
            return "error/500";
        }
    }
} 