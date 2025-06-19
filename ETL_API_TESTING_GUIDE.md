# ETL API Testing Guide

## Các bước để test ETL APIs thủ công:

### 1. Khởi động ứng dụng
```bash
.\mvnw.cmd spring-boot:run
```

### 2. Test ETL APIs bằng cURL hoặc Postman

#### A. Test ETL Status API (GET)
```bash
curl -X GET "http://localhost:8080/api/etl/status" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN"
```

#### B. Test Daily ETL API (POST)
```bash
curl -X POST "http://localhost:8080/api/etl/run/daily?date=2024-06-19" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

#### C. Test Current Day ETL API (POST)
```bash
curl -X POST "http://localhost:8080/api/etl/run/current" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

#### D. Test Yesterday ETL API (POST)
```bash
curl -X POST "http://localhost:8080/api/etl/run/yesterday" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

### 3. Test Seller Dashboard ETL APIs

#### A. Test Run ETL for All Sellers
```bash
curl -X POST "http://localhost:8080/api/admin/seller-dashboard-etl/run-all?date=2024-06-19" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

#### B. Test Run ETL for Specific Seller
```bash
curl -X POST "http://localhost:8080/api/admin/seller-dashboard-etl/run-seller/1?date=2024-06-19" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

#### C. Test Backfill ETL
```bash
curl -X POST "http://localhost:8080/api/admin/seller-dashboard-etl/backfill?startDate=2024-06-15&endDate=2024-06-19" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

### 4. Test Error Handling

#### A. Test với Invalid Date Format
```bash
curl -X POST "http://localhost:8080/api/etl/run/daily?date=invalid-date" \
  -H "Authorization: Bearer YOUR_ADMIN_TOKEN" \
  -H "Content-Type: application/json"
```

#### B. Test với Unauthorized Access
```bash
curl -X POST "http://localhost:8080/api/etl/run/daily?date=2024-06-19" \
  -H "Content-Type: application/json"
```

### 5. Expected Responses

#### Success Response:
```json
{
  "success": true,
  "status": "SUCCESS",
  "processDate": "2024-06-19",
  "duration": 300,
  "recordsExtracted": 100,
  "recordsTransformed": 100,
  "recordsLoaded": 100,
  "dataQualityScore": 95.0,
  "timestamp": 1719123456789
}
```

#### Error Response:
```json
{
  "success": false,
  "status": "FAILED",
  "message": "Error description",
  "timestamp": 1719123456789
}
```

### 6. Check Application Logs

Monitor application logs để xem ETL processes:
```bash
tail -f logs/application.log
```

### 7. Verify Database Changes

Kiểm tra database để xem dữ liệu đã được process:
```sql
-- Check ETL results in data warehouse tables
SELECT * FROM fact_monthly_sales ORDER BY created_at DESC LIMIT 10;
SELECT * FROM seller_dashboard ORDER BY updated_at DESC LIMIT 10;
SELECT * FROM admin_dashboard ORDER BY updated_at DESC LIMIT 10;
```

## Các điểm cần kiểm tra:

1. ✅ **API Accessibility**: Tất cả endpoints có thể access được
2. ✅ **Authentication**: Chỉ admin users có thể access ETL APIs
3. ✅ **Parameter Validation**: Invalid parameters được reject đúng cách
4. ✅ **ETL Execution**: ETL processes chạy thành công
5. ✅ **Error Handling**: Errors được handle gracefully
6. ✅ **Response Format**: API responses có format đúng
7. ✅ **Data Persistence**: Kết quả được lưu vào database
8. ✅ **Performance**: ETL processes complete trong thời gian hợp lý

## Troubleshooting

### Nếu APIs không hoạt động:

1. **Check Application Properties**:
   ```properties
   etl.enabled=true
   etl.scheduler.enabled=false  # Disable scheduler during testing
   ```

2. **Check Security Configuration**:
   - Ensure user có ADMIN role
   - Check JWT token validity

3. **Check Database Connection**:
   - Verify database tables exist
   - Check database connectivity

4. **Check Kafka Configuration** (if enabled):
   - Verify Kafka is running
   - Check topic configurations

5. **Check Application Logs**:
   - Look for error messages
   - Check ETL service initialization
