package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Entity.PrescriptionItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PrescriptionItemMapper {

    @Mapping(source = "medicine.id", target = "medicineId")
    @Mapping(source = "medicine.name", target = "medicineName")
    @Mapping(source = "medicine.scientificName", target = "scientificName")
    @Mapping(source = "medicine.quantity", target = "medicineQuantity")
    @Mapping(source = "medicine.unionPrice", target = "unionPrice")
    PrescriptionItemDTO toDto(PrescriptionItem entity);

    @Mapping(target = "medicine", ignore = true)
    @Mapping(target = "prescription", ignore = true)
    @Mapping(target = "finalPrice", ignore = true)
    @Mapping(target = "expiryDate", ignore = true)
    PrescriptionItem toEntity(PrescriptionItemDTO dto);
}