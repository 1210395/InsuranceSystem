package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.insurancesystem.Model.Entity.RadiologyRequest;
import org.mapstruct.*;
@Mapper(componentModel = "spring")
public interface RadiologyRequestMapper {

    // Entity → DTO
    @Mapping(target = "doctorId",
            expression = "java(request.getDoctor() != null ? request.getDoctor().getId() : null)")
    @Mapping(target = "doctorName",
            expression = "java(request.getDoctor() != null ? request.getDoctor().getFullName() : null)")

    @Mapping(target = "memberId",
            expression = "java(request.getMember() != null ? request.getMember().getId() : null)")
    @Mapping(target = "memberName",
            expression = "java(request.getMember() != null ? request.getMember().getFullName() : null)")

    @Mapping(target = "radiologistId",
            expression = "java(request.getRadiologist() != null ? request.getRadiologist().getId() : null)")
    @Mapping(target = "radiologistName",
            expression = "java(request.getRadiologist() != null ? request.getRadiologist().getFullName() : null)")

    @Mapping(
            target = "universityCardImages",
            expression = "java(request.getMember()!=null ? request.getMember().getUniversityCardImages() : null)"
    )

    @Mapping(target = "testId",
            expression = "java(request.getTest() != null ? request.getTest().getId() : null)")
    @Mapping(target = "testName",
            expression = "java(request.getTest() != null ? request.getTest().getServiceName() : null)")

    // انتبه: هاي بتعبي price تبع الاتحاد حتى لو فيه approvedPrice فعلي بالـ entity
    @Mapping(target = "approvedPrice",
            expression = "java(request.getApprovedPrice() != null ? request.getApprovedPrice() : (request.getTest() != null ? request.getTest().getPrice() : null))")

    @Mapping(target = "employeeId",
            expression = "java(request.getMember() != null ? request.getMember().getEmployeeId() : null)")

    RadiologyRequestDTO toDto(RadiologyRequest request);

    // DTO → Entity
    @Mapping(source = "doctorId", target = "doctor.id")
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "radiologistId", target = "radiologist.id")
    @Mapping(source = "testId", target = "test.id")

    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "member", ignore = true)
    @Mapping(target = "radiologist", ignore = true)
    @Mapping(target = "test", ignore = true)

    RadiologyRequest toEntity(RadiologyRequestDTO dto);
}
