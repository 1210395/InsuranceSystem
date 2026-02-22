package com.insurancesystem.Model.Dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateClientLimitsDTO {
    private Integer maxVisitsPerMonth;
    private Integer maxVisitsPerYear;
    private BigDecimal maxSpendingPerMonth;
    private BigDecimal maxSpendingPerYear;
    private BigDecimal annualDeductible;
}
