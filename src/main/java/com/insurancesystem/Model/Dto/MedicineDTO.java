package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicineDTO {
    private UUID id;
    private String name; // اسم الدواء
    private String scientificName; // المصطلح العلمي
    private Integer quantity; // عدد الحبات في العلبة
    private Double unionPrice; // سعر النقابة
    private String description; // وصف إضافي (اختياري)
    private Instant createdAt;
    private Instant updatedAt;
}