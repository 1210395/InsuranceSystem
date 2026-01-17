package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthcareProviderClaimMapper {

    // Entity → DTO
    @Mapping(source = "healthcareProvider.id", target = "providerId")
    @Mapping(target = "providerName", expression = "java(entity.getProviderName() != null ? entity.getProviderName() : entity.getHealthcareProvider() != null ? entity.getHealthcareProvider().getFullName() : null)")
    @Mapping(source = "clientName", target = "clientName")
    @Mapping(source = "clientId", target = "clientId")
    // New fields are mapped automatically by name
    HealthcareProviderClaimDTO toDto(HealthcareProviderClaim entity);

    // CreateDTO → Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "healthcareProvider", ignore = true)
    @Mapping(target = "clientName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submittedAt", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "invoiceImagePath", ignore = true)
    @Mapping(target = "medicalReviewedAt", ignore = true)
    @Mapping(target = "clientId", source = "clientId")
    @Mapping(target = "roleSpecificData", source = "roleSpecificData")
    HealthcareProviderClaim toEntity(CreateHealthcareProviderClaimDTO dto);
}

