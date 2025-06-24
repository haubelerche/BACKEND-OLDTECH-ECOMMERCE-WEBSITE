package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Configuration.MomoPayment;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Order.CreateOrderRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.PaymentRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoCreatePaymentResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Payment.MomoExecuteResponseModel;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.OrderStatusEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.PaymentMethodEnum;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.CartItem;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.OrderDetail;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Orders;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Product;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.OrderDetailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@Service
public class CheckoutService {
    
    private static final Logger logger = LoggerFactory.getLogger(CheckoutService.class);
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private CartItemService cartItemService;
    
    @Autowired
    private MomoPayment momoPayment;
    
    @Autowired
    private OrderDetailRepository orderDetailRepository;

    // ===== CHECKOUT METHODS =====

    @Transactional
    public Map<String, Object> checkoutAllItems(Integer userId, PaymentMethodEnum paymentMethod, 
                                               Integer shippingAddressId, String notes) {
        // Lấy tất cả sản phẩm trong giỏ hàng
        List<CartItem> cartItems = cartItemService.getCartItemsByCartId(userId);
        
        if (cartItems.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Giỏ hàng trống, không thể thanh toán");
            response.put("success", false);
            return response;
        }

        // Tính tổng tiền
        BigDecimal totalAmount = cartItemService.getTotalPriceByUserId(userId);

        // Tạo đơn hàng
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setPaymentMethod(paymentMethod);
        orderRequest.setTotalAmount(totalAmount);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setNotes(notes);

        Orders newOrder = createOrderFromCart(userId, orderRequest, cartItems);

        // Xử lý theo phương thức thanh toán
        if (paymentMethod == PaymentMethodEnum.Momo) {
            // Đối với MoMo, xóa items khỏi cart sau khi tạo order thành công  
            removeItemsFromCart(userId, cartItems);
            return handleMomoPayment(newOrder, cartItems);
        } else if (paymentMethod == PaymentMethodEnum.CashOnDelivery) {
            return handleCashOnDeliveryPayment(newOrder, cartItems);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Phương thức thanh toán không được hỗ trợ");
        response.put("success", false);
        return response;
    }

@Transactional
public Map<String, Object> checkoutCart(Integer userId, Integer cartId, PaymentMethodEnum paymentMethod,
                                        Integer shippingAddressId, String notes) {
    // Lấy tất cả sản phẩm trong giỏ hàng theo cartId
    List<CartItem> cartItems = cartItemService.getCartItemsByCartId(cartId);

    if (cartItems == null || cartItems.isEmpty()) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Giỏ hàng trống hoặc không tồn tại, không thể thanh toán");
        response.put("success", false);
        return response;
    }

    // Tính tổng tiền
    BigDecimal totalAmount = cartItems.stream()
            .map(item -> item.getProduct().getPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Tạo đơn hàng
    CreateOrderRequest orderRequest = new CreateOrderRequest();
    orderRequest.setPaymentMethod(paymentMethod);
    orderRequest.setTotalAmount(totalAmount);
    orderRequest.setShippingAddressId(shippingAddressId);
    orderRequest.setNotes(notes);

    Orders newOrder = createOrderFromCart(userId, orderRequest, cartItems);

 // Xử lý theo phương thức thanh toán
    if (paymentMethod == PaymentMethodEnum.Momo) {
        removeItemsFromCart(userId, cartItems);
        return handleMomoPayment(newOrder, cartItems);
    } else if (paymentMethod == PaymentMethodEnum.CashOnDelivery) {
        return handleCashOnDeliveryPayment(newOrder, cartItems);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("message", "Phương thức thanh toán không được hỗ trợ");
    response.put("success", false);
    return response;
}



    @Transactional
    public Map<String, Object> checkoutSelectedItems(Integer userId, PaymentMethodEnum paymentMethod,
                                                   List<Integer> selectedProductIds, Integer shippingAddressId, 
                                                   String notes) {
        if (selectedProductIds == null || selectedProductIds.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vui lòng chọn ít nhất một sản phẩm để thanh toán");
            response.put("success", false);
            return response;
        }

        // Lấy các sản phẩm được chọn từ giỏ hàng
        List<CartItem> allCartItems = cartItemService.getCartItemsByCartId(userId);
        List<CartItem> selectedItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem item : allCartItems) {
            if (selectedProductIds.contains(item.getProduct().getProductId())) {
                selectedItems.add(item);
                totalAmount = totalAmount.add(item.getProduct().getPrice());
            }
        }

        if (selectedItems.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Không tìm thấy sản phẩm được chọn trong giỏ hàng");
            response.put("success", false);
            return response;
        }

        // Tạo đơn hàng
        CreateOrderRequest orderRequest = new CreateOrderRequest();
        orderRequest.setPaymentMethod(paymentMethod);
        orderRequest.setTotalAmount(totalAmount);
        orderRequest.setShippingAddressId(shippingAddressId);
        orderRequest.setNotes(notes);

        Orders newOrder = createOrderFromCart(userId, orderRequest, selectedItems);

        // Xử lý theo phương thức thanh toán
        if (paymentMethod == PaymentMethodEnum.Momo) {
            // Đối với MoMo, xóa items khỏi cart sau khi tạo order thành công
            removeItemsFromCart(userId, selectedItems);
            return handleMomoPayment(newOrder, selectedItems);
        } else if (paymentMethod == PaymentMethodEnum.CashOnDelivery) {
            return handleCashOnDeliveryPayment(newOrder, selectedItems);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Phương thức thanh toán không được hỗ trợ");
        response.put("success", false);
        return response;    }

    // ===== PAYMENT CALLBACK METHODS =====

    public Map<String, Object> handleMomoCallback(Map<String, String> response) {
        try {
            logger.info("Received MoMo callback: {}", response);
            
            // Xử lý response từ MoMo
            boolean isValidResponse = momoPayment.verifyPaymentResponse(response);
            
            if (!isValidResponse) {
                Map<String, Object> result = new HashMap<>();
                result.put("message", "Invalid response signature");
                result.put("success", false);
                return result;
            }
            
            String orderId = response.get("orderId");
            String resultCode = response.get("resultCode");
            
            Map<String, Object> result = new HashMap<>();
            
            if ("0".equals(resultCode)) {
                // Payment successful - update order status
                // orderService.updateOrderStatus(Integer.valueOf(orderId), OrderStatusEnum.Paid);
                result.put("message", "Payment successful");
                result.put("success", true);
                result.put("orderId", orderId);
            } else {
                // Payment failed - handle failure
                result.put("message", "Payment failed");
                result.put("success", false);
                result.put("orderId", orderId);
                result.put("resultCode", resultCode);
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing MoMo callback", e);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Error processing callback");
            result.put("success", false);
            return result;
        }
    }

    public Map<String, Object> handleMomoReturn(Map<String, String> response) {
        try {
            logger.info("Received MoMo return: {}", response);
            
            String orderId = response.get("orderId");
            String resultCode = response.get("resultCode");
            
            Map<String, Object> result = new HashMap<>();
            result.put("orderId", orderId);
            result.put("resultCode", resultCode);
            
            if ("0".equals(resultCode)) {
                result.put("message", "Thanh toán thành công");
                result.put("success", true);
                result.put("status", "success");
            } else {
                result.put("message", "Thanh toán thất bại");
                result.put("success", false);
                result.put("status", "failed");
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing MoMo return", e);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "Lỗi xử lý kết quả thanh toán");
            result.put("success", false);
            result.put("status", "error");
            return result;
        }
    }

    // Xử lý thanh toán Momo
    private Map<String, Object> handleMomoPayment(Orders order, List<CartItem> items) {
        try {
            // Tạo payment request với MoMo API thực tế
            MomoCreatePaymentResponseModel momoResponse = momoPayment.createPaymentRequest(
                order.getOrderId().toString(),
                order.getTotalAmount().longValue(),
                "Thanh toán đơn hàng #" + order.getOrderId()
            );
            
            Map<String, Object> response = new HashMap<>();
            
            if (momoResponse.getErrorCode() == 0) {
                response.put("message", "Đang chuyển hướng đến cổng thanh toán MoMo");
                response.put("success", true);
                response.put("paymentMethod", "Momo");
                response.put("orderId", order.getOrderId());
                response.put("payUrl", momoResponse.getPayUrl());
                response.put("qrCodeUrl", momoResponse.getQrCodeUrl());
                response.put("deeplink", momoResponse.getDeeplink());
                
                logger.info("MoMo payment created successfully for order: {}", order.getOrderId());
            } else {
                response.put("message", "Lỗi khi tạo yêu cầu thanh toán MoMo: " + momoResponse.getMessage());
                response.put("success", false);
                response.put("errorCode", momoResponse.getErrorCode());
                
                logger.error("MoMo payment creation failed for order: {}, error: {}", 
                           order.getOrderId(), momoResponse.getMessage());
            }
            
            return response;
            
        } catch (Exception e) {
            logger.error("Exception creating MoMo payment for order: " + order.getOrderId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lỗi khi xử lý thanh toán MoMo: " + e.getMessage());
            response.put("success", false);
            return response;
        }
    }

    // Xử lý thanh toán khi nhận hàng
    private Map<String, Object> handleCashOnDeliveryPayment(Orders order, List<CartItem> items) {
        try {
            // Tạo hóa đơn ngay lập tức cho COD
            Map<String, Object> invoice = generateInvoice(order, items);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đơn hàng đã được tạo thành công với thanh toán khi nhận hàng");
            response.put("success", true);
            response.put("paymentMethod", "CashOnDelivery");
            response.put("orderId", order.getOrderId());
            response.put("note", "Người bán sẽ cập nhật trạng thái đơn hàng thành 'Completed' sau khi giao hàng thành công");
            response.put("invoice", invoice);
            
            // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
            removeItemsFromCart(order.getUserId(), items);
            
            logger.info("Đơn hàng COD {} đã được tạo thành công cho người dùng {}", order.getOrderId(), order.getUserId());
            
            return response;
        } catch (Exception e) {
            logger.error("Error processing COD payment for order: " + order.getOrderId(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Lỗi khi xử lý thanh toán COD: " + e.getMessage());
            response.put("success", false);
            return response;
        }
    }

    // Tạo đơn hàng từ giỏ hàng
    private Orders createOrderFromCart(Integer userId, CreateOrderRequest orderRequest, List<CartItem> items) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setTotalAmount(orderRequest.getTotalAmount());
        order.setShippingAddressId(orderRequest.getShippingAddressId());
        order.setNotes(orderRequest.getNotes());
        order.setStatus(OrderStatusEnum.Pending);
        order.setOrderTime(Timestamp.from(Instant.now()));
        order.setCreatedAt(Timestamp.from(Instant.now()));
        order.setUpdatedAt(Timestamp.from(Instant.now()));

        Orders savedOrder = orderService.createOrder(order);
        
        // Tạo Order Details từ Cart Items
        createOrderDetails(savedOrder, items);

        // Cập nhật trạng thái sản phẩm thành Sold và unavailable trên các giỏ hàng khác
        Set<Integer> updatedProductIds = new HashSet<>();
        for (CartItem item : items) {
            Product product = item.getProduct();
            if (product != null && !updatedProductIds.contains(product.getProductId())) {
                product.setIsVisible(false); // ẩn khỏi các giỏ hàng khác
                product.setStatus(com.example.BACKEND_OLDTECH_WEBSITE.Enums.ProductStatusEnum.Sold); // chuyển trạng thái Sold
                cartItemService.getProductRepository().save(product);
                updatedProductIds.add(product.getProductId());
            }
        }
        return savedOrder;
    }

    // ===== PRIVATE HELPER METHODS =====

    // Tạo hóa đơn
    private Map<String, Object> generateInvoice(Orders order, List<CartItem> items) {
        Map<String, Object> invoice = new HashMap<>();
        invoice.put("invoiceId", "INV-" + order.getOrderId() + "-" + System.currentTimeMillis());
        invoice.put("orderId", order.getOrderId());
        invoice.put("customerUserId", order.getUserId());
        invoice.put("paymentMethod", order.getPaymentMethod().toString());
        invoice.put("totalAmount", order.getTotalAmount());
        invoice.put("orderTime", order.getOrderTime());
        
        invoice.put("items", items.stream().map(item -> {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("productId", item.getProduct().getProductId());
            itemMap.put("productName", item.getProduct().getName());
            itemMap.put("price", item.getProduct().getPrice());
            itemMap.put("quantity", 1); 
            return itemMap;
        }).toList());
        
        invoice.put("shippingAddressId", order.getShippingAddressId());
        invoice.put("notes", order.getNotes());
        invoice.put("generatedAt", Timestamp.from(Instant.now()));
        
        return invoice;
    }

    // Tạo Order Details từ Cart Items
    private void createOrderDetails(Orders order, List<CartItem> items) {
        for (CartItem item : items) {
            OrderDetail orderDetail = OrderDetail.builder()
                .order(order)
                .product(item.getProduct())
                .priceAtPurchase(item.getProduct().getPrice())
                .build();
            
            orderDetailRepository.save(orderDetail);
        }
    }

    // Xóa các sản phẩm đã đặt hàng khỏi giỏ hàng
    private void removeItemsFromCart(Integer userId, List<CartItem> items) {
        for (CartItem item : items) {
            try {
                cartItemService.removeItem(userId, item.getProduct().getProductId());
            } catch (Exception e) {
                logger.warn("Không thể xóa sản phẩm {} khỏi giỏ hàng của người dùng {}: {}", 
                          item.getProduct().getProductId(), userId, e.getMessage());
            }
        }
    }

    // ===== PAYMENT METHODS từ PaymentService =====
    
    public Map<String, Object> createPaymentUrl(PaymentRequest request) {
        try {
            logger.info("Creating MoMo payment for order: {}, amount: {}", request.getOrderId(), request.getAmount());
            
            MomoCreatePaymentResponseModel response = momoPayment.createPaymentRequest(
                    request.getOrderId(),
                    request.getAmount(),
                    request.getOrderInfo()
            );
            
            Map<String, Object> result = new HashMap<>();
            
            if (response.getErrorCode() == 0) {
                // Success - return payment details
                result.put("success", true);
                result.put("message", "Payment URL created successfully");
                result.put("payUrl", response.getPayUrl());
                result.put("qrCodeUrl", response.getQrCodeUrl());
                result.put("deeplink", response.getDeeplink());
                result.put("orderId", response.getOrderId());
                
                logger.info("MoMo payment URL created successfully for order: {}", request.getOrderId());
            } else {
                // Error from MoMo
                result.put("success", false);
                result.put("message", "Failed to create payment URL: " + response.getMessage());
                result.put("errorCode", response.getErrorCode());
                
                logger.error("MoMo payment creation failed for order: {}, error: {}", 
                           request.getOrderId(), response.getMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Exception creating MoMo payment for order: " + request.getOrderId(), e);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Internal error: " + e.getMessage());
            
            return result;
        }
    }

    public Map<String, Object> handlePaymentReturn(Map<String, String> response) {
        try {
            MomoExecuteResponseModel result = momoPayment.processPaymentResponse(response);
            Map<String, Object> resultMap = new HashMap<>();
            
            if (result != null) {
                String resultCode = result.getResultCode();

                if ("0".equals(resultCode)) {
                    resultMap.put("status", "success");
                    resultMap.put("message", "Payment successful");
                    resultMap.put("orderId", result.getOrderId());
                    resultMap.put("data", result);
                    resultMap.put("success", true);
                } else {
                    resultMap.put("status", "failed");
                    resultMap.put("message", "Payment failed");
                    resultMap.put("orderId", result.getOrderId());
                    resultMap.put("resultCode", resultCode);
                    resultMap.put("data", result);
                    resultMap.put("success", false);
                }
            } else {
                resultMap.put("status", "error");
                resultMap.put("message", "Invalid payment response");
                resultMap.put("success", false);
            }

            return resultMap;
            
        } catch (Exception e) {
            logger.error("Error processing payment response", e);
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("status", "error");
            resultMap.put("message", "Error processing payment response: " + e.getMessage());
            resultMap.put("success", false);
            return resultMap;
        }
    }

    // ===== END PAYMENT METHODS =====
}
