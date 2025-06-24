package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller.SellerRegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller.SellerRegisterResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller.SellerVerifyRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SellerService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.NotificationService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Refund.RefundResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Notification.CreateNotificationRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sellers")
@CrossOrigin(origins = "*")
public class SellerController {

    private final SellerService sellerService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private UserService userService;

    @Autowired
    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    /**
     * Helper method to get current user ID from authentication token
     */
    private Integer getCurrentUserIdFromToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                // Assuming the user ID is stored as a part of the username in the format "userId:username"
                String[] userInfo = userDetails.getUsername().split(":");
                if (userInfo.length > 0) {
                    return Integer.parseInt(userInfo[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new SecurityException("Không thể xác định user từ token");
    }

    /**
     * Helper method to get current seller ID from authentication token
     */
    private Integer getCurrentSellerIdFromToken() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String email = userDetails.getUsername();
                // Lấy user từ email
                com.example.BACKEND_OLDTECH_WEBSITE.Model.User user = userService.findUserByEmail(email);
                if (user != null) {
                    return user.getUserId();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new SecurityException("Không thể xác định seller từ token");
    }





/*---DÀNH CHO ADMIN---*/

    //XÁC THỰC NGƯỜI BÁN
    @PostMapping("/verify/{sellerId}")
    @PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
    public ResponseEntity<?> verifySeller(@PathVariable Integer sellerId, @RequestBody SellerVerifyRequest request) {
        try {
            // Check if admin is approving or rejecting the seller
            boolean isApproving = request.getIsApproved() == null || request.getIsApproved();

            if (isApproving) {
                // Automatically set business status to 1 (active), account_status to Active
                sellerService.verifySeller(sellerId);

                Map<String, Object> response = new HashMap<>();
                response.put("status", "success");
                response.put("message", "Yêu cầu trở thành người bán đã được phê duyệt");
                response.put("sellerId", sellerId);
                response.put("isApproved", true);
                response.put("businessStatus", 1);
                response.put("accountStatus", "Active");

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            } else {
                // If admin is rejecting the seller request
                sellerService.rejectSellerRequest(sellerId, request.getReason());

                Map<String, Object> response = new HashMap<>();
                response.put("status", "failed");
                response.put("message", "Đã từ chối yêu cầu trở thành người bán cho người dùng " + sellerId);
                response.put("sellerId", sellerId);
                response.put("isApproved", false);

                return ResponseEntity
                        .ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(response);
            }
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi xác thực người bán: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }




 /*---DÀNH CHO NGƯỜI BÁN---*/    //YÊU CẦU TRỞ THÀNH NGƯỜI BÁN //ok
    @PostMapping("/apply/seller")
    public ResponseEntity<?> applyToBecomeSeller(@RequestBody SellerRegisterRequest request) {
        try {
            if (request.getMomoAccount() == null || request.getMomoAccount().trim().isEmpty()) {
                System.out.println("DEBUG - Momo account is empty in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản Momo không được để trống");
            }

            // TODO: Get userId from JWT token instead of request body
            Integer userId = getCurrentUserIdFromToken();
            if (userId == null) {
                System.out.println("DEBUG - Cannot extract userId from token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Không thể xác định người dùng từ token");
            }

            // Check for policy agreement
            if (request.getPolicyAgreement() == null || !request.getPolicyAgreement()) {
                System.out.println("DEBUG - Policy agreement not accepted in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Bạn phải đồng ý với chính sách người bán của O'Tech để tiếp tục");
            }

            System.out.println("DEBUG - Calling sellerService.requestToBecomeSeller with userId: " + userId);
            Seller seller = sellerService.requestToBecomeSeller(userId, request.getMomoAccount());

            SellerRegisterResponse response = new SellerRegisterResponse();
            response.setSellerId(seller.getSellerId());
            response.setMomoAccount(seller.getMomoAccount());
            response.setIsApproved(seller.getIsApproved());
            response.setAccountStatus(seller.getAccountStatus());
            response.setBusinessStatus(seller.getBusinessStatus());
            response.setCreatedAt(seller.getCreatedAt());
            response.setUpdatedAt(seller.getUpdatedAt());

            System.out.println("DEBUG - Successfully created seller application: " + response);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (EntityNotFoundException e) {
            System.out.println("DEBUG - EntityNotFoundException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            System.out.println("DEBUG - IllegalStateException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (SecurityException e) {
            System.out.println("DEBUG - SecurityException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("DEBUG - IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (org.springframework.dao.OptimisticLockingFailureException e) {
            System.out.println("DEBUG - Optimistic locking exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Yêu cầu đăng ký người bán đang được xử lý. Vui lòng thử lại sau ít phút.");
        } catch (Exception e) {
            System.out.println("DEBUG - Unexpected exception: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();

            // Check for SQL exceptions that might be nested
            Throwable cause = e.getCause();
            if (cause instanceof java.sql.SQLException) {
                java.sql.SQLException sqlEx = (java.sql.SQLException) cause;
                System.out.println("DEBUG - SQL Exception: " + sqlEx.getMessage() +
                        ", SQLState: " + sqlEx.getSQLState() +
                        ", ErrorCode: " + sqlEx.getErrorCode());

                // Check for duplicate key violation
                if (sqlEx.getErrorCode() == 1062 || (sqlEx.getSQLState() != null && sqlEx.getSQLState().equals("23000"))) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Đã có yêu cầu đăng ký cho người dùng này. Vui lòng thử lại sau.");
                }

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Lỗi cơ sở dữ liệu khi đăng ký: " + sqlEx.getMessage());
            }

            // Check if it's a transaction-related exception
            if (e.getMessage() != null && (
                    e.getMessage().contains("Row was updated or deleted by another transaction") ||
                            e.getMessage().contains("could not execute statement") ||
                            e.getMessage().contains("ConstraintViolationException") ||
                            e.getMessage().contains("Duplicate entry"))) {

                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Hệ thống đang xử lý nhiều yêu cầu. Vui lòng thử lại sau ít phút.");
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đăng ký làm người bán: " + e.getMessage());
        }
    }





    // ĐĂNG KÍ SẢN PHẨM BÁN  
    @PostMapping(path = "register/product", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request) {
        try {
            Integer sellerId = getCurrentSellerIdFromToken();
            // Validate that price is provided
            if (request.getPrice() == null) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Giá sản phẩm không được để trống. Vui lòng điền đầy đủ thông tin sản phẩm.");
            }
            // Validate name and description as well for completeness
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Tên sản phẩm không được để trống. Vui lòng điền đầy đủ thông tin sản phẩm.");
            }
            if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Mô tả sản phẩm không được để trống. Vui lòng điền đầy đủ thông tin sản phẩm.");
            }
            if (request.getCategoryName() == null || request.getCategoryName().trim().isEmpty()) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body("Danh mục sản phẩm không được để trống. Vui lòng điền đầy đủ thông tin sản phẩm.");
            }
            Product addedProduct = sellerService.addProduct(
                    sellerId,
                    request.getName(),
                    request.getDescription(),
                    request.getPrice(),
                    request.getCategoryName()
            );
            // Gửi thông báo cho Admin/SuperAdmin
            CreateNotificationRequest notificationRequest = new CreateNotificationRequest();
            notificationRequest.setNotificationType(NotificationTypeEnum.NEW_PRODUCT);
            notificationRequest.setTitle("Sản phẩm mới chờ phê duyệt");
            notificationRequest.setContent("Sản phẩm '" + addedProduct.getName() + "' vừa được đăng và đang chờ phê duyệt.");
            notificationRequest.setLinkUrl("/admin/products/pending");
            notificationRequest.setRecipientRoles(List.of("Admin", "SuperAdmin"));
            notificationService.createNotificationForUsersByRoles(
                List.of("Admin", "SuperAdmin"),
                notificationRequest,
                "Hệ thống"
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }


 //CẬP NHẬT THÔNG TIN SẢN PHẨM  //ok
    @PutMapping("/update/product/{productId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> updateProduct(@PathVariable Integer productId, @RequestBody Product product) {
        try {
            Integer sellerId = getCurrentSellerIdFromToken();            Product updatedProduct = sellerService.updateProduct(sellerId, productId, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }



//XÓA SẢN PHẨM
    @DeleteMapping("/delete/product/{productId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer productId) {
        try {
           
            Integer sellerId = getCurrentSellerIdFromToken();
            
            sellerService.deleteProduct(sellerId, productId);
            return ResponseEntity.ok("Sản phẩm đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }   
    
    
    
    
    
    
  //LẤY DANH SÁCH SẢN PHẨM
    @GetMapping("list/products/my-products")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> getMyProducts() {
        try {
            // TODO: Get sellerId from JWT token instead of path variable
            Integer sellerId = getCurrentSellerIdFromToken();
            
            List<Product> products = sellerService.getProductsBySeller(sellerId);
            return ResponseEntity.ok(products);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }   
    
    
    
    
    
  //TRẢ LỜI ĐÁNH GIÁ NGƯỜI MUA
    //TODO: RECHECK AFTER FINISH THE REVIEW FUNCTIONALITY
    @PostMapping("/respond_reviews/{reviewId}")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> respondToReview(@PathVariable Integer reviewId, @RequestBody String response) {
        try {
            // TODO: Get sellerId from JWT token instead of path variable
            Integer sellerId = getCurrentSellerIdFromToken();
            
            Review review = sellerService.respondToReview(sellerId, reviewId, response);
            return ResponseEntity.ok(review);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi phản hồi đánh giá: " + e.getMessage());
        }
    }   
    
    
    
    
    
  //TẠM NGƯNG HOẶC TIẾP TỤC KINH DOANH (CHỈ DÀNH CHO NGƯỜI BÁN)
    @PutMapping("/toggle-business")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> toggleBusinessStatus(@RequestBody(required = false) Map<String, Object> request) {
        try {
            // TODO: Get sellerId from JWT token instead of path variable
            Integer sellerId = getCurrentSellerIdFromToken();
            
            // Check if explicit state is provided or we should toggle
            boolean isActive;

            // If request is null, get the current status and toggle it
            if (request == null || request.isEmpty()) {

                Seller seller = sellerService.getSellerById(sellerId);

                isActive = !seller.getBusinessStatus();
            } else {
                // Check for "businessStatus" parameter (primary parameter)
                if (request.containsKey("businessStatus")) {
                    Object businessStatusObj = request.get("businessStatus");
                    if (businessStatusObj instanceof Boolean) {
                        isActive = (Boolean) businessStatusObj;
                    } else if (businessStatusObj instanceof String) {
                        isActive = Boolean.parseBoolean((String) businessStatusObj);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Tham số 'businessStatus' không hợp lệ. Phải là boolean hoặc string.");
                    }
                }
                // Check for "active" parameter (supporting older clients)
                else if (request.containsKey("active")) {
                    Object activeObj = request.get("active");
                    if (activeObj instanceof Boolean) {
                        isActive = (Boolean) activeObj;
                    } else if (activeObj instanceof String) {
                        isActive = Boolean.parseBoolean((String) activeObj);
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Tham số 'active' không hợp lệ. Phải là boolean hoặc string.");
                    }
                } else {
                    // No recognized parameter - get current status and toggle it
                    Seller seller = sellerService.getSellerById(sellerId);
                    isActive = !seller.getBusinessStatus();
                }
            }            // Update seller's business status using the service method
            sellerService.updateBusinessStatus(sellerId, isActive ? true : false);

            // Create response
            Map<String, Object> response = new HashMap<>();
            response.put("sellerId", sellerId);
            response.put("businessStatus", isActive);
            response.put("message", isActive ?
                "Hoạt động kinh doanh đã được kích hoạt." :
                "Hoạt động kinh doanh đã tạm ngưng.");
            response.put("status", "success");

            return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);

        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
        } catch (SecurityException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi thay đổi trạng thái kinh doanh: " + e.getMessage());

            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse);        }
    }
    
    
    // LẤY DANH SÁCH YÊU CẦU HOÀN TIỀN/ĐỔI TRẢ
    @GetMapping("/all-refunds")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> getMyRefundRequests() {
        try {
            Integer sellerId = getCurrentSellerIdFromToken();
            List<RefundResponse> refunds = sellerService.getRefundRequestsForSeller(sellerId);
            return ResponseEntity.ok(refunds);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi chi tiết ra console
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", 500);
            errorResponse.put("message", "An unexpected error occurred");
            errorResponse.put("timestamp", java.time.OffsetDateTime.now().toString());
            errorResponse.put("details", e.getMessage());
            errorResponse.put("path", "/oldtech/sellers/all-refunds");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
    
    // SELLER DUYỆT YÊU CẦU HOÀN TIỀN/ĐỔI TRẢ
    @PutMapping("/refunds/{refundId}/decision")
    @PreAuthorize("hasAuthority('Seller')")
    public ResponseEntity<?> decideRefund(
            @PathVariable Integer refundId,
            @RequestBody Map<String, Object> request) {
        try {
            Integer sellerId = getCurrentSellerIdFromToken();
            String decision = (String) request.get("decision"); // "APPROVED" hoặc "REJECTED"
            String note = (String) request.getOrDefault("note", null);

            if (!"APPROVED".equalsIgnoreCase(decision) && !"REJECTED".equalsIgnoreCase(decision)) {
                return ResponseEntity.badRequest().body("Quyết định không hợp lệ. Chỉ chấp nhận 'APPROVED' hoặc 'REJECTED'.");
            }

            boolean result = sellerService.decideRefundRequest(sellerId, refundId, decision, note);
            if (result) {
                return ResponseEntity.ok("Cập nhật quyết định hoàn tiền thành công.");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Không thể cập nhật quyết định hoàn tiền.");
            }
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            // Check if it's specifically about seller approval
            if (e.getMessage().contains("chưa được phê duyệt") || e.getMessage().contains("không hoạt động")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật quyết định hoàn tiền: " + e.getMessage());
        }
    }
}
