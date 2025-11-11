package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.LabTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LabTestRepository extends JpaRepository<LabTest, UUID> {

    // البحث عن فحص باسمه
    Optional<LabTest> findByTestName(String testName);

    // جميع الفحصوات النشطة
    List<LabTest> findByIsActiveTrue();

    // البحث برقم جزئي من الاسم
    List<LabTest> findByTestNameContainingIgnoreCase(String keyword);

}

