package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.DoctorSpecializationRequestDto;
import com.insurancesystem.Model.Dto.DoctorSpecializationResponseDto;
import com.insurancesystem.Model.Entity.DoctorSpecializationEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorSpecializationMapper {

    // Map from Entity to ResponseDto
    public DoctorSpecializationResponseDto toResponseDto(DoctorSpecializationEntity entity) {
        if (entity == null) {
            return null;
        }

        DoctorSpecializationResponseDto responseDto = new DoctorSpecializationResponseDto();
        responseDto.setId(entity.getId());
        responseDto.setDisplayName(entity.getDisplayName());
        responseDto.setConsultationPrice(entity.getConsultationPrice());
        responseDto.setDiagnoses(entity.getDiagnoses());
        responseDto.setTreatmentPlans(entity.getTreatmentPlans());
        responseDto.setAllowedGenders(entity.getAllowedGenders());
        responseDto.setMinAge(entity.getMinAge());
        responseDto.setMaxAge(entity.getMaxAge());
        responseDto.setGender(entity.getGender());
        return responseDto;
    }

    // Map from RequestDto to Entity
    public DoctorSpecializationEntity toEntity(DoctorSpecializationRequestDto requestDto) {
        if (requestDto == null) {
            return null;
        }

        DoctorSpecializationEntity entity = new DoctorSpecializationEntity();
        entity.setDisplayName(requestDto.getDisplayName());
        entity.setConsultationPrice(requestDto.getConsultationPrice());
        entity.setDiagnoses(requestDto.getDiagnoses());
        entity.setTreatmentPlans(requestDto.getTreatmentPlans());
        entity.setAllowedGenders(requestDto.getAllowedGenders());
        entity.setMinAge(requestDto.getMinAge());
        entity.setMaxAge(requestDto.getMaxAge());
        entity.setGender(requestDto.getGender());
        return entity;
    }


}