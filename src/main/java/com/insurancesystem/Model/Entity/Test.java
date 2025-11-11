package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tests")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Test {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String testName; // اسم الفحص (دم، صورة أشعة، إلخ)

    @Column(nullable = false)
    private Double unionPrice; // السعر النقابي

    private Instant createdAt;
    private Instant updatedAt;

}

