package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.GlobalPolicy;
import com.insurancesystem.Model.Entity.ServiceCategory;
import com.insurancesystem.Model.Entity.ServiceCoverage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCoverageRepository extends JpaRepository<ServiceCoverage, UUID> {

    List<ServiceCoverage> findByGlobalPolicy(GlobalPolicy globalPolicy);

    List<ServiceCoverage> findByGlobalPolicyId(UUID policyId);

    List<ServiceCoverage> findByGlobalPolicyIdAndCategoryId(UUID policyId, UUID categoryId);

    List<ServiceCoverage> findByGlobalPolicyIdAndIsActiveTrue(UUID policyId);

    Page<ServiceCoverage> findByGlobalPolicyId(UUID policyId, Pageable pageable);

    Optional<ServiceCoverage> findByGlobalPolicyIdAndServiceName(UUID policyId, String serviceName);

    @Query("SELECT sc FROM ServiceCoverage sc WHERE sc.globalPolicy.id = :policyId AND LOWER(sc.serviceName) = LOWER(:serviceName)")
    Optional<ServiceCoverage> findByGlobalPolicyIdAndServiceNameIgnoreCase(@Param("policyId") UUID policyId, @Param("serviceName") String serviceName);

    @Query(value = "SELECT * FROM service_coverage sc WHERE sc.policy_id = CAST(:policyId AS UUID) " +
           "AND (COALESCE(CAST(:categoryId AS VARCHAR), '') = '' OR sc.category_id = CAST(:categoryId AS UUID)) " +
           "AND (COALESCE(:searchTerm, '') = '' " +
           "OR LOWER(sc.service_name) LIKE LOWER('%' || COALESCE(:searchTerm, '') || '%') " +
           "OR LOWER(sc.medical_name) LIKE LOWER('%' || COALESCE(:searchTerm, '') || '%')) " +
           "ORDER BY sc.service_name ASC",
           countQuery = "SELECT COUNT(*) FROM service_coverage sc WHERE sc.policy_id = CAST(:policyId AS UUID) " +
           "AND (COALESCE(CAST(:categoryId AS VARCHAR), '') = '' OR sc.category_id = CAST(:categoryId AS UUID)) " +
           "AND (COALESCE(:searchTerm, '') = '' " +
           "OR LOWER(sc.service_name) LIKE LOWER('%' || COALESCE(:searchTerm, '') || '%') " +
           "OR LOWER(sc.medical_name) LIKE LOWER('%' || COALESCE(:searchTerm, '') || '%'))",
           nativeQuery = true)
    Page<ServiceCoverage> findByPolicyWithFilters(
            @Param("policyId") UUID policyId,
            @Param("categoryId") UUID categoryId,
            @Param("searchTerm") String searchTerm,
            Pageable pageable);

    @Query("SELECT COUNT(sc) FROM ServiceCoverage sc WHERE sc.globalPolicy.id = :policyId")
    long countByPolicyId(@Param("policyId") UUID policyId);

    void deleteByGlobalPolicyId(UUID policyId);

    boolean existsByGlobalPolicyIdAndServiceName(UUID policyId, String serviceName);
}
