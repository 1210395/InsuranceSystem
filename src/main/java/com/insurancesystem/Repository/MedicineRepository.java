package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {

    // 🔍 بحث بالاسم أو المصطلح العلمي
    @Query("SELECT m FROM Medicine m WHERE " +
            "LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.scientificName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Medicine> searchByNameOrScientific(@Param("search") String search);

    // بحث بالاسم فقط
    List<Medicine> findByNameContainingIgnoreCase(String name);

    // بحث بالمصطلح العلمي فقط
    List<Medicine> findByScientificNameContainingIgnoreCase(String scientificName);
}