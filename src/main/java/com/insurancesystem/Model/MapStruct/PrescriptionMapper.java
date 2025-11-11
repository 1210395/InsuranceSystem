package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Entity.Prescription;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {PrescriptionItemMapper.class})
public interface PrescriptionMapper {

    @Mapping(source = "pharmacist.fullName", target = "pharmacistName")
    @Mapping(source = "member.id", target = "memberId")
    @Mapping(source = "doctor.fullName", target = "doctorName")
    @Mapping(source = "member.fullName", target = "memberName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "items", target = "items") // 🆕 قائمة الأدوية
    @Mapping(source = "totalPrice", target = "totalPrice") // 🆕 المجموع
    @Mapping(source = "notes", target = "notes") // 🆕 الملاحظات
    PrescriptionDTO toDto(Prescription entity);

    @Mapping(target = "pharmacist", ignore = true)
    @Mapping(source = "memberId", target = "member.id")
    @Mapping(target = "doctor", ignore = true) // doctor يجي من الـ Service
    @Mapping(target = "status", ignore = true) // default → PENDING
    @Mapping(target = "items", ignore = true) // 🆕 نبنيها في الـ Service
    @Mapping(target = "totalPrice", ignore = true) // 🆕 نحسبها في الـ Service
    Prescription toEntity(PrescriptionDTO dto);
}