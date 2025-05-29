package com.example.BACKEND_OLDTECH_WEBSITE.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ComplaintRepository;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Service
public class ComplaintService {
    @Autowired
    private ComplaintRepository complaintRepository;

    @Transactional(readOnly = true)
    public Complaint getComplaintById(Long complaintId) {
        return complaintRepository.findById(complaintId)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy khiếu nại với ID: " + complaintId));
    }

    @Transactional
    public void updateComplaintStatus(Long complaintId, String statusString) {
        Complaint complaint = getComplaintById(complaintId);
        try {
            ComplaintStatus newStatus = ComplaintStatus.valueOf(statusString);
            complaint.setStatus(newStatus);
            complaintRepository.save(complaint);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusString + ". Trạng thái hợp lệ là: Open, InProgress, Resolved, Closed.", e);
        }
    }

    @Transactional(readOnly = true)
    public List<Complaint> getAllComplaints() {
        return complaintRepository.findAll();
    }

    @Transactional
    public void reviewComplaint(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
            .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khiếu nại với ID: " + complaintId));

        // Set status to InProgress when reviewing
        complaint.setStatus(ComplaintStatus.Pending);
        complaint.setUpdatedAt(Timestamp.from(Instant.now()));
        complaintRepository.save(complaint);
    }
}
