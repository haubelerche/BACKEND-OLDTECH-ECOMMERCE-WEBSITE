package com.example.BACKEND_OLDTECH_WEBSITE.ETL.Service;

import com.example.BACKEND_OLDTECH_WEBSITE.DTO.Admin.ComplaintManagementDTO;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import com.example.BACKEND_OLDTECH_WEBSITE.Repository.ComplaintRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintAdapterService {

    private final ComplaintRepository complaintRepository;

    public Page<ComplaintManagementDTO> getComplaints(String status, String type, String priority, Pageable pageable) {
        // Since the existing Complaint model has different structure, we adapt it
        Page<Complaint> complaints = complaintRepository.findAll(pageable);
        
        List<ComplaintManagementDTO> dtos = complaints.getContent().stream()
                .map(this::convertToManagementDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtos, pageable, complaints.getTotalElements());
    }

    public ComplaintManagementDTO getComplaintDetails(Long complaintId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        return convertToManagementDTO(complaint);
    }

    public void processComplaint(ComplaintManagementDTO.ComplaintActionRequest request) {
        Complaint complaint = complaintRepository.findById(request.getComplaintId())
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Update complaint based on action
        switch (request.getAction().toLowerCase()) {
            case "investigate":
                // Set status to investigating
                break;
            case "resolve":
                // Set status to resolved
                complaint.setAdminResponse(request.getResolution());
                break;
            case "dismiss":
                // Set status to dismissed
                break;
            case "escalate":
                // Set priority higher or status to escalated
                break;
        }
        
        complaint.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));
        complaintRepository.save(complaint);
    }

    public void assignComplaint(Long complaintId, Long adminId) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new RuntimeException("Complaint not found"));
        
        // Note: The existing complaint table might not have assigned_to field
        // You may need to add this field or handle assignment differently
        
        complaintRepository.save(complaint);
    }

    private ComplaintManagementDTO convertToManagementDTO(Complaint complaint) {
        ComplaintManagementDTO dto = new ComplaintManagementDTO();
        
        dto.setId(complaint.getComplaintId());
        dto.setComplainantId(complaint.getComplainantId() != null ? complaint.getComplainantId().longValue() : null);
        dto.setComplainedAgainstId(complaint.getRespondentId() != null ? complaint.getRespondentId().longValue() : null);
        dto.setOrderId(complaint.getOrderId() != null ? complaint.getOrderId().longValue() : null);
        dto.setComplaintType(complaint.getComplaintType() != null ? complaint.getComplaintType().name() : null);
        dto.setTitle("Complaint #" + complaint.getComplaintId()); // Generate title since original doesn't have it
        dto.setDescription(complaint.getReason());
        dto.setStatus(complaint.getStatus() != null ? complaint.getStatus().name() : null);
        dto.setPriority("medium"); // Default priority since original doesn't have it
        dto.setAdminNotes(complaint.getAdminResponse());
        dto.setResolution(complaint.getAdminResponse());
        
        // Convert timestamps
        if (complaint.getCreatedAt() != null) {
            dto.setCreatedAt(complaint.getCreatedAt().toLocalDateTime());
        }
        if (complaint.getUpdatedAt() != null) {
            dto.setUpdatedAt(complaint.getUpdatedAt().toLocalDateTime());
        }
        
        return dto;
    }
}
