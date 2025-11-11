package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "medicines")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Medicine {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name; // اسم الدواء

    @Column(nullable = false)
    private String scientificName; // المصطلح العلمي

    @Column(nullable = false)
    private Integer quantity; // عدد الحبات/الكمية

    @Column(nullable = false)
    private Double unionPrice; // سعر النقابة

    private String description; // وصف إضافي (اختياري)

    private Instant createdAt;
    private Instant updatedAt;
}