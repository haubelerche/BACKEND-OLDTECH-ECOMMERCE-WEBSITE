package com.example.BACKEND_OLDTECH_WEBSITE.Model;

import com.example.BACKEND_OLDTECH_WEBSITE.Enums.ComplaintStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "complaint")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "complaint_id")
    private Integer complaintId;

    @ManyToOne
    @JoinColumn(name = "complainant_id")
    private User complainantId;

    @ManyToOne
    @JoinColumn(name = "respondent_id")
    private User respondentId;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order orderId;

    @Column(name = "reason", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String reason;

    @Column(name = "status")
    private ComplaintStatusEnum status;

    @Column(name = "admin_response", columnDefinition = "MEDIUMTEXT")
    @NotBlank(message = "Thông tin không được để trống")
    private String resolutionNotes;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}