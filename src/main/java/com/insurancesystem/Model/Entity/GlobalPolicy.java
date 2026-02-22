package com.insurancesystem.Model.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "global_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GlobalPolicy {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 20)
    private String version;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GlobalPolicyStatus status = GlobalPolicyStatus.DRAFT;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Client createdBy;

    @JsonIgnore
    @OneToOne(mappedBy = "globalPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private ClientLimits clientLimits;

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "globalPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ServiceCoverage> serviceCoverages = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "globalPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CategoryLimits> categoryLimits = new ArrayList<>();

    @JsonIgnore
    @Builder.Default
    @OneToMany(mappedBy = "globalPolicy", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PolicyVersion> versions = new ArrayList<>();

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
