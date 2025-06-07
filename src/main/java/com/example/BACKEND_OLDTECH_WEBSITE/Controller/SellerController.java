package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Product.ProductRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller.SellerRegisterRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Seller.SellerRegisterResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ProductImageService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.SellerService;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sellers")
@CrossOrigin(origins = "*")
public class SellerController {

    private final SellerService sellerService;
    private final ProductImageService productImageService;
    private final UserService userService;

    @Autowired
    public SellerController(SellerService sellerService, ProductImageService productImageService, UserService userService) {
        this.sellerService = sellerService;
        this.productImageService = productImageService;
        this.userService = userService;
    }






/*---DÀNH CHO ADMIN---*/


//XÁC THỰC NGƯỜI BÁN
    @PostMapping("/sellers/verify/{sellerId}")
    public ResponseEntity<?> verifySeller(@PathVariable Integer sellerId) {
        try {
            sellerService.verifySeller(sellerId);

            // Return a proper JSON response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Người bán " + sellerId + " đã được xác thực thành công.");
            response.put("sellerId", sellerId);

            return ResponseEntity
                    .ok()
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (EntityNotFoundException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Lỗi khi xác thực người bán: " + e.getMessage());

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }




//LẤY DS TẤT CẢ NGƯỜI BÁN
    @GetMapping("/sellers")
    public ResponseEntity<?> getAllSellers() {
        try {
            List<Seller> sellers = sellerService.getAllSellers();
            return ResponseEntity.ok(sellers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách người bán: " + e.getMessage());
        }
    }






/*---DÀNH CHO NGƯỜI BÁN---*/

//YÊU CẦU TRỞ THÀNH NGƯỜI BÁN
@PostMapping("/apply")
    public ResponseEntity<?> applyToBecomeSeller(@RequestBody SellerRegisterRequest request) {
        try {
            if (request.getMomoAccount() == null || request.getMomoAccount().trim().isEmpty()) {
                System.out.println("DEBUG - Momo account is empty in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tài khoản Momo không được để trống");
            }

            Integer userId = request.getUserId();
            if (userId == null) {
                System.out.println("DEBUG - UserId is null in request");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID người dùng không được để trống");
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
    @PostMapping("/products/{sellerId}")
    public ResponseEntity<?> addProduct(@PathVariable Integer sellerId, @PathVariable ProductRequest request) {
        try {
            Product addedProduct = sellerService.addProduct(
                sellerId,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCategoryName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(addedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi thêm sản phẩm: " + e.getMessage());
        }
    }






//CẬP NHẬT THÔNG TIN SẢN PHẨM
    @PutMapping("/{sellerId}/products/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Integer sellerId, @PathVariable Integer productId, @RequestBody Product product) {
        try {
            Product updatedProduct = sellerService.updateProduct(sellerId, productId, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
    }


    


//XÓA SẢN PHẨM
    @DeleteMapping("/{sellerId}/products/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Integer sellerId, @PathVariable Integer productId) {
        try {
            sellerService.deleteProduct(sellerId, productId);
            return ResponseEntity.ok("Sản phẩm đã được xóa thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
    }




//LẤY DANH SÁCH SẢN PHẨM
    @GetMapping("/{sellerId}/products")
    public ResponseEntity<?> getProducts(@PathVariable Integer sellerId) {
        try {
            List<Product> products = sellerService.getProductsBySeller(sellerId);
            return ResponseEntity.ok(products);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách sản phẩm: " + e.getMessage());
        }
    }




//TRẢ LỜI ĐÁNH GIÁ NGƯỜI MUA
    @PostMapping("/{sellerId}/reviews/{reviewId}/respond")
    public ResponseEntity<?> respondToReview(@PathVariable Integer sellerId, @PathVariable Integer reviewId, @RequestBody String response) {
        try {
            Review review = sellerService.respondToReview(sellerId, reviewId, response);
            return ResponseEntity.ok(review);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi phản hồi đánh giá: " + e.getMessage());
        }
    }




//LẤY LỊCH SỬ BÁN HÀNG
    @GetMapping("/{sellerId}/sales")
    public ResponseEntity<?> getSalesHistory(@PathVariable Integer sellerId) {
        try {
            List<OrderDetail> salesHistory = sellerService.getSalesHistory(sellerId);
            return ResponseEntity.ok(salesHistory);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy lịch sử bán hàng: " + e.getMessage());
        }
    }





//CẬP NHẬT TRẠNG THÁI ĐƠN HÀNG
    @PutMapping("/{sellerId}/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Integer sellerId, @PathVariable Integer orderId, @RequestParam String status) {
        try {
            Orders updatedOrder = sellerService.updateOrderStatus(sellerId, orderId, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái đơn hàng: " + e.getMessage());
        }
    }






//XÁC NHẬN ĐÃ GIAO HÀNG
    @PostMapping("/{sellerId}/orders/{orderId}/ship")
    public ResponseEntity<?> confirmOrderShipped(@PathVariable Integer sellerId, @PathVariable Integer orderId) {
        try {
            Orders shippedOrder = sellerService.confirmOrderShipped(sellerId, orderId);
            return ResponseEntity.ok(shippedOrder);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xác nhận đã giao hàng: " + e.getMessage());
        }
    }



//THỐNG KÊ DOANH THU
    @GetMapping("/{sellerId}/statistics/revenue")
    public ResponseEntity<?> getRevenueStatistics(@PathVariable Integer sellerId) {
        try {
            Map<String, Object> statistics = sellerService.getRevenueStatistics(sellerId);
            return ResponseEntity.ok(statistics);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
        }
    }




//CẬP NHẬT TRẠNG THÁI NGƯỜI BÁN
    @PutMapping("/{sellerId}/status")
    public ResponseEntity<?> updateSellerStatus(@PathVariable Integer sellerId, @RequestParam(required = false) Boolean active, @RequestParam(required = false) Byte status) {
        try {
            // Determine which parameter to use (prioritize active if both are provided)
            Object activeStatus;
            if (active != null) {
                activeStatus = active;
            } else if (status != null) {
                activeStatus = status;
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Phải cung cấp tham số 'active' hoặc 'status'");
            }

            User user = sellerService.updateSellerStatus(sellerId, activeStatus);
            return ResponseEntity.ok(user);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái người bán: " + e.getMessage());
        }
    }



//CẬP NHẬT TRẠNG THÁI KINH DOANH
    @PutMapping("/{sellerId}/business-status")
    public ResponseEntity<?> updateBusinessStatus(@PathVariable Integer sellerId, @RequestParam byte status) {
        try {
            sellerService.updateBusinessStatus(sellerId, status);
            return ResponseEntity.ok("Trạng thái kinh doanh của người bán đã được cập nhật thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi cập nhật trạng thái kinh doanh: " + e.getMessage());
        }
    }






}
