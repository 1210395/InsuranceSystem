package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.ClientLimits;
import com.insurancesystem.Model.Entity.GlobalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientLimitsRepository extends JpaRepository<ClientLimits, UUID> {

    Optional<ClientLimits> findByGlobalPolicy(GlobalPolicy globalPolicy);

    Optional<ClientLimits> findByGlobalPolicyId(UUID policyId);

    void deleteByGlobalPolicyId(UUID policyId);
}
