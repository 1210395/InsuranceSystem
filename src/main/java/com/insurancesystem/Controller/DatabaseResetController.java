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
    private final GlobalPolicyRepository globalPolicyRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final CategoryLimitsRepository categoryLimitsRepository;
    private final ClientLimitsRepository clientLimitsRepository;

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
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
                            .amount(BigDecimal.valueOf(150.0 + (i * 50)))
                            .serviceDate(LocalDate.now().minusDays(i * 3))
                            .status(ClaimStatus.APPROVED_FINAL)
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
                            .amount(BigDecimal.valueOf(200.0 + (i * 30)))
                            .serviceDate(LocalDate.now().minusDays(10 + i))
                            .status(ClaimStatus.REJECTED_FINAL)
                            .rejectionReason("Not covered under policy")
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
                            .amount(BigDecimal.valueOf(180.0 + (i * 25)))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
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
                            .amount(BigDecimal.valueOf(220.0 + (i * 40)))
                            .serviceDate(LocalDate.now().minusDays(i + 1))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                                .amount(BigDecimal.valueOf(80.0 + (i * 25)))
                                .serviceDate(LocalDate.now().minusDays(i * 2))
                                .status(ClaimStatus.APPROVED_FINAL)
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
                                .amount(BigDecimal.valueOf(95.0 + (i * 15)))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
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
                                .amount(BigDecimal.valueOf(110.0 + (i * 20)))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                                .amount(BigDecimal.valueOf(120.0 + (i * 30)))
                                .serviceDate(LocalDate.now().minusDays(i * 3))
                                .status(ClaimStatus.APPROVED_FINAL)
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
                                .amount(BigDecimal.valueOf(110.0 + (i * 20)))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
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
                                .amount(BigDecimal.valueOf(140.0 + (i * 25)))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                                .amount(BigDecimal.valueOf(200.0 + (i * 50)))
                                .serviceDate(LocalDate.now().minusDays(i * 4))
                                .status(ClaimStatus.APPROVED_FINAL)
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
                                .amount(BigDecimal.valueOf(180.0 + (i * 35)))
                                .serviceDate(LocalDate.now().minusDays(i))
                                .status(ClaimStatus.PENDING_MEDICAL)
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
                                .amount(BigDecimal.valueOf(280.0 + (i * 50)))
                                .serviceDate(LocalDate.now().minusDays(i + 1))
                                .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                            .totalPrice(BigDecimal.valueOf(50.0 + (i * 20)))
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
                            .totalPrice(BigDecimal.valueOf(40.0 + (i * 15)))
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
                            .totalPrice(BigDecimal.valueOf(30.0))
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
                            .enteredPrice(BigDecimal.valueOf(75.0 + (i * 10)))
                            .approvedPrice(BigDecimal.valueOf(70.0 + (i * 10)))
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

    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
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
                            .amount(BigDecimal.valueOf(150.0 + (i * 50)))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
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
                            .amount(BigDecimal.valueOf(200.0 + (i * 30)))
                            .serviceDate(LocalDate.now().minusDays(10 + i))
                            .status(ClaimStatus.APPROVED_FINAL)
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
                            .amount(BigDecimal.valueOf(350.0))
                            .serviceDate(LocalDate.now().minusDays(5))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                            .amount(BigDecimal.valueOf(1500.0))
                            .serviceDate(LocalDate.now().minusDays(20))
                            .status(ClaimStatus.REJECTED_FINAL)
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
                            .amount(BigDecimal.valueOf(85.0 + (i * 20)))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
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
                            .amount(BigDecimal.valueOf(120.0))
                            .serviceDate(LocalDate.now().minusDays(7))
                            .status(ClaimStatus.APPROVED_FINAL)
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
                            .amount(BigDecimal.valueOf(450.0))
                            .serviceDate(LocalDate.now().minusDays(3))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                            .amount(BigDecimal.valueOf(110.0 + (i * 30)))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
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
                            .amount(BigDecimal.valueOf(150.0))
                            .serviceDate(LocalDate.now().minusDays(10))
                            .status(ClaimStatus.APPROVED_FINAL)
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
                            .amount(BigDecimal.valueOf(200.0))
                            .serviceDate(LocalDate.now().minusDays(4))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                            .amount(BigDecimal.valueOf(180.0 + (i * 50)))
                            .serviceDate(LocalDate.now().minusDays(i))
                            .status(ClaimStatus.PENDING_MEDICAL)
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
                            .amount(BigDecimal.valueOf(600.0))
                            .serviceDate(LocalDate.now().minusDays(15))
                            .status(ClaimStatus.APPROVED_FINAL)
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
                            .amount(BigDecimal.valueOf(800.0))
                            .serviceDate(LocalDate.now().minusDays(6))
                            .status(ClaimStatus.AWAITING_COORDINATION_REVIEW)
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
                            .amount(BigDecimal.valueOf(2500.0))
                            .serviceDate(LocalDate.now().minusDays(25))
                            .status(ClaimStatus.REJECTED_FINAL)
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

    /**
     * Cleans up all old policy-related data (claims, prescriptions, lab requests, etc.)
     * while keeping all accounts, then creates a fresh GlobalPolicy with comprehensive
     * service categories, coverages, and limits.
     */
    @PreAuthorize("hasRole('INSURANCE_MANAGER')")
    @PostMapping("/reset-policy-data")
    public ResponseEntity<Map<String, Object>> resetPolicyData() {
        Map<String, Object> response = new HashMap<>();

        try {
            log.info("Starting policy data reset - keeping accounts, removing all claims and policy data...");

            // Step 1: Delete all claims and related data
            long claimsDeleted = claimRepository.count();
            claimRepository.deleteAll();
            log.info("Deleted {} healthcare provider claims", claimsDeleted);

            long prescriptionsDeleted = prescriptionRepository.count();
            prescriptionRepository.deleteAll();
            log.info("Deleted {} prescriptions", prescriptionsDeleted);

            long labRequestsDeleted = labRequestRepository.count();
            labRequestRepository.deleteAll();
            log.info("Deleted {} lab requests", labRequestsDeleted);

            long emergencyRequestsDeleted = emergencyRequestRepository.count();
            emergencyRequestRepository.deleteAll();
            log.info("Deleted {} emergency requests", emergencyRequestsDeleted);

            long medicalRecordsDeleted = medicalRecordRepository.count();
            medicalRecordRepository.deleteAll();
            log.info("Deleted {} medical records", medicalRecordsDeleted);

            // Step 2: Delete all old GlobalPolicy related data
            clientLimitsRepository.deleteAll();
            categoryLimitsRepository.deleteAll();
            serviceCoverageRepository.deleteAll();
            globalPolicyRepository.deleteAll();
            serviceCategoryRepository.deleteAll();
            log.info("Deleted all old GlobalPolicy data (limits, coverages, categories)");

            // Step 3: Delete old Policy entities (legacy)
            // First, nullify all client policy references to avoid foreign key constraint violation
            jdbcTemplate.execute("UPDATE clients SET policy_id = NULL");
            log.info("Nullified all client policy references");
            policyRepository.deleteAll();
            log.info("Deleted all old legacy Policy entities");

            response.put("step1_cleanup", Map.of(
                "claimsDeleted", claimsDeleted,
                "prescriptionsDeleted", prescriptionsDeleted,
                "labRequestsDeleted", labRequestsDeleted,
                "emergencyRequestsDeleted", emergencyRequestsDeleted,
                "medicalRecordsDeleted", medicalRecordsDeleted
            ));

            // Step 4: Create Service Categories
            log.info("Creating service categories...");
            Map<String, ServiceCategory> categories = createServiceCategories();
            response.put("step2_categories", categories.size() + " categories created");

            // Step 5: Create GlobalPolicy
            log.info("Creating GlobalPolicy...");
            GlobalPolicy policy = createGlobalPolicy();
            response.put("step3_policy", "GlobalPolicy created: " + policy.getName());

            // Step 6: Create Client Limits
            log.info("Creating client limits...");
            ClientLimits clientLimits = createClientLimits(policy);
            response.put("step4_clientLimits", "Client limits created");

            // Step 7: Create Category Limits
            log.info("Creating category limits...");
            List<CategoryLimits> categoryLimitsList = createCategoryLimits(policy, categories);
            response.put("step5_categoryLimits", categoryLimitsList.size() + " category limits created");

            // Step 8: Create Service Coverages
            log.info("Creating service coverages...");
            List<ServiceCoverage> serviceCoverages = createServiceCoverages(policy, categories);
            response.put("step6_serviceCoverages", serviceCoverages.size() + " service coverages created");

            response.put("success", true);
            response.put("message", "Policy data reset successfully! All accounts preserved, fresh GlobalPolicy created.");

            log.info("Policy data reset completed successfully!");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Failed to reset policy data", e);
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private Map<String, ServiceCategory> createServiceCategories() {
        Map<String, ServiceCategory> categories = new LinkedHashMap<>();

        // General/Outpatient
        categories.put("OUTPATIENT", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("General Consultation")
            .nameAr("الكشف العام")
            .description("General medical consultations and outpatient visits")
            .icon("stethoscope")
            .color("#4CAF50")
            .displayOrder(1)
            .isActive(true)
            .build()));

        // Specialist Consultations
        categories.put("SPECIALIST", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Specialist Consultation")
            .nameAr("الكشف التخصصي")
            .description("Specialist medical consultations")
            .icon("user-md")
            .color("#2196F3")
            .displayOrder(2)
            .isActive(true)
            .build()));

        // Laboratory
        categories.put("LAB", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Laboratory Tests")
            .nameAr("الفحوصات المخبرية")
            .description("Blood tests, urine tests, and other lab work")
            .icon("flask")
            .color("#9C27B0")
            .displayOrder(3)
            .isActive(true)
            .build()));

        // Radiology
        categories.put("RADIOLOGY", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Radiology & Imaging")
            .nameAr("الأشعة والتصوير")
            .description("X-rays, MRI, CT scans, ultrasound")
            .icon("x-ray")
            .color("#FF9800")
            .displayOrder(4)
            .isActive(true)
            .build()));

        // Pharmacy
        categories.put("PHARMACY", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Pharmacy")
            .nameAr("الصيدلية")
            .description("Medications and pharmaceutical services")
            .icon("pills")
            .color("#E91E63")
            .displayOrder(5)
            .isActive(true)
            .build()));

        // Dental
        categories.put("DENTAL", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Dental Care")
            .nameAr("طب الأسنان")
            .description("Dental examinations and treatments")
            .icon("tooth")
            .color("#00BCD4")
            .displayOrder(6)
            .isActive(true)
            .build()));

        // Eye Care
        categories.put("OPTICAL", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Eye Care")
            .nameAr("طب العيون")
            .description("Eye examinations and optical services")
            .icon("eye")
            .color("#3F51B5")
            .displayOrder(7)
            .isActive(true)
            .build()));

        // Emergency
        categories.put("EMERGENCY", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Emergency Services")
            .nameAr("الطوارئ")
            .description("Emergency medical care")
            .icon("ambulance")
            .color("#F44336")
            .displayOrder(8)
            .isActive(true)
            .build()));

        // Inpatient/Hospital
        categories.put("INPATIENT", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Hospital/Inpatient")
            .nameAr("الإقامة في المستشفى")
            .description("Hospital stays and inpatient care")
            .icon("hospital")
            .color("#607D8B")
            .displayOrder(9)
            .isActive(true)
            .build()));

        // Pediatrics
        categories.put("PEDIATRICS", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Pediatrics")
            .nameAr("طب الأطفال")
            .description("Medical care for children")
            .icon("baby")
            .color("#8BC34A")
            .displayOrder(10)
            .isActive(true)
            .build()));

        // Women's Health
        categories.put("WOMENS_HEALTH", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Women's Health")
            .nameAr("صحة المرأة")
            .description("Obstetrics, gynecology, and women's health services")
            .icon("female")
            .color("#FF4081")
            .displayOrder(11)
            .isActive(true)
            .build()));

        // Mental Health
        categories.put("MENTAL_HEALTH", serviceCategoryRepository.save(ServiceCategory.builder()
            .name("Mental Health")
            .nameAr("الصحة النفسية")
            .description("Psychiatric and psychological services")
            .icon("brain")
            .color("#673AB7")
            .displayOrder(12)
            .isActive(true)
            .build()));

        log.info("Created {} service categories", categories.size());
        return categories;
    }

    private GlobalPolicy createGlobalPolicy() {
        GlobalPolicy policy = GlobalPolicy.builder()
            .name("Birzeit University Health Insurance Plan 2024-2025")
            .version("1.0")
            .description("Comprehensive health insurance plan for Birzeit University community. " +
                "This policy covers general consultations, specialist visits, laboratory tests, " +
                "radiology services, pharmacy, dental care, eye care, emergency services, and hospital stays.")
            .effectiveFrom(LocalDate.of(2024, 1, 1))
            .effectiveTo(LocalDate.of(2025, 12, 31))
            .status(GlobalPolicyStatus.ACTIVE)
            .build();

        return globalPolicyRepository.save(policy);
    }

    private ClientLimits createClientLimits(GlobalPolicy policy) {
        ClientLimits limits = ClientLimits.builder()
            .globalPolicy(policy)
            .maxVisitsPerMonth(15)
            .maxVisitsPerYear(150)
            .maxSpendingPerMonth(new BigDecimal("5000.00"))
            .maxSpendingPerYear(new BigDecimal("50000.00"))
            .annualDeductible(new BigDecimal("100.00"))
            .build();

        return clientLimitsRepository.save(limits);
    }

    private List<CategoryLimits> createCategoryLimits(GlobalPolicy policy, Map<String, ServiceCategory> categories) {
        List<CategoryLimits> limitsList = new ArrayList<>();

        // Outpatient limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("OUTPATIENT"))
            .maxVisitsPerMonth(8)
            .maxVisitsPerYear(80)
            .maxSpendingPerMonth(new BigDecimal("1000.00"))
            .maxSpendingPerYear(new BigDecimal("10000.00"))
            .build()));

        // Specialist limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("SPECIALIST"))
            .maxVisitsPerMonth(4)
            .maxVisitsPerYear(40)
            .maxSpendingPerMonth(new BigDecimal("1500.00"))
            .maxSpendingPerYear(new BigDecimal("15000.00"))
            .build()));

        // Lab limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("LAB"))
            .maxVisitsPerMonth(10)
            .maxVisitsPerYear(100)
            .maxSpendingPerMonth(new BigDecimal("800.00"))
            .maxSpendingPerYear(new BigDecimal("8000.00"))
            .build()));

        // Radiology limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("RADIOLOGY"))
            .maxVisitsPerMonth(3)
            .maxVisitsPerYear(24)
            .maxSpendingPerMonth(new BigDecimal("2000.00"))
            .maxSpendingPerYear(new BigDecimal("15000.00"))
            .build()));

        // Pharmacy limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("PHARMACY"))
            .maxVisitsPerMonth(10)
            .maxVisitsPerYear(120)
            .maxSpendingPerMonth(new BigDecimal("500.00"))
            .maxSpendingPerYear(new BigDecimal("5000.00"))
            .build()));

        // Dental limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("DENTAL"))
            .maxVisitsPerMonth(2)
            .maxVisitsPerYear(12)
            .maxSpendingPerMonth(new BigDecimal("600.00"))
            .maxSpendingPerYear(new BigDecimal("5000.00"))
            .build()));

        // Optical limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("OPTICAL"))
            .maxVisitsPerMonth(2)
            .maxVisitsPerYear(6)
            .maxSpendingPerMonth(new BigDecimal("400.00"))
            .maxSpendingPerYear(new BigDecimal("3000.00"))
            .build()));

        // Emergency limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("EMERGENCY"))
            .maxVisitsPerMonth(null) // Unlimited emergency visits
            .maxVisitsPerYear(null)
            .maxSpendingPerMonth(new BigDecimal("10000.00"))
            .maxSpendingPerYear(new BigDecimal("50000.00"))
            .build()));

        // Inpatient limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("INPATIENT"))
            .maxVisitsPerMonth(null)
            .maxVisitsPerYear(4)
            .maxSpendingPerMonth(new BigDecimal("20000.00"))
            .maxSpendingPerYear(new BigDecimal("100000.00"))
            .build()));

        // Pediatrics limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("PEDIATRICS"))
            .maxVisitsPerMonth(6)
            .maxVisitsPerYear(60)
            .maxSpendingPerMonth(new BigDecimal("1000.00"))
            .maxSpendingPerYear(new BigDecimal("10000.00"))
            .build()));

        // Women's Health limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("WOMENS_HEALTH"))
            .maxVisitsPerMonth(4)
            .maxVisitsPerYear(30)
            .maxSpendingPerMonth(new BigDecimal("1500.00"))
            .maxSpendingPerYear(new BigDecimal("15000.00"))
            .build()));

        // Mental Health limits
        limitsList.add(categoryLimitsRepository.save(CategoryLimits.builder()
            .globalPolicy(policy)
            .category(categories.get("MENTAL_HEALTH"))
            .maxVisitsPerMonth(4)
            .maxVisitsPerYear(48)
            .maxSpendingPerMonth(new BigDecimal("800.00"))
            .maxSpendingPerYear(new BigDecimal("8000.00"))
            .build()));

        log.info("Created {} category limits", limitsList.size());
        return limitsList;
    }

    private List<ServiceCoverage> createServiceCoverages(GlobalPolicy policy, Map<String, ServiceCategory> categories) {
        List<ServiceCoverage> coverages = new ArrayList<>();

        // ==================== OUTPATIENT SERVICES ====================
        ServiceCategory outpatient = categories.get("OUTPATIENT");
        coverages.add(createCoverage(policy, outpatient, "General Medical Consultation", "كشف طبي عام",
            100, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, outpatient, "Follow-up Visit", "زيارة متابعة",
            50, 80, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, 12, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, outpatient, "Preventive Health Checkup", "فحص صحي وقائي",
            200, 70, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));

        // ==================== SPECIALIST SERVICES ====================
        ServiceCategory specialist = categories.get("SPECIALIST");
        coverages.add(createCoverage(policy, specialist, "Cardiology Consultation", "استشارة قلب",
            200, 80, new BigDecimal("300.00"), 18, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Dermatology Consultation", "استشارة جلدية",
            150, 80, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Orthopedic Consultation", "استشارة عظام",
            180, 80, new BigDecimal("250.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "ENT Consultation", "استشارة أنف وأذن وحنجرة",
            150, 80, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Neurology Consultation", "استشارة أعصاب",
            220, 80, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Gastroenterology Consultation", "استشارة جهاز هضمي",
            180, 80, new BigDecimal("250.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));

        // Additional Specialist Services
        coverages.add(createCoverage(policy, specialist, "Urology Consultation", "استشارة المسالك البولية",
            170, 80, new BigDecimal("280.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Nephrology Consultation", "استشارة الكلى",
            200, 85, new BigDecimal("320.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Pulmonology Consultation", "استشارة الرئة والجهاز التنفسي",
            190, 80, new BigDecimal("300.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Endocrinology Consultation", "استشارة الغدد الصماء",
            200, 85, new BigDecimal("320.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Rheumatology Consultation", "استشارة الروماتيزم",
            190, 80, new BigDecimal("300.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Oncology Consultation", "استشارة الأورام",
            300, 90, new BigDecimal("500.00"), null, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Hematology Consultation", "استشارة أمراض الدم",
            220, 85, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Infectious Disease Consultation", "استشارة الأمراض المعدية",
            200, 85, new BigDecimal("320.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Allergy & Immunology Consultation", "استشارة الحساسية والمناعة",
            180, 80, new BigDecimal("280.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Pain Management Consultation", "استشارة إدارة الألم",
            200, 75, new BigDecimal("320.00"), 18, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Sports Medicine Consultation", "استشارة الطب الرياضي",
            170, 75, new BigDecimal("270.00"), null, null, AllowedGender.ALL, true, 3, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Geriatric Medicine Consultation", "استشارة طب الشيخوخة",
            180, 85, new BigDecimal("280.00"), 60, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Physical Therapy Session", "جلسة علاج طبيعي",
            100, 80, new BigDecimal("160.00"), null, null, AllowedGender.ALL, true, 12, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Occupational Therapy Session", "جلسة العلاج الوظيفي",
            100, 80, new BigDecimal("160.00"), null, null, AllowedGender.ALL, true, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Speech Therapy Session", "جلسة علاج النطق",
            120, 80, new BigDecimal("200.00"), null, null, AllowedGender.ALL, true, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Audiology Consultation", "استشارة السمعيات",
            150, 80, new BigDecimal("240.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Vascular Surgery Consultation", "استشارة جراحة الأوعية",
            220, 80, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, specialist, "Plastic Surgery Consultation - Medical", "استشارة الجراحة التجميلية الطبية",
            200, 70, new BigDecimal("320.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Plastic Surgery Consultation - Cosmetic", "استشارة الجراحة التجميلية",
            250, 0, new BigDecimal("400.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, specialist, "Sleep Study (Polysomnography)", "دراسة النوم",
            600, 70, new BigDecimal("1000.00"), 18, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Pulmonary Function Test", "فحص وظائف الرئة",
            150, 85, new BigDecimal("250.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Stress Test (Cardiac)", "فحص الجهد القلبي",
            300, 85, new BigDecimal("500.00"), 30, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Holter Monitor (24hr)", "جهاز هولتر 24 ساعة",
            200, 85, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Endoscopy - Upper GI", "تنظير الجهاز الهضمي العلوي",
            500, 80, new BigDecimal("900.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Colonoscopy", "تنظير القولون",
            700, 80, new BigDecimal("1200.00"), 45, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Bronchoscopy", "تنظير القصبات الهوائية",
            600, 80, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Cystoscopy", "تنظير المثانة",
            450, 80, new BigDecimal("800.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "EMG/Nerve Conduction Study", "تخطيط كهربية العضلات",
            300, 80, new BigDecimal("500.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "EEG (Brain Wave Test)", "تخطيط كهربية الدماغ",
            250, 85, new BigDecimal("400.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, specialist, "Hearing Aid - Basic", "سماعة أذن طبية أساسية",
            1000, 50, new BigDecimal("1500.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, specialist, "Dialysis Session", "جلسة غسيل كلى",
            500, 95, new BigDecimal("800.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, specialist, "Chemotherapy Session", "جلسة علاج كيماوي",
            2000, 90, new BigDecimal("4000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, specialist, "Radiation Therapy Session", "جلسة علاج إشعاعي",
            1500, 90, new BigDecimal("3000.00"), null, null, AllowedGender.ALL, true, null, null));

        // ==================== LABORATORY SERVICES ====================
        ServiceCategory lab = categories.get("LAB");
        coverages.add(createCoverage(policy, lab, "Complete Blood Count (CBC)", "فحص دم شامل",
            50, 90, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Blood Sugar Test (Fasting)", "فحص سكر صائم",
            25, 90, new BigDecimal("40.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "HbA1c Test", "فحص السكر التراكمي",
            60, 90, new BigDecimal("100.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Lipid Profile", "فحص الدهون",
            70, 90, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Kidney Function Test", "فحص وظائف الكلى",
            80, 90, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Liver Function Test", "فحص وظائف الكبد",
            90, 90, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Thyroid Function Test (TSH)", "فحص الغدة الدرقية",
            65, 90, new BigDecimal("110.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Urinalysis", "فحص البول",
            30, 90, new BigDecimal("50.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Vitamin D Test", "فحص فيتامين د",
            80, 85, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Iron Studies", "فحص الحديد",
            75, 90, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));

        // Additional Lab Tests
        coverages.add(createCoverage(policy, lab, "Vitamin B12 Test", "فحص فيتامين ب12",
            70, 85, new BigDecimal("110.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Calcium Level Test", "فحص الكالسيوم",
            40, 90, new BigDecimal("60.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Electrolyte Panel", "فحص الأملاح والمعادن",
            60, 90, new BigDecimal("100.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Uric Acid Test", "فحص حمض اليوريك",
            35, 90, new BigDecimal("55.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "PSA Test (Prostate)", "فحص البروستات PSA",
            90, 85, new BigDecimal("150.00"), 45, null, AllowedGender.MALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Testosterone Level", "فحص التستوستيرون",
            100, 80, new BigDecimal("170.00"), 18, null, AllowedGender.MALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Estrogen Level", "فحص الإستروجين",
            100, 80, new BigDecimal("170.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Pregnancy Test (Blood)", "فحص الحمل بالدم",
            45, 100, new BigDecimal("70.00"), 18, 50, AllowedGender.FEMALE, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Coagulation Profile (PT/INR)", "فحص تخثر الدم",
            80, 90, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Blood Culture", "مزرعة الدم",
            150, 90, new BigDecimal("250.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Stool Analysis", "تحليل البراز",
            40, 90, new BigDecimal("65.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "H. Pylori Test", "فحص جرثومة المعدة",
            70, 85, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Hepatitis B Test", "فحص التهاب الكبد ب",
            80, 90, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Hepatitis C Test", "فحص التهاب الكبد ج",
            80, 90, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "HIV Test", "فحص الإيدز",
            100, 100, new BigDecimal("160.00"), 18, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Rheumatoid Factor", "عامل الروماتويد",
            70, 85, new BigDecimal("115.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "ESR (Sedimentation Rate)", "سرعة الترسيب",
            30, 90, new BigDecimal("45.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "CRP (C-Reactive Protein)", "بروتين سي التفاعلي",
            50, 90, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Ferritin Level", "فحص الفيريتين",
            65, 90, new BigDecimal("105.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Folate Level", "فحص حمض الفوليك",
            55, 85, new BigDecimal("90.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Magnesium Level", "فحص المغنيسيوم",
            45, 90, new BigDecimal("70.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Phosphorus Level", "فحص الفسفور",
            40, 90, new BigDecimal("60.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, lab, "Genetic Testing - Basic", "الفحص الجيني الأساسي",
            500, 50, new BigDecimal("800.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, lab, "Allergy Panel Test", "فحص الحساسية الشامل",
            300, 60, new BigDecimal("500.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Drug Screening Test", "فحص المخدرات",
            120, 0, new BigDecimal("200.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, lab, "Fertility Hormone Panel (Female)", "فحص هرمونات الخصوبة للنساء",
            250, 70, new BigDecimal("400.00"), 18, 45, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, lab, "Semen Analysis", "تحليل السائل المنوي",
            150, 70, new BigDecimal("250.00"), 18, null, AllowedGender.MALE, true, 2, FrequencyPeriod.YEARLY));

        // ==================== RADIOLOGY SERVICES ====================
        ServiceCategory radiology = categories.get("RADIOLOGY");
        coverages.add(createCoverage(policy, radiology, "Chest X-Ray", "أشعة صدر",
            80, 85, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Bone X-Ray", "أشعة عظام",
            70, 85, new BigDecimal("100.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Abdominal Ultrasound", "سونار البطن",
            150, 85, new BigDecimal("250.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Thyroid Ultrasound", "سونار الغدة الدرقية",
            120, 85, new BigDecimal("200.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "CT Scan - Head", "أشعة مقطعية للرأس",
            400, 80, new BigDecimal("700.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "CT Scan - Abdomen", "أشعة مقطعية للبطن",
            500, 80, new BigDecimal("900.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Knee", "رنين مغناطيسي للركبة",
            800, 75, new BigDecimal("1500.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Spine", "رنين مغناطيسي للعمود الفقري",
            900, 75, new BigDecimal("1700.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Echocardiogram", "إيكو القلب",
            250, 85, new BigDecimal("400.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Mammogram", "ماموغرام",
            200, 90, new BigDecimal("350.00"), 35, null, AllowedGender.FEMALE, true, 1, FrequencyPeriod.YEARLY));

        // Additional Radiology Services
        coverages.add(createCoverage(policy, radiology, "Pelvic X-Ray", "أشعة الحوض",
            90, 85, new BigDecimal("140.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Spine X-Ray", "أشعة العمود الفقري",
            100, 85, new BigDecimal("160.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Skull X-Ray", "أشعة الجمجمة",
            85, 85, new BigDecimal("130.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Dental Panoramic X-Ray", "أشعة بانورامية للأسنان",
            100, 80, new BigDecimal("170.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Kidney Ultrasound", "سونار الكلى",
            130, 85, new BigDecimal("220.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "Breast Ultrasound", "سونار الثدي",
            140, 85, new BigDecimal("230.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Testicular Ultrasound", "سونار الخصية",
            140, 85, new BigDecimal("230.00"), 18, null, AllowedGender.MALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Obstetric Ultrasound", "سونار الحمل",
            150, 100, new BigDecimal("250.00"), 18, 50, AllowedGender.FEMALE, false, 8, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Doppler Ultrasound - Vascular", "دوبلر الأوعية الدموية",
            200, 80, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "CT Scan - Chest", "أشعة مقطعية للصدر",
            450, 80, new BigDecimal("800.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "CT Scan - Spine", "أشعة مقطعية للعمود الفقري",
            500, 80, new BigDecimal("900.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, radiology, "CT Angiography", "أشعة مقطعية للأوعية",
            700, 75, new BigDecimal("1200.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Brain", "رنين مغناطيسي للدماغ",
            850, 75, new BigDecimal("1600.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Shoulder", "رنين مغناطيسي للكتف",
            750, 75, new BigDecimal("1400.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Hip", "رنين مغناطيسي للورك",
            800, 75, new BigDecimal("1500.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "MRI - Abdomen", "رنين مغناطيسي للبطن",
            950, 70, new BigDecimal("1800.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "PET Scan", "فحص PET",
            3000, 60, new BigDecimal("5000.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, radiology, "Nuclear Medicine Scan", "التصوير النووي",
            1500, 65, new BigDecimal("2500.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Bone Density Scan (DEXA)", "فحص هشاشة العظام",
            180, 80, new BigDecimal("300.00"), 50, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Fluoroscopy", "التنظير الفلوري",
            250, 80, new BigDecimal("400.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Interventional Radiology - Minor", "أشعة تداخلية صغيرة",
            1000, 70, new BigDecimal("1800.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, radiology, "Cosmetic Imaging", "التصوير التجميلي",
            400, 0, new BigDecimal("600.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));

        // ==================== PHARMACY SERVICES ====================
        ServiceCategory pharmacy = categories.get("PHARMACY");
        coverages.add(createCoverage(policy, pharmacy, "Prescription Medications", "أدوية موصوفة",
            0, 80, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Chronic Disease Medications", "أدوية الأمراض المزمنة",
            0, 90, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Antibiotics", "المضادات الحيوية",
            0, 85, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Pain Relievers", "المسكنات",
            0, 80, new BigDecimal("100.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Vitamins & Supplements", "الفيتامينات والمكملات",
            0, 50, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, null, null));

        // Additional Pharmacy Services - Medications by Category
        // Cardiovascular Medications
        coverages.add(createCoverage(policy, pharmacy, "Blood Pressure Medications", "أدوية ضغط الدم",
            0, 90, new BigDecimal("300.00"), 18, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Cholesterol Medications (Statins)", "أدوية الكوليسترول",
            0, 85, new BigDecimal("250.00"), 18, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Blood Thinners (Anticoagulants)", "مميعات الدم",
            0, 90, new BigDecimal("400.00"), 18, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Heart Rhythm Medications", "أدوية اضطراب نظم القلب",
            0, 85, new BigDecimal("350.00"), 18, null, AllowedGender.ALL, true, null, null));

        // Diabetes Medications
        coverages.add(createCoverage(policy, pharmacy, "Insulin - All Types", "الأنسولين بجميع أنواعه",
            0, 95, new BigDecimal("800.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Oral Diabetes Medications", "أدوية السكري الفموية",
            0, 90, new BigDecimal("400.00"), 18, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Diabetes Supplies (Test Strips)", "مستلزمات السكري",
            0, 85, new BigDecimal("300.00"), null, null, AllowedGender.ALL, false, null, null));

        // Respiratory Medications
        coverages.add(createCoverage(policy, pharmacy, "Asthma Inhalers", "بخاخات الربو",
            0, 90, new BigDecimal("400.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "COPD Medications", "أدوية الانسداد الرئوي",
            0, 85, new BigDecimal("500.00"), 40, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Allergy Medications (Antihistamines)", "أدوية الحساسية",
            0, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Nasal Corticosteroids", "بخاخات الأنف الستيرويدية",
            0, 80, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, null, null));

        // Gastrointestinal Medications
        coverages.add(createCoverage(policy, pharmacy, "Proton Pump Inhibitors (PPIs)", "مثبطات مضخة البروتون",
            0, 85, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Anti-nausea Medications", "أدوية الغثيان",
            0, 80, new BigDecimal("100.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Laxatives", "الملينات",
            0, 70, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Anti-diarrheal Medications", "أدوية الإسهال",
            0, 75, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, null, null));

        // Pain Management
        coverages.add(createCoverage(policy, pharmacy, "NSAIDs (Anti-inflammatory)", "مضادات الالتهاب غير الستيرويدية",
            0, 80, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Muscle Relaxants", "مرخيات العضلات",
            0, 75, new BigDecimal("150.00"), 18, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Migraine Medications", "أدوية الصداع النصفي",
            0, 80, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Opioid Pain Medications", "مسكنات الألم الأفيونية",
            0, 70, new BigDecimal("300.00"), 18, null, AllowedGender.ALL, true, null, null));

        // Anti-infective Medications
        coverages.add(createCoverage(policy, pharmacy, "Antiviral Medications", "مضادات الفيروسات",
            0, 85, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Antifungal Medications", "مضادات الفطريات",
            0, 80, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Antiparasitic Medications", "مضادات الطفيليات",
            0, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, null, null));

        // Hormonal Medications
        coverages.add(createCoverage(policy, pharmacy, "Thyroid Medications", "أدوية الغدة الدرقية",
            0, 90, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Hormone Replacement (Female)", "العلاج الهرموني التعويضي",
            0, 75, new BigDecimal("250.00"), 40, null, AllowedGender.FEMALE, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Contraceptive Pills", "حبوب منع الحمل",
            0, 70, new BigDecimal("100.00"), 18, 50, AllowedGender.FEMALE, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Testosterone Replacement", "العلاج التعويضي بالتستوستيرون",
            0, 70, new BigDecimal("300.00"), 40, null, AllowedGender.MALE, true, null, null));

        // Mental Health Medications
        coverages.add(createCoverage(policy, pharmacy, "Antidepressants (SSRIs)", "مضادات الاكتئاب",
            0, 85, new BigDecimal("300.00"), 18, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Anti-anxiety Medications", "أدوية القلق",
            0, 80, new BigDecimal("250.00"), 18, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Sleep Medications", "أدوية النوم",
            0, 70, new BigDecimal("150.00"), 18, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "ADHD Medications", "أدوية فرط الحركة وتشتت الانتباه",
            0, 80, new BigDecimal("350.00"), 6, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Antipsychotic Medications", "مضادات الذهان",
            0, 85, new BigDecimal("400.00"), 18, null, AllowedGender.ALL, true, null, null));

        // Dermatology Medications
        coverages.add(createCoverage(policy, pharmacy, "Topical Corticosteroids", "الكورتيزون الموضعي",
            0, 80, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Acne Medications", "أدوية حب الشباب",
            0, 70, new BigDecimal("150.00"), 12, 35, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Eczema/Psoriasis Medications", "أدوية الأكزيما والصدفية",
            0, 80, new BigDecimal("300.00"), null, null, AllowedGender.ALL, true, null, null));

        // Eye Medications
        coverages.add(createCoverage(policy, pharmacy, "Glaucoma Eye Drops", "قطرات الجلوكوما",
            0, 85, new BigDecimal("250.00"), 40, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Antibiotic Eye Drops", "قطرات العين المضادة للبكتيريا",
            0, 80, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Artificial Tears", "الدموع الاصطناعية",
            0, 60, new BigDecimal("50.00"), null, null, AllowedGender.ALL, false, null, null));

        // Bone & Joint Medications
        coverages.add(createCoverage(policy, pharmacy, "Osteoporosis Medications", "أدوية هشاشة العظام",
            0, 85, new BigDecimal("400.00"), 50, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Gout Medications", "أدوية النقرس",
            0, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Arthritis Medications (DMARDs)", "أدوية التهاب المفاصل",
            0, 80, new BigDecimal("500.00"), null, null, AllowedGender.ALL, true, null, null));

        // Pediatric Medications
        coverages.add(createCoverage(policy, pharmacy, "Children's Fever Reducers", "خافضات حرارة الأطفال",
            0, 90, new BigDecimal("60.00"), 0, 12, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Children's Antibiotics", "مضادات حيوية للأطفال",
            0, 90, new BigDecimal("120.00"), 0, 18, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, pharmacy, "Children's Vitamins", "فيتامينات للأطفال",
            0, 60, new BigDecimal("80.00"), 0, 18, AllowedGender.ALL, false, null, null));

        // NOT COVERED - Cosmetic/Lifestyle
        coverages.add(createCoverage(policy, pharmacy, "Weight Loss Medications", "أدوية إنقاص الوزن",
            0, 0, new BigDecimal("0.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, pharmacy, "Hair Growth Medications", "أدوية نمو الشعر",
            0, 0, new BigDecimal("0.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, pharmacy, "Erectile Dysfunction Medications", "أدوية ضعف الانتصاب",
            0, 0, new BigDecimal("0.00"), 18, null, AllowedGender.MALE, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, pharmacy, "Smoking Cessation Products", "منتجات الإقلاع عن التدخين",
            0, 50, new BigDecimal("200.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.PARTIAL));

        // ==================== DENTAL SERVICES ====================
        ServiceCategory dental = categories.get("DENTAL");
        coverages.add(createCoverage(policy, dental, "Dental Examination", "فحص الأسنان",
            80, 80, new BigDecimal("120.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental Cleaning", "تنظيف الأسنان",
            100, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental Filling", "حشو الأسنان",
            120, 70, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Tooth Extraction", "خلع الأسنان",
            150, 80, new BigDecimal("250.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Root Canal Treatment", "علاج جذور الأسنان",
            400, 60, new BigDecimal("600.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental X-Ray", "أشعة الأسنان",
            50, 80, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));

        // Additional Dental Services
        coverages.add(createCoverage(policy, dental, "Dental Crown", "تاج الأسنان",
            500, 60, new BigDecimal("800.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental Bridge", "جسر الأسنان",
            800, 50, new BigDecimal("1200.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental Implant", "زراعة الأسنان",
            2000, 40, new BigDecimal("3000.00"), 18, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, dental, "Gum Treatment (Periodontal)", "علاج اللثة",
            300, 70, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Wisdom Tooth Extraction", "خلع ضرس العقل",
            250, 75, new BigDecimal("400.00"), 16, null, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Teeth Whitening", "تبييض الأسنان",
            300, 0, new BigDecimal("500.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, dental, "Orthodontic Treatment - Child", "تقويم الأسنان للأطفال",
            3000, 50, new BigDecimal("5000.00"), 8, 18, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, dental, "Orthodontic Treatment - Adult", "تقويم الأسنان للبالغين",
            4000, 30, new BigDecimal("6000.00"), 18, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, dental, "Night Guard (Bruxism)", "واقي الأسنان الليلي",
            200, 60, new BigDecimal("350.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dentures - Partial", "طقم أسنان جزئي",
            600, 50, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dentures - Full", "طقم أسنان كامل",
            1000, 50, new BigDecimal("1600.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Fluoride Treatment", "علاج الفلورايد",
            50, 90, new BigDecimal("80.00"), 0, 18, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, dental, "Dental Sealants", "حشوات وقائية للأسنان",
            80, 90, new BigDecimal("120.00"), 6, 18, AllowedGender.ALL, false, 8, FrequencyPeriod.YEARLY));

        // ==================== EYE CARE SERVICES ====================
        ServiceCategory optical = categories.get("OPTICAL");
        coverages.add(createCoverage(policy, optical, "Eye Examination", "فحص العيون",
            100, 80, new BigDecimal("150.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Prescription Glasses", "نظارات طبية",
            300, 60, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Contact Lenses", "عدسات لاصقة",
            200, 50, new BigDecimal("400.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Glaucoma Screening", "فحص الجلوكوما",
            120, 85, new BigDecimal("200.00"), 40, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Cataract Surgery", "عملية المياه البيضاء",
            3000, 70, new BigDecimal("5000.00"), 50, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));

        // Additional Eye Care Services
        coverages.add(createCoverage(policy, optical, "Retinal Examination", "فحص الشبكية",
            150, 85, new BigDecimal("250.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Visual Field Test", "فحص مجال الرؤية",
            100, 85, new BigDecimal("170.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "OCT Scan (Retina)", "فحص OCT للشبكية",
            200, 80, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Diabetic Eye Screening", "فحص العين لمرضى السكري",
            120, 90, new BigDecimal("200.00"), null, null, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "LASIK Surgery", "عملية الليزك",
            4000, 30, new BigDecimal("6000.00"), 18, 45, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, optical, "Strabismus Surgery", "عملية الحول",
            3500, 70, new BigDecimal("6000.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Retinal Laser Treatment", "علاج الشبكية بالليزر",
            1500, 75, new BigDecimal("2500.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Intravitreal Injection", "حقن داخل العين",
            800, 80, new BigDecimal("1400.00"), null, null, AllowedGender.ALL, true, 6, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Pediatric Eye Exam", "فحص العين للأطفال",
            80, 90, new BigDecimal("130.00"), 0, 18, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Color Vision Test", "فحص عمى الألوان",
            50, 80, new BigDecimal("80.00"), null, null, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, optical, "Colored Contact Lenses", "عدسات ملونة",
            150, 0, new BigDecimal("250.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));

        // ==================== EMERGENCY SERVICES ====================
        ServiceCategory emergency = categories.get("EMERGENCY");
        coverages.add(createCoverage(policy, emergency, "Emergency Room Visit", "زيارة الطوارئ",
            500, 100, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Ambulance Service", "خدمة الإسعاف",
            300, 100, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Emergency Surgery", "جراحة طارئة",
            10000, 90, new BigDecimal("20000.00"), null, null, AllowedGender.ALL, false, null, null));

        // Additional Emergency Services
        coverages.add(createCoverage(policy, emergency, "Trauma Care", "رعاية الإصابات",
            5000, 95, new BigDecimal("10000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Emergency Dental", "طوارئ الأسنان",
            300, 80, new BigDecimal("500.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Poisoning Treatment", "علاج التسمم",
            2000, 100, new BigDecimal("4000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Fracture Treatment - Emergency", "علاج الكسور الطارئة",
            1500, 90, new BigDecimal("3000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Burn Treatment - Emergency", "علاج الحروق الطارئة",
            2000, 95, new BigDecimal("4000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Cardiac Emergency", "طوارئ القلب",
            5000, 100, new BigDecimal("10000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Stroke Emergency", "طوارئ السكتة الدماغية",
            5000, 100, new BigDecimal("10000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Allergic Reaction - Severe", "حساسية شديدة",
            1000, 100, new BigDecimal("2000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Asthma Attack - Emergency", "نوبة ربو طارئة",
            800, 100, new BigDecimal("1500.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Pediatric Emergency", "طوارئ الأطفال",
            1000, 100, new BigDecimal("2000.00"), 0, 18, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, emergency, "Obstetric Emergency", "طوارئ الولادة",
            3000, 100, new BigDecimal("6000.00"), 18, 50, AllowedGender.FEMALE, false, null, null));

        // ==================== INPATIENT SERVICES ====================
        ServiceCategory inpatient = categories.get("INPATIENT");
        coverages.add(createCoverage(policy, inpatient, "Hospital Room - Standard", "غرفة مستشفى عادية",
            500, 90, new BigDecimal("800.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Hospital Room - Private", "غرفة مستشفى خاصة",
            800, 70, new BigDecimal("1200.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "ICU Stay", "العناية المركزة",
            2000, 90, new BigDecimal("4000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Surgery - Minor", "جراحة صغيرة",
            3000, 80, new BigDecimal("5000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Surgery - Major", "جراحة كبيرة",
            15000, 80, new BigDecimal("30000.00"), null, null, AllowedGender.ALL, true, null, null));

        // Additional Inpatient Services
        coverages.add(createCoverage(policy, inpatient, "NICU (Neonatal ICU)", "العناية المركزة للأطفال حديثي الولادة",
            3000, 95, new BigDecimal("6000.00"), 0, 1, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Cardiac Surgery", "جراحة القلب",
            25000, 80, new BigDecimal("50000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Brain Surgery", "جراحة الدماغ",
            30000, 75, new BigDecimal("60000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Organ Transplant", "زراعة الأعضاء",
            50000, 70, new BigDecimal("100000.00"), null, null, AllowedGender.ALL, true, null, null,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, inpatient, "Joint Replacement Surgery", "جراحة استبدال المفاصل",
            15000, 75, new BigDecimal("25000.00"), 40, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Spine Surgery", "جراحة العمود الفقري",
            20000, 75, new BigDecimal("40000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Laparoscopic Surgery", "جراحة بالمنظار",
            8000, 80, new BigDecimal("15000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Hernia Repair Surgery", "جراحة الفتق",
            5000, 85, new BigDecimal("8000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Appendectomy", "استئصال الزائدة الدودية",
            4000, 90, new BigDecimal("7000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Gallbladder Removal", "استئصال المرارة",
            5000, 85, new BigDecimal("9000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Tonsillectomy", "استئصال اللوزتين",
            3000, 90, new BigDecimal("5000.00"), null, 30, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Blood Transfusion", "نقل الدم",
            500, 100, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "IV Fluid Therapy", "العلاج بالسوائل الوريدية",
            200, 100, new BigDecimal("400.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Observation Stay (24hr)", "إقامة للمراقبة",
            400, 85, new BigDecimal("700.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, inpatient, "Rehabilitation Stay", "إقامة للتأهيل",
            600, 70, new BigDecimal("1000.00"), null, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, inpatient, "Cosmetic Surgery", "جراحة تجميلية",
            10000, 0, new BigDecimal("20000.00"), 18, null, AllowedGender.ALL, false, null, null,
            CoverageStatusType.NOT_COVERED));
        coverages.add(createCoverage(policy, inpatient, "Bariatric Surgery (Weight Loss)", "جراحة السمنة",
            15000, 40, new BigDecimal("25000.00"), 18, 65, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));

        // ==================== PEDIATRIC SERVICES ====================
        ServiceCategory pediatrics = categories.get("PEDIATRICS");
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Consultation", "كشف طب الأطفال",
            100, 90, new BigDecimal("150.00"), 0, 18, AllowedGender.ALL, false, 6, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, pediatrics, "Vaccination", "التطعيمات",
            50, 100, new BigDecimal("100.00"), 0, 18, AllowedGender.ALL, false, 12, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Growth Monitoring", "متابعة النمو",
            80, 90, new BigDecimal("120.00"), 0, 18, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));

        // Additional Pediatric Services
        coverages.add(createCoverage(policy, pediatrics, "Newborn Screening", "فحص حديثي الولادة",
            100, 100, new BigDecimal("180.00"), 0, 0, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Well-Child Visit", "زيارة صحة الطفل",
            80, 100, new BigDecimal("130.00"), 0, 5, AllowedGender.ALL, false, 12, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Developmental Assessment", "تقييم النمو",
            120, 90, new BigDecimal("200.00"), 0, 6, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Autism Screening", "فحص التوحد",
            150, 90, new BigDecimal("250.00"), 1, 5, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "School Health Examination", "فحص صحة المدرسة",
            70, 90, new BigDecimal("110.00"), 5, 18, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Circumcision", "الختان",
            200, 80, new BigDecimal("350.00"), 0, 1, AllowedGender.MALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Dental Checkup", "فحص أسنان الأطفال",
            60, 90, new BigDecimal("100.00"), 1, 12, AllowedGender.ALL, false, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Vision Screening", "فحص نظر الأطفال",
            50, 90, new BigDecimal("80.00"), 3, 18, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Hearing Screening", "فحص سمع الأطفال",
            60, 90, new BigDecimal("100.00"), 0, 18, AllowedGender.ALL, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Nutrition Consultation", "استشارة تغذية الأطفال",
            80, 80, new BigDecimal("130.00"), 0, 18, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Allergy Testing", "فحص حساسية الأطفال",
            200, 80, new BigDecimal("350.00"), 1, 18, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric Asthma Management", "إدارة ربو الأطفال",
            100, 90, new BigDecimal("170.00"), 0, 18, AllowedGender.ALL, false, 6, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Pediatric ENT Consultation", "استشارة أنف وأذن للأطفال",
            120, 85, new BigDecimal("200.00"), 0, 18, AllowedGender.ALL, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, pediatrics, "Ear Tube Insertion", "تركيب أنابيب الأذن",
            1500, 80, new BigDecimal("2500.00"), 0, 10, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));

        // ==================== WOMEN'S HEALTH SERVICES ====================
        ServiceCategory womensHealth = categories.get("WOMENS_HEALTH");
        coverages.add(createCoverage(policy, womensHealth, "Gynecology Consultation", "استشارة نسائية",
            150, 85, new BigDecimal("250.00"), 18, null, AllowedGender.FEMALE, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, womensHealth, "Prenatal Care", "متابعة الحمل",
            200, 90, new BigDecimal("350.00"), 18, 45, AllowedGender.FEMALE, false, 12, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Delivery - Normal", "ولادة طبيعية",
            5000, 90, new BigDecimal("8000.00"), 18, 45, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Delivery - Cesarean", "ولادة قيصرية",
            8000, 85, new BigDecimal("15000.00"), 18, 45, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Pap Smear Test", "فحص مسحة عنق الرحم",
            80, 90, new BigDecimal("150.00"), 21, null, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Pelvic Ultrasound", "سونار الحوض",
            150, 85, new BigDecimal("250.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));

        // Additional Women's Health Services
        coverages.add(createCoverage(policy, womensHealth, "HPV Test", "فحص فيروس الورم الحليمي",
            100, 90, new BigDecimal("170.00"), 21, 65, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Breast Cancer Screening", "فحص سرطان الثدي",
            200, 90, new BigDecimal("350.00"), 40, null, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Ovarian Screening", "فحص المبايض",
            150, 85, new BigDecimal("250.00"), 18, null, AllowedGender.FEMALE, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Fertility Consultation", "استشارة الخصوبة",
            250, 70, new BigDecimal("400.00"), 18, 45, AllowedGender.FEMALE, true, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "IVF Treatment", "علاج أطفال الأنابيب",
            10000, 40, new BigDecimal("15000.00"), 25, 42, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, womensHealth, "Menopause Management", "إدارة سن اليأس",
            150, 80, new BigDecimal("250.00"), 45, null, AllowedGender.FEMALE, false, 4, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Breast Biopsy", "خزعة الثدي",
            1000, 85, new BigDecimal("1800.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Hysteroscopy", "تنظير الرحم",
            800, 80, new BigDecimal("1400.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Colposcopy", "تنظير عنق الرحم",
            300, 85, new BigDecimal("500.00"), 18, null, AllowedGender.FEMALE, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "IUD Insertion", "تركيب اللولب",
            250, 80, new BigDecimal("400.00"), 18, 50, AllowedGender.FEMALE, false, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Hysterectomy", "استئصال الرحم",
            8000, 80, new BigDecimal("15000.00"), 35, null, AllowedGender.FEMALE, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Fibroid Treatment", "علاج الأورام الليفية",
            6000, 75, new BigDecimal("10000.00"), 30, null, AllowedGender.FEMALE, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "PCOS Management", "إدارة تكيس المبايض",
            150, 80, new BigDecimal("250.00"), 18, 45, AllowedGender.FEMALE, false, 6, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Endometriosis Treatment", "علاج بطانة الرحم المهاجرة",
            5000, 75, new BigDecimal("8000.00"), 18, 50, AllowedGender.FEMALE, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Breastfeeding Consultation", "استشارة الرضاعة الطبيعية",
            80, 90, new BigDecimal("130.00"), 18, 50, AllowedGender.FEMALE, false, 6, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, womensHealth, "Postpartum Care", "رعاية ما بعد الولادة",
            150, 90, new BigDecimal("250.00"), 18, 50, AllowedGender.FEMALE, false, 4, FrequencyPeriod.YEARLY));

        // ==================== MENTAL HEALTH SERVICES ====================
        ServiceCategory mentalHealth = categories.get("MENTAL_HEALTH");
        coverages.add(createCoverage(policy, mentalHealth, "Psychiatric Consultation", "استشارة نفسية",
            200, 80, new BigDecimal("350.00"), 18, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Psychological Therapy Session", "جلسة علاج نفسي",
            150, 80, new BigDecimal("250.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Child Psychology", "علم نفس الطفل",
            180, 80, new BigDecimal("300.00"), 0, 18, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));

        // Additional Mental Health Services
        coverages.add(createCoverage(policy, mentalHealth, "Group Therapy Session", "جلسة علاج جماعي",
            80, 80, new BigDecimal("130.00"), 18, null, AllowedGender.ALL, false, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Family Therapy Session", "جلسة علاج أسري",
            180, 75, new BigDecimal("300.00"), null, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Couples Counseling", "استشارة زوجية",
            170, 70, new BigDecimal("280.00"), 18, null, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Addiction Counseling", "استشارة الإدمان",
            180, 80, new BigDecimal("300.00"), 18, null, AllowedGender.ALL, true, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Eating Disorder Treatment", "علاج اضطرابات الأكل",
            200, 75, new BigDecimal("350.00"), 12, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "PTSD Treatment", "علاج اضطراب ما بعد الصدمة",
            200, 85, new BigDecimal("350.00"), null, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "OCD Treatment", "علاج الوسواس القهري",
            190, 80, new BigDecimal("320.00"), null, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Bipolar Disorder Management", "إدارة الاضطراب ثنائي القطب",
            200, 85, new BigDecimal("350.00"), 18, null, AllowedGender.ALL, true, 4, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Grief Counseling", "استشارة الحزن",
            150, 80, new BigDecimal("250.00"), null, null, AllowedGender.ALL, false, 6, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Stress Management Program", "برنامج إدارة التوتر",
            120, 75, new BigDecimal("200.00"), 18, null, AllowedGender.ALL, false, 8, FrequencyPeriod.MONTHLY));
        coverages.add(createCoverage(policy, mentalHealth, "Psychological Testing", "الاختبارات النفسية",
            300, 80, new BigDecimal("500.00"), null, null, AllowedGender.ALL, true, 2, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, mentalHealth, "Neuropsychological Assessment", "التقييم العصبي النفسي",
            500, 75, new BigDecimal("850.00"), null, null, AllowedGender.ALL, true, 1, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, mentalHealth, "Psychiatric Inpatient Stay", "إقامة نفسية داخلية",
            1500, 80, new BigDecimal("3000.00"), 18, null, AllowedGender.ALL, true, null, null));
        coverages.add(createCoverage(policy, mentalHealth, "Crisis Intervention", "التدخل في الأزمات",
            250, 100, new BigDecimal("450.00"), null, null, AllowedGender.ALL, false, null, null));
        coverages.add(createCoverage(policy, mentalHealth, "Electroconvulsive Therapy (ECT)", "العلاج بالصدمات الكهربائية",
            500, 75, new BigDecimal("900.00"), 18, null, AllowedGender.ALL, true, 12, FrequencyPeriod.YEARLY));
        coverages.add(createCoverage(policy, mentalHealth, "Transcranial Magnetic Stimulation", "التحفيز المغناطيسي عبر الجمجمة",
            400, 60, new BigDecimal("700.00"), 18, null, AllowedGender.ALL, true, 30, FrequencyPeriod.YEARLY,
            CoverageStatusType.PARTIAL));
        coverages.add(createCoverage(policy, mentalHealth, "Adolescent Mental Health", "الصحة النفسية للمراهقين",
            160, 85, new BigDecimal("270.00"), 12, 18, AllowedGender.ALL, false, 4, FrequencyPeriod.MONTHLY));

        log.info("Created {} service coverages", coverages.size());
        return coverages;
    }

    private ServiceCoverage createCoverage(GlobalPolicy policy, ServiceCategory category,
            String serviceName, String medicalName, double standardPrice, int coveragePercent,
            BigDecimal maxCoverageAmount, Integer minAge, Integer maxAge, AllowedGender gender,
            boolean requiresReferral, Integer frequencyLimit, FrequencyPeriod frequencyPeriod) {
        return createCoverage(policy, category, serviceName, medicalName, standardPrice, coveragePercent,
            maxCoverageAmount, minAge, maxAge, gender, requiresReferral, frequencyLimit, frequencyPeriod,
            CoverageStatusType.COVERED);
    }

    private ServiceCoverage createCoverage(GlobalPolicy policy, ServiceCategory category,
            String serviceName, String medicalName, double standardPrice, int coveragePercent,
            BigDecimal maxCoverageAmount, Integer minAge, Integer maxAge, AllowedGender gender,
            boolean requiresReferral, Integer frequencyLimit, FrequencyPeriod frequencyPeriod,
            CoverageStatusType status) {

        ServiceCoverage coverage = ServiceCoverage.builder()
            .globalPolicy(policy)
            .category(category)
            .serviceName(serviceName)
            .medicalName(medicalName)
            .standardPrice(new BigDecimal(standardPrice))
            .coveragePercent(new BigDecimal(coveragePercent))
            .coverageStatus(status)
            .maxCoverageAmount(maxCoverageAmount)
            .minAge(minAge)
            .maxAge(maxAge)
            .allowedGender(gender)
            .requiresReferral(requiresReferral)
            .frequencyLimit(frequencyLimit)
            .frequencyPeriod(frequencyPeriod)
            .isActive(true)
            .build();

        return serviceCoverageRepository.save(coverage);
    }
}
