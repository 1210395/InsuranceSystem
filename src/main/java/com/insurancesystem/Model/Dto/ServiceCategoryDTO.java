package com.insurancesystem.Model.Dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCategoryDTO {
    private UUID id;
    private String name;
    private String nameAr;
    private String description;
    private String icon;
    private String color;
    private Boolean isActive;
    private Integer displayOrder;
}
