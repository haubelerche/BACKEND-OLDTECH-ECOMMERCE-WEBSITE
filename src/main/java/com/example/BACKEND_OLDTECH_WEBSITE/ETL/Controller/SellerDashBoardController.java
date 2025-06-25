package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import com.example.BACKEND_OLDTECH_WEBSITE.Service.OrderService;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service.SellerDashboardService;
import com.example.BACKEND_OLDTECH_WEBSITE.ETL.DTO.SellerDashboardDTO;

// import các service khác nếu cần

@RestController
@RequestMapping("/api/seller-dashboard")
public class SellerDashBoardController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private SellerDashboardService sellerDashboardService;
    // @Autowired các service khác nếu cần

    // DTO cho request lọc thời gian
    public static class DateRangeRequest {
        public String preset; // 7days, 30days, this_week, last_week, this_month, last_month, this_quarter, last_quarter, custom
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate startDate;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        public LocalDate endDate;
        public Integer sellerId;
    }

    // DTO cho response tổng quan
    public static class DashboardSummaryResponse {
        public double revenue;
        public int totalOrders;
        public double averageOrderValue;
        public int totalRefundOrders;
        public double conversionRate;
        public double repeatCustomerRate;
    }

    // DTO cho response trend/line chart
    public static class DashboardTrendResponse {
        public List<String> labels; // ngày/tuần/tháng
        public List<Double> revenueTrend;
        public List<Double> aovTrend;
        public List<Integer> orderTrend;
        public List<Integer> refundOrderTrend;
        public List<Double> conversionRateTrend;
        public List<Double> repeatCustomerRateTrend;
    }

    // Endpoint tổng quan hiệu suất
    @PostMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@RequestBody DateRangeRequest request) {

        DashboardSummaryResponse resp = new DashboardSummaryResponse();

        return ResponseEntity.ok(resp);
    }

    // Endpoint trả về dữ liệu line chart/trend
    @PostMapping("/trend")
    public ResponseEntity<DashboardTrendResponse> getDashboardTrend(@RequestBody DateRangeRequest request) {
        // TODO: Xử lý logic lấy dữ liệu trend theo ngày/tuần/tháng
        DashboardTrendResponse resp = new DashboardTrendResponse();
        resp.labels = new ArrayList<>();
        resp.revenueTrend = new ArrayList<>();
        resp.aovTrend = new ArrayList<>();
        resp.orderTrend = new ArrayList<>();
        resp.refundOrderTrend = new ArrayList<>();
        resp.conversionRateTrend = new ArrayList<>();
        resp.repeatCustomerRateTrend = new ArrayList<>();
        // Ví dụ:
        // resp.labels = ...;
        // resp.revenueTrend = ...;
        // resp.aovTrend = ...;
        // resp.orderTrend = ...;
        // resp.refundOrderTrend = ...;
        // resp.visitTrend = ...;
        // resp.conversionRateTrend = ...;
        // resp.repeatCustomerRateTrend = ...;
        return ResponseEntity.ok(resp);
    }

    // Tiện ích: Xử lý date range theo preset
    private LocalDate[] resolveDateRange(String preset, LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        LocalDate from, to;
        switch (preset) {
            case "7days":
                from = today.minusDays(6); to = today; break;
            case "30days":
                from = today.minusDays(29); to = today; break;
            case "this_week":
                from = today.with(java.time.DayOfWeek.MONDAY); to = today.with(java.time.DayOfWeek.SUNDAY); break;
            case "last_week":
                from = today.with(java.time.DayOfWeek.MONDAY).minusWeeks(1); to = from.plusDays(6); break;
            case "this_month":
                from = today.withDayOfMonth(1); to = today.withDayOfMonth(today.lengthOfMonth()); break;
            case "last_month":
                from = today.minusMonths(1).withDayOfMonth(1); to = from.withDayOfMonth(from.lengthOfMonth()); break;
            case "this_quarter": {
                int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                from = LocalDate.of(today.getYear(), (currentQuarter - 1) * 3 + 1, 1);
                to = from.plusMonths(2).withDayOfMonth(from.plusMonths(2).lengthOfMonth());
                break;
            }
            case "last_quarter": {
                int currentQuarter = (today.getMonthValue() - 1) / 3 + 1;
                int lastQuarter = currentQuarter - 1;
                int year = today.getYear();
                if (lastQuarter == 0) { lastQuarter = 4; year--; }
                from = LocalDate.of(year, (lastQuarter - 1) * 3 + 1, 1);
                to = from.plusMonths(2).withDayOfMonth(from.plusMonths(2).lengthOfMonth());
                break;
            }
            case "custom":
                from = start; to = end; break;
            default:
                from = today.minusDays(6); to = today; // mặc định 7 ngày qua
        }
        return new LocalDate[]{from, to};
    }

    // API: Doanh thu
    @PostMapping("/revenue")
    public ResponseEntity<Double> getRevenue(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        double revenue = orderService.getRevenue(range[0], range[1]); // TODO: Implement trong OrderService
        return ResponseEntity.ok(revenue);
    }

    // API: Tổng số đơn hàng
    @PostMapping("/orders")
    public ResponseEntity<Integer> getTotalOrders(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        int totalOrders = orderService.getTotalOrders(range[0], range[1]); // TODO: Implement trong OrderService
        return ResponseEntity.ok(totalOrders);
    }

    // API: Giá trị đơn hàng trung bình
    @PostMapping("/aov")
    public ResponseEntity<Double> getAOV(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        double revenue = orderService.getRevenue(range[0], range[1]);
        int totalOrders = orderService.getTotalOrders(range[0], range[1]);
        double aov = totalOrders > 0 ? revenue / totalOrders : 0;
        return ResponseEntity.ok(aov);
    }

    // API: Số đơn đổi trả hàng
    @PostMapping("/refund-orders")
    public ResponseEntity<Integer> getRefundOrders(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        int refundOrders = orderService.getRefundOrders(range[0], range[1]); // TODO: Implement trong OrderService
        return ResponseEntity.ok(refundOrders);
    }

    // API: Tỷ lệ chuyển đổi
    @PostMapping("/conversion-rate")
    public ResponseEntity<Double> getConversionRate(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        int visits = 0; // Đã loại bỏ userService
        int totalOrders = orderService.getTotalOrders(range[0], range[1]);
        double cr = visits > 0 ? (double) totalOrders / visits : 0;
        return ResponseEntity.ok(cr);
    }

    // API: Tỷ lệ khách hàng quay lại
    @PostMapping("/repeat-customer-rate")
    public ResponseEntity<Double> getRepeatCustomerRate(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        double pdr = orderService.getRepeatCustomerRate(range[0], range[1]); // TODO: Implement trong OrderService
        return ResponseEntity.ok(pdr);
    }

    // API: Line chart doanh thu, AOV theo thời gian
    @PostMapping("/sales-trend")
    public ResponseEntity<DashboardTrendResponse> getSalesTrend(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        DashboardTrendResponse resp = orderService.getSalesTrend(range[0], range[1], request.preset); // TODO: Implement trả về labels, revenueTrend, aovTrend
        return ResponseEntity.ok(resp);
    }

    // API tổng hợp dashboard (summary + trend)
    @PostMapping("/full")
    public ResponseEntity<SellerDashboardDTO> getFullDashboard(@RequestBody DateRangeRequest request) {
        LocalDate[] range = resolveDateRange(request.preset, request.startDate, request.endDate);
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].atTime(23,59,59);
        SellerDashboardDTO dto = sellerDashboardService.getDashboardData(request.sellerId, start, end);
        return ResponseEntity.ok(dto);
    }
}
