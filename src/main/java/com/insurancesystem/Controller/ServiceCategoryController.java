package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Services.ServiceCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/policy/categories")
@RequiredArgsConstructor
public class ServiceCategoryController {

    private final ServiceCategoryService categoryService;

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping
    public ResponseEntity<List<ServiceCategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/active")
    public ResponseEntity<List<ServiceCategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCategoryDTO> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping
    public ResponseEntity<ServiceCategoryDTO> createCategory(
            @Valid @RequestBody CreateServiceCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}")
    public ResponseEntity<ServiceCategoryDTO> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceCategoryDTO dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Category deleted successfully"));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/reorder")
    public ResponseEntity<Map<String, Object>> reorderCategories(
            @RequestBody Map<String, List<UUID>> body) {
        List<UUID> categoryIds = body.get("categoryIds");
        categoryService.reorderCategories(categoryIds);
        return ResponseEntity.ok(Map.of("success", true, "message", "Categories reordered successfully"));
    }
}
