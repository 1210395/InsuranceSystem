package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.FamilyMemberDTO;
import com.insurancesystem.Model.Entity.FamilyMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FamilyMemberMapper {
    @Mapping(source = "status", target = "status")
    @Mapping(target = "documentImages", source = "documentImages")
    FamilyMemberDTO toDto(FamilyMember entity);
}
