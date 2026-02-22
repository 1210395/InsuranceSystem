package com.insurancesystem.Services;

import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.ServiceCategory;
import com.insurancesystem.Repository.ServiceCategoryRepository;
import com.insurancesystem.Repository.ServiceCoverageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ServiceCategoryService {

    private final ServiceCategoryRepository categoryRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;

    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getAllCategories() {
        return categoryRepository.findAllOrderByDisplayOrder().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ServiceCategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderByDisplayOrderAsc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceCategoryDTO getCategoryById(UUID id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
        return toDTO(category);
    }

    public ServiceCategoryDTO createCategory(CreateServiceCategoryDTO dto) {
        if (categoryRepository.existsByName(dto.getName())) {
            throw new BadRequestException("Category with this name already exists");
        }

        ServiceCategory category = ServiceCategory.builder()
                .name(dto.getName())
                .nameAr(dto.getNameAr())
                .description(dto.getDescription())
                .icon(dto.getIcon())
                .color(dto.getColor())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .displayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0)
                .build();

        return toDTO(categoryRepository.save(category));
    }

    public ServiceCategoryDTO updateCategory(UUID id, UpdateServiceCategoryDTO dto) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        if (dto.getName() != null && !dto.getName().equals(category.getName())) {
            if (categoryRepository.existsByName(dto.getName())) {
                throw new BadRequestException("Category with this name already exists");
            }
            category.setName(dto.getName());
        }

        if (dto.getNameAr() != null) category.setNameAr(dto.getNameAr());
        if (dto.getDescription() != null) category.setDescription(dto.getDescription());
        if (dto.getIcon() != null) category.setIcon(dto.getIcon());
        if (dto.getColor() != null) category.setColor(dto.getColor());
        if (dto.getIsActive() != null) category.setIsActive(dto.getIsActive());
        if (dto.getDisplayOrder() != null) category.setDisplayOrder(dto.getDisplayOrder());

        return toDTO(categoryRepository.save(category));
    }

    public void deleteCategory(UUID id) {
        ServiceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category not found"));

        // Check if category is being used by any service coverage
        // For now, we'll allow deletion - in production you might want to prevent this
        categoryRepository.delete(category);
    }

    public void reorderCategories(List<UUID> categoryIds) {
        for (int i = 0; i < categoryIds.size(); i++) {
            UUID categoryId = categoryIds.get(i);
            categoryRepository.findById(categoryId).ifPresent(category -> {
                category.setDisplayOrder(categoryIds.indexOf(categoryId));
                categoryRepository.save(category);
            });
        }
    }

    private ServiceCategoryDTO toDTO(ServiceCategory category) {
        return ServiceCategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .nameAr(category.getNameAr())
                .description(category.getDescription())
                .icon(category.getIcon())
                .color(category.getColor())
                .isActive(category.getIsActive())
                .displayOrder(category.getDisplayOrder())
                .build();
    }
}
