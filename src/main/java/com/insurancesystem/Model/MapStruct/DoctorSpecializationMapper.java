package com.insurancesystem.Model.MapStruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Dto.DoctorSpecializationRequestDto;
import com.insurancesystem.Model.Dto.DoctorSpecializationResponseDto;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface DoctorSpecializationMapper {

    ObjectMapper objectMapper = new ObjectMapper();

    // Map from Entity to ResponseDto
    @Mapping(source = "diagnosisTreatmentMappings", target = "diagnosisTreatmentMappings", qualifiedByName = "jsonToMap")
    DoctorSpecializationResponseDto toResponseDto(DoctorSpecializationEntity entity);

    // Map from RequestDto to Entity
    @Mapping(source = "diagnosisTreatmentMappings", target = "diagnosisTreatmentMappings", qualifiedByName = "mapToJson")
    DoctorSpecializationEntity toEntity(DoctorSpecializationRequestDto requestDto);

    @Named("jsonToMap")
    default Map<String, List<String>> jsonToMap(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, List<String>>>() {});
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }

    @Named("mapToJson")
    default String mapToJson(Map<String, List<String>> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
