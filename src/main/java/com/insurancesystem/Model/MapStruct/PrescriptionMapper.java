package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;
@Mapper(componentModel = "spring", uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {

    @Mapping(target = "pharmacistId",
            expression = "java(entity.getPharmacist() != null ? entity.getPharmacist().getId() : null)")
    @Mapping(target = "pharmacistName",
            expression = "java(entity.getPharmacist() != null ? entity.getPharmacist().getFullName() : null)")

    @Mapping(target = "doctorName",
            expression = "java(entity.getDoctor() != null ? entity.getDoctor().getFullName() : null)")

    @Mapping(target = "memberId",
            expression = "java(entity.getMember() != null ? entity.getMember().getId() : null)")
    @Mapping(target = "memberName",
            expression = "java(entity.getMember() != null ? entity.getMember().getFullName() : null)")

    @Mapping(target = "employeeId",
            expression = "java(entity.getMember() != null ? entity.getMember().getEmployeeId() : null)")

    @Mapping(
            target = "universityCardImages",
            expression = "java(entity.getMember()!=null ? entity.getMember().getUniversityCardImages() : null)"
    )

    @Mapping(source = "items", target = "items")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    PrescriptionDTO toDto(Prescription entity);

    @Mapping(target = "pharmacist", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(source = "diagnosis", target = "diagnosis")
    @Mapping(source = "treatment", target = "treatment")
    Prescription toEntity(PrescriptionDTO dto);
}
