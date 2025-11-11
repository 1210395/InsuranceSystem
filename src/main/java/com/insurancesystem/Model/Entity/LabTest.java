package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LabTest {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String testName; // اسم الفحص (دم، صورة أشعة، الخ)

    @Column(nullable = false)
    private Double unionPrice; // السعر النقابي (الحد الأدنى المقبول)

    @Column(nullable = false)
    private Double labPrice; // السعر الذي يدخله المختبر

    private String description; // وصف الفحص

    @Column(nullable = false)
    private Boolean isActive = true; // هل الفحص نشط أم لا

    private Instant createdAt;
    private Instant updatedAt;

}

