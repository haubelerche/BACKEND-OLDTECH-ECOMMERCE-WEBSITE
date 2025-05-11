package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.User;
// Assuming other models exist in a similar package structure
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Seller;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Category;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
//import com.example.BACKEND_OLDTECH_WEBSITE.Model.SystemSetting;

//import com.example.BACKEND_OLDTECH_WEBSITE.Model.ReturnRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;


import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
// Assuming other repositories exist
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SellerRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ProductRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.CategoryRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ComplaintRepository;
//import com.example.BACKEND_OLDTECH_WEBSITE.Repository.SystemSettingRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
//import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ReturnRequestRepository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.RoleEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.AccountStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Map;
import java.util.HashMap;


@Service
public class AdminService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository; 
    private final ProductRepository productRepository; 
    private final CategoryRepository categoryRepository;
    private final ComplaintRepository complaintRepository; 
    //private final SystemSettingRepository systemSettingRepository;
    private final OrderRepository orderRepository;
    //private final ReturnRequestRepository returnRequestRepository;

    @Autowired
    public AdminService(UserRepository userRepository,
                        SellerRepository sellerRepository,
                        ProductRepository productRepository,
                        CategoryRepository categoryRepository,
                        ComplaintRepository complaintRepository,
                      //  SystemSettingRepository systemSettingRepository,
                        OrderRepository orderRepository
                     
                      ) 
                      {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.complaintRepository = complaintRepository;
        //this.systemSettingRepository = systemSettingRepository;
        this.orderRepository = orderRepository;
        //this.returnRequestRepository = returnRequestRepository;
    }

    //manage User 
    @Transactional
    public boolean verifyUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));
        user.setIsVerified(true); 
        userRepository.save(user);
        System.out.println("AdminService: User " + userId + " verified.");
        return true;
    }

   

    @Transactional
    public boolean suspendUser(int userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy người dùng với ID: " + userId));
        user.setAccountStatus(AccountStatusEnum.Suspended); 
        userRepository.save(user);
        System.out.println("AdminService: Người dùng " + userId + " đã bị chặn.");
        return true;
    }
   
    public List<User> getAllUsers() {
        System.out.println("AdminService: Lấy thông tin của tất cả người dùng.");
        return userRepository.findAll();
    }

    @Transactional
    public boolean setUserRole(int userId, String roleString) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        RoleEnum newRole;
        try {
            newRole = RoleEnum.valueOf(roleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.err.println("AdminService: Invalid role string: " + roleString);
            throw new IllegalArgumentException("Invalid role: " + roleString, e);
        }

        if (newRole == RoleEnum.Admin) {
            if (user.getEmail() == null || !user.getEmail().toLowerCase().startsWith("staffotech@")) {
                
                System.err.println("AdminService: Attempt to set Admin role for user " + userId + " with non-compliant email: " + user.getEmail());
                
                throw new IllegalArgumentException("Users assigned the Admin role must have an email starting with 'staffotech@'.");
            }
        }
        
        user.setRole(newRole);
        userRepository.save(user);
        System.out.println("AdminService: Role for user " + userId + " set to " + newRole.name());
        return true;
    }

    // seller Management
    @Transactional
    public boolean verifySeller(int sellerId) {
        Seller seller = sellerRepository.findById(sellerId) 
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with ID: " + sellerId));
        seller.setIsApproved(true);
        seller.setAccountStatus(AccountStatusEnum.Active);
        seller.setApprovedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        sellerRepository.save(seller);
        System.out.println("AdminService: Người bán " + sellerId + " đã được xác thực và kích hoạt.");
        return true;
    }

    @Transactional
    public boolean deleteSeller(int sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with ID: " + sellerId + " for deletion."));
      
        seller.setAccountStatus(AccountStatusEnum.Deleted);
        seller.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        sellerRepository.save(seller);
        
        System.out.println("AdminService: Người bán " + sellerId + " đã được xóa.");
        return true;
    }

    @Transactional
    public boolean suspendSeller(int sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new EntityNotFoundException("Seller not found with ID: " + sellerId));
        seller.setAccountStatus(AccountStatusEnum.Suspended);
        seller.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        sellerRepository.save(seller);
        System.out.println("AdminService: Người bán " + sellerId + " đã bị chặn.");
        return true;
    }

    public List<Seller> getAllSellers() { 
        System.out.println("AdminService: Lấy thông tin tất cả người bán.");
        return sellerRepository.findAll(); 
    }

  

    // System Settings
    /* 
    @Transactional
    public boolean manageSystemSettings(String settingName, String settingValue) {
        System.out.println("AdminService: Managing system setting: " + settingName + " = " + settingValue);
        // Assumed SystemSetting model and SystemSettingRepository
        Optional<SystemSetting> existingSetting = systemSettingRepository.findByName(settingName);
        SystemSetting setting;
        if (existingSetting.isPresent()) {
            setting = existingSetting.get();
        } else {
            setting = new SystemSetting(); // Assuming constructor SystemSetting()
            setting.setName(settingName); // Assuming setName method
        }
        setting.setValue(settingValue); // Assuming setValue method
        systemSettingRepository.save(setting);
        System.out.println("System setting " + settingName + " updated to " + settingValue + ".");
        return true;
    }
*/






    // Product
    public List<Product> getPendingProducts() {
        System.out.println("AdminService: Lấy các sản phẩm đang chờ duyệt.");
        return productRepository.findByStatus(ProductStatusEnum.Pending);
    }
    

    @Transactional
    public boolean markProductAsPending(int productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Không thấy sản phẩm với ID: " + productId));
        product.setStatus(ProductStatusEnum.Pending);
        productRepository.save(product);
        System.out.println("AdminService: Sản phẩm " + productId + " đã được đánh dấu là chờ duyệt.");
        return true;
    }
    @Transactional
    public boolean verifyProduct(int productId) {
        Product product = productRepository.findById(productId) 
                .orElseThrow(() -> new EntityNotFoundException("Không thấy sản phẩm với ID: " + productId));
        product.setStatus(ProductStatusEnum.Approved);
        productRepository.save(product);
        System.out.println("AdminService: Sản phẩm " + productId + " đã được xác nhận.");
        return true;
    }


// Add these new methods for additional product status management
@Transactional
public boolean rejectProduct(int productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không thấy sản phẩm với ID: " + productId));
    product.setStatus(ProductStatusEnum.Rejected);
    productRepository.save(product);
    System.out.println("AdminService: Sản phẩm " + productId + " đã bị từ chối.");
    return true;
}

@Transactional
public boolean hideProduct(int productId) {
    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new EntityNotFoundException("Không thấy sản phẩm với ID: " + productId));
    product.setStatus(ProductStatusEnum.Hidden);
    productRepository.save(product);
    System.out.println("AdminService: Sản phẩm " + productId + " đã bị ẩn.");
    return true;
}

    public List<Product> getAllProducts() {
        System.out.println("AdminService: Lấy tất cả sản phẩm.");
        return productRepository.findAll();
    }


    @Transactional
    public boolean setProductCategory(int productId, int categoryId) {
        Product product = productRepository.findById(productId) 
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + productId));
        
        product.setCategoryId(categoryId);
        productRepository.save(product);
        System.out.println("AdminService: Danh mục cho sản phẩm " + productId + " đã được thiết lập thành " + categoryId);
        return true;
    }

    public List<Category> getAllCategories() { 
        System.out.println("AdminService: Lấy tất cả danh mục.");
        return categoryRepository.findAll(); 
    }

    // Complaint Management
    @Transactional
    public boolean reviewComplaint(long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Complaint not found with ID: " + complaintId));
        complaint.setStatus(ComplaintStatus.Resolved); 
        System.out.println("AdminService: Complaint " + complaintId + " resolved.");
        return true;
}

    public List<Complaint> getAllComplaints() {
        System.out.println("AdminService: Getting all complaints.");
        return complaintRepository.findAll();
    }

    @Transactional
    public boolean resolveComplaint(long complaintId, String resolutionNote) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khiếu nại với ID: " + complaintId));
   
        complaint.setStatus(ComplaintStatus.Resolved);
        
        
        complaint.setAdminResponse(resolutionNote);
     
        complaintRepository.save(complaint);
        
    
        if (complaint.getComplainant() != null) {
            sendNotification(complaint.getComplainantId(), 
                "Khiếu nại của bạn đã được xử lý: " + resolutionNote);
        }
        
        System.out.println("AdminService: Khiếu nại " + complaintId + " đã được giải quyết với ghi chú: " + resolutionNote);
        return true;
    }

    // Notif and Statistics
    public boolean sendNotification(int userId, String message) {
       
        if (userId <= 0 || message == null || message.isEmpty()) {
            System.err.println("AdminService: Không thể gửi thông báo cho người dùng với ID: " + userId + " vì thông báo là null hoặc rỗng.");
            return false;
        }
        System.out.println("AdminService: Đã thử gửi thông báo cho người dùng " + userId + ": '" + message + "' (Cần thực hiện thực tế).");
        // Simulate success for now
        return true;
    }

    public Map<String, Object> getWebsiteStatistics() {
        System.out.println("AdminService: Lấy thống kê trang web.");
        // Placeholder: Implement logic to gather and return statistics
        // Example: 
        Map<String, Object> stats = new HashMap<>();
        stats.put("activeUsers", userRepository.count()); // Example, if count method exists
        stats.put("totalProducts", productRepository.count()); // Example
        // ... add more statistics
        System.out.println("Đã lấy thống kê trang web.");
        return stats;
    }

    // Order and Return Management
    /* 
    @Transactional
    public boolean reviewReturnRequest(int returnRequestId) {
        ReturnRequest returnRequest = returnRequestRepository.findById((long) returnRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Return request not found with ID: " + returnRequestId));
        returnRequest.setReviewed(true); // Assuming ReturnRequest model has setReviewed
        returnRequestRepository.save(returnRequest);
        System.out.println("AdminService: Return request " + returnRequestId + " reviewed.");
        return true;
    }
*/
    public List<Orders> getAllOrders() {
        System.out.println("AdminService: Lấy tất cả đơn hàng.");
        return orderRepository.findAll();
    }

    // Traffic Management
    /*public Map<String, Object> getWebsiteTraffic() {
        System.out.println("AdminService: Lấy dữ liệu lượt truy cập trang web.");
        // Placeholder: Implement logic to gather and return website traffic data
        // Example: 
        Map<String, Object> trafficData = new HashMap<>();
        trafficData.put("dailyVisits", 0); // Replace with actual data source
        trafficData.put("uniqueVisitors", 0); // Replace with actual data source
        System.out.println("Đã lấy dữ liệu lượt truy cập trang web.");
        return trafficData;
    }*/
}

//verify user
//delete user
//block user
//get all users
//set user role

//verify seller
//delete seller
//block seller
//get all sellers
//set seller status

//manage system settings

//verify product
//get all products
//set product category
//view all products
//view all categories

//review complaint
//view all complaints

//send notification 
//view website statistics
//review return request
//view all orders


//view the website traffic




