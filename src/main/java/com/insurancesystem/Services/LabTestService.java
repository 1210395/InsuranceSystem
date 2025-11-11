package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.LabTestDTO;
import com.insurancesystem.Model.Entity.LabTest;
import com.insurancesystem.Model.MapStruct.LabTestMapper;
import com.insurancesystem.Repository.LabTestRepository;
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
public class LabTestService {

    private final LabTestRepository labTestRepository;
    private final LabTestMapper labTestMapper;

    // ➕ إنشاء فحص مختبري جديد (Admin فقط)
    @Transactional
    public LabTestDTO create(LabTestDTO dto) {
        log.info("🔹 Creating new lab test: {}", dto.getTestName());

        // التحقق من عدم وجود فحص بنفس الاسم
        if (labTestRepository.findByTestName(dto.getTestName()).isPresent()) {
            log.error("❌ Lab test already exists: {}", dto.getTestName());
            throw new IllegalArgumentException("Lab test already exists with this name");
        }

        LabTest labTest = labTestMapper.toEntity(dto);
        labTest.setCreatedAt(Instant.now());
        labTest.setUpdatedAt(Instant.now());
        labTest.setIsActive(true);

        LabTest saved = labTestRepository.save(labTest);
        log.info("✅ Lab test created successfully: {}", saved.getId());

        return labTestMapper.toDto(saved);
    }

    // 📖 الحصول على جميع الفحصوات النشطة
    public List<LabTestDTO> getAllActiveTests() {
        log.info("🔹 Fetching all active lab tests");
        return labTestRepository.findByIsActiveTrue()
                .stream()
                .map(labTestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 الحصول على فحص محدد باسمه
    public LabTestDTO getByTestName(String testName) {
        log.info("🔹 Fetching lab test by name: {}", testName);
        LabTest labTest = labTestRepository.findByTestName(testName)
                .orElseThrow(() -> {
                    log.error("❌ Lab test not found: {}", testName);
                    return new NotFoundException("Lab test not found with name: " + testName);
                });
        return labTestMapper.toDto(labTest);
    }

    // 📖 البحث عن فحصوات بكلمة مفتاحية
    public List<LabTestDTO> searchTests(String keyword) {
        log.info("🔹 Searching lab tests with keyword: {}", keyword);
        return labTestRepository.findByTestNameContainingIgnoreCase(keyword)
                .stream()
                .map(labTestMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 الحصول على فحص بـ ID
    public LabTestDTO getById(UUID id) {
        log.info("🔹 Fetching lab test by ID: {}", id);
        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Lab test not found by ID: {}", id);
                    return new NotFoundException("Lab test not found");
                });
        return labTestMapper.toDto(labTest);
    }

    // ✏️ تحديث فحص مختبري
    @Transactional
    public LabTestDTO update(UUID id, LabTestDTO dto) {
        log.info("🔹 Updating lab test: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Lab test not found: {}", id);
                    return new NotFoundException("Lab test not found");
                });

        if (dto.getTestName() != null && !dto.getTestName().isBlank()) {
            labTest.setTestName(dto.getTestName());
        }
        if (dto.getUnionPrice() != null) {
            labTest.setUnionPrice(dto.getUnionPrice());
        }
        if (dto.getLabPrice() != null) {
            labTest.setLabPrice(dto.getLabPrice());
        }
        if (dto.getDescription() != null) {
            labTest.setDescription(dto.getDescription());
        }
        if (dto.getIsActive() != null) {
            labTest.setIsActive(dto.getIsActive());
        }

        labTest.setUpdatedAt(Instant.now());
        LabTest updated = labTestRepository.save(labTest);
        log.info("✅ Lab test updated successfully");

        return labTestMapper.toDto(updated);
    }

    // ❌ حذف فحص مختبري
    @Transactional
    public void delete(UUID id) {
        log.info("🔹 Deleting lab test: {}", id);

        LabTest labTest = labTestRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("❌ Lab test not found: {}", id);
                    return new NotFoundException("Lab test not found");
                });

        labTestRepository.delete(labTest);
        log.info("✅ Lab test deleted successfully");
    }

    // 🔍 التحقق من السعر (هل السعر المدخل يساوي أو أقل من السعر النقابي؟)
    public boolean isPriceValid(UUID labTestId, Double enteredPrice) {
        LabTest labTest = labTestRepository.findById(labTestId)
                .orElseThrow(() -> new NotFoundException("Lab test not found"));

        log.info("🔍 Validating price - Union: {}, Entered: {}", labTest.getUnionPrice(), enteredPrice);
        return enteredPrice <= labTest.getUnionPrice();
    }

}

