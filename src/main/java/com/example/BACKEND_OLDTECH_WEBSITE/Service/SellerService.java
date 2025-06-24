package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Refund.RefundResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Refund;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderDetailRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReviewRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.RefundRepository;

@Service
public class SellerService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    public SellerService(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository,
                         OrderDetailRepository orderDetailRepository, OrderRepository orderRepository,
                         SellerRepository sellerRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
    }

    // Retrieve a seller by ID
    public Seller getSellerById(Integer sellerId) {
        return sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));
    }

    @Transactional
    public Product addProduct(Integer sellerId, String name, String description, BigDecimal price, String categoryName) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);
        
        Category category = categoryRepository.findByName(categoryName)
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với tên: " + categoryName));
        Product product = Product.builder()
        .sellerId(sellerId)
        .name(name)
        .description(description)
        .price(price)
        .categoryId(category.getId().longValue()) // Changed from intValue() to longValue() to match the Long type
        .status(ProductStatusEnum.Pending)
        .isApproved(false)
        .isVisible(false)
        .createdAt(new Timestamp(System.currentTimeMillis()))
        .updatedAt(new Timestamp(System.currentTimeMillis()))
        .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Integer sellerId, Integer productId, Product productDetails) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);

        Product existingProduct = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));

        if (!existingProduct.getSellerId().equals(sellerId)) {
            throw new SecurityException("Sản phẩm với ID: " + productId + " không thuộc về người bán ID: " + sellerId + ". Cập nhật bị cấm.");
        }


        if (productDetails.getName() != null) {
            existingProduct.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            existingProduct.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            existingProduct.setPrice(productDetails.getPrice());
        }
        if (productDetails.getCategoryId() != null) {
            existingProduct.setCategoryId(productDetails.getCategoryId());
        }
        if (productDetails.getStatus() != null) {
            existingProduct.setStatus(productDetails.getStatus());
        }

        existingProduct.setUpdatedAt(Timestamp.from(Instant.now()));

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Integer sellerId, Integer productId) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);


        Product productToDelete = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));


        if (!productToDelete.getSellerId().equals(sellerId)) {
            throw new SecurityException("Sản phẩm với ID: " + productId + " không thuộc về người bán ID: " + sellerId + ". Xóa bị cấm.");
        }

        productRepository.deleteById(productId);

    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySeller(Integer sellerId) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);
        
        return productRepository.findBySellerId(sellerId);
    }

    @Transactional
    public Review respondToReview(Integer sellerId, Integer reviewId, String responseText) {

        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));

        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để trả lời đánh giá.");
        }


        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new EntityNotFoundException("Đánh giá không tồn tại với ID: " + reviewId));


        if (!review.getSellerId().equals(sellerId)) {
            throw new SecurityException("Người bán " + sellerId + " không được cấp quyền để trả lời đánh giá ID: " + reviewId + ". Đánh giá là cho sản phẩm của người bán khác.");
        }

        review.setSellerResponse(responseText);
        review.setResponseTime(Timestamp.from(Instant.now()));

        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public List<OrderDetail> getSalesHistory(Integer sellerId) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);
        
        return orderDetailRepository.findByProduct_SellerId(sellerId);
    }    @Transactional
    public Orders updateOrderStatus(Integer sellerId, Integer orderId, String statusString) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);

        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Đơn hàng không tồn tại với ID: " + orderId));

        boolean sellerInvolvedInOrder = orderDetailRepository.findByProduct_SellerId(sellerId)
            .stream()
            .anyMatch(detail -> detail.getOrder().getOrderId().equals(orderId));
        
        if (!sellerInvolvedInOrder) {
            throw new SecurityException("Người bán " + sellerId + " không tham gia vào đơn hàng ID: " + orderId + " và không thể cập nhật trạng thái của nó.");
        }


        OrderStatusEnum newStatus;
        try {
            newStatus = OrderStatusEnum.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: " + statusString + ". Trạng thái hợp lệ là: Pending, Processing, Shipped, Delivered, Cancelled, Returned, Completed."); // Corrected valid statuses
        }


        order.setStatus(newStatus);
        order.setUpdatedAt(Timestamp.from(Instant.now()));

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getRevenueStatistics(Integer sellerId) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);

        List<OrderDetail> sellerOrderDetails = orderDetailRepository.findByProduct_SellerId(sellerId);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalItemsSold = 0;

        for (OrderDetail detail : sellerOrderDetails) {

            if (detail.getPriceAtPurchase() != null) {
                 totalRevenue = totalRevenue.add(detail.getPriceAtPurchase());
            }
            totalItemsSold++;
        }

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("sellerId", sellerId);
        statistics.put("totalRevenue", totalRevenue);
        statistics.put("totalItemsSold", totalItemsSold); 

        long distinctOrders = sellerOrderDetails.stream().map(od -> od.getOrder().getOrderId()).distinct().count();
        statistics.put("distinctOrdersInvolvedIn", distinctOrders);



        return statistics;
    }    @Transactional
    public Orders confirmOrderShipped(Integer sellerId, Integer orderId) {
        // Validate that seller is approved and can perform seller operations
        validateApprovedSeller(sellerId);

        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Đơn hàng không tồn tại với ID: " + orderId));

        boolean sellerInvolvedInOrder = orderDetailRepository.findByProduct_SellerId(sellerId)
            .stream()
            .anyMatch(detail -> detail.getOrder().getOrderId().equals(orderId));
        
        if (!sellerInvolvedInOrder) {
            throw new SecurityException("Người bán " + sellerId + " không tham gia vào đơn hàng ID: " + orderId + " và không thể xác nhận giao hàng.");
        }


        order.setStatus(OrderStatusEnum.Shipped); 
        order.setUpdatedAt(Timestamp.from(Instant.now()));

        return orderRepository.save(order);
    }

    @Transactional
    public User toggleSellingActive(Integer sellerId, boolean makeActive) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));

        if (seller.getRole() != RoleEnum.Seller) {
            throw new SecurityException("Người bán " + sellerId + " không phải là người bán và trạng thái tài khoản không thể được chuyển đổi thông qua phương thức này.");
        }

        AccountStatusEnum newStatus = makeActive ? AccountStatusEnum.Active : AccountStatusEnum.Inactive;

        if (seller.getAccountStatus() == newStatus) {
            System.out.println("Người bán " + sellerId + " đã có trạng thái " + newStatus + ". Không thực hiện thay đổi.");
            return seller;
        }

        seller.setAccountStatus(newStatus);
        seller.setUpdatedAt(Timestamp.from(Instant.now()));

       
        Seller sellerEntity = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin người bán với ID: " + sellerId));
        
       
        sellerEntity.setBusinessStatus(makeActive);
        sellerEntity.setUpdatedAt(Timestamp.from(Instant.now()));
        sellerRepository.save(sellerEntity);

     
        if (!makeActive) {
            System.out.println("Trạng thái tài khoản người bán " + sellerId + " đã được đặt thành " + newStatus + ", nên sản phẩm sẽ bị ẩn.");
            productRepository.findBySellerId(sellerId).forEach(p -> {
                p.setIsVisible(false);
                productRepository.save(p);
            });
        } else {
            System.out.println("Người bán " + sellerId + " đã được kích hoạt.");
        }

        return userRepository.save(seller);
    }

    @Transactional
    public boolean updateBusinessStatus(Integer sellerId, boolean newBusinessStatus) {
      
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));
        
       
        seller.setBusinessStatus(newBusinessStatus);
        seller.setUpdatedAt(Timestamp.from(Instant.now()));
        
    
        if (!newBusinessStatus) {

            AccountStatusEnum newStatus = AccountStatusEnum.Inactive;
            seller.setAccountStatus(newStatus);
           
            userRepository.findById(sellerId).ifPresent(user -> {
                if (user.getRole() == RoleEnum.Seller) {
                    user.setAccountStatus(newStatus);
                    user.setUpdatedAt(Timestamp.from(Instant.now()));
                    userRepository.save(user);
                }
            });
            
           
            productRepository.findBySellerId(sellerId).forEach(p -> {
                p.setIsVisible(false);
                productRepository.save(p);
            });
            
            System.out.println("Người bán " + sellerId + " đã tắt trạng thái kinh doanh và được đặt thành không hoạt động.");
        } else if (newBusinessStatus && seller.getIsApproved()) {

            AccountStatusEnum newStatus = AccountStatusEnum.Active;
            seller.setAccountStatus(newStatus);
            
           
            userRepository.findById(sellerId).ifPresent(user -> {
                if (user.getRole() == RoleEnum.Seller) {
                    user.setAccountStatus(newStatus);
                    user.setUpdatedAt(Timestamp.from(Instant.now()));
                    userRepository.save(user);
                }
            });
            
            System.out.println("Người bán " + sellerId + " đã bật trạng thái kinh doanh và được đặt thành hoạt động.");
        }
        
        sellerRepository.save(seller);
        return true;
    }
    
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)
    public Seller requestToBecomeSeller(Integer userId, String momoAccount) {
        System.out.println("requestToBecomeSeller called with userId: " + userId + ", momoAccount: " + momoAccount);
        
        try {
            // Find the user
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
            
            System.out.println("User found: " + user.getUserId() + ", role: " + user.getRole());
            
            // Check if user is already a seller
            if (user.getRole() == RoleEnum.Seller) {
                System.out.println("User is already a seller");
                throw new IllegalStateException("Người dùng này đã là người bán hàng.");
            }
            
            // Check if user is a customer
            if (user.getRole() != RoleEnum.Customer) {
                System.out.println("User is not a customer but: " + user.getRole());
                throw new SecurityException("Chỉ có khách hàng mới có thể đăng ký làm người bán. Vai trò hiện tại: " + user.getRole());
            }
            
            // Validate momo account
            if (momoAccount == null || momoAccount.trim().isEmpty()) {
                System.out.println("Momo account is empty");
                throw new IllegalArgumentException("Tài khoản Momo không được để trống");
            }
            
            if (momoAccount.length() < 8) {
                System.out.println("Momo account is too short: " + momoAccount.length());
                throw new IllegalArgumentException("Tài khoản Momo phải có ít nhất 8 ký tự");
            }
            
            // Check if seller record exists and delete if found
            boolean sellerExists = sellerRepository.existsById(userId);
            if (sellerExists) {
                sellerRepository.deleteById(userId);
                System.out.println("Deleted existing seller record for userId: " + userId);
            }
            
            // Create timestamp for audit fields
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            
            // Create a new seller entity using builder pattern or constructor
            Seller newSeller = new Seller();
            newSeller.setSellerId(userId);
            newSeller.setMomoAccount(momoAccount);
            newSeller.setIsApproved(false);
            newSeller.setAccountStatus(AccountStatusEnum.Inactive);
            newSeller.setCreatedAt(now);
            newSeller.setUpdatedAt(now);
            newSeller.setBusinessStatus(false);

            // Save the entity using repository
            Seller savedSeller = sellerRepository.save(newSeller);

            System.out.println("New seller record created: " + savedSeller.getSellerId());
            return savedSeller;

        } catch (Exception e) {
            System.out.println("Exception in requestToBecomeSeller: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    @Transactional(readOnly = true)
    public List<Seller> getPendingSellerRequests() {
        return sellerRepository.findByIsApproved(false);
    }
    
    @Transactional
    public Seller approveSellerRequest(Integer sellerId, Integer adminUserId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu người bán với ID: " + sellerId));
        
        if (seller.getIsApproved()) {
            throw new IllegalStateException("Yêu cầu người bán này đã được duyệt trước đó.");
        }
        
        // Update seller record
        seller.setIsApproved(true);
        seller.setAccountStatus(AccountStatusEnum.Active);
        seller.setApprovedAt(new Timestamp(System.currentTimeMillis()));
        seller.setBusinessStatus(true); // Active
        seller.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        // Update user role
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + sellerId));
        user.setRole(RoleEnum.Seller);
        userRepository.save(user);
        
        System.out.println("Yêu cầu người bán " + sellerId + " đã được phê duyệt bởi admin " + adminUserId);
        
        return sellerRepository.save(seller);
    }
    
    @Transactional
    public Seller rejectSellerRequest(Integer sellerId, Integer adminUserId, String reason) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu người bán với ID: " + sellerId));
        
        if (seller.getIsApproved()) {
            throw new IllegalStateException("Yêu cầu người bán này đã được duyệt trước đó và không thể bị từ chối.");
        }
        
        // Update seller record
        seller.setAccountStatus(AccountStatusEnum.Inactive); // Using Inactive instead of Rejected
        seller.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        
        System.out.println("Yêu cầu người bán " + sellerId + " đã bị từ chối bởi admin " + adminUserId + ". Lý do: " + reason);
        
        return sellerRepository.save(seller);
    }

    @Transactional
    public void rejectSellerRequest(Integer sellerId, String reason) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu người bán với ID: " + sellerId));

        if (seller.getIsApproved()) {
            throw new IllegalStateException("Yêu cầu người bán này đã được duyệt trước đó và không thể bị từ chối.");
        }

        // Update seller record
        seller.setAccountStatus(AccountStatusEnum.Inactive);
        seller.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        // Save the updated seller
        sellerRepository.save(seller);

        System.out.println("Yêu cầu người bán " + sellerId + " đã bị từ chối. Lý do: " +
                         (reason != null && !reason.trim().isEmpty() ? reason : "Không có lý do được cung cấp"));
    }


    @Transactional
    public User updateSellerStatus(Integer sellerId, Object isActive) {
        // Convert input parameter to Boolean value
        Boolean newBusinessStatus;
        boolean makeActive;
        
        if (isActive instanceof Boolean) {
            makeActive = (Boolean) isActive;
            newBusinessStatus = makeActive;
        } else if (isActive instanceof Byte) {
            byte byteValue = (Byte) isActive;
            makeActive = byteValue == 1;
            newBusinessStatus = makeActive;
        } else {
            throw new IllegalArgumentException("Tham số trạng thái phải là Boolean hoặc Byte");
        }

        // Find both user and seller entities
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + sellerId));
            
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin người bán với ID: " + sellerId));
        
        // Validate the user has seller role
        if (user.getRole() != RoleEnum.Seller && user.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không phải là người bán.");
        }
        
        // Set the new status
        AccountStatusEnum newStatus = makeActive ? AccountStatusEnum.Active : AccountStatusEnum.Inactive;
        
        // If active requested but seller not approved, prevent activation
        if (makeActive && !seller.getIsApproved()) {
            throw new IllegalStateException("Không thể kích hoạt người bán chưa được duyệt");
        }
        
        // Check if status isn't changing
        if (user.getAccountStatus() == newStatus && seller.getBusinessStatus().equals(newBusinessStatus)) {
            System.out.println("Người bán " + sellerId + " đã có trạng thái " + newStatus + ". Không thực hiện thay đổi.");
            return user;
        }
        
        // Update the user entity
        user.setAccountStatus(newStatus);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        
        // Update the seller entity
        seller.setAccountStatus(newStatus);
        seller.setBusinessStatus(newBusinessStatus);
        seller.setUpdatedAt(Timestamp.from(Instant.now()));
        
        // If making inactive, hide all products
        if (!makeActive) {
            System.out.println("Trạng thái tài khoản người bán " + sellerId + " đã được đặt thành " + newStatus + ", nên sản phẩm sẽ bị ẩn.");
            productRepository.findBySellerId(sellerId).forEach(p -> {
                p.setIsVisible(false);
                productRepository.save(p);
            });
        } else {
            System.out.println("Người bán " + sellerId + " đã được kích hoạt.");
        }
        
        // Save both entities
        sellerRepository.save(seller);
        return userRepository.save(user);
    }

    @Transactional
    public Product approveProduct(Integer productId, Integer adminId) {
        // 1. Verify adminId has Admin role
        User admin = userRepository.findById(adminId)
            .orElseThrow(() -> new EntityNotFoundException("Admin không tồn tại với ID: " + adminId));
        if (admin.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Chỉ có Admin mới được duyệt sản phẩm.");
        }

        // 2. Find the product
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));

        // 3. Set approval status
        product.setIsApproved(true);
        product.setStatus(ProductStatusEnum.Approved); // Or some other appropriate status
        product.setUpdatedAt(Timestamp.from(Instant.now()));
        // You might also want to set an approvedBy field if you track that

        return productRepository.save(product);
    }

    @Transactional
    public void setUserVerifiedStatus(Integer userId, boolean isVerified) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        user.setIsVerified(isVerified);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);

        // If verified and the user is a seller, update seller record too
        if (user.getRole() == RoleEnum.Seller) {
            sellerRepository.findById(userId).ifPresent(seller -> {
                if (isVerified) {
                    seller.setAccountStatus(AccountStatusEnum.Active);
                }
                seller.setUpdatedAt(Timestamp.from(Instant.now()));
                sellerRepository.save(seller);
            });
        }
    }

    @Transactional
    public void verifySeller(Integer sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng tương ứng với ID: " + sellerId));

        // Check if already approved
        if (seller.getIsApproved()) {
            throw new IllegalStateException("Người bán này đã được xác thực trước đó.");
        }

        // Update seller record - automatically approve with active business status
        seller.setIsApproved(true);
        seller.setAccountStatus(AccountStatusEnum.Active);
        seller.setBusinessStatus(true); // Active business
        seller.setApprovedAt(new Timestamp(System.currentTimeMillis())); // Set approval timestamp
        seller.setUpdatedAt(Timestamp.from(Instant.now()));

        // Update user record - set role and sync account status
        user.setRole(RoleEnum.Seller);
        user.setAccountStatus(AccountStatusEnum.Active); // Sync with seller account status
        user.setIsVerified(true);
        user.setUpdatedAt(Timestamp.from(Instant.now()));

        // Save both entities
        sellerRepository.save(seller);
        userRepository.save(user);
    }

    @Transactional
    public void deleteSeller(Integer sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));

        User user = userRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng tương ứng với ID: " + sellerId));

        // Mark all products as invisible
        productRepository.findBySellerId(sellerId).forEach(p -> {
            p.setIsVisible(false);
            productRepository.save(p);
        });

        // Delete seller record
        sellerRepository.delete(seller);

        // Reset user role to Customer
        user.setRole(RoleEnum.Customer);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<Seller> getAllSellers() {
        return sellerRepository.findAll();
    }

    /**
     * Helper method to validate that a seller is approved and can perform seller operations
     * @param sellerId the ID of the seller to validate
     * @throws EntityNotFoundException if seller doesn't exist
     * @throws SecurityException if seller is not approved or doesn't have seller role
     */
    private void validateApprovedSeller(Integer sellerId) {
        // Check if user exists and has seller role
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));
        
        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không có quyền truy cập tính năng người bán.");
        }
        
        // For non-admin users, check if their seller request is approved
        if (seller.getRole() == RoleEnum.Seller) {
            Seller sellerEntity = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin người bán với ID: " + sellerId));
            
            if (!sellerEntity.getIsApproved()) {
                throw new SecurityException("Yêu cầu trở thành người bán của bạn chưa được phê duyệt. Vui lòng chờ admin xác nhận.");
            }
            
            if (sellerEntity.getAccountStatus() != AccountStatusEnum.Active) {
                throw new SecurityException("Tài khoản người bán của bạn hiện tại không hoạt động. Vui lòng liên hệ admin.");
            }
        }
    }

    @Transactional
    public User toggleSellerBusinessStatus(Integer sellerId, boolean isActive) {
        // First validate that seller exists and is approved
        validateApprovedSeller(sellerId);
        
        User user = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + sellerId));
            
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy thông tin người bán với ID: " + sellerId));
        
        // Set the new business status
        Boolean newBusinessStatus = isActive;
        AccountStatusEnum newAccountStatus = isActive ? AccountStatusEnum.Active : AccountStatusEnum.Inactive;
        
        // Check if status isn't changing
        if (seller.getBusinessStatus() == newBusinessStatus) {
            System.out.println("Người bán " + sellerId + " đã có trạng thái kinh doanh " + (isActive ? "hoạt động" : "không hoạt động") + ". Không thực hiện thay đổi.");
            return user;
        }
        
        // Update the seller entity - keep isApproved unchanged
        seller.setBusinessStatus(newBusinessStatus);
        seller.setAccountStatus(newAccountStatus);
        seller.setUpdatedAt(Timestamp.from(Instant.now()));
        
        // Update the user entity - sync account status
        user.setAccountStatus(newAccountStatus);
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        
        // If making inactive, hide all products
        if (!isActive) {
            System.out.println("Người bán " + sellerId + " đã tắt trạng thái kinh doanh, nên sản phẩm sẽ bị ẩn.");
            productRepository.findBySellerId(sellerId).forEach(p -> {
                p.setIsVisible(false);
                productRepository.save(p);
            });
        } else {
            System.out.println("Người bán " + sellerId + " đã bật trạng thái kinh doanh.");
        }
        
        // Save both entities
        sellerRepository.save(seller);
        return userRepository.save(user);
    }

    // Lấy danh sách yêu cầu hoàn tiền/đổi trả cho seller
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundRequestsForSeller(Integer sellerId) {
        // Lấy refund liên quan đến seller bằng cách join sang order_detail/product nếu Refund không có sellerId
        // Giả sử Refund có trường orderId, ta sẽ lấy các orderDetail của order đó và kiểm tra sellerId
        List<Refund> refunds = refundRepository.findAll();
        List<RefundResponse> result = refunds.stream()
            .filter(r -> {
                // Lấy orderId từ refund
                Integer orderId = r.getOrderId();
                if (orderId == null) return false;
                // Lấy tất cả orderDetail của order này
                List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(orderId);
                // Kiểm tra có sản phẩm nào của seller này không
                return orderDetails.stream().anyMatch(od -> od.getProduct() != null && sellerId.equals(od.getProduct().getSellerId()));
            })
            .map(r -> {
                RefundResponse resp = new RefundResponse();
                // Gán các trường cơ bản từ Refund sang RefundResponse
                resp.setRefundId(r.getRefundId());
                resp.setOrderId(r.getOrderId());
                resp.setStatus(r.getStatus());
                resp.setUserId(r.getUserId());
                resp.setRequestedAt(r.getRequestedAt());
                resp.setUpdatedAt(r.getUpdatedAt());
                resp.setSellerNotes(r.getSellerNotes());
                resp.setReason(r.getReason());
                // ...bổ sung các trường khác nếu cần...
                return resp;
            })
            .toList();
        System.out.println("DEBUG refunds (filtered by sellerId via orderDetail) size: " + result.size());
        return result;
    }

    @Transactional
    public boolean decideRefundRequest(Integer sellerId, Integer refundId, String decision, String note) {
        // Validate seller
        validateApprovedSeller(sellerId);

        Refund refund = refundRepository.findById(refundId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy yêu cầu hoàn tiền với ID: " + refundId));

        // Check if this refund belongs to this seller (by orderId -> orderDetail -> product -> sellerId)
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(refund.getOrderId());
        boolean sellerOwnsOrder = orderDetails.stream()
                .anyMatch(od -> od.getProduct() != null && sellerId.equals(od.getProduct().getSellerId()));
        if (!sellerOwnsOrder) {
            throw new SecurityException("Yêu cầu hoàn tiền này không thuộc về người bán hiện tại.");
        }

        // Only allow decision if refund is Pending or Processing
        if (refund.getStatus() != com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum.Pending &&
            refund.getStatus() != com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum.Processing) {
            throw new IllegalStateException("Chỉ có thể duyệt/từ chối yêu cầu hoàn tiền ở trạng thái Pending hoặc Processing.");
        }

        // Update status and seller notes
        if ("APPROVED".equalsIgnoreCase(decision)) {
            refund.setStatus(com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum.Approved);
        } else if ("REJECTED".equalsIgnoreCase(decision)) {
            refund.setStatus(com.example.BACKEND_OLDTECH_WEBSITE.Enums.RefundStatusEnum.Rejected);
        } else {
            throw new IllegalArgumentException("Quyết định không hợp lệ. Chỉ chấp nhận 'APPROVED' hoặc 'REJECTED'.");
        }
        refund.setSellerNotes(note);
        refund.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        refundRepository.save(refund);
        return true;
    }

    // Lấy chi tiết refund theo refundId, chỉ trả về nếu thuộc về sellerId
    public RefundResponse getRefundRequestByIdForSeller(Integer sellerId, Integer refundId) {
        Refund refund = refundRepository.findById(refundId)
                .orElse(null);
        if (refund == null) return null;
        // Kiểm tra quyền sở hữu
        Integer orderId = refund.getOrderId();
        if (orderId == null) return null;
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(orderId);
        boolean sellerOwnsOrder = orderDetails.stream()
                .anyMatch(od -> od.getProduct() != null && sellerId.equals(od.getProduct().getSellerId()));
        if (!sellerOwnsOrder) return null;
        // Map Refund -> RefundResponse
        RefundResponse resp = new RefundResponse();
        resp.setRefundId(refund.getRefundId());
        resp.setOrderId(refund.getOrderId());
        resp.setStatus(refund.getStatus());
        resp.setUserId(refund.getUserId());
        resp.setRequestedAt(refund.getRequestedAt());
        resp.setUpdatedAt(refund.getUpdatedAt());
        resp.setSellerNotes(refund.getSellerNotes());
        resp.setReason(refund.getReason());
        // Lấy sellerId từ orderDetail đầu tiên (nếu có)
        orderDetails.stream().filter(od -> od.getProduct() != null).findFirst().ifPresent(od -> resp.setSellerId(od.getProduct().getSellerId()));
        return resp;
    }
}

