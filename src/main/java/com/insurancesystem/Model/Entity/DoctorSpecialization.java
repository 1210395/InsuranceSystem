package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctor_specialization")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorSpecialization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "consultation_price", nullable = false)
    private Double consultationPrice;

    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Column(name = "gender_restriction")
    private String genderRestriction;

    @Column(name = "diagnoses", columnDefinition = "text[]")
    private String[] diagnoses;

    @Column(name = "treatment_plans", columnDefinition = "text[]")
    private String[] treatmentPlans;
}
