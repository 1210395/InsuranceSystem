package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/database")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_INSURANCE_MANAGER')")
@Slf4j
public class DatabaseResetController {

    private final JdbcTemplate jdbcTemplate;
    private final ClientRepository clientRepository;
    private final PasswordEncoder passwordEncoder;
    private final PolicyRepository policyRepository;
    private final SearchProfileRepository searchProfileRepository;
    private final HealthcareProviderClaimRepository claimRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final LabRequestRepository labRequestRepository;
    private final EmergencyRequestRepository emergencyRequestRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @PostMapping("/reset-and-seed")
    public ResponseEntity<Map<String, Object>> resetAndSeed() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, String>> createdUsers = new ArrayList<>();

        try {
            // Step 1: Delete all data
            jdbcTemplate.execute("TRUNCATE TABLE clients CASCADE");
            response.put("step1", "All data deleted successfully");

            // Step 2: Get or create default policy
            Policy defaultPolicy = policyRepository.findAll().stream().findFirst().orElse(null);
            if (defaultPolicy == null) {
                defaultPolicy = Policy.builder()
                        .policyNo("POL-001")
                        .name("Birzeit University Premium Plus Plan")
                        .description("Comprehensive health insurance for Birzeit University community")
                        .startDate(LocalDate.now())
                        .endDate(LocalDate.now().plusYears(1))
                        .coverageLimit(BigDecimal.valueOf(50000))
                        .deductible(BigDecimal.valueOf(100))
                        .status(PolicyStatus.ACTIVE)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();
                defaultPolicy = policyRepository.save(defaultPolicy);
            }

            // Step 3: Create test users with different roles
            List<Client> testUsers = new ArrayList<>();

            // 1. Manager
            testUsers.add(Client.builder()
                    .email("manager@insurance.com")
                    .passwordHash(passwordEncoder.encode("Manager123"))
                    .fullName("System Manager")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1980-01-01"))
                    .phone("0591111111")
                    .nationalId("900000001")
                    .requestedRole(RoleName.INSURANCE_MANAGER)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .employeeId("EMP001")
                    .department("Management")
                    .faculty("Business Administration")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 2. Medical Admin
            testUsers.add(Client.builder()
                    .email("medicaladmin@insurance.com")
                    .passwordHash(passwordEncoder.encode("Medical123"))
                    .fullName("Dr. Rami Yousef")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1975-06-15"))
                    .phone("0592222222")
                    .nationalId("900000002")
                    .requestedRole(RoleName.MEDICAL_ADMIN)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .employeeId("EMP002")
                    .department("Medical Affairs")
                    .faculty("Medicine")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 3. Doctor (General Practice)
            testUsers.add(Client.builder()
                    .email("doctor@insurance.com")
                    .passwordHash(passwordEncoder.encode("Doctor123"))
                    .fullName("Dr. Ahmad Hassan")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1985-05-10"))
                    .phone("0594444444")
                    .nationalId("900000004")
                    .requestedRole(RoleName.DOCTOR)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .employeeId("EMP004")
                    .department("Medical")
                    .faculty("Medicine")
                    .specialization("General Practice")
                    .clinicLocation("Ramallah Medical Center")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 5. Cardiologist
            testUsers.add(Client.builder()
                    .email("cardiologist@insurance.com")
                    .passwordHash(passwordEncoder.encode("Cardio123"))
                    .fullName("Dr. Sarah Mohammed")
                    .gender("F")
                    .dateOfBirth(LocalDate.parse("1982-08-22"))
                    .phone("0595555555")
                    .nationalId("900000005")
                    .requestedRole(RoleName.DOCTOR)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .employeeId("EMP005")
                    .department("Medical")
                    .faculty("Medicine")
                    .specialization("Cardiology")
                    .clinicLocation("Heart Care Clinic")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 6. Pharmacist
            testUsers.add(Client.builder()
                    .email("pharmacist@insurance.com")
                    .passwordHash(passwordEncoder.encode("Pharma123"))
                    .fullName("Khaled Ali")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1990-02-14"))
                    .phone("0596666666")
                    .nationalId("900000006")
                    .requestedRole(RoleName.PHARMACIST)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .pharmacyCode("PH001")
                    .pharmacyName("Al-Shifa Pharmacy")
                    .pharmacyLocation("Ramallah")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 7. Lab Technician
            testUsers.add(Client.builder()
                    .email("labtech@insurance.com")
                    .passwordHash(passwordEncoder.encode("Lab123"))
                    .fullName("Fatima Noor")
                    .gender("F")
                    .dateOfBirth(LocalDate.parse("1988-11-30"))
                    .phone("0597777777")
                    .nationalId("900000007")
                    .requestedRole(RoleName.LAB_TECH)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .labCode("LAB001")
                    .labName("Medical Lab Center")
                    .labLocation("Birzeit")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 8. Radiologist
            testUsers.add(Client.builder()
                    .email("radiologist@insurance.com")
                    .passwordHash(passwordEncoder.encode("Radio123"))
                    .fullName("Omar Saleh")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1983-07-18"))
                    .phone("0598888888")
                    .nationalId("900000008")
                    .requestedRole(RoleName.RADIOLOGIST)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .radiologyCode("RAD001")
                    .radiologyName("Imaging Center")
                    .radiologyLocation("Ramallah")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 9. Active Client
            testUsers.add(Client.builder()
                    .email("client@insurance.com")
                    .passwordHash(passwordEncoder.encode("Client123"))
                    .fullName("Mohammed Abdullah")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1995-03-25"))
                    .phone("0599999999")
                    .nationalId("900000009")
                    .requestedRole(RoleName.INSURANCE_CLIENT)
                    .status(MemberStatus.ACTIVE)
                    .roleRequestStatus(RoleRequestStatus.APPROVED)
                    .policy(defaultPolicy)
                    .employeeId("STU001")
                    .department("Computer Science")
                    .faculty("Engineering")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 10. Pending Client (needs approval)
            testUsers.add(Client.builder()
                    .email("pending@insurance.com")
                    .passwordHash(passwordEncoder.encode("Pending123"))
                    .fullName("Layla Ibrahim")
                    .gender("F")
                    .dateOfBirth(LocalDate.parse("1998-09-12"))
                    .phone("0591234567")
                    .nationalId("900000010")
                    .requestedRole(RoleName.INSURANCE_CLIENT)
                    .status(MemberStatus.INACTIVE)
                    .roleRequestStatus(RoleRequestStatus.PENDING)
                    .policy(defaultPolicy)
                    .employeeId("STU002")
                    .department("Business")
                    .faculty("Commerce")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 11. Pending Doctor (waiting for approval)
            testUsers.add(Client.builder()
                    .email("pending.doctor@insurance.com")
                    .passwordHash(passwordEncoder.encode("PendingDoc123"))
                    .fullName("Dr. Khaled Nasser")
                    .gender("M")
                    .dateOfBirth(LocalDate.parse("1987-04-15"))
                    .phone("0591112222")
                    .nationalId("900000011")
                    .requestedRole(RoleName.DOCTOR)
                    .status(MemberStatus.INACTIVE)
                    .roleRequestStatus(RoleRequestStatus.PENDING)
                    .policy(defaultPolicy)
                    .employeeId("EMP011")
                    .department("Medical")
                    .faculty("Medicine")
                    .specialization("Pediatrics")
                    .clinicLocation("Nablus Medical Center")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 12. Pending Insurance Client (waiting for approval)
            testUsers.add(Client.builder()
                    .email("pending.client@insurance.com")
                    .passwordHash(passwordEncoder.encode("PendingClient123"))
                    .fullName("Hana Mahmoud")
                    .gender("F")
                    .dateOfBirth(LocalDate.parse("1999-07-20"))
                    .phone("0592223333")
                    .nationalId("900000012")
                    .requestedRole(RoleName.INSURANCE_CLIENT)
                    .status(MemberStatus.INACTIVE)
                    .roleRequestStatus(RoleRequestStatus.PENDING)
                    .policy(defaultPolicy)
                    .employeeId("STU003")
                    .department("Computer Science")
                    .faculty("Engineering")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // 13. Pending Pharmacist (waiting for approval)
            testUsers.add(Client.builder()
                    .email("pending.pharmacist@insurance.com")
                    .passwordHash(passwordEncoder.encode("PendingPharma123"))
                    .fullName("Nour Sami")
                    .gender("F")
                    .dateOfBirth(LocalDate.parse("1991-09-10"))
                    .phone("0593334444")
                    .nationalId("900000013")
                    .requestedRole(RoleName.PHARMACIST)
                    .status(MemberStatus.INACTIVE)
                    .roleRequestStatus(RoleRequestStatus.PENDING)
                    .policy(defaultPolicy)
                    .pharmacyCode("PH002")
                    .pharmacyName("Hope Pharmacy")
                    .pharmacyLocation("Bethlehem")
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build());

            // Save all users
            List<Client> savedUsers = clientRepository.saveAll(testUsers);

            // Step 3: Create pending search profiles
            // Find the approved doctor and pharmacist to be owners
            Client doctorOwner = savedUsers.stream()
                    .filter(c -> c.getEmail().equals("doctor@insurance.com"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            Client pharmacistOwner = savedUsers.stream()
                    .filter(c -> c.getEmail().equals("pharmacist@insurance.com"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Pharmacist not found"));

            List<SearchProfile> searchProfiles = new ArrayList<>();

            // 1. Pending Clinic Profile
            searchProfiles.add(SearchProfile.builder()
                    .name("Al-Amal Family Clinic")
                    .type(SearchProfileType.CLINIC)
                    .status(ProfileStatus.PENDING)
                    .address("Ramallah, Main Street")
                    .contactInfo("02-2956789")
                    .owner(doctorOwner)
                    .medicalLicense("sample_license_1.pdf")
                    .universityDegree("sample_degree_1.pdf")
                    .clinicRegistration("sample_registration_1.pdf")
                    .idOrPassportCopy("sample_id_1.pdf")
                    .build());

            // 2. Pending Pharmacy Profile
            searchProfiles.add(SearchProfile.builder()
                    .name("Care Plus Pharmacy")
                    .type(SearchProfileType.PHARMACY)
                    .status(ProfileStatus.PENDING)
                    .address("Birzeit, University Road")
                    .contactInfo("02-2812345")
                    .owner(pharmacistOwner)
                    .medicalLicense("sample_license_2.pdf")
                    .universityDegree("sample_degree_2.pdf")
                    .clinicRegistration("sample_registration_2.pdf")
                    .idOrPassportCopy("sample_id_2.pdf")
                    .build());

            // Save search profiles
            searchProfileRepository.saveAll(searchProfiles);

            // ============================
            // Step 4: Create Sample Reports Data
            // ============================

            // Find doctor and client for creating sample data
            Client sampleDoctor = testUsers.stream()
                    .filter(u -> u.getRequestedRole() == RoleName.DOCTOR && u.getEmail().equals("doctor@insurance.com"))
                    .findFirst().orElse(null);
            Client sampleClient = testUsers.stream()
                    .filter(u -> u.getRequestedRole() == RoleName.INSURANCE_CLIENT && u.getEmail().equals("client@insurance.com"))
                    .findFirst().orElse(null);
            Client samplePharmacist = testUsers.stream()
                    .filter(u -> u.getRequestedRole() == RoleName.PHARMACIST)
                    .findFirst().orElse(null);
            Client sampleLabTech = testUsers.stream()
                    .filter(u -> u.getRequestedRole() == RoleName.LAB_TECH)
                    .findFirst().orElse(null);

            if (sampleDoctor != null && sampleClient != null) {
                // Create Sample Healthcare Provider Claims
                List<HealthcareProviderClaim> claims = new ArrayList<>();

                // Approved claims
                for (int i = 0; i < 5; i++) {
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(sampleDoctor)
                            .clientId(sampleClient.getId())
                            .clientName(sampleClient.getFullName())
                            .diagnosis("Sample Diagnosis " + (i + 1))
                            .treatmentDetails("Treatment details for claim " + (i + 1))
                            .description("Approved claim description " + (i + 1))
                            .amount(150.0 + (i * 50))
                            .serviceDate(LocalDate.now().minusDays(i * 3))
                            .status(ClaimStatus.APPROVED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(120.0 + (i * 40)))
                            .clientPayAmount(BigDecimal.valueOf(30.0 + (i * 10)))
                            .build());
                }

                // Rejected claims
                for (int i = 0; i < 3; i++) {
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(sampleDoctor)
                            .clientId(sampleClient.getId())
                            .clientName(sampleClient.getFullName())
                            .diagnosis("Rejected Diagnosis " + (i + 1))
                            .treatmentDetails("Treatment for rejected claim " + (i + 1))
                            .description("Rejected claim description " + (i + 1))
                            .amount(200.0 + (i * 30))
                            .serviceDate(LocalDate.now().minusDays(10 + i))
                            .status(ClaimStatus.REJECTED_FINAL)
                            .rejectionReason("Not covered under policy")
                            .policy(defaultPolicy)
                            .isCovered(false)
                            .build());
                }

                // Pending claims for Doctor
                for (int i = 0; i < 4; i++) {
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(sampleDoctor)
                            .clientId(sampleClient.getId())
                            .clientName(sampleClient.getFullName())
                            .diagnosis("Pending Diagnosis " + (i + 1))
                            .treatmentDetails("Treatment for pending claim " + (i + 1))
                            .description("Pending claim description " + (i + 1))
                            .amount(180.0 + (i * 25))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
                            .policy(defaultPolicy)
                            .build());
                }

                // ============================
                // AWAITING_COORDINATION_REVIEW claims for Doctor
                // These are claims approved by Medical Admin, waiting for Coordination Admin review
                // ============================
                for (int i = 0; i < 3; i++) {
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(sampleDoctor)
                            .clientId(sampleClient.getId())
                            .clientName(sampleClient.getFullName())
                            .diagnosis("Awaiting Coordination Review - " + (i + 1))
                            .treatmentDetails("Medical approved treatment " + (i + 1))
                            .description("Claim approved by medical admin, awaiting coordination review " + (i + 1))
                            .amount(220.0 + (i * 40))
                            .serviceDate(LocalDate.now().minusDays(i + 1))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(180.0 + (i * 30)))
                            .clientPayAmount(BigDecimal.valueOf(40.0 + (i * 10)))
                            .build());
                }

                // ============================
                // Claims for Pharmacist
                // ============================
                if (samplePharmacist != null) {
                    // Approved pharmacy claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(samplePharmacist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Pharmacy service - medication dispensing " + (i + 1))
                                .treatmentDetails("Dispensed prescribed medications")
                                .description("Approved pharmacy claim " + (i + 1))
                                .amount(80.0 + (i * 25))
                                .serviceDate(LocalDate.now().minusDays(i * 2))
                                .status(ClaimStatus.APPROVED_FINAL)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(60.0 + (i * 20)))
                                .clientPayAmount(BigDecimal.valueOf(20.0 + (i * 5)))
                                .build());
                    }

                    // Pending pharmacy claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(samplePharmacist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Pharmacy pending - " + (i + 1))
                                .treatmentDetails("Medication dispensing pending review")
                                .description("Pending pharmacy claim " + (i + 1))
                                .amount(95.0 + (i * 15))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
                                .policy(defaultPolicy)
                                .build());
                    }

                    // Awaiting Coordination Review pharmacy claims
                    for (int i = 0; i < 2; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(samplePharmacist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Pharmacy awaiting coordination - " + (i + 1))
                                .treatmentDetails("Medications approved by medical, awaiting coordination")
                                .description("Pharmacy claim awaiting coordination review " + (i + 1))
                                .amount(110.0 + (i * 20))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(85.0 + (i * 15)))
                                .clientPayAmount(BigDecimal.valueOf(25.0 + (i * 5)))
                                .build());
                    }
                }

                // ============================
                // Claims for Lab Technician
                // ============================
                if (sampleLabTech != null) {
                    // Approved lab claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleLabTech)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Lab service - diagnostic testing " + (i + 1))
                                .treatmentDetails("Performed laboratory analysis")
                                .description("Approved lab claim " + (i + 1))
                                .amount(120.0 + (i * 30))
                                .serviceDate(LocalDate.now().minusDays(i * 3))
                                .status(ClaimStatus.APPROVED_FINAL)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(100.0 + (i * 25)))
                                .clientPayAmount(BigDecimal.valueOf(20.0 + (i * 5)))
                                .build());
                    }

                    // Pending lab claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleLabTech)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Lab pending - blood work " + (i + 1))
                                .treatmentDetails("Laboratory testing pending review")
                                .description("Pending lab claim " + (i + 1))
                                .amount(110.0 + (i * 20))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
                                .policy(defaultPolicy)
                                .build());
                    }

                    // Awaiting Coordination Review lab claims
                    for (int i = 0; i < 2; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleLabTech)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Lab awaiting coordination - CBC test " + (i + 1))
                                .treatmentDetails("Lab results approved by medical, awaiting coordination")
                                .description("Lab claim awaiting coordination review " + (i + 1))
                                .amount(140.0 + (i * 25))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(115.0 + (i * 20)))
                                .clientPayAmount(BigDecimal.valueOf(25.0 + (i * 5)))
                                .build());
                    }
                }

                // ============================
                // Claims for Radiologist
                // ============================
                Client sampleRadiologist = testUsers.stream()
                        .filter(u -> u.getRequestedRole() == RoleName.RADIOLOGIST)
                        .findFirst().orElse(null);

                if (sampleRadiologist != null) {
                    // Approved radiology claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleRadiologist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Radiology service - imaging " + (i + 1))
                                .treatmentDetails("Performed diagnostic imaging")
                                .description("Approved radiology claim " + (i + 1))
                                .amount(200.0 + (i * 50))
                                .serviceDate(LocalDate.now().minusDays(i * 4))
                                .status(ClaimStatus.APPROVED_FINAL)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(170.0 + (i * 40)))
                                .clientPayAmount(BigDecimal.valueOf(30.0 + (i * 10)))
                                .build());
                    }

                    // Pending radiology claims
                    for (int i = 0; i < 3; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleRadiologist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Radiology pending - X-ray " + (i + 1))
                                .treatmentDetails("Imaging pending medical review")
                                .description("Pending radiology claim " + (i + 1))
                                .amount(180.0 + (i * 35))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
                                .policy(defaultPolicy)
                                .build());
                    }

                    // Awaiting Coordination Review radiology claims
                    for (int i = 0; i < 2; i++) {
                        claims.add(HealthcareProviderClaim.builder()
                                .healthcareProvider(sampleRadiologist)
                                .clientId(sampleClient.getId())
                                .clientName(sampleClient.getFullName())
                                .diagnosis("Radiology awaiting coordination - MRI " + (i + 1))
                                .treatmentDetails("Imaging approved by medical, awaiting coordination")
                                .description("Radiology claim awaiting coordination review " + (i + 1))
                                .amount(280.0 + (i * 50))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                                .policy(defaultPolicy)
                                .isCovered(true)
                                .insuranceCoveredAmount(BigDecimal.valueOf(230.0 + (i * 40)))
                                .clientPayAmount(BigDecimal.valueOf(50.0 + (i * 10)))
                                .build());
                    }
                }

                claimRepository.saveAll(claims);

                // Create Sample Prescriptions
                List<Prescription> prescriptions = new ArrayList<>();

                // Verified prescriptions
                for (int i = 0; i < 4; i++) {
                    prescriptions.add(Prescription.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .pharmacist(samplePharmacist)
                            .status(PrescriptionStatus.VERIFIED)
                            .diagnosis("Verified prescription diagnosis " + (i + 1))
                            .treatment("Treatment plan " + (i + 1))
                            .totalPrice(50.0 + (i * 20))
                            .createdAt(Instant.now().minusSeconds(86400 * (i + 1)))
                            .build());
                }

                // Pending prescriptions
                for (int i = 0; i < 3; i++) {
                    prescriptions.add(Prescription.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .status(PrescriptionStatus.PENDING)
                            .diagnosis("Pending prescription diagnosis " + (i + 1))
                            .treatment("Pending treatment " + (i + 1))
                            .totalPrice(40.0 + (i * 15))
                            .createdAt(Instant.now().minusSeconds(43200 * (i + 1)))
                            .build());
                }

                // Rejected prescriptions
                for (int i = 0; i < 2; i++) {
                    prescriptions.add(Prescription.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .status(PrescriptionStatus.REJECTED)
                            .diagnosis("Rejected prescription " + (i + 1))
                            .treatment("Rejected treatment " + (i + 1))
                            .totalPrice(30.0)
                            .createdAt(Instant.now().minusSeconds(172800 * (i + 1)))
                            .build());
                }

                prescriptionRepository.saveAll(prescriptions);

                // Create Sample Lab Requests
                List<LabRequest> labRequests = new ArrayList<>();

                // Completed lab requests
                for (int i = 0; i < 5; i++) {
                    labRequests.add(LabRequest.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .labTech(sampleLabTech)
                            .testName("Blood Test " + (i + 1))
                            .notes("Lab notes " + (i + 1))
                            .status(LabRequestStatus.COMPLETED)
                            .diagnosis("Lab diagnosis " + (i + 1))
                            .enteredPrice(75.0 + (i * 10))
                            .approvedPrice(70.0 + (i * 10))
                            .createdAt(Instant.now().minusSeconds(86400 * (i + 1)))
                            .build());
                }

                // Pending lab requests
                for (int i = 0; i < 3; i++) {
                    labRequests.add(LabRequest.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .testName("CBC Test " + (i + 1))
                            .notes("Pending lab notes " + (i + 1))
                            .status(LabRequestStatus.PENDING)
                            .diagnosis("Pending lab diagnosis " + (i + 1))
                            .createdAt(Instant.now().minusSeconds(43200 * (i + 1)))
                            .build());
                }

                labRequestRepository.saveAll(labRequests);

                // Create Sample Emergency Requests
                List<EmergencyRequest> emergencyRequests = new ArrayList<>();

                // Approved emergency requests (using legacy APPROVED status for DB compatibility)
                for (int i = 0; i < 3; i++) {
                    emergencyRequests.add(EmergencyRequest.builder()
                            .member(sampleClient)
                            .doctorId(sampleDoctor.getId())
                            .description("Emergency case " + (i + 1) + " - chest pain")
                            .location("Birzeit University Campus")
                            .contactPhone("059" + (1000000 + i))
                            .incidentDate(LocalDate.now().minusDays(i * 2))
                            .status(EmergencyStatus.APPROVED)
                            .submittedAt(Instant.now().minusSeconds(86400 * (i + 1)))
                            .approvedAt(Instant.now().minusSeconds(43200 * (i + 1)))
                            .build());
                }

                // Pending emergency requests
                for (int i = 0; i < 2; i++) {
                    emergencyRequests.add(EmergencyRequest.builder()
                            .member(sampleClient)
                            .doctorId(sampleDoctor.getId())
                            .description("Pending emergency " + (i + 1))
                            .location("Ramallah Hospital")
                            .contactPhone("059" + (2000000 + i))
                            .incidentDate(LocalDate.now())
                            .status(EmergencyStatus.PENDING)
                            .submittedAt(Instant.now())
                            .build());
                }

                // Rejected emergency requests
                emergencyRequests.add(EmergencyRequest.builder()
                        .member(sampleClient)
                        .doctorId(sampleDoctor.getId())
                        .description("Rejected emergency case")
                        .location("Home")
                        .contactPhone("0593000000")
                        .incidentDate(LocalDate.now().minusDays(5))
                        .status(EmergencyStatus.REJECTED)
                        .rejectionReason("Not a valid emergency case")
                        .submittedAt(Instant.now().minusSeconds(432000))
                        .rejectedAt(Instant.now().minusSeconds(345600))
                        .build());

                emergencyRequestRepository.saveAll(emergencyRequests);

                // Create Sample Medical Records
                List<MedicalRecord> medicalRecords = new ArrayList<>();
                for (int i = 0; i < 8; i++) {
                    medicalRecords.add(MedicalRecord.builder()
                            .doctor(sampleDoctor)
                            .member(sampleClient)
                            .diagnosis("Medical record diagnosis " + (i + 1))
                            .treatment("Treatment plan for record " + (i + 1))
                            .notes("Clinical notes " + (i + 1))
                            .createdAt(Instant.now().minusSeconds(86400 * (i + 1)))
                            .build());
                }
                medicalRecordRepository.saveAll(medicalRecords);

                response.put("step3", "Sample reports data created successfully");
                response.put("sampleData", Map.of(
                        "claims", claims.size(),
                        "prescriptions", prescriptions.size(),
                        "labRequests", labRequests.size(),
                        "emergencyRequests", emergencyRequests.size(),
                        "medicalRecords", medicalRecords.size()
                ));
            }

            // Prepare response
            for (Client user : testUsers) {
                Map<String, String> userInfo = new HashMap<>();
                userInfo.put("email", user.getEmail());
                userInfo.put("password", getPasswordForRole(user.getRequestedRole()));
                userInfo.put("role", user.getRequestedRole().name());
                userInfo.put("status", user.getStatus().name());
                userInfo.put("fullName", user.getFullName());
                createdUsers.add(userInfo);
            }

            response.put("step2", "Test users created successfully");
            response.put("totalUsers", testUsers.size());
            response.put("users", createdUsers);
            response.put("success", true);
            response.put("message", "Database reset and seeded successfully! All users use email for login.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Failed to reset and seed database", e);
            return ResponseEntity.status(500).body(response);
        }
    }

    private String getPasswordForRole(RoleName role) {
        return switch (role) {
            case INSURANCE_MANAGER -> "Manager123";
            case MEDICAL_ADMIN -> "Medical123";
            case COORDINATION_ADMIN -> "Coordination123";
            case DOCTOR -> "Doctor123 or Cardio123 or PendingDoc123";
            case PHARMACIST -> "Pharma123 or PendingPharma123";
            case LAB_TECH -> "Lab123";
            case RADIOLOGIST -> "Radio123";
            case INSURANCE_CLIENT -> "Client123 or Pending123 or PendingClient123";
        };
    }

    @PostMapping("/seed-claims")
    public ResponseEntity<Map<String, Object>> seedClaims() {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find healthcare providers by role
            List<Client> doctors = clientRepository.findAll().stream()
                    .filter(c -> c.getRoles() != null && c.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.DOCTOR))
                    .toList();

            List<Client> pharmacists = clientRepository.findAll().stream()
                    .filter(c -> c.getRoles() != null && c.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.PHARMACIST))
                    .toList();

            List<Client> labTechs = clientRepository.findAll().stream()
                    .filter(c -> c.getRoles() != null && c.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.LAB_TECH))
                    .toList();

            List<Client> radiologists = clientRepository.findAll().stream()
                    .filter(c -> c.getRoles() != null && c.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.RADIOLOGIST))
                    .toList();

            List<Client> insuranceClients = clientRepository.findAll().stream()
                    .filter(c -> c.getRoles() != null && c.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT))
                    .toList();

            // Get default policy
            Policy defaultPolicy = policyRepository.findAll().stream().findFirst().orElse(null);

            if (defaultPolicy == null) {
                response.put("success", false);
                response.put("error", "No policy found in database");
                return ResponseEntity.status(400).body(response);
            }

            if (insuranceClients.isEmpty()) {
                response.put("success", false);
                response.put("error", "No insurance clients found");
                return ResponseEntity.status(400).body(response);
            }

            List<HealthcareProviderClaim> claims = new ArrayList<>();
            // Use all insurance clients, rotating through them
            int[] clientIndex = {0}; // Array to allow modification in lambdas

            // Create claims for Doctors
            if (!doctors.isEmpty()) {
                Client doctor = doctors.get(0);

                // Pending Medical claims - distribute across clients
                for (int i = 0; i < 3; i++) {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(doctor)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("General consultation - " + (i + 1))
                            .treatmentDetails("Treatment plan " + (i + 1))
                            .description("Doctor consultation - Pending medical review " + (i + 1))
                            .amount(150.0 + (i * 50))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
                            .policy(defaultPolicy)
                            .doctorName(doctor.getFullName())
                            .build());
                }

                // Approved Final claims - distribute across clients
                for (int i = 0; i < 2; i++) {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(doctor)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Approved diagnosis " + (i + 1))
                            .treatmentDetails("Approved treatment " + (i + 1))
                            .description("Doctor consultation - Approved " + (i + 1))
                            .amount(200.0 + (i * 30))
                            .serviceDate(LocalDate.now().minusDays(10 + i))
                            .status(ClaimStatus.APPROVED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(180.0 + (i * 25)))
                            .clientPayAmount(BigDecimal.valueOf(20.0 + (i * 5)))
                            .doctorName(doctor.getFullName())
                            .approvedAt(Instant.now().minusSeconds(86400 * (i + 5)))
                            .build());
                }

                // Awaiting Coordination claims - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(doctor)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Awaiting coordination - cardiology")
                            .treatmentDetails("Cardiology treatment plan")
                            .description("Doctor consultation - Awaiting coordination review")
                            .amount(350.0)
                            .serviceDate(LocalDate.now().minusDays(5))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(300.0))
                            .clientPayAmount(BigDecimal.valueOf(50.0))
                            .doctorName(doctor.getFullName())
                            .medicalReviewedAt(Instant.now().minusSeconds(86400 * 3))
                            .build());
                }

                // Rejected Final claim - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(doctor)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Cosmetic procedure")
                            .treatmentDetails("Cosmetic surgery")
                            .description("Doctor consultation - Rejected (not covered)")
                            .amount(1500.0)
                            .serviceDate(LocalDate.now().minusDays(20))
                            .status(ClaimStatus.REJECTED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(false)
                            .rejectionReason("Cosmetic procedures are not covered under the policy")
                            .doctorName(doctor.getFullName())
                            .rejectedAt(Instant.now().minusSeconds(86400 * 15))
                            .build());
                }
            }

            // Create claims for Pharmacists
            if (!pharmacists.isEmpty()) {
                Client pharmacist = pharmacists.get(0);

                // Pending Medical claims - distribute across clients
                for (int i = 0; i < 2; i++) {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(pharmacist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Prescription fill - " + (i + 1))
                            .treatmentDetails("Medication dispensing")
                            .description("Pharmacy claim - Pending " + (i + 1))
                            .amount(85.0 + (i * 20))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
                            .policy(defaultPolicy)
                            .doctorName("Dr. Ahmad")
                            .roleSpecificData("{\"prescriptionNumber\": \"RX-2024-" + (i + 1) + "\"}")
                            .build());
                }

                // Approved Final claims - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(pharmacist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Chronic medication refill")
                            .treatmentDetails("Monthly chronic medication")
                            .description("Pharmacy claim - Approved chronic meds")
                            .amount(120.0)
                            .serviceDate(LocalDate.now().minusDays(7))
                            .status(ClaimStatus.APPROVED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .isChronic(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(108.0))
                            .clientPayAmount(BigDecimal.valueOf(12.0))
                            .doctorName("Dr. Khaled")
                            .approvedAt(Instant.now().minusSeconds(86400 * 5))
                            .build());
                }

                // Awaiting Coordination claim - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(pharmacist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Specialty medication")
                            .treatmentDetails("High-cost specialty drug")
                            .description("Pharmacy claim - Awaiting coordination")
                            .amount(450.0)
                            .serviceDate(LocalDate.now().minusDays(3))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(400.0))
                            .clientPayAmount(BigDecimal.valueOf(50.0))
                            .doctorName("Dr. Sara")
                            .medicalReviewedAt(Instant.now().minusSeconds(86400 * 2))
                            .build());
                }
            }

            // Create claims for Lab Techs
            if (!labTechs.isEmpty()) {
                Client labTech = labTechs.get(0);

                // Pending Medical claims - distribute across clients
                for (int i = 0; i < 2; i++) {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(labTech)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Blood work - CBC " + (i + 1))
                            .treatmentDetails("Complete blood count analysis")
                            .description("Lab claim - Pending " + (i + 1))
                            .amount(110.0 + (i * 30))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
                            .policy(defaultPolicy)
                            .doctorName("Dr. Khaled")
                            .roleSpecificData("{\"testType\": \"Blood Work\", \"tests\": [\"CBC\", \"Lipid Panel\"]}")
                            .build());
                }

                // Approved Final claims - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(labTech)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Thyroid panel")
                            .treatmentDetails("TSH, Free T4 analysis")
                            .description("Lab claim - Approved thyroid test")
                            .amount(150.0)
                            .serviceDate(LocalDate.now().minusDays(10))
                            .status(ClaimStatus.APPROVED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(135.0))
                            .clientPayAmount(BigDecimal.valueOf(15.0))
                            .doctorName("Dr. Hana")
                            .approvedAt(Instant.now().minusSeconds(86400 * 8))
                            .build());
                }

                // Awaiting Coordination claim - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(labTech)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Comprehensive metabolic panel")
                            .treatmentDetails("Full metabolic analysis")
                            .description("Lab claim - Awaiting coordination")
                            .amount(200.0)
                            .serviceDate(LocalDate.now().minusDays(4))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(180.0))
                            .clientPayAmount(BigDecimal.valueOf(20.0))
                            .doctorName("Dr. Ahmad")
                            .medicalReviewedAt(Instant.now().minusSeconds(86400 * 2))
                            .build());
                }
            }

            // Create claims for Radiologists
            if (!radiologists.isEmpty()) {
                Client radiologist = radiologists.get(0);

                // Pending Medical claims - distribute across clients
                for (int i = 0; i < 2; i++) {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(radiologist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("X-Ray - Chest " + (i + 1))
                            .treatmentDetails("Chest radiograph PA and Lateral")
                            .description("Radiology claim - Pending " + (i + 1))
                            .amount(180.0 + (i * 50))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
                            .policy(defaultPolicy)
                            .doctorName("Dr. Sara")
                            .roleSpecificData("{\"imagingType\": \"X-Ray\", \"bodyPart\": \"Chest\"}")
                            .build());
                }

                // Approved Final claims - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(radiologist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("MRI - Knee")
                            .treatmentDetails("MRI scan of right knee")
                            .description("Radiology claim - Approved MRI")
                            .amount(600.0)
                            .serviceDate(LocalDate.now().minusDays(15))
                            .status(ClaimStatus.APPROVED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(540.0))
                            .clientPayAmount(BigDecimal.valueOf(60.0))
                            .doctorName("Dr. Nadia")
                            .approvedAt(Instant.now().minusSeconds(86400 * 12))
                            .build());
                }

                // Awaiting Coordination claim - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(radiologist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("CT Scan - Abdomen")
                            .treatmentDetails("Abdominal CT with contrast")
                            .description("Radiology claim - Awaiting coordination")
                            .amount(800.0)
                            .serviceDate(LocalDate.now().minusDays(6))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                            .policy(defaultPolicy)
                            .isCovered(true)
                            .insuranceCoveredAmount(BigDecimal.valueOf(720.0))
                            .clientPayAmount(BigDecimal.valueOf(80.0))
                            .doctorName("Dr. Ahmad")
                            .medicalReviewedAt(Instant.now().minusSeconds(86400 * 4))
                            .build());
                }

                // Rejected Final claim - rotate client
                {
                    Client client = insuranceClients.get(clientIndex[0] % insuranceClients.size());
                    clientIndex[0]++;
                    claims.add(HealthcareProviderClaim.builder()
                            .healthcareProvider(radiologist)
                            .clientId(client.getId())
                            .clientName(client.getFullName())
                            .diagnosis("Full body MRI - preventive")
                            .treatmentDetails("Preventive full body scan")
                            .description("Radiology claim - Rejected (preventive not covered)")
                            .amount(2500.0)
                            .serviceDate(LocalDate.now().minusDays(25))
                            .status(ClaimStatus.REJECTED_FINAL)
                            .policy(defaultPolicy)
                            .isCovered(false)
                            .rejectionReason("Preventive full body scans are not covered")
                            .doctorName("Dr. Youssef")
                            .rejectedAt(Instant.now().minusSeconds(86400 * 20))
                            .build());
                }
            }

            // Save all claims
            claimRepository.saveAll(claims);

            response.put("success", true);
            response.put("totalClaims", claims.size());
            response.put("message", "Claims seeded successfully!");
            response.put("breakdown", Map.of(
                    "doctors", doctors.size(),
                    "pharmacists", pharmacists.size(),
                    "labTechs", labTechs.size(),
                    "radiologists", radiologists.size()
            ));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            log.error("Failed to seed claims", e);
            return ResponseEntity.status(500).body(response);
        }
    }
}
