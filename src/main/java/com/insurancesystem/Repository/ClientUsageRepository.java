package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.ClientUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientUsageRepository extends JpaRepository<ClientUsage, UUID> {

    Optional<ClientUsage> findByClientIdAndYearAndMonth(UUID clientId, Integer year, Integer month);

    Optional<ClientUsage> findByClientAndYearAndMonth(Client client, Integer year, Integer month);

    List<ClientUsage> findByClientId(UUID clientId);

    List<ClientUsage> findByClientIdAndYear(UUID clientId, Integer year);

    @Query("SELECT COALESCE(SUM(cu.totalVisits), 0) FROM ClientUsage cu " +
           "WHERE cu.client.id = :clientId AND cu.year = :year")
    Integer getTotalVisitsForYear(@Param("clientId") UUID clientId, @Param("year") Integer year);

    @Query("SELECT COALESCE(SUM(cu.totalSpending), 0) FROM ClientUsage cu " +
           "WHERE cu.client.id = :clientId AND cu.year = :year")
    BigDecimal getTotalSpendingForYear(@Param("clientId") UUID clientId, @Param("year") Integer year);

    @Query("SELECT cu FROM ClientUsage cu WHERE cu.client.id = :clientId " +
           "AND cu.year = :year AND cu.month = :month")
    Optional<ClientUsage> findCurrentPeriodUsage(
            @Param("clientId") UUID clientId,
            @Param("year") Integer year,
            @Param("month") Integer month);
}
