package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.PolicyVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PolicyVersionRepository extends JpaRepository<PolicyVersion, UUID> {

    List<PolicyVersion> findByGlobalPolicyIdOrderByCreatedAtDesc(UUID policyId);

    @Query("SELECT pv FROM PolicyVersion pv WHERE pv.globalPolicy.id = :policyId ORDER BY pv.createdAt DESC")
    List<PolicyVersion> findAllVersionsByPolicy(@Param("policyId") UUID policyId);

    Optional<PolicyVersion> findByGlobalPolicyIdAndVersion(UUID policyId, String version);

    @Query("SELECT pv FROM PolicyVersion pv WHERE pv.globalPolicy.id = :policyId ORDER BY pv.createdAt DESC LIMIT 1")
    Optional<PolicyVersion> findLatestByPolicyId(@Param("policyId") UUID policyId);

    long countByGlobalPolicyId(UUID policyId);
}
