package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "prescription_items")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrescriptionItem {

    @Id
    @GeneratedValue
    private UUID id;

    // 🔗 الوصفة اللي فيها هاد الدواء
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private Prescription prescription;

    // 💊 الدواء
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    // ✅ الجرعة (رقم) - عدد الحبات في المرة الواحدة
    @Column(nullable = false)
    private Integer dosage; // كان String، صار Integer

    // 🔢 عدد المرات في اليوم (للحساب)
    @Column(nullable = false)
    private Integer timesPerDay;

    // 💰 سعر الصيدلي (يدخله الصيدلي لما يوافق)
    private Double pharmacistPrice;

    // 💰 السعر النهائي المعتمد = min(pharmacistPrice, unionPrice)
    private Double finalPrice;

    // 📅 تاريخ انتهاء الدواء (يُحسب تلقائياً)
    private Instant expiryDate;

    private Instant createdAt;
    private Instant updatedAt;
}