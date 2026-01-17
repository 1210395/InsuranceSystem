package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "healthcare_provider_claims")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthcareProviderClaim {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "provider_id")
    private Client healthcareProvider; // الطبيب/الصيدلي/فني المختبر/فني الأشعة

    @Column(name = "client_id")
    private UUID clientId; // معرف المريض

    @Column(name = "client_name")
    private String clientName; // اسم المريض

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description; // وصف الخدمة المقدمة

    @Column(nullable = false)
    private Double amount; // قيمة الخدمة

    @Column(nullable = false)
    private LocalDate serviceDate; // تاريخ تقديم الخدمة

    @Column(columnDefinition = "TEXT")
    private String roleSpecificData; // بيانات إضافية حسب الدور (JSON)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ClaimStatus status;

    @Column(name = "invoice_image_path")
    private String invoiceImagePath; // مسار صورة الفاتورة/الوثيقة

    private Instant submittedAt;

    private Instant approvedAt;

    private Instant rejectedAt;

    private Instant medicalReviewedAt;

    @Column(columnDefinition = "text")
    private String rejectionReason;

    // Additional fields for frontend display
    @Column(name = "provider_role")
    private String providerRole;

    @Column(columnDefinition = "text")
    private String diagnosis;

    @Column(name = "treatment_details", columnDefinition = "text")
    private String treatmentDetails;

    @Column(name = "is_follow_up")
    private Boolean isFollowUp;

    // Client info fields for display
    @Column(name = "client_age")
    private Integer clientAge;

    @Column(name = "client_gender")
    private String clientGender;

    @Column(name = "client_employee_id")
    private String clientEmployeeId;

    @Column(name = "client_national_id")
    private String clientNationalId;

    @Column(name = "client_faculty")
    private String clientFaculty;

    @Column(name = "client_department")
    private String clientDepartment;

    // Provider info fields for display
    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_employee_id")
    private String providerEmployeeId;

    @Column(name = "provider_national_id")
    private String providerNationalId;

    @Column(name = "provider_specialization")
    private String providerSpecialization;

    @Column(name = "provider_pharmacy_code")
    private String providerPharmacyCode;

    @Column(name = "provider_lab_code")
    private String providerLabCode;

    @Column(name = "provider_radiology_code")
    private String providerRadiologyCode;

    // Family member fields
    @Column(name = "family_member_name")
    private String familyMemberName;

    @Column(name = "family_member_relation")
    private String familyMemberRelation;

    @Column(name = "family_member_age")
    private Integer familyMemberAge;

    @Column(name = "family_member_gender")
    private String familyMemberGender;

    @Column(name = "family_member_insurance_number")
    private String familyMemberInsuranceNumber;

    @Column(name = "family_member_national_id")
    private String familyMemberNationalId;

    @PrePersist
    void onCreate() {
        this.submittedAt = Instant.now();
        if (this.status == null) {
            this.status = ClaimStatus.PENDING;
        }
    }
}

