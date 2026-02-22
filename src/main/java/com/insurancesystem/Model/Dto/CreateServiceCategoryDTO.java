package com.insurancesystem.Model.Dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateServiceCategoryDTO {

    @NotBlank(message = "Category name is required")
    private String name;

    private String nameAr;

    private String description;

    private String icon;

    private String color;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Integer displayOrder = 0;
}
