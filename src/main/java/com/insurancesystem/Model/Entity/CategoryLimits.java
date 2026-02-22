package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "category_limits",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "category_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryLimits {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private GlobalPolicy globalPolicy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private ServiceCategory category;

    @Column(name = "max_visits_per_month")
    private Integer maxVisitsPerMonth;

    @Column(name = "max_visits_per_year")
    private Integer maxVisitsPerYear;

    @Column(name = "max_spending_per_month", precision = 12, scale = 2)
    private BigDecimal maxSpendingPerMonth;

    @Column(name = "max_spending_per_year", precision = 12, scale = 2)
    private BigDecimal maxSpendingPerYear;

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
