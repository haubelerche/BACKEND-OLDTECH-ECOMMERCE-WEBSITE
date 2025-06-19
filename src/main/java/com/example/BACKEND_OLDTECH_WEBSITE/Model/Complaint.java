package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatus;
import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintTypeEnum;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "complaint")
@Data
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Long complaintId;

    @Column(name = "order_id", columnDefinition = "INT UNSIGNED")
    private Integer orderId;

    @Column(name = "complainant_id", columnDefinition = "INT UNSIGNED")
    private Integer complainantId;

    @Column(name = "respondent_id", columnDefinition = "INT UNSIGNED")
    private Integer respondentId;

    @Column(name = "complaint_type")
    @Enumerated(EnumType.STRING)
    private ComplaintTypeEnum complaintType;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "admin_response", columnDefinition = "TEXT")
    private String adminResponse;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private ComplaintStatus status = ComplaintStatus.Pending;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondent_id", referencedColumnName = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private User respondent;
}