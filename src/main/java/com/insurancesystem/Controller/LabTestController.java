package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.LabTestDTO;
import com.insurancesystem.Services.LabTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lab-tests")
@RequiredArgsConstructor
public class LabTestController {

    private final LabTestService labTestService;

    // ➕ إنشاء فحص مختبري جديد (Admin فقط)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabTestDTO> create(@RequestBody @Valid LabTestDTO dto) {
        LabTestDTO createdTest = labTestService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTest);
    }

    // 📖 الحصول على جميع الفحصوات النشطة
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<List<LabTestDTO>> getAllActiveTests() {
        List<LabTestDTO> tests = labTestService.getAllActiveTests();
        return ResponseEntity.ok(tests);
    }

    // 📖 الحصول على فحص بـ ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<LabTestDTO> getById(@PathVariable UUID id) {
        LabTestDTO test = labTestService.getById(id);
        return ResponseEntity.ok(test);
    }

    // 📖 الحصول على فحص باسمه
    @GetMapping("/by-name/{testName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<LabTestDTO> getByTestName(@PathVariable String testName) {
        LabTestDTO test = labTestService.getByTestName(testName);
        return ResponseEntity.ok(test);
    }

    // 🔍 البحث عن فحصوات بكلمة مفتاحية
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<List<LabTestDTO>> searchTests(@RequestParam String keyword) {
        List<LabTestDTO> tests = labTestService.searchTests(keyword);
        return ResponseEntity.ok(tests);
    }

    // ✏️ تحديث فحص مختبري
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LabTestDTO> update(@PathVariable UUID id, @RequestBody @Valid LabTestDTO dto) {
        LabTestDTO updatedTest = labTestService.update(id, dto);
        return ResponseEntity.ok(updatedTest);
    }

    // ❌ حذف فحص مختبري
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        labTestService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

