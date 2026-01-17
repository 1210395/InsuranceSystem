package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class HealthcareProviderClaimDTO {
    private UUID id;
    private UUID providerId;
    private String providerName;
    private UUID clientId;
    private String clientName;
    private String description;
    private Double amount;
    private LocalDate serviceDate;
    private String roleSpecificData;
    private ClaimStatus status;
    private String invoiceImagePath;
    private Instant submittedAt;
    private Instant approvedAt;
    private Instant rejectedAt;
    private Instant medicalReviewedAt;
    private String rejectionReason;

    // Additional fields for frontend display
    private String providerRole;
    private String diagnosis;
    private String treatmentDetails;
    private Boolean isFollowUp;

    // Client info fields
    private Integer clientAge;
    private String clientGender;
    private String clientEmployeeId;
    private String clientNationalId;
    private String clientFaculty;
    private String clientDepartment;

    // Provider info fields
    private String providerEmployeeId;
    private String providerNationalId;
    private String providerSpecialization;
    private String providerPharmacyCode;
    private String providerLabCode;
    private String providerRadiologyCode;

    // Family member fields
    private String familyMemberName;
    private String familyMemberRelation;
    private Integer familyMemberAge;
    private String familyMemberGender;
    private String familyMemberInsuranceNumber;
    private String familyMemberNationalId;
}

