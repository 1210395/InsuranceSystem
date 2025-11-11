package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "radiology_requests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RadiologyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false)
    private String testName; // اسم الفحص الإشعاعي (يدخله الراديولوجي أو الدكتور)

    private String notes; // ملاحظات من الطبيب

    private String resultUrl; // رابط أو مسار ملف النتيجة

    @Enumerated(EnumType.STRING)
    private LabRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // 🟢 الأسعار (يدخلها الراديولوجي مباشرة)
    private Double enteredPrice; // السعر الذي يدخله الراديولوجي

    private Instant createdAt;
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radiologist_id")
    private Client radiologist;

}
