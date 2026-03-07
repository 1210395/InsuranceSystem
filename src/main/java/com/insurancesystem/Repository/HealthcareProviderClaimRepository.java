package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;

import com.insurancesystem.Model.Entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface HealthcareProviderClaimRepository extends JpaRepository<HealthcareProviderClaim, UUID> {

    List<HealthcareProviderClaim> findByHealthcareProvider(Client provider);
    
    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        WHERE c.healthcareProvider.id = :providerId
    """)
    List<HealthcareProviderClaim> findByHealthcareProviderId(@Param("providerId") UUID providerId);

    List<HealthcareProviderClaim> findByStatus(ClaimStatus status);

    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        JOIN FETCH c.healthcareProvider
        LEFT JOIN FETCH c.policy
        WHERE c.status = :status
    """)
    List<HealthcareProviderClaim> findByStatusWithProvider(@Param("status") ClaimStatus status);

    @Query("SELECT c FROM HealthcareProviderClaim c JOIN FETCH c.healthcareProvider LEFT JOIN FETCH c.policy")
    List<HealthcareProviderClaim> findAllWithProvider();

    @Query(value = "SELECT c FROM HealthcareProviderClaim c JOIN FETCH c.healthcareProvider LEFT JOIN FETCH c.policy",
           countQuery = "SELECT COUNT(c) FROM HealthcareProviderClaim c")
    Page<HealthcareProviderClaim> findAllWithProvider(Pageable pageable);

    @Query(value = """
        SELECT c FROM HealthcareProviderClaim c
        JOIN FETCH c.healthcareProvider
        LEFT JOIN FETCH c.policy
        WHERE c.status = :status
    """, countQuery = "SELECT COUNT(c) FROM HealthcareProviderClaim c WHERE c.status = :status")
    Page<HealthcareProviderClaim> findByStatusWithProvider(@Param("status") ClaimStatus status, Pageable pageable);

    void deleteAllByPolicy(Policy policy);


    long countByStatus(ClaimStatus status);

    long countByEmergencyTrueAndStatusIn(List<ClaimStatus> statuses);

    @Query("""
    SELECT c.healthcareProvider.id AS providerId,
           c.healthcareProvider.fullName AS providerName,
           COALESCE(SUM(c.insuranceCoveredAmount), 0) AS totalAmount,
           c.healthcareProvider.requestedRole AS providerType,
           COUNT(c) AS claimCount
    FROM HealthcareProviderClaim c
    WHERE c.status IN (
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
    )
    AND c.healthcareProvider.requestedRole IN (com.insurancesystem.Model.Entity.Enums.RoleName.DOCTOR,
                   com.insurancesystem.Model.Entity.Enums.RoleName.PHARMACIST,
                   com.insurancesystem.Model.Entity.Enums.RoleName.LAB_TECH,
                   com.insurancesystem.Model.Entity.Enums.RoleName.RADIOLOGIST)
    GROUP BY c.healthcareProvider.id, c.healthcareProvider.fullName, c.healthcareProvider.requestedRole
    ORDER BY totalAmount DESC
""")
    List<Object[]> findTopProviders();

    @Query("""
    SELECT c FROM HealthcareProviderClaim c
    WHERE c.healthcareProvider.id = :providerId
    AND c.status IN (
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
    )
    AND (:fromDate IS NULL OR c.serviceDate >= :fromDate)
    AND (:toDate IS NULL OR c.serviceDate <= :toDate)
    ORDER BY c.serviceDate DESC
""")
    List<HealthcareProviderClaim> findProviderExpenses(
            @Param("providerId") UUID providerId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate
    );



    @Query("""
    SELECT COALESCE(SUM(c.insuranceCoveredAmount),0)
    FROM HealthcareProviderClaim c
    WHERE c.status IN (
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
    )
""")
    Double getTotalApprovedAmount();


    @Query("SELECT COALESCE(SUM(c.insuranceCoveredAmount),0) FROM HealthcareProviderClaim c WHERE c.status = :status")
    double sumInsuranceCoveredByStatus(@Param("status") ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);

    List<HealthcareProviderClaim> findByStatusIn(List<ClaimStatus> statuses);
    
    @Query("""
        SELECT c FROM HealthcareProviderClaim c
        JOIN FETCH c.healthcareProvider
        LEFT JOIN FETCH c.policy
        WHERE c.status IN :statuses
    """)
    List<HealthcareProviderClaim> findByStatusInWithProvider(@Param("statuses") List<ClaimStatus> statuses);
    @Query("""
    SELECT c FROM HealthcareProviderClaim c
    WHERE c.status IN (
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
    )
""")
    List<HealthcareProviderClaim> findAllApprovedClaims();


    @Query("""
        SELECT c.doctorName, COUNT(c)
        FROM HealthcareProviderClaim c
        WHERE c.doctorName IS NOT NULL AND c.doctorName <> ''
        GROUP BY c.doctorName
        ORDER BY COUNT(c) DESC
    """)
    List<Object[]> findTopDoctorsByClaims();

    @Query("""
SELECT DISTINCT c FROM HealthcareProviderClaim c
JOIN c.healthcareProvider.roles r
WHERE (:status IS NULL OR c.status = :status)
AND (:roleName IS NULL OR r.name = :roleName)
AND (:from IS NULL OR c.serviceDate >= :from)
AND (:to IS NULL OR c.serviceDate <= :to)
""")
    List<HealthcareProviderClaim> filterClaims(
            @Param("status") ClaimStatus status,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("roleName") RoleName roleName
    );
    List<HealthcareProviderClaim> findByClientId(UUID clientId);

    List<HealthcareProviderClaim> findByClientIdIn(List<UUID> clientIds);

    @Query("SELECT COUNT(c) > 0 FROM HealthcareProviderClaim c " +
           "JOIN c.healthcareProvider hp " +
           "JOIN SearchProfile sp ON sp.owner.id = hp.id " +
           "WHERE c.clientId = :clientId " +
           "AND sp.owner.specialization = :specialization " +
           "AND c.serviceDate > :sinceDate " +
           "AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL, " +
           "com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)")
    boolean existsByClientIdAndSpecializationAndServiceDateAfter(
        @Param("clientId") UUID clientId,
        @Param("specialization") String specialization,
        @Param("sinceDate") LocalDate sinceDate);

    @Query("SELECT COUNT(DISTINCT c.clientId) FROM HealthcareProviderClaim c WHERE c.clientId IS NOT NULL")
    long countDistinctClients();

    // ============= Usage Tracking Queries for GlobalPolicy =============

    @Query("""
        SELECT COUNT(c) FROM HealthcareProviderClaim c
        WHERE c.clientId = :clientId
        AND YEAR(c.serviceDate) = :year
        AND MONTH(c.serviceDate) = :month
        AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL,
                            com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)
    """)
    int countVisitsInMonth(@Param("clientId") UUID clientId, @Param("year") int year, @Param("month") int month);

    @Query("""
        SELECT COUNT(c) FROM HealthcareProviderClaim c
        WHERE c.clientId = :clientId
        AND YEAR(c.serviceDate) = :year
        AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL,
                            com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)
    """)
    int countVisitsInYear(@Param("clientId") UUID clientId, @Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(c.insuranceCoveredAmount), 0) FROM HealthcareProviderClaim c
        WHERE c.clientId = :clientId
        AND YEAR(c.serviceDate) = :year
        AND MONTH(c.serviceDate) = :month
        AND c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID)
    """)
    BigDecimal sumSpendingInMonth(@Param("clientId") UUID clientId, @Param("year") int year, @Param("month") int month);

    @Query("""
        SELECT COALESCE(SUM(c.insuranceCoveredAmount), 0) FROM HealthcareProviderClaim c
        WHERE c.clientId = :clientId
        AND YEAR(c.serviceDate) = :year
        AND c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID)
    """)
    BigDecimal sumSpendingInYear(@Param("clientId") UUID clientId, @Param("year") int year);

    // System-wide counts (no clientId filter) for quick stats
    @Query("""
        SELECT COUNT(c) FROM HealthcareProviderClaim c
        WHERE YEAR(c.serviceDate) = :year
        AND MONTH(c.serviceDate) = :month
        AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL,
                            com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)
    """)
    int countAllVisitsInMonth(@Param("year") int year, @Param("month") int month);

    @Query("""
        SELECT COUNT(c) FROM HealthcareProviderClaim c
        WHERE YEAR(c.serviceDate) = :year
        AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL,
                            com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)
    """)
    int countAllVisitsInYear(@Param("year") int year);

    @Query("""
        SELECT COALESCE(SUM(c.insuranceCoveredAmount), 0) FROM HealthcareProviderClaim c
        WHERE YEAR(c.serviceDate) = :year
        AND MONTH(c.serviceDate) = :month
        AND c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID)
    """)
    BigDecimal sumAllSpendingInMonth(@Param("year") int year, @Param("month") int month);

    @Query("""
        SELECT COALESCE(SUM(c.insuranceCoveredAmount), 0) FROM HealthcareProviderClaim c
        WHERE YEAR(c.serviceDate) = :year
        AND c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
                        com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID)
    """)
    BigDecimal sumAllSpendingInYear(@Param("year") int year);

    @Query("""
        SELECT COUNT(c) FROM HealthcareProviderClaim c
        WHERE c.clientId = :clientId
        AND LOWER(c.description) = LOWER(:serviceName)
        AND c.serviceDate >= :sinceDate
        AND c.status NOT IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL,
                            com.insurancesystem.Model.Entity.Enums.ClaimStatus.RETURNED_TO_PROVIDER)
    """)
    int countServiceUsageSince(@Param("clientId") UUID clientId, @Param("serviceName") String serviceName, @Param("sinceDate") LocalDate sinceDate);

    // Get client usage summary for a period (returns: clientId, clientName, count, insurancePaid, totalAmount, clientPaid)
    @Query("""
        SELECT c.clientId, c.clientName, COUNT(c),
               COALESCE(SUM(c.insuranceCoveredAmount), 0),
               COALESCE(SUM(c.amount), 0),
               COALESCE(SUM(c.clientPayAmount), 0)
        FROM HealthcareProviderClaim c
        WHERE c.serviceDate BETWEEN :fromDate AND :toDate
        AND c.status IN (
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
        )
        GROUP BY c.clientId, c.clientName
        ORDER BY SUM(c.insuranceCoveredAmount) DESC
    """)
    List<Object[]> getClientUsageSummary(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Get service usage breakdown (returns: description, count, insurancePaid, totalAmount)
    @Query("""
        SELECT c.description, COUNT(c),
               COALESCE(SUM(c.insuranceCoveredAmount), 0),
               COALESCE(SUM(c.amount), 0)
        FROM HealthcareProviderClaim c
        WHERE c.serviceDate BETWEEN :fromDate AND :toDate
        AND c.status IN (
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL,
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAYMENT_PENDING,
            com.insurancesystem.Model.Entity.Enums.ClaimStatus.PAID
        )
        GROUP BY c.description
        ORDER BY COUNT(c) DESC
    """)
    List<Object[]> getServiceUsageBreakdown(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);

    // Fix #60: Check for duplicate claims by roleSpecificData content
    @Query("SELECT COUNT(c) > 0 FROM HealthcareProviderClaim c " +
           "WHERE c.roleSpecificData LIKE CONCAT('%', :referenceId, '%') " +
           "AND c.status NOT IN :excludedStatuses")
    boolean existsByRoleSpecificDataContainingAndStatusNotIn(
            @Param("referenceId") String referenceId,
            @Param("excludedStatuses") List<ClaimStatus> excludedStatuses);

}

