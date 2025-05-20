package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

//da statistic for later

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.AdminService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.VerificationService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SellerService;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.VerificationDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Verification.VerificationResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/*
verifyUser()
verifySeller()
verifyProduct()
reviewComplaint()
reviewReturnRequest(
sendNotification()
changeUserStatus() (blockUser/unblock)
 */

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private final AdminService adminService;
    private final VerificationService verificationService;
    private final SellerService sellerService;

    @Autowired
    public AdminController(AdminService adminService, VerificationService verificationService, SellerService sellerService) {
        this.adminService = adminService;
        this.verificationService = verificationService;
        this.sellerService = sellerService;
    }

    // User Management Endpoints
    @PostMapping("/users/{userId}/verify")
    public ResponseEntity<?> verifyUser(@PathVariable Integer userId) {
        try {
            adminService.verifyUser(userId);
            return ResponseEntity.ok("Người dùng " + userId + " đã được xác thực thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực người dùng: " + e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/suspend")
    public ResponseEntity<?> suspendUser(@PathVariable Integer userId) {
        try {
            adminService.suspendUser(userId);
            return ResponseEntity.ok("Người dùng " + userId + " đã bị chặn thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi chặn người dùng: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách người dùng: " + e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/role")
    public ResponseEntity<?> setUserRole(@PathVariable Integer userId, @RequestParam String role) {
        try {
            adminService.setUserRole(userId, role);
            return ResponseEntity.ok("Vai trò của người dùng " + userId + " đã được cập nhật thành " + role);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật vai trò người dùng: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUserAccount(@PathVariable Integer userId) {
        try {
            adminService.deleteUserAccount(userId);
            return ResponseEntity.ok("Tài khoản người dùng " + userId + " đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa tài khoản người dùng: " + e.getMessage());
        }
    }

    // Toggle user verification status (yes/no)
    @PostMapping("/users/{userId}/verify-status")
    public ResponseEntity<?> setUserVerifiedStatus(@PathVariable Integer userId, @RequestParam boolean isVerified) {
        try {
            adminService.setUserVerifiedStatus(userId, isVerified);
            return ResponseEntity.ok("Trạng thái xác thực của người dùng " + userId + " đã được cập nhật thành: " + (isVerified ? "Đã xác thực" : "Chưa xác thực"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái xác thực: " + e.getMessage());
        }
    }

    // Seller Management Endpoints
    @PostMapping("/sellers/{sellerId}/verify")
    public ResponseEntity<?> verifySeller(@PathVariable Integer sellerId) {
        try {
            adminService.verifySeller(sellerId);
            return ResponseEntity.ok("Người bán " + sellerId + " đã được xác thực thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực người bán: " + e.getMessage());
        }
    }

    @PostMapping("/sellers/{sellerId}/delete")
    public ResponseEntity<?> deleteSeller(@PathVariable Integer sellerId) {
        try {
            adminService.deleteSeller(sellerId);
            return ResponseEntity.ok("Người bán " + sellerId + " đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa người bán: " + e.getMessage());
        }
    }

    @PostMapping("/sellers/{sellerId}/suspend")
    public ResponseEntity<?> suspendSeller(@PathVariable Integer sellerId) {
        try {
            adminService.suspendSeller(sellerId);
            return ResponseEntity.ok("Người bán " + sellerId + " đã bị chặn thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi chặn người bán: " + e.getMessage());
        }
    }

    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers() {
        try {
            List<Seller> sellers = adminService.getAllSellers();
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách người bán: " + e.getMessage());
        }
    }

    // Product Management Endpoints
    @GetMapping("/products/pending")
    public ResponseEntity<?> getPendingProducts() {
        try {
            List<Product> products = adminService.getPendingProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách sản phẩm đang chờ duyệt: " + e.getMessage());
        }
    }

    @PostMapping("/products/{productId}/mark-pending")
    public ResponseEntity<?> markProductAsPending(@PathVariable Integer productId) {
        try {
            adminService.markProductAsPending(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã được đánh dấu là đang chờ duyệt.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi đánh dấu sản phẩm đang chờ duyệt: " + e.getMessage());
        }
    }

    @PostMapping("/products/{productId}/verify")
    public ResponseEntity<?> verifyProduct(@PathVariable Integer productId) {
        try {
            adminService.verifyProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã được xác thực thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác thực sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/products/{productId}/reject")
    public ResponseEntity<?> rejectProduct(@PathVariable Integer productId) {
        try {
            adminService.rejectProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã bị từ chối.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi từ chối sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/products/{productId}/hide")
    public ResponseEntity<?> hideProduct(@PathVariable Integer productId) {
        try {
            adminService.hideProduct(productId);
            return ResponseEntity.ok("Sản phẩm " + productId + " đã bị ẩn.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi ẩn sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts() {
        try {
            List<Product> products = adminService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }

    @PostMapping("/products/{productId}/category")
    public ResponseEntity<?> setProductCategory(@PathVariable Integer productId, @RequestParam Integer categoryId) {
        try {
            adminService.setProductCategory(productId, categoryId);
            return ResponseEntity.ok("Danh mục của sản phẩm " + productId + " đã được cập nhật thành " + categoryId);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật danh mục sản phẩm: " + e.getMessage());
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<?> getAllCategories() {
        try {
            List<Category> categories = adminService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách danh mục: " + e.getMessage());
        }
    }

    // Complaint Management Endpoints
    @PostMapping("/complaints/{complaintId}/review")
    public ResponseEntity<?> reviewComplaint(@PathVariable Long complaintId) {
        try {
            adminService.reviewComplaint(complaintId);
            return ResponseEntity.ok("Khiếu nại " + complaintId + " đã được xem xét thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xem xét khiếu nại: " + e.getMessage());
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getAllComplaints() {
        try {
            List<Complaint> complaints = adminService.getAllComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách khiếu nại: " + e.getMessage());
        }
    }

    @PostMapping("/complaints/{complaintId}/resolve")
    public ResponseEntity<?> resolveComplaint(@PathVariable Long complaintId, @RequestBody String resolutionNote) {
        try {
            adminService.resolveComplaint(complaintId, resolutionNote);
            return ResponseEntity.ok("Khiếu nại " + complaintId + " đã được giải quyết với ghi chú: " + resolutionNote);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi giải quyết khiếu nại: " + e.getMessage());
        }
    }

    // Notification and Statistics Endpoints
    @PostMapping("/notifications")
    public ResponseEntity<?> sendNotification(@RequestParam Integer userId, @RequestParam String message) {
        try {
            adminService.sendNotification(userId, message);
            return ResponseEntity.ok("Thông báo đã được gửi đến người dùng " + userId + ": " + message);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi gửi thông báo: " + e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public ResponseEntity<?> getWebsiteStatistics() {
        try {
            Map<String, Object> stats = adminService.getWebsiteStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy thống kê trang web: " + e.getMessage());
        }
    }

    // Order Management Endpoints
    @GetMapping("/orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Orders> orders = adminService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách đơn hàng: " + e.getMessage());
        }
    }

    // User Verification Management Endpoints
    @PostMapping("/verifications/{verificationDetailId}/status")
    public ResponseEntity<?> updateVerificationStatus(@PathVariable Integer verificationDetailId,
                                                    @RequestParam Integer adminUserId,
                                                    @RequestParam(value = "isApproved") String isApprovedStr,
                                                    @RequestBody(required = false) String reason) {
        try {
            // Log input values for debugging
            System.out.println("Updating verification status for ID: " + verificationDetailId);
            System.out.println("Admin user ID: " + adminUserId);
            System.out.println("isApproved value: " + isApprovedStr);
            System.out.println("Reason: " + reason);
            
            boolean isApproved = "1".equals(isApprovedStr) || "true".equalsIgnoreCase(isApprovedStr);
            VerificationDetail updatedDetail;
            if (isApproved) {
                updatedDetail = verificationService.approveVerification(verificationDetailId, adminUserId);
                return ResponseEntity.ok(Map.of(
                    "message", "Yêu cầu xác thực " + verificationDetailId + " đã được phê duyệt.",
                    "success", true,
                    "verificationId", verificationDetailId,
                    "isApproved", true
                ));
            } else {
                String rejectionReason = (reason != null && !reason.trim().isEmpty()) ? reason : "Không đáp ứng yêu cầu của hệ thống";
                updatedDetail = verificationService.rejectVerification(verificationDetailId, adminUserId, rejectionReason);
                return ResponseEntity.ok(Map.of(
                    "message", "Yêu cầu xác thực " + verificationDetailId + " đã bị từ chối.",
                    "success", true,
                    "verificationId", verificationDetailId,
                    "isApproved", false
                ));
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "message", e.getMessage(),
                "success", false
            ));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "message", e.getMessage(),
                "success", false
            ));
        } catch (Exception e) {
            e.printStackTrace(); // Log the full stack trace
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "message", "Lỗi khi cập nhật trạng thái xác thực: " + e.getMessage(),
                "success", false
            ));
        }
    }

    @GetMapping("/verifications")
    public ResponseEntity<?> getAllVerifications() {
        try {
            List<VerificationDetail> verifications = verificationService.getPendingVerifications();
            List<VerificationResponse> responseList = verifications.stream()
                .map(verificationService::mapToVerificationResponse)
                .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(responseList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách yêu cầu xác thực: " + e.getMessage());
        }
    }

    // Seller Application Management
    @GetMapping("/seller-applications")
    public ResponseEntity<?> getPendingSellerApplications() {
        try {
            List<Seller> pendingSellers = sellerService.getPendingSellerRequests();
            return ResponseEntity.ok(pendingSellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi lấy danh sách đơn đăng ký bán hàng: " + e.getMessage());
        }
    }

    @PostMapping("/seller-applications/{sellerId}/approve")
    public ResponseEntity<?> approveSellerApplication(@PathVariable Integer sellerId, @RequestParam Integer adminUserId) {
        try {
            Seller approvedSeller = sellerService.approveSellerRequest(sellerId, adminUserId);
            return ResponseEntity.ok(Map.of(
                "message", "Đơn đăng ký bán hàng " + sellerId + " đã được phê duyệt thành công.",
                "seller", approvedSeller
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi phê duyệt đơn đăng ký bán hàng: " + e.getMessage());
        }
    }

    @PostMapping("/seller-applications/{sellerId}/reject")
    public ResponseEntity<?> rejectSellerApplication(
            @PathVariable Integer sellerId,
            @RequestParam Integer adminUserId,
            @RequestBody(required = false) String reason) {
        try {
            String rejectionReason = (reason != null && !reason.isEmpty()) ? reason : "Không đáp ứng yêu cầu của hệ thống";
            Seller rejectedSeller = sellerService.rejectSellerRequest(sellerId, adminUserId, rejectionReason);
            return ResponseEntity.ok(Map.of(
                "message", "Đơn đăng ký bán hàng " + sellerId + " đã bị từ chối.",
                "seller", rejectedSeller
            ));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Lỗi khi từ chối đơn đăng ký bán hàng: " + e.getMessage());
        }
    }
}
