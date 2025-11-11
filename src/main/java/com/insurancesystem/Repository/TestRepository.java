package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestRepository extends JpaRepository<Test, UUID> {

    // البحث عن فحص باسمه
    Optional<Test> findByTestName(String testName);

    // البحث برقم جزئي من الاسم
    List<Test> findByTestNameContainingIgnoreCase(String keyword);

}

