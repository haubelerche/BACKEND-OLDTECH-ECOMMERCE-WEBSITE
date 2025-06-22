# Hướng dẫn tích hợp thanh toán MoMo

## Tổng quan hệ thống thanh toán

Hệ thống hỗ trợ 2 phương thức thanh toán:
1. **Cash on Delivery (COD)** - Thanh toán khi nhận hàng
2. **MoMo** - Thanh toán qua ví điện tử MoMo

## Luồng thanh toán

### 1. Thanh toán COD
- Khách hàng chọn sản phẩm và thanh toán COD
- Đơn hàng được tạo với trạng thái `Pending`
- Hóa đơn được tạo ngay lập tức
- Người bán xác nhận và giao hàng
- Người bán xác nhận hoàn thành đơn hàng sau khi giao hàng thành công

### 2. Thanh toán MoMo
- Khách hàng chọn sản phẩm và thanh toán MoMo
- Hệ thống tạo payment request tới MoMo API
- Khách hàng được chuyển hướng tới trang thanh toán MoMo
- Sau khi thanh toán, MoMo gửi callback về hệ thống
- Hệ thống xác minh và cập nhật trạng thái đơn hàng
- Người mua xác nhận hoàn thành đơn hàng

## Cấu hình MoMo

### 1. Environment Variables
Cần thiết lập các biến môi trường sau:

```bash
MOMO_PARTNER_CODE=your_partner_code
MOMO_ACCESS_KEY=your_access_key
MOMO_SECRET_KEY=your_secret_key
MOMO_ENDPOINT=https://test-payment.momo.vn/v2/gateway/api/create
MOMO_RETURN_URL=http://localhost:3000/payment/momo/return
MOMO_IPN_URL=http://your-domain.com/cart/momo/callback
```

### 2. Cấu hình trong application.properties
```properties
momo.partnerCode=${MOMO_PARTNER_CODE}
momo.accessKey=${MOMO_ACCESS_KEY}
momo.secretKey=${MOMO_SECRET_KEY}
momo.endpoint=${MOMO_ENDPOINT}
momo.returnUrl=${MOMO_RETURN_URL}
momo.ipnUrl=${MOMO_IPN_URL}
```

### 3. Endpoints API

#### a. Tạo thanh toán
```
POST /payment/createPaymentUrl
Content-Type: application/json

{
  "orderId": "123",
  "amount": 100000,
  "orderInfo": "Thanh toán đơn hàng #123"
}
```

#### b. Callback từ MoMo (IPN)
```
POST /cart/momo/callback
Content-Type: application/x-www-form-urlencoded

partnerCode=MOMO&orderId=123&requestId=uuid&amount=100000&orderInfo=...&signature=...
```

#### c. Return URL cho frontend
```
GET /cart/momo/return?orderId=123&resultCode=0&...
```

## Luồng xử lý chi tiết

### 1. Checkout Process
```
1. Customer selects items → POST /cart/checkout/selected/{userId}
2. System creates order with PENDING status
3. If payment method is MoMo:
   - Remove items from cart immediately
   - Create MoMo payment request
   - Return payment URL to frontend
4. If payment method is COD:
   - Remove items from cart
   - Generate invoice immediately
   - Set order status to PENDING
```

### 2. MoMo Payment Flow
```
1. Customer clicks on MoMo payment URL
2. MoMo processes payment
3. MoMo sends IPN callback to /cart/momo/callback
4. System verifies signature and updates order status
5. Customer is redirected to /cart/momo/return
6. Frontend displays payment result
```

### 3. Order Completion Logic
```
- COD Orders: Seller confirms completion after delivery
- MoMo Orders: Buyer confirms completion after receiving goods
- Auto-completion: After 3 days if no manual confirmation
```

## Security Considerations

### 1. Signature Verification
- Tất cả callback từ MoMo đều được verify signature bằng HMAC-SHA256
- Sử dụng secretKey để tạo và verify signature
- Reject tất cả request có signature không hợp lệ

### 2. Environment Variables
- Không hard-code credentials trong source code
- Sử dụng environment variables cho tất cả sensitive data
- Khác nhau giữa test và production environment

### 3. HTTPS Required
- Production phải sử dụng HTTPS cho tất cả callbacks
- IPN URL phải accessible từ internet
- Return URL có thể là localhost trong development

## Testing

### 1. Test Environment
- Sử dụng MoMo sandbox/test environment
- Test với các case scenarios khác nhau
- Verify signature verification works correctly

### 2. Test Cases
- Successful payment
- Failed payment  
- Invalid signature
- Network timeout
- Invalid order ID

## Error Handling

### 1. Common Error Codes
- `0`: Success
- `9000`: Transaction cancelled by user
- `1000`: Invalid signature
- `1001`: Invalid request format

### 2. Error Response Format
```json
{
  "success": false,
  "message": "Error description",
  "errorCode": "error_code",
  "orderId": "123"
}
```

## Monitoring and Logging

### 1. Important Logs
- All MoMo API calls (request/response)
- Signature verification results
- Order status changes
- Payment callback processing

### 2. Metrics to Monitor
- Payment success rate
- Average payment processing time
- Failed payment reasons
- Order completion rates

## Troubleshooting

### 1. Common Issues
- **Invalid signature**: Check secretKey and signature generation
- **Callback not received**: Verify IPN URL is accessible
- **Payment stuck**: Check order status and MoMo transaction status
- **Duplicate processing**: Implement idempotency checks

### 2. Debug Steps
1. Check environment variables are loaded correctly
2. Verify IPN URL is accessible from internet
3. Check MoMo API response for error codes
4. Verify signature generation matches MoMo's algorithm
5. Check database for order status updates
