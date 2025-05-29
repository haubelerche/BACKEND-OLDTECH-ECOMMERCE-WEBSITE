package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ComplaintService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/complaint")
public class ComplaintController {

    @Autowired
    private ComplaintService complaintService;

    // Get a complaint by ID for admin
    @GetMapping("/getComplaintById/{complaintId}")
    public ResponseEntity<Complaint> getComplaint(@PathVariable Long complaintId) {
        Complaint complaint = complaintService.getComplaintById(complaintId);
        return ResponseEntity.ok(complaint);
    }

    // Update complaint status (e.g., resolved, pending) for admin
    @PostMapping("/updateComplaintStatus/{complaintId}")
    public ResponseEntity<?> updateComplaintStatus(@PathVariable Long complaintId, @RequestParam String status) {
        complaintService.updateComplaintStatus(complaintId, status);
        return ResponseEntity.ok("Complaint " + complaintId + " status updated to " + status + ".");
    }


    @PostMapping("/complaints/{complaintId}/review")
    public ResponseEntity<?> reviewComplaint(@PathVariable Long complaintId) {
        try {
            complaintService.reviewComplaint(complaintId);
            return ResponseEntity.ok("Khiếu nại " + complaintId + " đã được xem xét thành công.");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi xem xét khiếu nại: " + e.getMessage());
        }
    }

    @GetMapping("/complaints")
    public ResponseEntity<?> getAllComplaints() {
        try {
            List<Complaint> complaints = complaintService.getAllComplaints();
            return ResponseEntity.ok(complaints);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi lấy danh sách khiếu nại: " + e.getMessage());
        }
    }
}
