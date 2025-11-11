package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabRequestDTO {

    private UUID id;

    private String testName;

    private String notes;

    private String resultUrl;

    private String status;

    private UUID doctorId;
    private String doctorName;

    private UUID memberId;
    private String memberName;

    private UUID labTechId;
    private String labTechName;

    // 🟢 الفحص
    private UUID testId;
    private String testName_test; // اسم الفحص من جدول tests
    private Double unionPrice; // السعر النقابي

    // 🟢 الأسعار
    private Double enteredPrice; // السعر الذي دخله اللاب تِك
    private Double approvedPrice; // السعر المعتمد النهائي

    private long total;
    private long pending;
    private long completed;

    private Instant createdAt;
    private Instant updatedAt;

}
