package com.insurancesystem.Model.MapStruct;

import com.insurancesystem.Model.Dto.TestDTO;
import com.insurancesystem.Model.Entity.Test;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestMapper {

    TestDTO toDto(Test test);

    Test toEntity(TestDTO testDTO);

}

