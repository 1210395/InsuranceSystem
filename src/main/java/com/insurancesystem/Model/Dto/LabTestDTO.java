package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabTestDTO {

    private UUID id;

    private String testName; // اسم الفحص

    private Double unionPrice; // السعر النقابي

    private Double labPrice; // السعر الذي يدخله المختبر

    private String description; // وصف الفحص

    private Boolean isActive; // هل الفحص نشط

    private Instant createdAt;

    private Instant updatedAt;

}

