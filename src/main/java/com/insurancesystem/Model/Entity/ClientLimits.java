package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "client_limits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientLimits {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id", unique = true)
    private GlobalPolicy globalPolicy;

    // Visit Limits
    @Column(name = "max_visits_per_month")
    private Integer maxVisitsPerMonth;

    @Column(name = "max_visits_per_year")
    private Integer maxVisitsPerYear;

    // Spending Limits
    @Column(name = "max_spending_per_month", precision = 12, scale = 2)
    private BigDecimal maxSpendingPerMonth;

    @Column(name = "max_spending_per_year", precision = 12, scale = 2)
    private BigDecimal maxSpendingPerYear;

    // Deductibles
    @Builder.Default
    @Column(name = "annual_deductible", precision = 12, scale = 2)
    private BigDecimal annualDeductible = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
