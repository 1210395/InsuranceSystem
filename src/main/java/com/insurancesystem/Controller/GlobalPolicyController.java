package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Services.GlobalPolicyManagementService;
import com.insurancesystem.Services.PolicyUsageReportService;
import com.insurancesystem.Services.ServiceCoverageManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/policy")
@RequiredArgsConstructor
public class GlobalPolicyController {

    private final GlobalPolicyManagementService policyService;
    private final ServiceCoverageManagementService serviceCoverageService;
    private final PolicyUsageReportService reportService;

    // ==================== Policy CRUD ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping
    public ResponseEntity<GlobalPolicyDTO> getActivePolicy() {
        return ResponseEntity.ok(policyService.getActivePolicy());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/draft")
    public ResponseEntity<GlobalPolicyDTO> getDraftPolicy() {
        return ResponseEntity.ok(policyService.getDraftPolicy());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/all")
    public ResponseEntity<List<GlobalPolicyDTO>> getAllPolicies() {
        return ResponseEntity.ok(policyService.getAllPolicies());
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/{id}")
    public ResponseEntity<GlobalPolicyDTO> getPolicyById(@PathVariable UUID id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping
    public ResponseEntity<GlobalPolicyDTO> createPolicy(
            @Valid @RequestBody CreateGlobalPolicyDTO dto,
            Authentication authentication) {
        UUID createdById = getUserId(authentication);
        return ResponseEntity.ok(policyService.createPolicy(dto, createdById));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/{id}")
    public ResponseEntity<GlobalPolicyDTO> updatePolicy(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGlobalPolicyDTO dto) {
        return ResponseEntity.ok(policyService.updatePolicy(id, dto));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/{id}/activate")
    public ResponseEntity<GlobalPolicyDTO> activatePolicy(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication authentication) {
        UUID changedById = getUserId(authentication);
        String reason = body != null ? body.get("reason") : null;
        return ResponseEntity.ok(policyService.activatePolicy(id, changedById, reason));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePolicy(@PathVariable UUID id) {
        policyService.deletePolicy(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Policy deleted successfully"));
    }

    // ==================== Client Limits ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/client-limits")
    public ResponseEntity<ClientLimitsDTO> getClientLimits(@RequestParam UUID policyId) {
        return ResponseEntity.ok(policyService.getClientLimits(policyId));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/client-limits")
    public ResponseEntity<ClientLimitsDTO> updateClientLimits(
            @RequestParam UUID policyId,
            @Valid @RequestBody UpdateClientLimitsDTO dto) {
        return ResponseEntity.ok(policyService.updateClientLimits(policyId, dto));
    }

    // ==================== Category Limits ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/category-limits")
    public ResponseEntity<List<CategoryLimitsDTO>> getCategoryLimits(@RequestParam UUID policyId) {
        return ResponseEntity.ok(policyService.getCategoryLimits(policyId));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/category-limits/{categoryId}")
    public ResponseEntity<CategoryLimitsDTO> updateCategoryLimits(
            @RequestParam UUID policyId,
            @PathVariable UUID categoryId,
            @Valid @RequestBody UpdateCategoryLimitsDTO dto) {
        return ResponseEntity.ok(policyService.updateCategoryLimits(policyId, categoryId, dto));
    }

    // ==================== Service Coverage ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/services")
    public ResponseEntity<List<ServiceCoverageDTO>> getServices(@RequestParam UUID policyId) {
        return ResponseEntity.ok(serviceCoverageService.getServicesByPolicy(policyId));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/services/paginated")
    public ResponseEntity<Page<ServiceCoverageDTO>> getServicesPaginated(
            @RequestParam UUID policyId,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(serviceCoverageService.getServicesByPolicyPaginated(
                policyId, categoryId, search, page, size));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceCoverageDTO> getServiceById(@PathVariable UUID id) {
        return ResponseEntity.ok(serviceCoverageService.getServiceById(id));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/services")
    public ResponseEntity<ServiceCoverageDTO> createService(
            @Valid @RequestBody CreateServiceCoverageDTO dto) {
        return ResponseEntity.ok(serviceCoverageService.createService(dto));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PatchMapping("/services/{id}")
    public ResponseEntity<ServiceCoverageDTO> updateService(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServiceCoverageDTO dto) {
        return ResponseEntity.ok(serviceCoverageService.updateService(id, dto));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @DeleteMapping("/services/{id}")
    public ResponseEntity<Map<String, Object>> deleteService(@PathVariable UUID id) {
        serviceCoverageService.deleteService(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "Service deleted successfully"));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/services/bulk")
    public ResponseEntity<List<ServiceCoverageDTO>> bulkCreateServices(
            @RequestParam UUID policyId,
            @Valid @RequestBody List<CreateServiceCoverageDTO> dtos) {
        return ResponseEntity.ok(serviceCoverageService.bulkCreateServices(policyId, dtos));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/services/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteServices(
            @RequestBody Map<String, List<UUID>> body) {
        List<UUID> serviceIds = body.get("serviceIds");
        serviceCoverageService.bulkDeleteServices(serviceIds);
        return ResponseEntity.ok(Map.of("success", true, "message", serviceIds.size() + " services deleted"));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/services/bulk-status")
    public ResponseEntity<Map<String, Object>> bulkUpdateStatus(
            @RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<String> serviceIdStrings = (List<String>) body.get("serviceIds");
        List<UUID> serviceIds = serviceIdStrings.stream().map(UUID::fromString).toList();
        boolean isActive = (Boolean) body.get("isActive");
        serviceCoverageService.bulkUpdateStatus(serviceIds, isActive);
        return ResponseEntity.ok(Map.of("success", true, "message", "Status updated for " + serviceIds.size() + " services"));
    }

    // ==================== Policy Versions ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/versions")
    public ResponseEntity<List<PolicyVersionDTO>> getPolicyVersions(@RequestParam UUID policyId) {
        return ResponseEntity.ok(policyService.getPolicyVersions(policyId));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/versions/{id}")
    public ResponseEntity<PolicyVersionDTO> getPolicyVersion(@PathVariable UUID id) {
        return ResponseEntity.ok(policyService.getPolicyVersion(id));
    }

    // ==================== Usage Reports ====================

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/reports/usage")
    public ResponseEntity<PolicyUsageReportService.PolicyUsageSummary> getUsageReport(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate fromDate,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate toDate) {
        return ResponseEntity.ok(reportService.generateReport(fromDate, toDate));
    }

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @GetMapping("/reports/quick-stats")
    public ResponseEntity<Map<String, Object>> getQuickStats() {
        return ResponseEntity.ok(reportService.getQuickStats());
    }

    // ==================== Helper Methods ====================

    private UUID getUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() != null) {
            try {
                return UUID.fromString(authentication.getName());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
