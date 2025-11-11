package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequestDTO {

    private UUID id;

    private String testName; // اسم الفحص (يدخله الراديولوجي)

    private String notes; // ملاحظات من الدكتور

    private String resultUrl; // رابط النتيجة

    private String status; // حالة الطلب

    private UUID doctorId;
    private String doctorName;

    private UUID memberId;
    private String memberName;

    private UUID radiologistId;
    private String radiologistName;

    // 🟢 السعر (يدخله الراديولوجي مباشرة)
    private Double enteredPrice; // السعر الذي يدخله الراديولوجي

    private long total;
    private long pending;
    private long completed;

    private Instant createdAt;
    private Instant updatedAt;

}
