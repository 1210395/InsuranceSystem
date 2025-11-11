package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.LabTestDTO;
import com.insurancesystem.Model.Entity.LabTest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LabTestMapper {

    LabTestDTO toDto(LabTest labTest);

    LabTest toEntity(LabTestDTO labTestDTO);

}

