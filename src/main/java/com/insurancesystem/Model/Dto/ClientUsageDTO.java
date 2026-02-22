package com.insurancesystem.Model.Dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUsageDTO {
    private UUID id;
    private UUID clientId;
    private String clientName;
    private Integer year;
    private Integer month;
    private Integer totalVisits;
    private BigDecimal totalSpending;
    private String lastUpdated;
}
