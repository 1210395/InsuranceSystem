package com.insurancesystem.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GlobalPolicyManagementService {

    private final GlobalPolicyRepository globalPolicyRepository;
    private final ClientLimitsRepository clientLimitsRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final CategoryLimitsRepository categoryLimitsRepository;
    private final PolicyVersionRepository policyVersionRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ClientRepository clientRepository;
    private final ObjectMapper objectMapper;

    // ==================== Policy CRUD ====================

    public GlobalPolicyDTO createPolicy(CreateGlobalPolicyDTO dto, UUID createdById) {
        GlobalPolicy policy = GlobalPolicy.builder()
                .name(dto.getName())
                .version(dto.getVersion())
                .description(dto.getDescription())
                .effectiveFrom(dto.getEffectiveFrom())
                .effectiveTo(dto.getEffectiveTo())
                .status(GlobalPolicyStatus.DRAFT)
                .build();

        if (createdById != null) {
            clientRepository.findById(createdById).ifPresent(policy::setCreatedBy);
        }

        GlobalPolicy saved = globalPolicyRepository.save(policy);

        // Create default client limits
        ClientLimits clientLimits = ClientLimits.builder()
                .globalPolicy(saved)
                .build();
        clientLimitsRepository.save(clientLimits);

        return toDTO(saved);
    }

    @Transactional(readOnly = true)
    public GlobalPolicyDTO getActivePolicy() {
        GlobalPolicy policy = globalPolicyRepository.findActivePolicy()
                .orElseThrow(() -> new NotFoundException("No active policy found"));
        return toDTO(policy);
    }

    @Transactional(readOnly = true)
    public GlobalPolicyDTO getDraftPolicy() {
        GlobalPolicy policy = globalPolicyRepository.findDraftPolicy()
                .orElseThrow(() -> new NotFoundException("No draft policy found"));
        return toDTO(policy);
    }

    @Transactional(readOnly = true)
    public GlobalPolicyDTO getPolicyById(UUID id) {
        GlobalPolicy policy = globalPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));
        return toDTO(policy);
    }

    @Transactional(readOnly = true)
    public List<GlobalPolicyDTO> getAllPolicies() {
        return globalPolicyRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public GlobalPolicyDTO updatePolicy(UUID id, UpdateGlobalPolicyDTO dto) {
        GlobalPolicy policy = globalPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (dto.getName() != null) policy.setName(dto.getName());
        if (dto.getVersion() != null) policy.setVersion(dto.getVersion());
        if (dto.getDescription() != null) policy.setDescription(dto.getDescription());
        if (dto.getEffectiveFrom() != null) policy.setEffectiveFrom(dto.getEffectiveFrom());
        if (dto.getEffectiveTo() != null) policy.setEffectiveTo(dto.getEffectiveTo());

        return toDTO(globalPolicyRepository.save(policy));
    }

    public GlobalPolicyDTO activatePolicy(UUID id, UUID changedById, String reason) {
        GlobalPolicy policy = globalPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (policy.getStatus() != GlobalPolicyStatus.DRAFT) {
            throw new BadRequestException("Only draft policies can be activated");
        }

        // Deactivate current active policy (with pessimistic lock to prevent concurrent activation)
        globalPolicyRepository.findActivePolicyForUpdate().ifPresent(activePolicy -> {
            activePolicy.setStatus(GlobalPolicyStatus.EXPIRED);
            globalPolicyRepository.save(activePolicy);
        });

        // Create version snapshot before activation
        createVersionSnapshot(policy, changedById, reason != null ? reason : "Policy activated");

        // Activate new policy
        policy.setStatus(GlobalPolicyStatus.ACTIVE);
        return toDTO(globalPolicyRepository.save(policy));
    }

    public void deletePolicy(UUID id) {
        GlobalPolicy policy = globalPolicyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (policy.getStatus() == GlobalPolicyStatus.ACTIVE) {
            throw new BadRequestException("Cannot delete active policy");
        }

        globalPolicyRepository.delete(policy);
    }

    // ==================== Client Limits ====================

    @Transactional(readOnly = true)
    public ClientLimitsDTO getClientLimits(UUID policyId) {
        ClientLimits limits = clientLimitsRepository.findByGlobalPolicyId(policyId)
                .orElseThrow(() -> new NotFoundException("Client limits not found"));
        return toClientLimitsDTO(limits);
    }

    public ClientLimitsDTO updateClientLimits(UUID policyId, UpdateClientLimitsDTO dto) {
        ClientLimits limits = clientLimitsRepository.findByGlobalPolicyId(policyId)
                .orElseGet(() -> {
                    GlobalPolicy policy = globalPolicyRepository.findById(policyId)
                            .orElseThrow(() -> new NotFoundException("Policy not found"));
                    return ClientLimits.builder().globalPolicy(policy).build();
                });

        if (dto.getMaxVisitsPerMonth() != null) limits.setMaxVisitsPerMonth(dto.getMaxVisitsPerMonth());
        if (dto.getMaxVisitsPerYear() != null) limits.setMaxVisitsPerYear(dto.getMaxVisitsPerYear());
        if (dto.getMaxSpendingPerMonth() != null) limits.setMaxSpendingPerMonth(dto.getMaxSpendingPerMonth());
        if (dto.getMaxSpendingPerYear() != null) limits.setMaxSpendingPerYear(dto.getMaxSpendingPerYear());
        if (dto.getAnnualDeductible() != null) limits.setAnnualDeductible(dto.getAnnualDeductible());

        return toClientLimitsDTO(clientLimitsRepository.save(limits));
    }

    // ==================== Category Limits ====================

    @Transactional(readOnly = true)
    public List<CategoryLimitsDTO> getCategoryLimits(UUID policyId) {
        return categoryLimitsRepository.findByGlobalPolicyId(policyId).stream()
                .map(this::toCategoryLimitsDTO)
                .collect(Collectors.toList());
    }

    public CategoryLimitsDTO updateCategoryLimits(UUID policyId, UUID categoryId, UpdateCategoryLimitsDTO dto) {
        CategoryLimits limits = categoryLimitsRepository.findByGlobalPolicyIdAndCategoryId(policyId, categoryId)
                .orElseGet(() -> {
                    GlobalPolicy policy = globalPolicyRepository.findById(policyId)
                            .orElseThrow(() -> new NotFoundException("Policy not found"));
                    ServiceCategory category = serviceCategoryRepository.findById(categoryId)
                            .orElseThrow(() -> new NotFoundException("Category not found"));
                    return CategoryLimits.builder()
                            .globalPolicy(policy)
                            .category(category)
                            .build();
                });

        if (dto.getMaxVisitsPerMonth() != null) limits.setMaxVisitsPerMonth(dto.getMaxVisitsPerMonth());
        if (dto.getMaxVisitsPerYear() != null) limits.setMaxVisitsPerYear(dto.getMaxVisitsPerYear());
        if (dto.getMaxSpendingPerMonth() != null) limits.setMaxSpendingPerMonth(dto.getMaxSpendingPerMonth());
        if (dto.getMaxSpendingPerYear() != null) limits.setMaxSpendingPerYear(dto.getMaxSpendingPerYear());

        return toCategoryLimitsDTO(categoryLimitsRepository.save(limits));
    }

    // ==================== Policy Versions ====================

    @Transactional(readOnly = true)
    public List<PolicyVersionDTO> getPolicyVersions(UUID policyId) {
        return policyVersionRepository.findByGlobalPolicyIdOrderByCreatedAtDesc(policyId).stream()
                .map(this::toPolicyVersionDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PolicyVersionDTO getPolicyVersion(UUID versionId) {
        PolicyVersion version = policyVersionRepository.findById(versionId)
                .orElseThrow(() -> new NotFoundException("Policy version not found"));
        return toPolicyVersionDTO(version);
    }

    private void createVersionSnapshot(GlobalPolicy policy, UUID changedById, String reason) {
        try {
            String snapshot = objectMapper.writeValueAsString(toDTO(policy));
            PolicyVersion version = PolicyVersion.builder()
                    .globalPolicy(policy)
                    .version(policy.getVersion())
                    .snapshot(snapshot)
                    .changeReason(reason)
                    .build();

            if (changedById != null) {
                clientRepository.findById(changedById).ifPresent(version::setChangedBy);
            }

            policyVersionRepository.save(version);
        } catch (JsonProcessingException e) {
            log.error("Failed to create policy version snapshot", e);
        }
    }

    // ==================== DTO Converters ====================

    private GlobalPolicyDTO toDTO(GlobalPolicy policy) {
        ClientLimitsDTO clientLimitsDTO = clientLimitsRepository.findByGlobalPolicyId(policy.getId())
                .map(this::toClientLimitsDTO)
                .orElse(null);

        List<CategoryLimitsDTO> categoryLimitsDTOs = categoryLimitsRepository
                .findByGlobalPolicyId(policy.getId()).stream()
                .map(this::toCategoryLimitsDTO)
                .collect(Collectors.toList());

        long servicesCount = serviceCoverageRepository.countByPolicyId(policy.getId());
        long categoriesCount = categoryLimitsDTOs.size();

        return GlobalPolicyDTO.builder()
                .id(policy.getId())
                .name(policy.getName())
                .version(policy.getVersion())
                .description(policy.getDescription())
                .effectiveFrom(policy.getEffectiveFrom())
                .effectiveTo(policy.getEffectiveTo())
                .status(policy.getStatus())
                .clientLimits(clientLimitsDTO)
                .categoryLimits(categoryLimitsDTOs)
                .servicesCount((int) servicesCount)
                .categoriesCount((int) categoriesCount)
                .createdAt(policy.getCreatedAt() != null ? policy.getCreatedAt().toString() : null)
                .updatedAt(policy.getUpdatedAt() != null ? policy.getUpdatedAt().toString() : null)
                .build();
    }

    private ClientLimitsDTO toClientLimitsDTO(ClientLimits limits) {
        return ClientLimitsDTO.builder()
                .id(limits.getId())
                .policyId(limits.getGlobalPolicy().getId())
                .maxVisitsPerMonth(limits.getMaxVisitsPerMonth())
                .maxVisitsPerYear(limits.getMaxVisitsPerYear())
                .maxSpendingPerMonth(limits.getMaxSpendingPerMonth())
                .maxSpendingPerYear(limits.getMaxSpendingPerYear())
                .annualDeductible(limits.getAnnualDeductible())
                .build();
    }

    private CategoryLimitsDTO toCategoryLimitsDTO(CategoryLimits limits) {
        return CategoryLimitsDTO.builder()
                .id(limits.getId())
                .policyId(limits.getGlobalPolicy().getId())
                .categoryId(limits.getCategory().getId())
                .categoryName(limits.getCategory().getName())
                .maxVisitsPerMonth(limits.getMaxVisitsPerMonth())
                .maxVisitsPerYear(limits.getMaxVisitsPerYear())
                .maxSpendingPerMonth(limits.getMaxSpendingPerMonth())
                .maxSpendingPerYear(limits.getMaxSpendingPerYear())
                .build();
    }

    private PolicyVersionDTO toPolicyVersionDTO(PolicyVersion version) {
        return PolicyVersionDTO.builder()
                .id(version.getId())
                .policyId(version.getGlobalPolicy().getId())
                .version(version.getVersion())
                .snapshot(version.getSnapshot())
                .changedById(version.getChangedBy() != null ? version.getChangedBy().getId() : null)
                .changedByName(version.getChangedBy() != null ? version.getChangedBy().getFullName() : null)
                .changeReason(version.getChangeReason())
                .createdAt(version.getCreatedAt() != null ? version.getCreatedAt().toString() : null)
                .build();
    }
}
