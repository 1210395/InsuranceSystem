package com.insurancesystem.Model.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLimitsDTO {
    private UUID id;
    private UUID policyId;
    private Integer maxVisitsPerMonth;
    private Integer maxVisitsPerYear;
    private BigDecimal maxSpendingPerMonth;
    private BigDecimal maxSpendingPerYear;
    private BigDecimal annualDeductible;
}
