package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatusEnum;
import jakarta.persistence.*;

import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "complaint")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id", nullable = false)
    private Integer complaintId;

    @ManyToOne
    @JoinColumn(name = "complainant_id", nullable = false)
    private User complainantId;

    @ManyToOne
    @JoinColumn(name = "respondent_id", nullable = false)
    private User respondentId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Orders orderId;

    @Column(name = "reason", columnDefinition = "MEDIUMTEXT", nullable = false)
    
    private String reason;



    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ComplaintStatusEnum status;

    @Column(name = "admin_response", columnDefinition = "MEDIUMTEXT", nullable = false)
    private String adminResponse;

    @Column(name = "created_at", nullable = false)
    private Timestamp createdAt;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

}