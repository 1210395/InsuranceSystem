package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.ServiceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ServiceCategoryRepository extends JpaRepository<ServiceCategory, UUID> {

    Optional<ServiceCategory> findByName(String name);

    boolean existsByName(String name);

    List<ServiceCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    @Query("SELECT sc FROM ServiceCategory sc ORDER BY sc.displayOrder ASC")
    List<ServiceCategory> findAllOrderByDisplayOrder();

    List<ServiceCategory> findByIsActive(Boolean isActive);
}
