package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceCoverageManagementService {

    private final ServiceCoverageRepository serviceCoverageRepository;
    private final GlobalPolicyRepository globalPolicyRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;

    @Transactional(readOnly = true)
    public List<ServiceCoverageDTO> getServicesByPolicy(UUID policyId) {
        return serviceCoverageRepository.findByGlobalPolicyId(policyId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ServiceCoverageDTO> getServicesByPolicyPaginated(
            UUID policyId, UUID categoryId, String searchTerm, int page, int size) {
        // Use unsorted pageable since native query handles sorting
        Pageable pageable = PageRequest.of(page, size);
        return serviceCoverageRepository.findByPolicyWithFilters(policyId, categoryId, searchTerm, pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public List<ServiceCoverageDTO> getServicesByCategory(UUID policyId, UUID categoryId) {
        return serviceCoverageRepository.findByGlobalPolicyIdAndCategoryId(policyId, categoryId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceCoverageDTO getServiceById(UUID id) {
        ServiceCoverage service = serviceCoverageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service coverage not found"));
        return toDTO(service);
    }

    public ServiceCoverageDTO createService(CreateServiceCoverageDTO dto) {
        GlobalPolicy policy = globalPolicyRepository.findById(dto.getPolicyId())
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        if (serviceCoverageRepository.existsByGlobalPolicyIdAndServiceName(dto.getPolicyId(), dto.getServiceName())) {
            throw new BadRequestException("Service with this name already exists in this policy");
        }

        ServiceCoverage service = ServiceCoverage.builder()
                .globalPolicy(policy)
                .serviceName(dto.getServiceName())
                .medicalName(dto.getMedicalName())
                .description(dto.getDescription())
                .coverageStatus(dto.getCoverageStatus())
                .coveragePercent(dto.getCoveragePercent())
                .standardPrice(dto.getStandardPrice())
                .maxCoverageAmount(dto.getMaxCoverageAmount())
                .minAge(dto.getMinAge())
                .maxAge(dto.getMaxAge())
                .allowedGender(dto.getAllowedGender())
                .requiresReferral(dto.getRequiresReferral())
                .frequencyLimit(dto.getFrequencyLimit())
                .frequencyPeriod(dto.getFrequencyPeriod())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .build();

        if (dto.getCategoryId() != null) {
            ServiceCategory category = serviceCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            service.setCategory(category);
        }

        return toDTO(serviceCoverageRepository.save(service));
    }

    public ServiceCoverageDTO updateService(UUID id, UpdateServiceCoverageDTO dto) {
        ServiceCoverage service = serviceCoverageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service coverage not found"));

        if (dto.getCategoryId() != null) {
            ServiceCategory category = serviceCategoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Category not found"));
            service.setCategory(category);
        }

        if (dto.getServiceName() != null) service.setServiceName(dto.getServiceName());
        if (dto.getMedicalName() != null) service.setMedicalName(dto.getMedicalName());
        if (dto.getDescription() != null) service.setDescription(dto.getDescription());
        if (dto.getCoverageStatus() != null) service.setCoverageStatus(dto.getCoverageStatus());
        if (dto.getCoveragePercent() != null) service.setCoveragePercent(dto.getCoveragePercent());
        if (dto.getStandardPrice() != null) service.setStandardPrice(dto.getStandardPrice());
        if (dto.getMaxCoverageAmount() != null) service.setMaxCoverageAmount(dto.getMaxCoverageAmount());
        if (dto.getMinAge() != null) service.setMinAge(dto.getMinAge());
        if (dto.getMaxAge() != null) service.setMaxAge(dto.getMaxAge());
        if (dto.getAllowedGender() != null) service.setAllowedGender(dto.getAllowedGender());
        if (dto.getRequiresReferral() != null) service.setRequiresReferral(dto.getRequiresReferral());
        if (dto.getFrequencyLimit() != null) service.setFrequencyLimit(dto.getFrequencyLimit());
        if (dto.getFrequencyPeriod() != null) service.setFrequencyPeriod(dto.getFrequencyPeriod());
        if (dto.getIsActive() != null) service.setIsActive(dto.getIsActive());

        return toDTO(serviceCoverageRepository.save(service));
    }

    public void deleteService(UUID id) {
        ServiceCoverage service = serviceCoverageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Service coverage not found"));
        serviceCoverageRepository.delete(service);
    }

    public List<ServiceCoverageDTO> bulkCreateServices(UUID policyId, List<CreateServiceCoverageDTO> dtos) {
        GlobalPolicy policy = globalPolicyRepository.findById(policyId)
                .orElseThrow(() -> new NotFoundException("Policy not found"));

        return dtos.stream().map(dto -> {
            dto.setPolicyId(policyId);
            return createService(dto);
        }).collect(Collectors.toList());
    }

    public void bulkDeleteServices(List<UUID> serviceIds) {
        serviceIds.forEach(this::deleteService);
    }

    public void bulkUpdateStatus(List<UUID> serviceIds, boolean isActive) {
        serviceIds.forEach(id -> {
            serviceCoverageRepository.findById(id).ifPresent(service -> {
                service.setIsActive(isActive);
                serviceCoverageRepository.save(service);
            });
        });
    }

    private ServiceCoverageDTO toDTO(ServiceCoverage service) {
        ServiceCategoryDTO categoryDTO = null;
        if (service.getCategory() != null) {
            categoryDTO = ServiceCategoryDTO.builder()
                    .id(service.getCategory().getId())
                    .name(service.getCategory().getName())
                    .nameAr(service.getCategory().getNameAr())
                    .icon(service.getCategory().getIcon())
                    .color(service.getCategory().getColor())
                    .build();
        }

        return ServiceCoverageDTO.builder()
                .id(service.getId())
                .policyId(service.getGlobalPolicy().getId())
                .serviceName(service.getServiceName())
                .medicalName(service.getMedicalName())
                .description(service.getDescription())
                .category(categoryDTO)
                .coverageStatus(service.getCoverageStatus())
                .coveragePercent(service.getCoveragePercent())
                .standardPrice(service.getStandardPrice())
                .maxCoverageAmount(service.getMaxCoverageAmount())
                .minAge(service.getMinAge())
                .maxAge(service.getMaxAge())
                .allowedGender(service.getAllowedGender())
                .requiresReferral(service.getRequiresReferral())
                .frequencyLimit(service.getFrequencyLimit())
                .frequencyPeriod(service.getFrequencyPeriod())
                .isActive(service.getIsActive())
                .build();
    }
}
