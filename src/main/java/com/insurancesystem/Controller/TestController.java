package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.TestDTO;
import com.insurancesystem.Services.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestController {

    private final TestService testService;

    // ➕ إنشاء فحص جديد (Admin فقط)
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestDTO> create(@RequestBody @Valid TestDTO dto) {
        TestDTO createdTest = testService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTest);
    }

    // 📖 الحصول على جميع الفحصوات
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<List<TestDTO>> getAll() {
        List<TestDTO> tests = testService.getAll();
        return ResponseEntity.ok(tests);
    }

    // 📖 الحصول على فحص بـ ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<TestDTO> getById(@PathVariable UUID id) {
        TestDTO test = testService.getById(id);
        return ResponseEntity.ok(test);
    }

    // 📖 الحصول على فحص باسمه
    @GetMapping("/by-name/{testName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<TestDTO> getByTestName(@PathVariable String testName) {
        TestDTO test = testService.getByTestName(testName);
        return ResponseEntity.ok(test);
    }

    // 🔍 البحث عن فحصوات بكلمة مفتاحية
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'LAB_TECH', 'INSURANCE_CLIENT')")
    public ResponseEntity<List<TestDTO>> searchTests(@RequestParam String keyword) {
        List<TestDTO> tests = testService.searchTests(keyword);
        return ResponseEntity.ok(tests);
    }

    // ✏️ تحديث فحص
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TestDTO> update(@PathVariable UUID id, @RequestBody @Valid TestDTO dto) {
        TestDTO updatedTest = testService.update(id, dto);
        return ResponseEntity.ok(updatedTest);
    }

    // ❌ حذف فحص
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        testService.delete(id);
        return ResponseEntity.noContent().build();
    }

}

