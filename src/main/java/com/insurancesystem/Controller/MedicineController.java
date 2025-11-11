package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.MedicineDTO;
import com.insurancesystem.Services.MedicineService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineService medicineService;

    // ➕ إنشاء دواء (Admin/Manager)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MedicineDTO> create(@RequestBody @Valid MedicineDTO dto) {
        return ResponseEntity.ok(medicineService.create(dto));
    }

    // 🔍 بحث (Doctor يستخدمه عند كتابة الوصفة)
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<MedicineDTO>> search(@RequestParam(required = false) String query) {
        return ResponseEntity.ok(medicineService.search(query));
    }

    // 📖 جميع الأدوية
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<MedicineDTO>> getAll() {
        return ResponseEntity.ok(medicineService.getAll());
    }

    // 📖 دواء واحد
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PHARMACIST', 'ADMIN', 'MANAGER')")
    public ResponseEntity<MedicineDTO> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(medicineService.getById(id));
    }

    // ✏️ تعديل
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<MedicineDTO> update(@PathVariable UUID id, @RequestBody @Valid MedicineDTO dto) {
        return ResponseEntity.ok(medicineService.update(id, dto));
    }

    // ❌ حذف
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        medicineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}