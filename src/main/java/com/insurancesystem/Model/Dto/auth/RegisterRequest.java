package com.insurancesystem.Model.Dto.auth;

import com.insurancesystem.Model.Dto.RegisterFamilyMemberDTO;
import com.insurancesystem.Model.Entity.Enums.ChronicDisease;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {



    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain letters and numbers"
    )
    private String password;


    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 150, message = "Full name must be between 3 and 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 150)
    private String email;

    @Size(max = 40)
    private String phone;

    private RoleName desiredRole; // INSURANCE_CLIENT, PHARMACY_STAFF, LAB_STAFF, CLINIC_STAFF...
    private boolean agreeToPolicy;

    // optional info (depends on role)
    private String employeeId;
    private String department;
    private String faculty;
    private String specialization;
    private String clinicLocation;
    private String pharmacyCode;
    private String pharmacyName;
    private String pharmacyLocation;
    private String labCode;
    private String labName;
    @NotBlank(message = "National ID is required")
    @Size(min = 9, max = 20, message = "National ID must be between 9 and 20 characters")
    private String nationalId;
    private String gender;
    private String labLocation;
    private String radiologyCode;
    private String radiologyName;
    private String radiologyLocation;
    @NotNull
    private LocalDate dateOfBirth;
    private List<RegisterFamilyMemberDTO> familyMembers;
    private boolean hasChronicDiseases;
    private List<ChronicDisease> chronicDiseases;


}