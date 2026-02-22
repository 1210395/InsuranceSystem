package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.CategoryLimits;
import com.insurancesystem.Model.Entity.GlobalPolicy;
import com.insurancesystem.Model.Entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryLimitsRepository extends JpaRepository<CategoryLimits, UUID> {

    List<CategoryLimits> findByGlobalPolicy(GlobalPolicy globalPolicy);

    List<CategoryLimits> findByGlobalPolicyId(UUID policyId);

    Optional<CategoryLimits> findByGlobalPolicyIdAndCategoryId(UUID policyId, UUID categoryId);

    Optional<CategoryLimits> findByGlobalPolicyAndCategory(GlobalPolicy globalPolicy, ServiceCategory category);

    void deleteByGlobalPolicyId(UUID policyId);

    boolean existsByGlobalPolicyIdAndCategoryId(UUID policyId, UUID categoryId);
}
