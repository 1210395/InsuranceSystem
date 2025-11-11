package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Claim {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Client member; // صاحب المطالبة

    @ManyToOne(optional = false)
    @JoinColumn(name = "policy_id")
    private Policy policy; // البوليصة

    @Column(nullable = false)
    private String description;

    private String diagnosis;
    private String treatmentDetails;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate serviceDate;

    private String providerName;
    private String doctorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClaimStatus status;

    @Column(name = "invoice_image_path")
    private String invoiceImagePath;

    private Instant submittedAt;
    private Instant medicalReviewedAt;
    private Instant adminReviewedAt;
    private Instant approvedAt;
    private Instant rejectedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    // تتبع من راجع الموافقة الطبية والإدارية
    @ManyToOne
    @JoinColumn(name = "medical_reviewer_id")
    private Client medicalReviewer;

    @ManyToOne
    @JoinColumn(name = "admin_reviewer_id")
    private Client adminReviewer;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null)
            this.status = ClaimStatus.PENDING;
    }
}
