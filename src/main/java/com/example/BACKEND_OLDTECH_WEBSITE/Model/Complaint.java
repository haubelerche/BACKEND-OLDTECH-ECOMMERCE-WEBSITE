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
    private Integer complaintId;


    private Integer complainantId;


    private Integer respondentId;


    private Integer orderId;


    private String reason;




    private ComplaintStatusEnum status;

    private String adminResponse;

    private Timestamp createdAt;

    private Timestamp updatedAt;

}