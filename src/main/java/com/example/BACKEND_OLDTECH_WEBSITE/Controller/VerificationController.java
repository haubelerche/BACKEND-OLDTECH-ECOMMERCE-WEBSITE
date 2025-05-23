package com.example.BACKEND_OLDTECH_WEBSITE.Controller;
//100% ready
//FOR ADMIN GANG ONLY
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationToggleRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.VerificationDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.VerificationService;

@RestController
@RequestMapping("/verification")
public class VerificationController {

    private final UserService userService;
    private final VerificationService verificationService;

    @Autowired
    public VerificationController(UserService userService, VerificationService verificationService) {
        this.userService = userService;
        this.verificationService = verificationService;
    }



//LẤY DANH SÁCH TOÀN BỘ NGƯỜI CHỜ XÁC THỰC
    @GetMapping("/admin/pending")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public String showPendingVerifications(Model model) {
        List<VerificationDetail> pendingList = verificationService.getPendingVerifications();
        model.addAttribute("pendingVerifications", pendingList);
        return "admin/pending";
    }




//PHÊ DUYỆT HOẶC TỪ CHỐI YÊU CẦU XÁC THỰC
    @PutMapping("/admin/toggle/{verificationDetailId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> toggleVerificationStatus(
            @PathVariable Integer verificationDetailId,
            @RequestBody VerificationToggleRequest request) {
        try {

            Integer adminUserId = 1;
            VerificationDetail updatedDetail = verificationService.reviewVerification(
                    verificationDetailId, request.getIsApproved(), request.getAdminResponse(), adminUserId);
            return ResponseEntity.ok().body(updatedDetail);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }}




//LẤY THÔNG TIN CỤ THỂ MỘT NGƯỜI CHỜ XÁC THỰC
    @GetMapping("/form/{userId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public String showVerificationForm(@PathVariable Integer userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);


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



//LẤY TRẠNG THÁI XÁC THỰC CỦA MỘT NGƯỜI
    @GetMapping("/status/{userId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public String showVerificationStatus(@PathVariable Integer userId, Model model) {
        try {
            User user = userService.getUserById(userId);
            model.addAttribute("user", user);

            // Get verification status
            model.addAttribute("verificationStatus", userService.getVerificationStatus(userId));

            // Get verification details if available
            Optional<VerificationDetail> verificationDetail = verificationService.getVerificationDetailsByUserId(userId);
            verificationDetail.ifPresent(detail -> model.addAttribute("verificationDetail", detail));

            return "verification-status";
        } catch (UsernameNotFoundException e) {

            return "error/404";
        } catch (Exception e) {
            // Handle general errors
            return "error/500";
        }
    }}




