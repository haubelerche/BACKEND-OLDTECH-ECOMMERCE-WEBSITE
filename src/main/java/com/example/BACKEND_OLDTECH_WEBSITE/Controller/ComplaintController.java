package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.AdminComplaintResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.ComplaintResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.CreateComplaintRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/complaints")
@CrossOrigin(origins = "*")
public class ComplaintController {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintController.class);

    @Autowired
    private ComplaintService complaintService;

    /*--USER --*/    /**
     * TẠO RA KHIẾU NẠI
     */
    @PostMapping("/submit")
    @PreAuthorize("hasAnyAuthority('Customer', 'Seller')")
    public ResponseEntity<?> submitComplaint(@Valid @RequestBody CreateComplaintRequest request) {
        try {
            logger.info("User submitting complaint about order {} or user {}", 
                       request.getOrderId(), request.getRespondentId());
            
            Complaint complaint = complaintService.createComplaint(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Khiếu nại đã được gửi thành công. Chúng tôi sẽ xem xét và phản hồi sớm nhất có thể.");
            response.put("complaintId", complaint.getComplaintId());
            response.put("status", complaint.getStatus());
            response.put("createdAt", complaint.getCreatedAt());
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found when submitting complaint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error submitting complaint: {}", e.getMessage(), e);
            return handleException("Lỗi khi gửi khiếu nại", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    /*--ADMIN --*/
    /**
     * LẤY DANH SÁCH KHIẾU NẠI
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> getAllComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Integer respondentId,
            @RequestParam(required = false) Integer orderId) {
        try {
            logger.info("Admin getting all complaints with filters - Status: {}, RespondentId: {}, OrderId: {}", 
                       status, respondentId, orderId);
            
            List<ComplaintResponse> complaints = complaintService.getAllComplaints(status, respondentId, orderId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy danh sách khiếu nại thành công");
            response.put("complaints", complaints);
            response.put("totalCount", complaints.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error getting all complaints: {}", e.getMessage(), e);
            return handleException("Lỗi khi lấy danh sách khiếu nại", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    /**
     * TRẢ LOI TÂM THƯ :))
     */

    /**
     * Get complaint details (Admin only for now, simplified)
     */
    @GetMapping("/{complaintId}")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> getComplaintDetails(@PathVariable Long complaintId) {
        try {
            ComplaintResponse complaint = complaintService.getComplaintById(complaintId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Lấy thông tin khiếu nại thành công");
            response.put("complaint", complaint);

            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Complaint not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error getting complaint details: {}", e.getMessage(), e);
            return handleException("Lỗi khi lấy thông tin khiếu nại", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }






    @PutMapping("/{complaintId}/respond")
    @PreAuthorize("hasAnyAuthority('Admin', 'SuperAdmin')")
    public ResponseEntity<?> respondToComplaint(@PathVariable Long complaintId,
                                               @Valid @RequestBody AdminComplaintResponse adminResponse) {
        try {
            logger.info("Admin responding to complaint {}", complaintId);
            
            complaintService.respondToComplaint(complaintId, adminResponse);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã phản hồi khiếu nại thành công");
            response.put("complaintId", complaintId);
            response.put("newStatus", adminResponse.getStatus());
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            logger.error("Entity not found when responding to complaint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalStateException e) {
            logger.error("Invalid state when responding to complaint: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error responding to complaint: {}", e.getMessage(), e);
            return handleException("Lỗi khi phản hồi khiếu nại", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    // Helper methods

    /**
     * Handle exceptions with consistent error response format
     */
    private ResponseEntity<Map<String, Object>> handleException(String message, Exception e, HttpStatus status) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message + ": " + e.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        return new ResponseEntity<>(response, status);
    }

    /**
     * Get complaint statuses with display names
     */
    private Map<String, String> getStatusesWithDisplayNames() {
        Map<String, String> statuses = new HashMap<>();
        for (ComplaintStatus status : ComplaintStatus.values()) {
            statuses.put(status.name(), status.getDisplayName());
        }
        return statuses;
    }
}
