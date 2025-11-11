package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RadiologyRequestMapper {

    // ✅ Entity → DTO
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "radiologist.id", target = "radiologistId")
    @Mapping(source = "radiologist.fullName", target = "radiologistName")
    RadiologyRequestDTO toDto(RadiologyRequest radiologyRequest);

    // ✅ DTO → Entity
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "radiologistId", target = "radiologist.id")
    @Mapping(target = "doctor", ignore = true)  // يضاف من Service
    @Mapping(target = "radiologist", ignore = true)  // يضاف من Service
    @Mapping(target = "member", ignore = true)  // يضاف من Service
    RadiologyRequest toEntity(RadiologyRequestDTO radiologyRequestDTO);

}
