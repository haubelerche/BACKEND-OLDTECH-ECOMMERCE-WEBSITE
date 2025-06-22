package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.AdminComplaintResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.ComplaintResponse;
import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Complaint.CreateComplaintRequest;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ComplaintRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.UserRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

@Service
public class
ComplaintService {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintService.class);
    
    @Autowired
    private ComplaintRepository complaintRepository;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * USER: Submit a new complaint
     */
    @Transactional
    public Complaint createComplaint(CreateComplaintRequest request) {
        logger.info("Creating new complaint about order {} or user {}", 
                   request.getOrderId(), request.getRespondentId());
        
        // Validate respondent if provided
        if (request.getRespondentId() != null) {
            userRepository.findById(request.getRespondentId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Người bị khiếu nại không tồn tại với ID: " + request.getRespondentId()));
        }
        
        // Build complaint entity
        Complaint complaint = Complaint.builder()
            .orderId(request.getOrderId())
            .respondentId(request.getRespondentId())
            .reason(request.getReason())
            .status(ComplaintStatus.Pending)
            .createdAt(Timestamp.from(Instant.now()))
            .updatedAt(Timestamp.from(Instant.now()))
            .build();
        
        Complaint savedComplaint = complaintRepository.save(complaint);
        logger.info("Complaint created successfully with ID: {}", savedComplaint.getComplaintId());
        
        return savedComplaint;
    }

    /**
     * ADMIN: Get all complaints with filtering
     */
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getAllComplaints(ComplaintStatus status, Integer respondentId, Integer orderId) {
        logger.info("Admin getting all complaints with filters - Status: {}, RespondentId: {}, OrderId: {}", 
                   status, respondentId, orderId);
        
        List<Complaint> complaints;        
        if (status == null && respondentId == null && orderId == null) {
            complaints = complaintRepository.findAll();
        } else if (status != null && respondentId != null) {
            complaints = complaintRepository.findByStatusAndRespondentIdOrderByCreatedAtDesc(status, respondentId);
        } else if (status != null && orderId != null) {
            complaints = complaintRepository.findByStatusAndOrderIdOrderByCreatedAtDesc(status, orderId);
        } else if (respondentId != null && orderId != null) {
            complaints = complaintRepository.findByRespondentIdAndOrderIdOrderByCreatedAtDesc(respondentId, orderId);
        } else if (status != null) {
            complaints = complaintRepository.findByStatus(status);
        } else if (respondentId != null) {
            complaints = complaintRepository.findByRespondentId(respondentId);
        } else {
            complaints = complaintRepository.findByOrderId(orderId);
        }
        
        return complaints.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * ADMIN: Get pending complaints for dashboard
     */
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getPendingComplaints() {
        logger.info("Getting pending complaints for admin dashboard");
        
        List<ComplaintStatus> pendingStatuses = Arrays.asList(
            ComplaintStatus.Pending, 
            ComplaintStatus.Reviewing, 
            ComplaintStatus.InProgress
        );
        
        List<Complaint> complaints = complaintRepository.findByStatusInOrderByCreatedAtDesc(pendingStatuses);
        return complaints.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * ADMIN: Get complaint details by ID
     */
    @Transactional(readOnly = true)
    public ComplaintResponse getComplaintById(Long complaintId) {
        logger.info("Getting complaint details for ID: {}", complaintId);
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Khiếu nại không tồn tại với ID: " + complaintId));
        
        return convertToResponse(complaint);
    }

    /**
     * ADMIN: Start reviewing a complaint
     */
    @Transactional
    public void startReview(Long complaintId) {
        logger.info("Admin starting review for complaint {}", complaintId);
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Khiếu nại không tồn tại với ID: " + complaintId));
        
        if (complaint.getStatus() != ComplaintStatus.Pending) {
            throw new IllegalStateException("Chỉ có thể xem xét khiếu nại đang chờ xử lý");
        }
        
        complaint.setStatus(ComplaintStatus.Reviewing);
        complaint.setUpdatedAt(Timestamp.from(Instant.now()));
        
        complaintRepository.save(complaint);
        logger.info("Complaint {} is now being reviewed", complaintId);
    }

    /**
     * ADMIN: Respond to and resolve complaint
     */
    @Transactional
    public void respondToComplaint(Long complaintId, AdminComplaintResponse response) {
        logger.info("Admin responding to complaint {}", complaintId);
        
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Khiếu nại không tồn tại với ID: " + complaintId));
        
        // Validate status transition
        if (complaint.getStatus() == ComplaintStatus.Closed) {
            throw new IllegalStateException("Không thể cập nhật khiếu nại đã đóng");
        }
        
        complaint.setStatus(response.getStatus());
        complaint.setAdminResponse(response.getAdminResponse());
        complaint.setUpdatedAt(Timestamp.from(Instant.now()));
        
        complaintRepository.save(complaint);
        logger.info("Complaint {} has been updated to status: {}", complaintId, response.getStatus());
    }    /**
     * Update complaint status only
     */
    @Transactional
    public void updateComplaintStatus(Long complaintId, String statusString) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Khiếu nại không tồn tại với ID: " + complaintId));
        try {
            ComplaintStatus newStatus = ComplaintStatus.valueOf(statusString);
            complaint.setStatus(newStatus);
            complaint.setUpdatedAt(Timestamp.from(Instant.now()));
            complaintRepository.save(complaint);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusString + 
                ". Trạng thái hợp lệ là: " + ComplaintStatus.getValidStatusesString(), e);
        }
    }

    /**
     * Get all complaints (for backward compatibility)
     */
    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    /**
     * Review complaint (for backward compatibility)
     */
    @Transactional
    public void reviewComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khiếu nại với ID: " + complaintId));

        // Set status to Reviewing when reviewing
        complaint.setStatus(ComplaintStatus.Reviewing);
        complaint.setUpdatedAt(Timestamp.from(Instant.now()));
        complaintRepository.save(complaint);
    }

    /**
     * ADMIN: Get complaint statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getComplaintStatistics() {
        logger.info("Getting complaint statistics");
        
        Map<String, Object> stats = new HashMap<>();
        
        // Status statistics        stats.put("totalComplaints", complaintRepository.count());
        stats.put("pendingComplaints", complaintRepository.countByStatus(ComplaintStatus.Pending));
        stats.put("reviewingComplaints", complaintRepository.countByStatus(ComplaintStatus.Reviewing));
        stats.put("inProgressComplaints", complaintRepository.countByStatus(ComplaintStatus.InProgress));
        stats.put("resolvedComplaints", complaintRepository.countByStatus(ComplaintStatus.Resolved));
        stats.put("rejectedComplaints", complaintRepository.countByStatus(ComplaintStatus.Rejected));
        
        // Calculate unresolved complaints (sum of pending, reviewing, inProgress)
        long unresolvedCount = complaintRepository.countByStatus(ComplaintStatus.Pending) +
                              complaintRepository.countByStatus(ComplaintStatus.Reviewing) +
                              complaintRepository.countByStatus(ComplaintStatus.InProgress);
        stats.put("unresolvedComplaints", unresolvedCount);
        
        return stats;
    }

    /**
     * Get complaints by specific criteria for reports
     */
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsByDateRange(Timestamp startDate, Timestamp endDate) {
        logger.info("Getting complaints between {} and {}", startDate, endDate);
        
        List<Complaint> complaints = complaintRepository.findByCreatedAtBetween(startDate, endDate);
        return complaints.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get complaints against a specific user/seller
     */
    @Transactional(readOnly = true)
    public List<ComplaintResponse> getComplaintsAgainstUser(Integer userId) {
        logger.info("Getting complaints against user: {}", userId);
        
        List<Complaint> complaints = complaintRepository.findByRespondentId(userId);
        return complaints.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
    }

    // Helper methods

    /**
     * Convert Complaint entity to ComplaintResponse DTO
     */
    private ComplaintResponse convertToResponse(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setComplaintId(complaint.getComplaintId());
        response.setOrderId(complaint.getOrderId());
        response.setRespondentId(complaint.getRespondentId());
        response.setReason(complaint.getReason());
        response.setStatus(complaint.getStatus());
        response.setStatusDisplay(complaint.getStatus().getDisplayName());
        response.setAdminResponse(complaint.getAdminResponse());
        response.setCreatedAt(complaint.getCreatedAt());
        response.setUpdatedAt(complaint.getUpdatedAt());
        
        // Set user name if available
        if (complaint.getRespondent() != null) {
            response.setRespondentName(complaint.getRespondent().getFirstName() + " " + 
                                     complaint.getRespondent().getLastName());
        }
        
        return response;
    }
}
