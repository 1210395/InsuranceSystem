package com.insurancesystem.Model.Dto;

import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGlobalPolicyDTO {
    private String name;
    private String version;
    private String description;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private GlobalPolicyStatus status;
}
