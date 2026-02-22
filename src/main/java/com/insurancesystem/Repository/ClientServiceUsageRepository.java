package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.ClientServiceUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientServiceUsageRepository extends JpaRepository<ClientServiceUsage, UUID> {

    Optional<ClientServiceUsage> findByClientIdAndServiceCoverageIdAndYearAndMonth(
            UUID clientId, UUID serviceCoverageId, Integer year, Integer month);

    List<ClientServiceUsage> findByClientIdAndYear(UUID clientId, Integer year);

    List<ClientServiceUsage> findByClientIdAndYearAndMonth(UUID clientId, Integer year, Integer month);

    @Query("SELECT COALESCE(SUM(csu.usageCount), 0) FROM ClientServiceUsage csu " +
           "WHERE csu.client.id = :clientId AND csu.serviceCoverage.id = :serviceId " +
           "AND csu.year = :year AND (:month IS NULL OR csu.month = :month)")
    Integer getServiceUsageCount(
            @Param("clientId") UUID clientId,
            @Param("serviceId") UUID serviceId,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(csu.usageCount), 0) FROM ClientServiceUsage csu " +
           "WHERE csu.client.id = :clientId AND csu.category.id = :categoryId " +
           "AND csu.year = :year AND (:month IS NULL OR csu.month = :month)")
    Integer getCategoryUsageCount(
            @Param("clientId") UUID clientId,
            @Param("categoryId") UUID categoryId,
            @Param("year") Integer year,
            @Param("month") Integer month);

    @Query("SELECT COALESCE(SUM(csu.amountUsed), 0) FROM ClientServiceUsage csu " +
           "WHERE csu.client.id = :clientId AND csu.category.id = :categoryId " +
           "AND csu.year = :year AND (:month IS NULL OR csu.month = :month)")
    BigDecimal getCategorySpending(
            @Param("clientId") UUID clientId,
            @Param("categoryId") UUID categoryId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
