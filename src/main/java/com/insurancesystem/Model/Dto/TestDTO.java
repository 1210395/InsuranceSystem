package com.insurancesystem.Model.Dto;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO {

    private UUID id;

    private String testName; // اسم الفحص

    private Double unionPrice; // السعر النقابي

    private Instant createdAt;

    private Instant updatedAt;

}

