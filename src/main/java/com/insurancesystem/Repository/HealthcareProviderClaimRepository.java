package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HealthcareProviderClaimRepository extends JpaRepository<HealthcareProviderClaim, UUID> {

    List<HealthcareProviderClaim> findByHealthcareProvider(Client provider);

    List<HealthcareProviderClaim> findByStatus(ClaimStatus status);

    long countByStatus(ClaimStatus status);

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = 'APPROVED'")
    double sumApprovedAmounts();

    @Query("SELECT SUM(c.amount) FROM HealthcareProviderClaim c WHERE c.status = 'APPROVED'")
    Double getTotalApprovedAmount();

    @Query("SELECT COALESCE(SUM(c.amount),0) FROM HealthcareProviderClaim c WHERE c.status = :status")
    double sumAmountByStatus(@Param("status") ClaimStatus status);

    // Find claims by multiple statuses (for medical review - PENDING + RETURNED_FOR_REVIEW)
    List<HealthcareProviderClaim> findByStatusIn(List<ClaimStatus> statuses);

    // Find claims for coordination review (APPROVED_MEDICAL or APPROVED_BY_MEDICAL)
    @Query("SELECT c FROM HealthcareProviderClaim c WHERE c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_MEDICAL, com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_BY_MEDICAL, com.insurancesystem.Model.Entity.Enums.ClaimStatus.AWAITING_COORDINATION_REVIEW)")
    List<HealthcareProviderClaim> findClaimsForCoordinationReview();

    // Find final decisions (APPROVED or REJECTED)
    @Query("SELECT c FROM HealthcareProviderClaim c WHERE c.status IN (com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED, com.insurancesystem.Model.Entity.Enums.ClaimStatus.APPROVED_FINAL, com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED, com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL)")
    List<HealthcareProviderClaim> findFinalDecisions();
}

