package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Review;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReviewRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderDetailRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum; 
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

@Service
public class SellerService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final SellerRepository sellerRepository;

    @Autowired
    public SellerService(UserRepository userRepository, ProductRepository productRepository, ReviewRepository reviewRepository, OrderDetailRepository orderDetailRepository, OrderRepository orderRepository, SellerRepository sellerRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.reviewRepository = reviewRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.sellerRepository = sellerRepository;
    }

    @Transactional
    public Product addProduct(Integer sellerId, Product product) {
        // Find the seller (User)
        User seller = userRepository.findById(sellerId)
            .orElseThrow(() -> new EntityNotFoundException("Người bán không tồn tại với ID: " + sellerId));


        if (seller.getRole() != RoleEnum.Seller && seller.getRole() != RoleEnum.Admin) {
            throw new SecurityException("Người dùng với ID: " + sellerId + " không được cấp quyền để thêm sản phẩm.");
        }

        product.setSellerId(sellerId);


        if (product.getCreatedAt() == null) {
            product.setCreatedAt(Timestamp.from(Instant.now()));
        }
        

        if (product.getIsApproved() == null) {
             product.setIsApproved(false); // Default to not approved, requiring admin verification
        }


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
    
  













    
}