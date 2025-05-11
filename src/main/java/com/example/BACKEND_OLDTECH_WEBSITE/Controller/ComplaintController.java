package com.example.BACKEND_OLDTECH_WEBSITE.Controller;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Service.ComplaintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}