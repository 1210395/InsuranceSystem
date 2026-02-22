package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.FrequencyPeriod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceCoverageDTO {

    private UUID policyId;

    private UUID categoryId;

    @NotBlank(message = "Service name is required")
    private String serviceName;

    private String medicalName;

    private String description;

    @Builder.Default
    private CoverageStatusType coverageStatus = CoverageStatusType.COVERED;

    @Builder.Default
    private BigDecimal coveragePercent = BigDecimal.valueOf(100.00);

    @NotNull(message = "Standard price is required")
    private BigDecimal standardPrice;

    private BigDecimal maxCoverageAmount;

    private Integer minAge;

    private Integer maxAge;

    @Builder.Default
    private AllowedGender allowedGender = AllowedGender.ALL;

    @Builder.Default
    private Boolean requiresReferral = false;

    private Integer frequencyLimit;

    private FrequencyPeriod frequencyPeriod;

    @Builder.Default
    private Boolean isActive = true;
}
