package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.FrequencyPeriod;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCoverageDTO {
    private UUID id;
    private UUID policyId;
    private String serviceName;
    private String medicalName;
    private String description;
    private ServiceCategoryDTO category;
    private CoverageStatusType coverageStatus;
    private BigDecimal coveragePercent;
    private BigDecimal standardPrice;
    private BigDecimal maxCoverageAmount;
    private Integer minAge;
    private Integer maxAge;
    private AllowedGender allowedGender;
    private Boolean requiresReferral;
    private Integer frequencyLimit;
    private FrequencyPeriod frequencyPeriod;
    private Boolean isActive;
}
