package com.example.BACKEND_OLDTECH_WEBSITE.Service;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Orders createOrder(Orders order) {
        if (order.getOrderId() != null) {
            throw new IllegalArgumentException("Đơn hàng mới không nên có ID.");
        }
        
        // Set default values if not provided
        if (order.getStatus() == null) {
            order.setStatus(OrderStatusEnum.Pending);
        }
        
        Timestamp now = Timestamp.from(Instant.now());
        if (order.getCreatedAt() == null) {
            order.setCreatedAt(now);
        }
        if (order.getUpdatedAt() == null) {
            order.setUpdatedAt(now);
        }
        if (order.getOrderTime() == null) {
            order.setOrderTime(now);
        }
        
        return orderRepository.save(order);
    }

    @Transactional
    public void confirmOrder(Integer orderId) {
        Orders order = getOrderById(orderId);

        if (order.getStatus() == OrderStatusEnum.Pending) {
            order.setStatus(OrderStatusEnum.Processing);
            order.setUpdatedAt(Timestamp.from(Instant.now()));
            orderRepository.save(order);
            logger.info("Đơn hàng {} đã được xác nhận và chuyển sang trạng thái Chờ Xử Lý.", orderId);
        } else if (order.getStatus() == OrderStatusEnum.Processing) {
            logger.info("Đơn hàng {} đã ở trạng thái Chờ Xử Lý.", orderId);
        } else if (order.getStatus() == OrderStatusEnum.Cancelled) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã bị hủy và không thể xác nhận.");
        } else {
            throw new IllegalStateException("Đơn hàng " + orderId + " ở trạng thái " + order.getStatus() + " và không thể được xác nhận lại.");
        }
    }

    @Transactional(readOnly = true)
    public Orders getOrderById(Integer orderId) {
        return orderRepository.findById(orderId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy đơn hàng với ID: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Orders> getOrderHistory(Integer userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional
    public void updateOrderStatus(Integer orderId, String statusString) {
        Orders order = getOrderById(orderId);
        OrderStatusEnum currentStatus = order.getStatus();
        OrderStatusEnum newStatus;
        
        try {
            newStatus = OrderStatusEnum.valueOf(statusString.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: '" + statusString + "'. Trạng thái hợp lệ là: " + OrderStatusEnum.getValidStatusesString() + ".", e);
        }
        
        // Validate status transition
        validateStatusTransition(currentStatus, newStatus, orderId);
        
        order.setStatus(newStatus);
        order.setUpdatedAt(Timestamp.from(Instant.now()));
        orderRepository.save(order);
        logger.info("Đơn hàng {} đã được cập nhật từ trạng thái {} sang {}.", orderId, currentStatus, newStatus);
    }

    private void validateStatusTransition(OrderStatusEnum currentStatus, OrderStatusEnum newStatus, Integer orderId) {
        // Cancelled orders can only be changed to Returned (in specific cases)
        if (currentStatus == OrderStatusEnum.Cancelled && newStatus != OrderStatusEnum.Returned) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã bị hủy và không thể chuyển sang trạng thái " + newStatus);
        }
        
        // Completed orders should not be changed
        if (currentStatus == OrderStatusEnum.Completed) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã hoàn thành và không thể thay đổi trạng thái.");
        }
        
        // Delivered orders can only be changed to Completed or Returned
        if (currentStatus == OrderStatusEnum.Delivered && 
            newStatus != OrderStatusEnum.Completed && 
            newStatus != OrderStatusEnum.Returned) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã giao và chỉ có thể chuyển sang trạng thái Completed hoặc Returned.");
        }
        
        // Cannot move back from Shipped to Processing or Pending
        if (currentStatus == OrderStatusEnum.Shipped && 
            (newStatus == OrderStatusEnum.Processing || newStatus == OrderStatusEnum.Pending)) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã gửi đi và không thể chuyển ngược về trạng thái " + newStatus);
        }
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Orders order = getOrderById(orderId);
        if (order.getStatus() == OrderStatusEnum.Shipped || order.getStatus() == OrderStatusEnum.Delivered || order.getStatus() == OrderStatusEnum.Completed) {
            throw new IllegalStateException("Đơn hàng " + orderId + " không thể hủy vì đã được giao hàng.");
        }
        if (order.getStatus() == OrderStatusEnum.Cancelled) {
            logger.info("Đơn hàng {} đã bị hủy.", orderId);
            return;
        }
        if (order.getStatus() == OrderStatusEnum.Returned) {
            throw new IllegalStateException("Đơn hàng " + orderId + " đã được trả lại và không thể hủy theo cách này.");
        }
        order.setStatus(OrderStatusEnum.Cancelled);
        order.setUpdatedAt(Timestamp.from(Instant.now()));
        orderRepository.save(order);
        logger.info("Đơn hàng {} đã bị hủy.", orderId);
    }
}