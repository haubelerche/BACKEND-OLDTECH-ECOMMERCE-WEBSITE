package com.example.BACKEND_OLDTECH_WEBSITE.Repository;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    // Basic status queries
    List<Complaint> findByStatus(ComplaintStatus status);
    
    // Find by respondent (for complaints against users/sellers)
    List<Complaint> findByRespondentId(Integer respondentId);
    
    // Find by complainant (for complaints filed by users)
    List<Complaint> findByComplainantId(Integer complainantId);
    
    // Find by order
    List<Complaint> findByOrderId(Integer orderId);
    
    // Find pending complaints for admin dashboard (using method naming)
    List<Complaint> findByStatusInOrderByCreatedAtDesc(List<ComplaintStatus> statuses);
    
    // Find complaints by date range
    List<Complaint> findByCreatedAtBetween(Timestamp startDate, Timestamp endDate);
    
    // Find complaints by status and respondent (replaces complex query)
    List<Complaint> findByStatusAndRespondentIdOrderByCreatedAtDesc(ComplaintStatus status, Integer respondentId);
    
    // Find complaints by status and order (replaces complex query)  
    List<Complaint> findByStatusAndOrderIdOrderByCreatedAtDesc(ComplaintStatus status, Integer orderId);
    
    // Find complaints by respondent and order
    List<Complaint> findByRespondentIdAndOrderIdOrderByCreatedAtDesc(Integer respondentId, Integer orderId);
    
    // Count methods
    long countByStatus(ComplaintStatus status);
    long countByRespondentId(Integer respondentId);
    long countByComplainantId(Integer complainantId);
    
    // Find unresolved complaints using method naming
    List<Complaint> findByStatusInOrderByCreatedAtDesc(ComplaintStatus... statuses);
}