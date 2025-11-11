package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Prescription {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrescriptionStatus status;

    // ✅ الدكتور اللي أنشأ الوصفة
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Client doctor;

    // ✅ المريض (العضو) اللي إله الوصفة
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Client member;

    // ✅ الصيدلي اللي تعامل مع الوصفة (اختياري - بيصير موجود لما يوافق/يرفض)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pharmacist_id")
    private Client pharmacist;

    // 🆕 قائمة الأدوية في الوصفة (One-to-Many)
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PrescriptionItem> items = new ArrayList<>();

    // 💰 المجموع الكلي للوصفة
    private Double totalPrice;

    private String notes; // ملاحظات إضافية من الدكتور

    private Instant createdAt;
    private Instant updatedAt;
}