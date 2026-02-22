package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalPolicyDTO {
    private UUID id;
    private String name;
    private String version;
    private String description;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private GlobalPolicyStatus status;
    private ClientLimitsDTO clientLimits;
    private List<CategoryLimitsDTO> categoryLimits;
    private Integer servicesCount;
    private Integer categoriesCount;
    private String createdAt;
    private String updatedAt;
}
