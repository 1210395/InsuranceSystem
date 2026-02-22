package com.insurancesystem.Model.Dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyVersionDTO {
    private UUID id;
    private UUID policyId;
    private String version;
    private String snapshot;
    private UUID changedById;
    private String changedByName;
    private String changeReason;
    private String createdAt;
}
