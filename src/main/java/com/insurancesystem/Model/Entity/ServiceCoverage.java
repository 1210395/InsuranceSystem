package com.insurancesystem.Model.Entity;

import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.FrequencyPeriod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "service_coverage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"policy_id", "service_name"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCoverage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "policy_id")
    private GlobalPolicy globalPolicy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ServiceCategory category;

    // Service Identification
    @Column(name = "service_name", nullable = false, length = 160)
    private String serviceName;

    @Column(name = "medical_name", length = 200)
    private String medicalName;

    @Column(columnDefinition = "text")
    private String description;

    // Coverage Rules
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "coverage_status", nullable = false, length = 20)
    private CoverageStatusType coverageStatus = CoverageStatusType.COVERED;

    @Builder.Default
    @Column(name = "coverage_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal coveragePercent = BigDecimal.valueOf(100.00);

    @Column(name = "standard_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal standardPrice;

    @Column(name = "max_coverage_amount", precision = 12, scale = 2)
    private BigDecimal maxCoverageAmount;

    // Restrictions
    @Column(name = "min_age")
    private Integer minAge;

    @Column(name = "max_age")
    private Integer maxAge;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "allowed_gender", length = 10)
    private AllowedGender allowedGender = AllowedGender.ALL;

    @Builder.Default
    @Column(name = "requires_referral", nullable = false)
    private Boolean requiresReferral = false;

    // Usage Limits
    @Column(name = "frequency_limit")
    private Integer frequencyLimit;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency_period", length = 20)
    private FrequencyPeriod frequencyPeriod;

    // Metadata
    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

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
