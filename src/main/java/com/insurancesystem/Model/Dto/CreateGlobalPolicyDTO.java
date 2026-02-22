package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGlobalPolicyDTO {

    @NotBlank(message = "Policy name is required")
    private String name;

    @NotBlank(message = "Version is required")
    private String version;

    private String description;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}
