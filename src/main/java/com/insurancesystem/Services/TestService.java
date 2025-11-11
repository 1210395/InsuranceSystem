package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.TestDTO;
import com.insurancesystem.Model.Entity.Test;
import com.insurancesystem.Model.MapStruct.TestMapper;
import com.insurancesystem.Repository.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestRepository testRepository;
    private final TestMapper testMapper;

    // ➕ إنشاء فحص جديد
    @Transactional
    public TestDTO create(TestDTO dto) {
        log.info("🔹 Creating new test: {}", dto.getTestName());

        // التحقق من عدم وجود فحص بنفس الاسم
        if (testRepository.findByTestName(dto.getTestName()).isPresent()) {
            log.error("❌ Test already exists: {}", dto.getTestName());
            throw new IllegalArgumentException("Test already exists with this name: " + dto.getTestName());
        }

        Test test = testMapper.toEntity(dto);
        test.setCreatedAt(Instant.now());
        test.setUpdatedAt(Instant.now());

        Test saved = testRepository.save(test);
        log.info("✅ Test created successfully: {}", saved.getId());

        return testMapper.toDto(saved);
    }

    // 📖 الحصول على جميع الفحصوات
    public List<TestDTO> getAll() {
        log.info("🔹 Fetching all tests");
        return testRepository.findAll()
                .stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 الحصول على فحص محدد باسمه
    public TestDTO getByTestName(String testName) {
        log.info("🔹 Fetching test by name: {}", testName);
        Test test = testRepository.findByTestName(testName)
                .orElseThrow(() -> {
                    log.error("❌ Test not found: {}", testName);
                    return new NotFoundException("Test not found with name: " + testName);
                });
        return testMapper.toDto(test);
    }

    // 📖 البحث عن فحصوات بكلمة مفتاحية
    public List<TestDTO> searchTests(String keyword) {
        log.info("🔹 Searching tests with keyword: {}", keyword);
        return testRepository.findByTestNameContainingIgnoreCase(keyword)
                .stream()
                .map(testMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 الحصول على فحص بـ ID
    public TestDTO getById(UUID id) {
        log.info("🔹 Fetching test by ID: {}", id);
        Test test = testRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Test not found by ID: {}", id);
                    return new NotFoundException("Test not found");
                });
        return testMapper.toDto(test);
    }

    // ✏️ تحديث فحص
    @Transactional
    public TestDTO update(UUID id, TestDTO dto) {
        log.info("🔹 Updating test: {}", id);

        Test test = testRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Test not found: {}", id);
                    return new NotFoundException("Test not found");
                });

        if (dto.getTestName() != null && !dto.getTestName().isBlank()) {
            test.setTestName(dto.getTestName());
        }
        if (dto.getUnionPrice() != null) {
            test.setUnionPrice(dto.getUnionPrice());
        }

        test.setUpdatedAt(Instant.now());
        Test updated = testRepository.save(test);
        log.info("✅ Test updated successfully");

        return testMapper.toDto(updated);
    }

    // ❌ حذف فحص
    @Transactional
    public void delete(UUID id) {
        log.info("🔹 Deleting test: {}", id);

        Test test = testRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Test not found: {}", id);
                    return new NotFoundException("Test not found");
                });

        testRepository.delete(test);
        log.info("✅ Test deleted successfully");
    }

}

