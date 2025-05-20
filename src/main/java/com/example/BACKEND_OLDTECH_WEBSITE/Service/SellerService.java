package com.example.BACKEND_OLDTECH_WEBSITE.Service;

// Standard Java imports
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Jakarta Persistence imports
import jakarta.persistence.EntityNotFoundException;

// Spring Framework imports
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Application-specific Enums
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;

// Application-specific Models
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category; // Assuming you meant to use this, was previously fully qualified
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;

// Application-specific Repositories
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderDetailRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReviewRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;

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
    public SellerService(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository, OrderDetailRepository orderDetailRepository, OrderRepository orderRepository, SellerRepository sellerRepository, CategoryRepository categoryRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Product addProduct(Integer sellerId, String name, String description, BigDecimal price, String categoryName) {
        // Find the seller (User)
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));

        // Check role (Seller or Admin can add)
        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để thêm sản phẩm.");
        }

        // Find category by name
        Category category = categoryRepository.findByName(categoryName)
            .stream()
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy danh mục với tên: " + categoryName));

        // Create new product with default values
       // Create new product with default values
        Product product = Product.builder()
        .sellerId(sellerId)
        .name(name)
        .description(description)
        .price(price)
        .categoryId(category.getId().intValue()) // Use the 'category' instance here
        .status(ProductStatusEnum.Pending)  // Default status
        .isApproved(false)  // Default to not approved
        .isVisible(false) // Default visibility to false
        .createdAt(new Timestamp(System.currentTimeMillis()))
        .updatedAt(new Timestamp(System.currentTimeMillis()))
        .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Integer sellerId, Integer productId, Product productDetails) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));
        

        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để cập nhật sản phẩm.");
        }


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
        // Potentially reset approval status if significant details change, or handle as per business logic
        // existingProduct.setIsApproved(false); 

        existingProduct.setUpdatedAt(Timestamp.from(Instant.now()));

        return productRepository.save(existingProduct);
    }

    @Transactional
    public void deleteProduct(Integer sellerId, Integer productId) {

        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));
        
        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để xóa sản phẩm.");
        }


        Product productToDelete = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Sản phẩm không tồn tại với ID: " + productId));


        if (!productToDelete.getSellerId().equals(sellerId)) {
            throw new SecurityException("Sản phẩm với ID: " + productId + " không thuộc về người bán ID: " + sellerId + ". Xóa bị cấm.");
        }

        productRepository.deleteById(productId);

    }

    @Transactional(readOnly = true)
    public List<Product> getProductsBySeller(Integer sellerId) {

        if (!userRepository.existsById(sellerId)) {
            throw new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId + ", không thể truy xuất sản phẩm.");
        }
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

        if (!userRepository.existsById(sellerId)) {
            throw new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId + ", không thể truy xuất lịch sử bán hàng.");
        }
        return orderDetailRepository.findByProduct_SellerId(sellerId);
    }

    @Transactional
    public Orders updateOrderStatus(Integer sellerId, Integer orderId, String statusString) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));
        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để cập nhật trạng thái đơn hàng.");
        }


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

        if (!userRepository.existsById(sellerId)) {
            throw new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId);
        }

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
    }

    @Transactional
    public Orders confirmOrderShipped(Integer sellerId, Integer orderId) {
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));
        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người bán " + sellerId + " không được cấp quyền để xác nhận giao hàng.");
        }

       
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
        
       
        sellerEntity.setBusinessStatus(makeActive ? (byte)1 : (byte)0);
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
    public boolean updateBusinessStatus(Integer sellerId, byte newBusinessStatus) {
      
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người bán với ID: " + sellerId));
        
       
        seller.setBusinessStatus(newBusinessStatus);
        seller.setUpdatedAt(Timestamp.from(Instant.now()));
        
    
        if (newBusinessStatus == 0) {
          
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
        } else if (newBusinessStatus == 1 && seller.getIsApproved()) {
            
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
            
            // Get EntityManager from repository
            jakarta.persistence.EntityManager em = sellerRepository.getEntityManager();
            
            // Check if seller record exists using native query
            Long count = (Long) em.createNativeQuery(
                "SELECT COUNT(*) FROM seller WHERE seller_id = ?")
                .setParameter(1, userId)
                .getSingleResult();
                    
            // If record exists, delete it
            if (count > 0) {
                em.createNativeQuery("DELETE FROM seller WHERE seller_id = ?")
                    .setParameter(1, userId)
                    .executeUpdate();
                System.out.println("Deleted existing seller record(s) for userId: " + userId);
            }
            
            // Create a new seller record
            java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
            
            em.createNativeQuery(
                "INSERT INTO seller (seller_id, momo_account, is_approved, account_status, created_at, updated_at, business_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)")
                .setParameter(1, userId)
                .setParameter(2, momoAccount)
                .setParameter(3, false) // not approved
                .setParameter(4, AccountStatusEnum.Inactive.toString())
                .setParameter(5, now)
                .setParameter(6, now)
                .setParameter(7, (byte)0)
                .executeUpdate();
            
            // Build and return the seller object
            Seller newSeller = new Seller();
            newSeller.setSellerId(userId);
            newSeller.setMomoAccount(momoAccount);
            newSeller.setIsApproved(false);
            newSeller.setAccountStatus(AccountStatusEnum.Inactive);
            newSeller.setCreatedAt(now);
            newSeller.setUpdatedAt(now);
            newSeller.setBusinessStatus((byte)0);
            
            System.out.println("New seller record created: " + newSeller.getSellerId());
            return newSeller;
            
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
        seller.setBusinessStatus((byte)1); // Active
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

    /**
     * Updates the business status of a seller and their account status accordingly.
     * This method replaces the previous toggleSellingActive and updateBusinessStatus methods.
     * 
     * @param sellerId The ID of the seller to update
     * @param isActive Whether the seller should be active or not (can be boolean or byte)
     * @return The updated User entity, or null if only the Seller entity was updated
     */
    @Transactional
    public User updateSellerStatus(Integer sellerId, Object isActive) {
        // Convert input parameter to byte value
        byte newBusinessStatus;
        boolean makeActive;
        
        if (isActive instanceof Boolean) {
            makeActive = (Boolean) isActive;
            newBusinessStatus = makeActive ? (byte)1 : (byte)0;
        } else if (isActive instanceof Byte) {
            newBusinessStatus = (Byte) isActive;
            makeActive = newBusinessStatus == 1;
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
        if (user.getAccountStatus() == newStatus && seller.getBusinessStatus() == newBusinessStatus) {
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
}