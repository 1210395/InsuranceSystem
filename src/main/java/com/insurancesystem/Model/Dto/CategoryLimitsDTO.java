package com.insurancesystem.Model.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryLimitsDTO {
    private UUID id;
    private UUID policyId;
    private UUID categoryId;
    private String categoryName;
    private Integer maxVisitsPerMonth;
    private Integer maxVisitsPerYear;
    private BigDecimal maxSpendingPerMonth;
    private BigDecimal maxSpendingPerYear;
}
