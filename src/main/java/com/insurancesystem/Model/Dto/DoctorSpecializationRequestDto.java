package com.insurancesystem.Model.Dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DoctorSpecializationRequestDto {

    private String displayName; // Name of the specialization
    private double consultationPrice; // Price of the consultation
    private List<String> diagnoses; // Diagnoses related to the specialization
    private List<String> treatmentPlans; // Treatment plans related to the specialization

    /**
     * Mapping of diagnosis to its associated treatments
     * Key: diagnosis name, Value: list of treatment names linked to this diagnosis
     */
    private Map<String, List<String>> diagnosisTreatmentMappings;

    /**
     * List of allowed genders for this specialization (e.g., "MALE", "FEMALE")
     * If null or empty, the specialization can treat ALL genders
     */
    private List<String> allowedGenders;

    /**
     * Minimum age that this specialization can treat
     * If null, there is no minimum age restriction
     */
    private Integer minAge;

    /**
     * Maximum age that this specialization can treat
     * If null, there is no maximum age restriction
     */
    private Integer maxAge;

    /**
     * Gender restriction for this specialization (e.g., "MALE", "FEMALE", "ALL")
     * If null or "ALL", the specialization can treat all genders
     */
    private String gender;
}
