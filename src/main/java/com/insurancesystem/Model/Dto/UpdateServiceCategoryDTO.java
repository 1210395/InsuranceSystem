package com.insurancesystem.Model.Dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateServiceCategoryDTO {
    private String name;
    private String nameAr;
    private String description;
    private String icon;
    private String color;
    private Boolean isActive;
    private Integer displayOrder;
}
