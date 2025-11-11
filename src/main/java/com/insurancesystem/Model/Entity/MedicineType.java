package com.insurancesystem.Model.Entity;



import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "medicine_types")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MedicineType {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(length = 500)
    private String description;
}