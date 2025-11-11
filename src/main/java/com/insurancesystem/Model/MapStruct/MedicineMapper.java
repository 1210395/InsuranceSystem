package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.MedicineDTO;
import com.insurancesystem.Model.Entity.Medicine;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MedicineMapper {
    MedicineDTO toDto(Medicine entity);
    Medicine toEntity(MedicineDTO dto);
}