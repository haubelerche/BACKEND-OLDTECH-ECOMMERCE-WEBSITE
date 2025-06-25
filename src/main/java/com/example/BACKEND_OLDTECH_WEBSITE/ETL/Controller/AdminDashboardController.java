package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.*;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.AdminDashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin-dashboard")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('Admin') or hasAuthority('SuperAdmin')")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;


    @GetMapping("/kpis")
    public ResponseEntity<KPIMetricsDTO> getKPIMetrics(
            @RequestParam(defaultValue = "weekly") String period,
            @RequestParam(required = false) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();
        KPIMetricsDTO kpis = adminDashboardService.getKPIMetrics(period, targetDate);
        return ResponseEntity.ok(kpis);
    }

    /**
     * API làm mới KPIs (trigger manual update)
     */
    @PostMapping("/kpis/refresh")
    public ResponseEntity<String> refreshKPIs(@RequestParam(required = false) LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        adminDashboardService.refreshKPIs(targetDate);
        return ResponseEntity.ok("KPIs refreshed successfully for date: " + targetDate);
    }

    /**
     * API cho biểu đồ doanh thu theo thời gian
     */
    @GetMapping("/charts/revenue")
    public ResponseEntity<ChartDataDTO> getRevenueChart(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getRevenueChart(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API cho biểu đồ đơn hàng theo thời gian
     */
    @GetMapping("/charts/orders")
    public ResponseEntity<ChartDataDTO> getOrdersChart(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getOrdersChart(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API cho biểu đồ người dùng mới
     */
    @GetMapping("/charts/users")
    public ResponseEntity<ChartDataDTO> getUsersChart(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getUsersChart(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API cho mật độ đơn hàng theo khu vực địa lý (legacy - deprecated)
     * @deprecated Use /charts/geographic-heatmap instead
     */
    @GetMapping("/charts/geographic")
    @Deprecated
    public ResponseEntity<List<Map<String, Object>>> getGeographicData(
            @RequestParam(defaultValue = "monthly") String period) {

        List<Map<String, Object>> geoData = adminDashboardService.getGeographicOrderDensity(period);
        return ResponseEntity.ok(geoData);    }

    /**
     * API báo cáo tài chính
     */
    @GetMapping("/reports/financial")
    public ResponseEntity<Map<String, Object>> getFinancialReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "monthly") String groupBy) {

        Map<String, Object> report = adminDashboardService.getFinancialReport(startDate, endDate, groupBy);
        return ResponseEntity.ok(report);
    }

    /**
     * API báo cáo bán hàng (top sản phẩm, người bán)
     */
    @GetMapping("/reports/sales")
    public ResponseEntity<Map<String, Object>> getSalesReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(defaultValue = "10") int topN) {

        Map<String, Object> report = adminDashboardService.getSalesReport(startDate, endDate, topN);
        return ResponseEntity.ok(report);
    }

    /**
     * API phân tích người dùng
     */
    @GetMapping("/reports/users")
    public ResponseEntity<Map<String, Object>> getUserAnalyticsReport(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        Map<String, Object> report = adminDashboardService.getUserAnalyticsReport(startDate, endDate);
        return ResponseEntity.ok(report);
    }

    /**
     * API lấy thống kê tổng quan nhanh cho dashboard header
     */
    @GetMapping("/overview/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        Map<String, Object> quickStats = adminDashboardService.getQuickStats();
        return ResponseEntity.ok(quickStats);
    }

    /**
     * API so sánh hiệu suất theo kỳ (tháng này vs tháng trước, quý này vs quý trước)
     */
    @GetMapping("/analytics/period-comparison")
    public ResponseEntity<Map<String, Object>> getPeriodComparison(
            @RequestParam(defaultValue = "month") String period) {

        Map<String, Object> comparison = adminDashboardService.getPeriodComparison(period);
        return ResponseEntity.ok(comparison);
    }

    /**
     * API lấy top categories và products theo doanh thu
     */
    @GetMapping("/analytics/top-performers")
    public ResponseEntity<Map<String, Object>> getTopPerformers(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> topPerformers = adminDashboardService.getTopPerformers(period, limit);
        return ResponseEntity.ok(topPerformers);
    }

    /**
     * API export dữ liệu dashboard ra Excel/PDF
     */
    @GetMapping("/export/dashboard-data")
    public ResponseEntity<byte[]> exportDashboardData(
            @RequestParam String format, // "excel" or "pdf"
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        byte[] exportData = adminDashboardService.exportDashboardData(format, startDate, endDate);

        String filename = "dashboard-report." + (format.equals("excel") ? "xlsx" : "pdf");
        String contentType = format.equals("excel") ?
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" :
                "application/pdf";

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .header("Content-Type", contentType)
                .body(exportData);
    }

    // ===============================
    // II. QUẢN LÝ NGƯỜI BÁN (Seller Management)
    // ===============================

    /**
     * API lấy danh sách người bán với bộ lọc
     */
    @GetMapping("/sellers")
    public ResponseEntity<Page<SellerManagementDTO>> getSellers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String approvalStatus,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<SellerManagementDTO> sellers = adminDashboardService.getSellers(status, approvalStatus, search, pageable);
        return ResponseEntity.ok(sellers);
    }

    /**
     * API lấy chi tiết người bán
     */
    @GetMapping("/sellers/{sellerId}")
    public ResponseEntity<SellerManagementDTO> getSellerDetails(@PathVariable Long sellerId) {
        SellerManagementDTO seller = adminDashboardService.getSellerDetails(sellerId);
        return ResponseEntity.ok(seller);
    }

    /**
     * API duyệt người bán
     */
    @PostMapping("/sellers/approve")
    public ResponseEntity<String> approveSeller(@RequestBody SellerManagementDTO.SellerApprovalRequest request) {
        adminDashboardService.processSeller(request);
        return ResponseEntity.ok("Seller " + request.getAction() + " successfully");
    }

    /**
     * API thực hiện hành động với người bán (khóa/mở/reset password/update commission)
     */
    @PostMapping("/sellers/action")
    public ResponseEntity<String> performSellerAction(@RequestBody SellerManagementDTO.SellerActionRequest request) {
        adminDashboardService.performSellerAction(request);
        return ResponseEntity.ok("Action " + request.getAction() + " performed successfully");
    }

    // ===============================
    // III. QUẢN LÝ NGƯỜI MUA (Customer Management)
    // ===============================

    /**
     * API lấy danh sách người mua
     */
    @GetMapping("/customers")
    public ResponseEntity<Page<Map<String, Object>>> getCustomers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable) {

        Page<Map<String, Object>> customers = adminDashboardService.getCustomers(status, search, pageable);
        return ResponseEntity.ok(customers);
    }

    /**
     * API lấy chi tiết người mua và lịch sử mua hàng
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<Map<String, Object>> getCustomerDetails(@PathVariable Long customerId) {
        Map<String, Object> customerDetails = adminDashboardService.getCustomerDetails(customerId);
        return ResponseEntity.ok(customerDetails);
    }

    /**
     * API khóa/mở khóa tài khoản người mua
     */
    @PostMapping("/customers/{customerId}/toggle-status")
    public ResponseEntity<String> toggleCustomerStatus(
            @PathVariable Long customerId,
            @RequestParam String action,
            @RequestParam(required = false) String reason) {

        adminDashboardService.toggleCustomerStatus(customerId, action, reason);
        return ResponseEntity.ok("Customer status updated successfully");
    }

    // ===============================
    // IV. QUẢN LÝ SẢN PHẨM & DANH MỤC (Product & Category Management)
    // ===============================

    /**
     * API lấy danh sách sản phẩm chờ duyệt
     */
    @GetMapping("/products/pending")
    public ResponseEntity<Page<Map<String, Object>>> getPendingProducts(Pageable pageable) {
        Page<Map<String, Object>> products = adminDashboardService.getPendingProducts(pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * API duyệt/từ chối sản phẩm
     */
    @PostMapping("/products/{productId}/approve")
    public ResponseEntity<String> approveProduct(
            @PathVariable Long productId,
            @RequestParam String action,
            @RequestParam(required = false) String reason) {

        adminDashboardService.approveProduct(productId, action, reason);
        return ResponseEntity.ok("Product " + action + " successfully");
    }

    /**
     * API tìm kiếm và quản lý sản phẩm
     */
    @GetMapping("/products")
    public ResponseEntity<Page<Map<String, Object>>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            Pageable pageable) {

        Page<Map<String, Object>> products = adminDashboardService.getProducts(search, status, category, pageable);
        return ResponseEntity.ok(products);
    }

    /**
     * API gỡ sản phẩm
     */
    @PostMapping("/products/{productId}/remove")
    public ResponseEntity<String> removeProduct(
            @PathVariable Long productId,
            @RequestParam String reason) {

        adminDashboardService.removeProduct(productId, reason);
        return ResponseEntity.ok("Product removed successfully");
    }

    /**
     * API quản lý danh mục
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Map<String, Object>> categories = adminDashboardService.getCategories();
        return ResponseEntity.ok(categories);
    }

    @PostMapping("/categories")
    public ResponseEntity<String> createCategory(@RequestBody Map<String, Object> categoryData) {
        adminDashboardService.createCategory(categoryData);
        return ResponseEntity.ok("Category created successfully");
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<String> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody Map<String, Object> categoryData) {

        adminDashboardService.updateCategory(categoryId, categoryData);
        return ResponseEntity.ok("Category updated successfully");
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        adminDashboardService.deleteCategory(categoryId);
        return ResponseEntity.ok("Category deleted successfully");
    }

    // ===============================
    // V. QUẢN LÝ ĐƠN HÀNG & GIAO DỊCH (Order & Transaction Management)
    // ===============================

    /**
     * API lấy danh sách đơn hàng với bộ lọc phức tạp
     */
    @GetMapping("/orders")
    public ResponseEntity<Page<Map<String, Object>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String customerSearch,
            @RequestParam(required = false) String sellerSearch,
            Pageable pageable) {

        Page<Map<String, Object>> orders = adminDashboardService.getOrders(
                status, startDate, endDate, customerSearch, sellerSearch, pageable);
        return ResponseEntity.ok(orders);
    }

    /**
     * API can thiệp đơn hàng (cập nhật trạng thái)
     */
    @PostMapping("/orders/{orderId}/intervene")
    public ResponseEntity<String> interveneOrder(
            @PathVariable Long orderId,
            @RequestParam String newStatus,
            @RequestParam String reason) {

        adminDashboardService.interveneOrder(orderId, newStatus, reason);
        return ResponseEntity.ok("Order status updated successfully");
    }

    /**
     * API theo dõi lịch sử giao dịch
     */
    @GetMapping("/transactions")
    public ResponseEntity<Page<Map<String, Object>>> getTransactions(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String type,
            Pageable pageable) {

        Page<Map<String, Object>> transactions = adminDashboardService.getTransactions(startDate, endDate, type, pageable);
        return ResponseEntity.ok(transactions);
    }

    // ===============================
    // V.1 TRẢ HÀNG/HOÀN TIỀN
    // ===============================

    /**
     * API lấy danh sách yêu cầu trả hàng/hoàn tiền
     */
    @GetMapping("/returns")
    public ResponseEntity<Page<Map<String, Object>>> getReturnRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            Pageable pageable) {

        Page<Map<String, Object>> returns = adminDashboardService.getReturnRequests(status, type, pageable);
        return ResponseEntity.ok(returns);
    }

    /**
     * API xử lý yêu cầu trả hàng/hoàn tiền
     */
    @PostMapping("/returns/{returnId}/process")
    public ResponseEntity<String> processReturnRequest(
            @PathVariable Long returnId,
            @RequestParam String action,
            @RequestParam(required = false) String adminNotes) {

        adminDashboardService.processReturnRequest(returnId, action, adminNotes);
        return ResponseEntity.ok("Return request processed successfully");
    }

    // ===============================
    // V.2 KHIẾU NẠI
    // ===============================

    /**
     * API lấy danh sách khiếu nại
     */
    @GetMapping("/complaints")
    public ResponseEntity<Page<ComplaintManagementDTO>> getComplaints(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String priority,
            Pageable pageable) {

        Page<ComplaintManagementDTO> complaints = adminDashboardService.getComplaints(status, type, priority, pageable);
        return ResponseEntity.ok(complaints);
    }

    /**
     * API lấy chi tiết khiếu nại
     */
    @GetMapping("/complaints/{complaintId}")
    public ResponseEntity<ComplaintManagementDTO> getComplaintDetails(@PathVariable Long complaintId) {
        ComplaintManagementDTO complaint = adminDashboardService.getComplaintDetails(complaintId);
        return ResponseEntity.ok(complaint);
    }

    /**
     * API xử lý khiếu nại
     */
    @PostMapping("/complaints/process")
    public ResponseEntity<String> processComplaint(@RequestBody ComplaintManagementDTO.ComplaintActionRequest request) {
        adminDashboardService.processComplaint(request);
        return ResponseEntity.ok("Complaint processed successfully");
    }

    /**
     * API gán khiếu nại cho admin
     */
    @PostMapping("/complaints/{complaintId}/assign")
    public ResponseEntity<String> assignComplaint(
            @PathVariable Long complaintId,
            @RequestParam Long adminId) {

        adminDashboardService.assignComplaint(complaintId, adminId);
        return ResponseEntity.ok("Complaint assigned successfully");
    }

    /**
     * API lấy KPIs hiệu suất bán hàng chi tiết
     */
    @GetMapping("/kpis/sales-performance")
    public ResponseEntity<Map<String, Object>> getSalesPerformanceKPIs(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Map<String, Object> salesKPIs = adminDashboardService.getSalesPerformanceKPIs(period, startDate, endDate);
        return ResponseEntity.ok(salesKPIs);
    }

    /**
     * API lấy KPIs hiệu suất người dùng chi tiết
     */
    @GetMapping("/kpis/user-performance")
    public ResponseEntity<Map<String, Object>> getUserPerformanceKPIs(
            @RequestParam(defaultValue = "daily") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        Map<String, Object> userKPIs = adminDashboardService.getUserPerformanceKPIs(period, startDate, endDate);
        return ResponseEntity.ok(userKPIs);
    }

    /**
     * API lấy dữ liệu dự đoán ARIMA cho người dùng, đơn hàng và doanh thu
     */
    @GetMapping("/predictions/arima")
    public ResponseEntity<Map<String, Object>> getARIMAPredictions(
            @RequestParam(defaultValue = "3") int forecastMonths,
            @RequestParam(defaultValue = "9") int historicalMonths) {

        Map<String, Object> predictions = adminDashboardService.getARIMAPredictions(forecastMonths, historicalMonths);
        return ResponseEntity.ok(predictions);
    }

    /**
     * API lấy dữ liệu xu hướng tỷ lệ chuyển đổi và đổi trả
     */
    @GetMapping("/charts/conversion-trends")
    public ResponseEntity<ChartDataDTO> getConversionTrends(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getConversionTrends(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API lấy dữ liệu xu hướng tỷ lệ khách hàng quay lại
     */
    @GetMapping("/charts/customer-retention")
    public ResponseEntity<ChartDataDTO> getCustomerRetentionTrends(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getCustomerRetentionTrends(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API lấy dữ liệu heatmap mật độ đơn hàng theo khu vực địa lý
     */
    @GetMapping("/charts/geographic-heatmap")
    public ResponseEntity<List<Map<String, Object>>> getGeographicHeatmap(
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {

        List<Map<String, Object>> heatmapData = adminDashboardService.getGeographicHeatmap(period, startDate, endDate);
        return ResponseEntity.ok(heatmapData);
    }

    /**
     * API lấy số lượng truy cập website theo thời gian
     */
    @GetMapping("/charts/website-visits")
    public ResponseEntity<ChartDataDTO> getWebsiteVisitsChart(
            @RequestParam(defaultValue = "daily") String timeRange,
            @RequestParam(defaultValue = "30") int periods) {

        ChartDataDTO chartData = adminDashboardService.getWebsiteVisitsChart(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API lấy biểu đồ đơn đổi trả hàng theo thời gian
     */
    @GetMapping("/charts/returns")
    public ResponseEntity<ChartDataDTO> getReturnsChart(
            @RequestParam(defaultValue = "monthly") String timeRange,
            @RequestParam(defaultValue = "12") int periods) {

        ChartDataDTO chartData = adminDashboardService.getReturnsChart(timeRange, periods);
        return ResponseEntity.ok(chartData);
    }

    /**
     * API lấy danh sách người bán mới chờ duyệt (cho cảnh báo dashboard)
     */
    @GetMapping("/alerts/pending-sellers")
    public ResponseEntity<List<Map<String, Object>>> getPendingSellersAlert() {
        List<Map<String, Object>> pendingSellers = adminDashboardService.getPendingSellersForAlert();
        return ResponseEntity.ok(pendingSellers);
    }

    /**
     * API lấy cảnh báo giao dịch gian lận
     */
    @GetMapping("/alerts/fraud-transactions")
    public ResponseEntity<List<Map<String, Object>>> getFraudTransactionAlerts(
            @RequestParam(defaultValue = "24") int hoursBack) {

        List<Map<String, Object>> fraudAlerts = adminDashboardService.getFraudTransactionAlerts(hoursBack);
        return ResponseEntity.ok(fraudAlerts);
    }

    /**
     * API lấy cảnh báo hệ thống
     */
    @GetMapping("/alerts/system")
    public ResponseEntity<List<Map<String, Object>>> getSystemAlerts() {
        List<Map<String, Object>> systemAlerts = adminDashboardService.getSystemAlerts();
        return ResponseEntity.ok(systemAlerts);
    }

    /**
     * API lấy báo cáo lạm dụng/khiếu nại mới
     */
    @GetMapping("/alerts/new-complaints")
    public ResponseEntity<List<Map<String, Object>>> getNewComplaintAlerts(
            @RequestParam(defaultValue = "24") int hoursBack) {

        List<Map<String, Object>> newComplaints = adminDashboardService.getNewComplaintAlerts(hoursBack);
        return ResponseEntity.ok(newComplaints);
    }

    /**
     * API tổng hợp tất cả cảnh báo quan trọng
     */
    @GetMapping("/alerts/summary")
    public ResponseEntity<Map<String, Object>> getAlertsSummary() {
        Map<String, Object> alertsSummary = adminDashboardService.getAlertsSummary();
        return ResponseEntity.ok(alertsSummary);
    }


}
