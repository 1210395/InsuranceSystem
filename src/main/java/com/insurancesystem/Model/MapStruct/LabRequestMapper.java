package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Entity.LabRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LabRequestMapper {

    // ✅ Entity → DTO
    @Mapping(source = "doctor.id", target = "doctorId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "labTech.id", target = "labTechId")
    @Mapping(source = "labTech.fullName", target = "labTechName")
    @Mapping(source = "test.id", target = "testId")
    @Mapping(source = "test.testName", target = "testName_test")
    @Mapping(source = "test.unionPrice", target = "unionPrice")
    LabRequestDTO toDto(LabRequest request);

    // ✅ DTO → Entity
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "labTech", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "test", ignore = true)
    LabRequest toEntity(LabRequestDTO dto);

}
