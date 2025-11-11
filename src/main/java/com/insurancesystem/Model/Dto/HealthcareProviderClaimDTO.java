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
    private String rejectionReason;
}

