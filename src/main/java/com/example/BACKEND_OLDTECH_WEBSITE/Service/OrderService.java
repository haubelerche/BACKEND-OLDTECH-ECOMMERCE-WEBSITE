package com.example.BACKEND_OLDTECH_WEBSITE.Service;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;

import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderRepository;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller.SellerDashBoardController.DashboardTrendResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.RefundRepository;

@Service
public class OrderService {
    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }    @Transactional
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
    }    @Transactional
    public void updateOrderStatus(Integer orderId, String statusString) {
        Orders order = getOrderById(orderId);
        OrderStatusEnum currentStatus = order.getStatus();
        OrderStatusEnum newStatus;

        try {
            // Try exact match first
            newStatus = OrderStatusEnum.valueOf(statusString.trim());
        } catch (IllegalArgumentException e) {
            // If exact match fails, try with first letter uppercase (PascalCase)
            try {
                String pascalCaseStatus = statusString.trim().substring(0, 1).toUpperCase() +
                        statusString.trim().substring(1).toLowerCase();
                newStatus = OrderStatusEnum.valueOf(pascalCaseStatus);
            } catch (IllegalArgumentException e2) {
                // If both fail, try uppercase
                try {
                    newStatus = OrderStatusEnum.valueOf(statusString.trim().toUpperCase());
                } catch (IllegalArgumentException e3) {
                    throw new IllegalArgumentException("Trạng thái đơn hàng không hợp lệ: '" + statusString + "'. Trạng thái hợp lệ là: " + OrderStatusEnum.getValidStatusesString() + ".", e3);
                }
            }
        }

        // Validate status transition
        validateStatusTransition(currentStatus, newStatus, orderId);

        order.setStatus(newStatus);
        order.setUpdatedAt(Timestamp.from(Instant.now()));

        // Track delivery time for auto-completion logic
        if (newStatus == OrderStatusEnum.Delivered && currentStatus != OrderStatusEnum.Delivered) {
            order.setDeliveredAt(Timestamp.from(Instant.now()));
            logger.info("Đơn hàng {} đã được giao lúc {}. Sẽ tự động hoàn thành sau 3 ngày nếu không được xác nhận.",
                    orderId, order.getDeliveredAt());
        }

        orderRepository.save(order);
        logger.info("Đơn hàng {} đã được cập nhật từ trạng thái {} sang {}.", orderId, currentStatus, newStatus);

        // Gửi thông báo cho khách hàng khi trạng thái đơn hàng thay đổi
        try {
            notificationService.sendOrderUpdateNotification(order.getUserId(), newStatus.name(), orderId);
        } catch (Exception e) {
            logger.warn("Không thể gửi thông báo cập nhật đơn hàng {} cho user {}: {}", orderId, order.getUserId(), e.getMessage());
        }
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

    /**
     * Manual completion by buyer (for Momo payment) or seller (for COD payment)
     */
    @Transactional
    public void completeOrder(Integer orderId, Integer confirmingUserId) {
        Orders order = getOrderById(orderId);

        if (order.getStatus() != OrderStatusEnum.Delivered) {
            throw new IllegalStateException("Đơn hàng " + orderId + " phải ở trạng thái 'Delivered' mới có thể hoàn thành.");
        }

        // Validate who can complete the order based on payment method
        validateCompletionPermission(order, confirmingUserId);        order.setStatus(OrderStatusEnum.Completed);
        order.setUpdatedAt(Timestamp.from(Instant.now()));
        orderRepository.save(order);        // Send notification about manual completion
        try {
            String completedBy = order.getPaymentMethod() == PaymentMethodEnum.Momo ? "người mua" : "người bán";
            String notificationContent = String.format(
                "Đơn hàng #%d đã được hoàn thành bởi %s. Cảm ơn bạn đã sử dụng dịch vụ!", 
                order.getOrderId(), completedBy);
            
            notificationService.createSimpleNotification(
                order.getUserId(), 
                notificationContent, 
                com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum.ORDER_UPDATE
            );
            
        } catch (Exception e) {
            logger.warn("Failed to send completion notification for order {}: {}", 
                       order.getOrderId(), e.getMessage());
        }

        String paymentInfo = order.getPaymentMethod() == PaymentMethodEnum.Momo ? "người mua" : "người bán";
        logger.info("Đơn hàng {} đã được hoàn thành bởi {} (User ID: {})", orderId, paymentInfo, confirmingUserId);
    }

    /**
     * Validate who can complete the order based on payment method
     */
    private void validateCompletionPermission(Orders order, Integer confirmingUserId) {
        if (order.getPaymentMethod() == PaymentMethodEnum.Momo) {
            // For Momo payment, only the buyer can complete
            if (!order.getUserId().equals(confirmingUserId)) {
                throw new IllegalStateException("Với phương thức thanh toán Momo, chỉ người mua mới có thể xác nhận hoàn thành đơn hàng.");
            }
        } else if (order.getPaymentMethod() == PaymentMethodEnum.CashOnDelivery) {
            // For COD, only the seller can complete
            // Note: We would need seller information to validate this properly
            // For now, we'll assume the confirming user is authorized
            logger.info("COD order {} being completed by user {}", order.getOrderId(), confirmingUserId);
        } else {
            throw new IllegalStateException("Phương thức thanh toán không được hỗ trợ cho tự động hoàn thành: " + order.getPaymentMethod());
        }
    }
    /**
     * Scheduled task to auto-complete orders after 3 days
     * Runs every hour to check for orders that need auto-completion
     */    @Scheduled(fixedRate = 3600000) // Run every hour (3600000 ms)
    @Transactional
    public void autoCompleteOrders() {
        logger.info("Đang kiểm tra các đơn hàng cần tự động hoàn thành...");

        Timestamp threeDaysAgo = Timestamp.from(Instant.now().minus(3, ChronoUnit.DAYS));
        List<Orders> ordersToComplete = orderRepository.findByStatusAndDeliveredAtIsNotNullAndDeliveredAtBefore(
            OrderStatusEnum.Delivered, threeDaysAgo);

        int autoCompletedCount = 0;
        for (Orders order : ordersToComplete) {
            // Auto-complete the order
            order.setStatus(OrderStatusEnum.Completed);
            order.setUpdatedAt(Timestamp.from(Instant.now()));
            orderRepository.save(order);

            // Send notification to user about auto-completion
            try {
                String paymentInfo = order.getPaymentMethod() == PaymentMethodEnum.Momo ?
                        "người mua chưa xác nhận" : "người bán chưa xác nhận";                String notificationContent = String.format(
                        "Đơn hàng #%d đã được tự động hoàn thành sau 3 ngày. " +
                                "Lý do: %s trong thời gian quy định.",
                        order.getOrderId(), paymentInfo);

                notificationService.createSimpleNotification(
                        order.getUserId(),
                        notificationContent,
                        com.example.BACKEND_OLDTECH_WEBSITE.Enums.NotificationTypeEnum.ORDER_UPDATE
                );

            } catch (Exception e) {
                logger.warn("Failed to send auto-completion notification for order {}: {}",
                        order.getOrderId(), e.getMessage());
            }

            autoCompletedCount++;
            String paymentInfo = order.getPaymentMethod() == PaymentMethodEnum.Momo ?
                    "người mua chưa xác nhận" : "người bán chưa xác nhận";
            logger.info("Đơn hàng {} đã được tự động hoàn thành sau 3 ngày ({})",
                    order.getOrderId(), paymentInfo);
        }

        if (autoCompletedCount > 0) {
            logger.info("Đã tự động hoàn thành {} đơn hàng", autoCompletedCount);
        }
    }    /**
     * Get orders that are eligible for auto-completion (for monitoring)
     */
    @Transactional(readOnly = true)
    public List<Orders> getOrdersPendingAutoCompletion() {
        // Get all delivered orders with delivery time set
        List<Orders> deliveredOrders = orderRepository.findByStatusAndDeliveredAtIsNotNull(OrderStatusEnum.Delivered);
        Instant now = Instant.now();

        // Filter for orders that are within 3 days of delivery (pending auto-completion)
        return deliveredOrders.stream()
                .filter(order -> {
                    Instant deliveryTime = order.getDeliveredAt().toInstant();
                    long daysSinceDelivery = ChronoUnit.DAYS.between(deliveryTime, now);
                    return daysSinceDelivery >= 0 && daysSinceDelivery < 3; // Orders within 3 days of delivery
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // Tổng doanh thu seller trong khoảng thời gian
    public double getRevenue(LocalDate start, LocalDate end) {
        // TODO: Lấy sellerId từ context hoặc truyền vào nếu cần
        Integer sellerId = 1; // demo, cần sửa lại lấy đúng sellerId
        BigDecimal revenue = orderRepository.getTotalRevenueBySellerAndDateRange(sellerId, start, end);
        return revenue != null ? revenue.doubleValue() : 0.0;
    }

    // Tổng số đơn hàng seller trong khoảng thời gian
    public int getTotalOrders(LocalDate start, LocalDate end) {
        Integer sellerId = 1; // demo
        Long total = orderRepository.getTotalOrdersBySellerAndDateRange(sellerId, start, end);
        return total != null ? total.intValue() : 0;
    }

    // Số đơn đổi trả seller trong khoảng thời gian
    public int getRefundOrders(LocalDate start, LocalDate end) {
        Integer sellerId = 1; // demo
        Long total = refundRepository.getReturnOrdersCountBySellerAndDateRange(sellerId, start, end);
        return total != null ? total.intValue() : 0;
    }

    // Tỷ lệ khách hàng quay lại (giả lập, cần bổ sung query thực tế nếu muốn)
    public double getRepeatCustomerRate(LocalDate start, LocalDate end) {
        return 0.0;
    }

    // Trend doanh thu, đơn hàng, đổi trả (giả lập, cần bổ sung nếu muốn)
    public DashboardTrendResponse getSalesTrend(LocalDate start, LocalDate end, String preset) {
        Integer sellerId = 1; // demo, cần truyền sellerId động nếu có
        DashboardTrendResponse resp = new DashboardTrendResponse();
        resp.labels = new ArrayList<>();
        resp.revenueTrend = new ArrayList<>();
        resp.aovTrend = new ArrayList<>();
        resp.orderTrend = new ArrayList<>();
        resp.refundOrderTrend = new ArrayList<>();
        resp.conversionRateTrend = new ArrayList<>();
        resp.repeatCustomerRateTrend = new ArrayList<>();
        // Lấy doanh thu và số đơn theo ngày
        List<Object[]> revenueRows = orderRepository.getDailyRevenueBySellerAndDateRange(sellerId, start, end);
        List<Object[]> orderRows = orderRepository.getDailyOrdersBySellerAndDateRange(sellerId, start, end);
        List<Object[]> refundRows = refundRepository.getDailyReturnsBySellerAndDateRange(sellerId, start, end);
        // Map ngày -> doanh thu, đơn, đổi trả
        java.util.Map<String, Double> revenueMap = new java.util.HashMap<>();
        java.util.Map<String, Integer> orderMap = new java.util.HashMap<>();
        java.util.Map<String, Integer> refundMap = new java.util.HashMap<>();
        for (Object[] row : revenueRows) revenueMap.put(row[0].toString(), ((Number)row[1]).doubleValue());
        for (Object[] row : orderRows) orderMap.put(row[0].toString(), ((Number)row[1]).intValue());
        for (Object[] row : refundRows) refundMap.put(row[0].toString(), ((Number)row[1]).intValue());
        // Duyệt từng ngày trong khoảng
        LocalDate d = start;
        while (!d.isAfter(end)) {
            String label = d.toString();
            resp.labels.add(label);
            double revenue = revenueMap.getOrDefault(label, 0.0);
            int orders = orderMap.getOrDefault(label, 0);
            int refunds = refundMap.getOrDefault(label, 0);
            resp.revenueTrend.add(revenue);
            resp.orderTrend.add(orders);
            resp.refundOrderTrend.add(refunds);
            resp.aovTrend.add(orders > 0 ? revenue / orders : 0.0);
            d = d.plusDays(1);
        }
        return resp;
    }
}