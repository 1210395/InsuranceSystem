package com.insurancesystem.Model.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "client_usage",
        uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "year", "month"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientUsage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    @Builder.Default
    @Column(name = "total_visits")
    private Integer totalVisits = 0;

    @Builder.Default
    @Column(name = "total_spending", precision = 12, scale = 2)
    private BigDecimal totalSpending = BigDecimal.ZERO;

    @Column(name = "last_updated")
    private Instant lastUpdated;

    @PrePersist
    void prePersist() {
        this.lastUpdated = Instant.now();
    }

    @PreUpdate
    void preUpdate() {
        this.lastUpdated = Instant.now();
    }
}
