package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicineType;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MedicineTypeRepository extends JpaRepository<MedicineType, Long> {
    boolean existsByNameIgnoreCase(String name);
}