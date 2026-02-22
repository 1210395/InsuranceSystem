package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.GlobalPolicy;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GlobalPolicyRepository extends JpaRepository<GlobalPolicy, UUID> {

    Optional<GlobalPolicy> findByStatus(GlobalPolicyStatus status);

    @Query("SELECT gp FROM GlobalPolicy gp WHERE gp.status = 'ACTIVE'")
    Optional<GlobalPolicy> findActivePolicy();

    @Query("SELECT gp FROM GlobalPolicy gp WHERE gp.status = 'DRAFT'")
    Optional<GlobalPolicy> findDraftPolicy();

    boolean existsByStatus(GlobalPolicyStatus status);

    Optional<GlobalPolicy> findByName(String name);

    long countByStatus(GlobalPolicyStatus status);
}
