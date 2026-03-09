package com.insurancesystem.Services;
import com.insurancesystem.Exception.BadRequestException;
import com.insurancesystem.Model.Entity.Enums.ReportType;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.HealthcareProviderClaimMapper;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.RadiologistRepository;
import com.insurancesystem.Repository.SearchProfileRepository;
import com.insurancesystem.Model.Entity.SearchProfile;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.LabRequest;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Model.MapStruct.LabRequestMapper;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.LabRequestDTO;
import com.insurancesystem.Model.Dto.RadiologyRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Entity.Enums.FamilyRelation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class HealthcareProviderClaimService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    private final HealthcareProviderClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRequestRepo;
    private final RadiologistRepository radiologyRequestRepo;
    private final SearchProfileRepository searchProfileRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final LabRequestMapper labRequestMapper;
    private final com.insurancesystem.Model.MapStruct.RadiologyRequestMapper radiologyRequestMapper;
    private final HealthcareProviderClaimMapper claimMapper;
    private final NotificationService notificationService;
    private final ClaimEngineService claimEngineService;
    private final ClientUsageService clientUsageService;
    private final ObjectMapper objectMapper;
    private final String UPLOAD_DIR = "uploads/healthcare-claims/";

    // Create claim by healthcare provider (doctor, pharmacist, lab tech, radiologist)
    @Transactional
    public HealthcareProviderClaimDTO createClaim(
            UUID providerId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        // Fix #62: Verify patient is active
        if (dto.getClientId() != null) {
            Optional<Client> patientOpt = clientRepo.findById(dto.getClientId());
            if (patientOpt.isPresent()) {
                Client patient = patientOpt.get();
                if (patient.getStatus() != null &&
                    patient.getStatus() != com.insurancesystem.Model.Entity.Enums.MemberStatus.ACTIVE) {
                    // Check if it's a family member instead
                    Optional<FamilyMember> fmOpt = familyMemberRepo.findById(dto.getClientId());
                    if (fmOpt.isEmpty()) {
                        throw new BadRequestException("Patient account is not active. Current status: " + patient.getStatus());
                    }
                }
            }
        }

        // Check provider role
        RoleName providerRole = provider.getRoles().stream()
                .map(r -> r.getName())
                .filter(r -> r == RoleName.DOCTOR || r == RoleName.PHARMACIST ||
                            r == RoleName.LAB_TECH || r == RoleName.RADIOLOGIST)
                .findFirst()
                .orElse(null);

        // Check for follow-up visit (Doctor claims only)
        // First check if the claim is explicitly marked as a follow-up
        boolean isExplicitFollowUp = false;
        if (dto.getRoleSpecificData() != null) {
            try {
                java.util.Map<String, Object> preCheckRoleData = objectMapper.readValue(
                        dto.getRoleSpecificData(), java.util.Map.class);
                Object isFollowUpObj = preCheckRoleData.get("isFollowUp");
                if (isFollowUpObj instanceof Boolean && (Boolean) isFollowUpObj) {
                    isExplicitFollowUp = true;
                } else if (isFollowUpObj instanceof String && Boolean.parseBoolean((String) isFollowUpObj)) {
                    isExplicitFollowUp = true;
                }
            } catch (Exception e) {
                log.debug("Could not parse roleSpecificData for follow-up pre-check: {}", e.getMessage());
            }
        }

        // Only block duplicate claims if this is NOT an explicit follow-up claim
        if (!isExplicitFollowUp && providerRole == RoleName.DOCTOR && dto.getClientId() != null) {
            String specialization = provider.getSpecialization();

            if (specialization != null && !specialization.isEmpty()) {
                LocalDate fourteenDaysAgo = LocalDate.now().minusDays(14);

                // Check if there's a recent claim from same specialization
                boolean hasRecentVisit = claimRepo.existsByClientIdAndSpecializationAndServiceDateAfter(
                    dto.getClientId(), specialization, fourteenDaysAgo);

                if (hasRecentVisit) {
                    throw new IllegalStateException(
                        "Follow-up visits within 14 days are not covered. " +
                        "Patient had a recent visit to " + specialization + " specialist.");
                }
            }
        }

        // Fix #60: Prevent duplicate claims for same prescription/lab test/radiology request
        if (dto.getRoleSpecificData() != null) {
            try {
                java.util.Map<String, Object> checkData = objectMapper.readValue(
                        dto.getRoleSpecificData(), java.util.Map.class);
                String prescriptionId = (String) checkData.get("prescriptionId");
                String testId = (String) checkData.get("testId");
                String referenceId = prescriptionId != null ? prescriptionId : testId;

                if (referenceId != null) {
                    boolean duplicateExists = claimRepo.existsByRoleSpecificDataContainingAndStatusNotIn(
                            referenceId,
                            List.of(ClaimStatus.REJECTED_MEDICAL, ClaimStatus.REJECTED_FINAL));
                    if (duplicateExists) {
                        throw new IllegalStateException(
                                "A claim has already been submitted for this service. Duplicate claims are not allowed.");
                    }
                }
            } catch (IllegalStateException e) {
                throw e; // Re-throw our own exception
            } catch (Exception e) {
                log.debug("Could not check for duplicate claims: {}", e.getMessage());
            }
        }

        // Fix #63: Check prescription expiry for pharmacist claims
        if (dto.getRoleSpecificData() != null) {
            try {
                java.util.Map<String, Object> expiryCheckData = objectMapper.readValue(
                        dto.getRoleSpecificData(), java.util.Map.class);
                String prescIdStr = (String) expiryCheckData.get("prescriptionId");
                if (prescIdStr != null) {
                    UUID prescId = UUID.fromString(prescIdStr);
                    Optional<Prescription> prescOpt = prescriptionRepo.findById(prescId);
                    if (prescOpt.isPresent()) {
                        Prescription presc = prescOpt.get();
                        boolean hasExpiredItem = presc.getItems().stream()
                                .anyMatch(item -> item.getExpiryDate() != null
                                        && item.getExpiryDate().isBefore(Instant.now()));
                        if (hasExpiredItem) {
                            throw new BadRequestException("Prescription has expired");
                        }
                    }
                }
            } catch (BadRequestException e) {
                throw e; // Re-throw our own exception
            } catch (Exception e) {
                log.debug("Could not check prescription expiry: {}", e.getMessage());
            }
        }

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(provider);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // Check if this is a follow-up visit from roleSpecificData
        boolean isFollowUp = false;
        java.math.BigDecimal originalConsultationFee = null;
        if (dto.getRoleSpecificData() != null) {
            try {
                java.util.Map<String, Object> roleData = objectMapper.readValue(
                        dto.getRoleSpecificData(),
                        java.util.Map.class
                );
                Object isFollowUpObj = roleData.get("isFollowUp");
                if (isFollowUpObj instanceof Boolean) {
                    isFollowUp = (Boolean) isFollowUpObj;
                } else if (isFollowUpObj instanceof String) {
                    isFollowUp = Boolean.parseBoolean((String) isFollowUpObj);
                }
                
                // If follow-up, store original consultation fee and set amount to 0
                if (isFollowUp) {
                    // Get original consultation fee from roleData if available
                    Object originalFeeObj = roleData.get("originalConsultationFee");
                    if (originalFeeObj != null) {
                        if (originalFeeObj instanceof Number) {
                            double feeValue = ((Number) originalFeeObj).doubleValue();
                            if (feeValue > 0) {
                                originalConsultationFee = java.math.BigDecimal.valueOf(feeValue);
                            }
                        } else if (originalFeeObj instanceof String) {
                            try {
                                double feeValue = Double.parseDouble((String) originalFeeObj);
                                if (feeValue > 0) {
                                    originalConsultationFee = new java.math.BigDecimal((String) originalFeeObj);
                                }
                            } catch (Exception e) {
                                log.warn("Error parsing originalConsultationFee: {}", e.getMessage());
                            }
                        }
                    }

                    claim.setAmount(java.math.BigDecimal.ZERO); // Insurance doesn't pay for follow-up consultation
                    if (originalConsultationFee != null && originalConsultationFee.compareTo(java.math.BigDecimal.ZERO) > 0) {
                        claim.setOriginalConsultationFee(originalConsultationFee);
                    } else {
                        log.warn("Follow-up visit detected but originalConsultationFee is missing or zero. Claim ID: {}", claim.getId());
                    }
                }
            } catch (Exception e) {
                log.warn("Error parsing roleSpecificData for follow-up check: {}", e.getMessage());
            }
        }
        
        claim.setIsFollowUp(isFollowUp);

        // Handle patient info (can be Client or FamilyMember)
        Client patient = null;
        String patientName = null;
        
        if (claim.getClientId() != null) {
            Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
            if (familyMemberOpt.isPresent()) {
                FamilyMember familyMember = familyMemberOpt.get();
                claim.setClientName(familyMember.getFullName());
                patientName = familyMember.getFullName();
                patient = familyMember.getClient();
            } else {
                Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
                if (clientOpt.isPresent()) {
                    Client client = clientOpt.get();
                    
                    // Extract family member info from role-specific data (pharmacist, lab, radiology)
                    boolean isPharmacist = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.PHARMACIST);
                    
                    boolean foundFamilyMember = false;
                    
                    if (isPharmacist && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String prescriptionIdStr = (String) roleData.get("prescriptionId");
                            
                            if (prescriptionIdStr != null) {
                                UUID prescriptionId = UUID.fromString(prescriptionIdStr);
                                Optional<Prescription> prescriptionOpt = prescriptionRepo.findById(prescriptionId);
                                
                                if (prescriptionOpt.isPresent()) {
                                    Prescription prescription = prescriptionOpt.get();
                                    PrescriptionDTO prescriptionDto = prescriptionMapper.toDto(prescription, familyMemberRepo);
                                    
                                    if (prescriptionDto.getIsFamilyMember() != null && prescriptionDto.getIsFamilyMember()) {
                                        String familyMemberName = prescriptionDto.getFamilyMemberName();
                                        String familyMemberRelationStr = prescriptionDto.getFamilyMemberRelation();
                                        
                                        if (familyMemberName != null && familyMemberRelationStr != null) {
                                            try {
                                                FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                
                                                Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                        client.getId(), 
                                                        familyMemberName, 
                                                        relation
                                                );
                                                
                                                if (fmOpt.isPresent()) {
                                                    FamilyMember fm = fmOpt.get();
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            } catch (IllegalArgumentException e) {
                                                log.debug("Invalid family relation value while parsing prescription data: {}", e.getMessage());
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing prescription role-specific data for family member lookup: {}", e.getMessage());
                        }
                    }

                    if (!foundFamilyMember && isPharmacist && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.debug("Invalid family relation value while parsing pharmacist treatment details: {}", e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing pharmacist treatment details for family member lookup: {}", e.getMessage());
                        }
                    }

                    boolean isLabTech = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.LAB_TECH);
                    
                    if (!foundFamilyMember && isLabTech && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String testIdStr = (String) roleData.get("testId");
                            
                            if (testIdStr != null) {
                                UUID labRequestId = UUID.fromString(testIdStr);
                                Optional<LabRequest> labRequestOpt = labRequestRepo.findByIdWithMember(labRequestId);
                                
                                if (labRequestOpt.isPresent()) {
                                    LabRequest labRequest = labRequestOpt.get();
                                    LabRequestDTO labRequestDto = labRequestMapper.toDto(labRequest, familyMemberRepo);
                                    
                                    if ((labRequestDto.getIsFamilyMember() == null || !labRequestDto.getIsFamilyMember()) 
                                            && (labRequest.getNotes() != null || labRequest.getTreatment() != null)) {
                                        String textToSearch = labRequest.getNotes() != null ? labRequest.getNotes() : labRequest.getTreatment();
                                        if (textToSearch != null && textToSearch.toLowerCase().contains("family member:")) {
                                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";
                                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                                            java.util.regex.Matcher matcher = pattern.matcher(textToSearch);
                                            
                                            if (matcher.find()) {
                                                String familyMemberName = matcher.group(1).trim();
                                                String familyMemberRelationStr = matcher.group(2).trim();
                                                
                                                try {
                                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                    
                                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                            client.getId(), 
                                                            familyMemberName, 
                                                            relation
                                                    );
                                                    
                                                    if (fmOpt.isPresent()) {
                                                        FamilyMember fm = fmOpt.get();
                                                        claim.setClientId(fm.getId());
                                                        claim.setClientName(fm.getFullName());
                                                        patientName = fm.getFullName();
                                                        patient = client;
                                                        foundFamilyMember = true;
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    log.debug("Invalid family relation value in lab request notes: {}", e.getMessage());
                                                }
                                            }
                                        }
                                    }

                                    if (labRequestDto.getIsFamilyMember() != null && labRequestDto.getIsFamilyMember()) {
                                        if (labRequestDto.getFamilyMemberId() != null) {
                                            Optional<FamilyMember> fmOpt = familyMemberRepo.findById(labRequestDto.getFamilyMemberId());
                                            
                                            if (fmOpt.isPresent()) {
                                                FamilyMember fm = fmOpt.get();
                                                if (fm.getClient() != null && fm.getClient().getId().equals(client.getId())) {
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            }
                                        }
                                        
                                        if (!foundFamilyMember) {
                                        String familyMemberName = labRequestDto.getFamilyMemberName();
                                        String familyMemberRelationStr = labRequestDto.getFamilyMemberRelation();
                                        
                                        if (familyMemberName != null && familyMemberRelationStr != null) {
                                            try {
                                                FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                
                                                Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                        client.getId(), 
                                                        familyMemberName, 
                                                        relation
                                                );
                                                
                                                if (fmOpt.isPresent()) {
                                                    FamilyMember fm = fmOpt.get();
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                        patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            } catch (IllegalArgumentException e) {
                                                log.debug("Invalid family relation value in lab request DTO: {}", e.getMessage());
                                            }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing lab tech role-specific data for family member lookup: {}", e.getMessage());
                        }
                    }

                    if (!foundFamilyMember && isLabTech && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.debug("Invalid family relation value in lab tech treatment details: {}", e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing lab tech treatment details for family member lookup: {}", e.getMessage());
                        }
                    }

                    boolean isRadiologist = provider.getRoles().stream()
                            .anyMatch(r -> r.getName() == RoleName.RADIOLOGIST);
                    
                    if (!foundFamilyMember && isRadiologist && claim.getRoleSpecificData() != null) {
                        try {
                            java.util.Map<String, Object> roleData = objectMapper.readValue(
                                    claim.getRoleSpecificData(), 
                                    java.util.Map.class
                            );
                            String testIdStr = (String) roleData.get("testId");
                            
                            if (testIdStr != null) {
                                UUID radiologyRequestId = UUID.fromString(testIdStr);
                                Optional<com.insurancesystem.Model.Entity.RadiologyRequest> radiologyRequestOpt = radiologyRequestRepo.findById(radiologyRequestId);
                                
                                if (radiologyRequestOpt.isPresent()) {
                                    com.insurancesystem.Model.Entity.RadiologyRequest radiologyRequest = radiologyRequestOpt.get();
                                    RadiologyRequestDTO radiologyRequestDto = radiologyRequestMapper.toDto(radiologyRequest, familyMemberRepo);
                                    
                                    if ((radiologyRequestDto.getIsFamilyMember() == null || !radiologyRequestDto.getIsFamilyMember()) 
                                            && (radiologyRequest.getNotes() != null || radiologyRequest.getTreatment() != null)) {
                                        String textToSearch = radiologyRequest.getNotes() != null ? radiologyRequest.getNotes() : radiologyRequest.getTreatment();
                                        if (textToSearch != null && textToSearch.toLowerCase().contains("family member:")) {
                                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:\\s*([^-]+?)(?:\\s*-\\s*Age:\\s*([^-]+?))?(?:\\s*-\\s*Gender:\\s*([^\\n\\r]+))?";
                                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                                            java.util.regex.Matcher matcher = pattern.matcher(textToSearch);
                                            
                                            if (matcher.find()) {
                                                String familyMemberName = matcher.group(1).trim();
                                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.debug("Invalid family relation value in radiology request notes: {}", e.getMessage());
                                }
                                            }
                                        }
                                    }

                                    if (radiologyRequestDto.getIsFamilyMember() != null && radiologyRequestDto.getIsFamilyMember()) {
                                        if (radiologyRequestDto.getFamilyMemberId() != null) {
                                            Optional<FamilyMember> fmOpt = familyMemberRepo.findById(radiologyRequestDto.getFamilyMemberId());
                                            
                                            if (fmOpt.isPresent()) {
                                                FamilyMember fm = fmOpt.get();
                                                if (fm.getClient() != null && fm.getClient().getId().equals(client.getId())) {
                                                    claim.setClientId(fm.getId());
                                                    claim.setClientName(fm.getFullName());
                                                    patientName = fm.getFullName();
                                                    patient = client;
                                                    foundFamilyMember = true;
                                                }
                                            }
                                        }
                                        
                                        if (!foundFamilyMember) {
                                            String familyMemberName = radiologyRequestDto.getFamilyMemberName();
                                            String familyMemberRelationStr = radiologyRequestDto.getFamilyMemberRelation();
                                            
                                            if (familyMemberName != null && familyMemberRelationStr != null) {
                                                try {
                                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                                    
                                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                                            client.getId(), 
                                                            familyMemberName, 
                                                            relation
                                                    );
                                                    
                                                    if (fmOpt.isPresent()) {
                                                        FamilyMember fm = fmOpt.get();
                                                        claim.setClientId(fm.getId());
                                                        claim.setClientName(fm.getFullName());
                                                        patientName = fm.getFullName();
                                                        patient = client;
                                                        foundFamilyMember = true;
                                                    }
                                                } catch (IllegalArgumentException e) {
                                                    log.debug("Invalid family relation value in radiology request DTO: {}", e.getMessage());
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing radiologist role-specific data for family member lookup: {}", e.getMessage());
                        }
                    }

                    if (!foundFamilyMember && isRadiologist && claim.getTreatmentDetails() != null) {
                        try {
                            String treatmentDetails = claim.getTreatmentDetails();
                            String familyMemberPattern = "Family Member:\\s*([^-]+?)\\s*\\(([^)]+)\\)\\s*-\\s*Insurance:";
                            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(familyMemberPattern, java.util.regex.Pattern.CASE_INSENSITIVE);
                            java.util.regex.Matcher matcher = pattern.matcher(treatmentDetails);
                            
                            if (matcher.find()) {
                                String familyMemberName = matcher.group(1).trim();
                                String familyMemberRelationStr = matcher.group(2).trim();
                                
                                try {
                                    FamilyRelation relation = FamilyRelation.valueOf(familyMemberRelationStr.toUpperCase());
                                    
                                    Optional<FamilyMember> fmOpt = familyMemberRepo.findByClient_IdAndFullNameAndRelation(
                                            client.getId(), 
                                            familyMemberName, 
                                            relation
                                    );
                                    
                                    if (fmOpt.isPresent()) {
                                        FamilyMember fm = fmOpt.get();
                                        claim.setClientId(fm.getId());
                                        claim.setClientName(fm.getFullName());
                                        patientName = fm.getFullName();
                                        patient = client;
                                        foundFamilyMember = true;
                                    }
                                } catch (IllegalArgumentException e) {
                                    log.debug("Invalid family relation value in radiologist treatment details: {}", e.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            log.debug("Error parsing radiologist treatment details for family member lookup: {}", e.getMessage());
                        }
                    }

                    if (patientName == null) {
                        claim.setClientName(client.getFullName());
                        patientName = client.getFullName();
                        patient = client;
                    }
                } else {
                    patientName = null;
                }
            }
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        // Check for chronic disease bypass
        boolean isChronicBypass = false;
        if (dto.getIsChronic() != null && dto.getIsChronic()) {
            // Validate patient has chronic disease registered
            Client patientForChronic = dto.getClientId() != null ?
                    clientRepo.findById(dto.getClientId()).orElse(null) : null;

            if (patientForChronic == null) {
                // Check if it's a family member
                Optional<FamilyMember> familyMemberOpt = dto.getClientId() != null ?
                        familyMemberRepo.findById(dto.getClientId()) : Optional.empty();
                if (familyMemberOpt.isPresent()) {
                    patientForChronic = familyMemberOpt.get().getClient();
                }
            }

            if (patientForChronic != null) {
                // Check if patient has chronicDiseases field populated
                if (patientForChronic.getChronicDiseases() != null && !patientForChronic.getChronicDiseases().isEmpty()) {
                    isChronicBypass = true;
                    claim.setIsChronic(true);
                } else {
                    throw new IllegalStateException(
                        "Patient is not registered with chronic diseases. Cannot bypass medical review.");
                }
            } else {
                throw new NotFoundException("Patient not found for chronic disease validation");
            }
        }

        // Set initial status based on chronic bypass
        if (isChronicBypass) {
            claim.setStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW);
        } else {
            claim.setStatus(ClaimStatus.PENDING_MEDICAL);
        }

        // Apply coverage rules
        claim = claimEngineService.applyCoverageRules(claim);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);
        final Client finalPatient = patient;
        final String finalPatientName = patientName;
        final HealthcareProviderClaim finalClaim = savedClaim;

        // Send notification to medical admins
        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> {
                    String notificationMessage;
                    // Check if follow-up: either isFollowUp flag is true OR amount is 0 for DOCTOR claims
                    boolean isFollowUpClaim = (finalClaim.getIsFollowUp() != null && finalClaim.getIsFollowUp()) ||
                            (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) &&
                             finalClaim.getAmount() != null && finalClaim.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0);

                    if (isFollowUpClaim) {
                        // Follow-up visit notification - only mention it's follow-up with 0 amount, patient pays
                        notificationMessage = "⚠️ مطالبة زيارة متابعة (Follow-up Visit) من الدكتور " + provider.getFullName() +
                                (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                                " - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع - المريض يدفع المبلغ)";
                    } else {
                        // Normal visit notification
                        notificationMessage = "📋 مطالبة جديدة من " + provider.getFullName() +
                                (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                                " - المبلغ: " + finalClaim.getAmount() + " شيكل";
                    }
                    String englishNotificationMessage;
                    if (isFollowUpClaim) {
                        englishNotificationMessage = "⚠️ Follow-up visit claim from Dr. " + provider.getFullName() +
                                (finalPatientName != null ? " for patient " + finalPatientName : "") +
                                " - Doctor amount: 0 ILS (Insurance does not pay - patient pays the amount)";
                    } else {
                        englishNotificationMessage = "📋 New claim from " + provider.getFullName() +
                                (finalPatientName != null ? " for patient " + finalPatientName : "") +
                                " - Amount: " + finalClaim.getAmount() + " ILS";
                    }
                    notificationService.sendToUser(medicalAdmin.getId(), notificationMessage, englishNotificationMessage, finalClaim.getId(), "CLAIM");
                });

        // Check if follow-up for provider notification
        boolean isFollowUpForProvider = (finalClaim.getIsFollowUp() != null && finalClaim.getIsFollowUp()) ||
                (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) &&
                 finalClaim.getAmount() != null && finalClaim.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0);

        if (isFollowUpForProvider) {
            // Follow-up visit notification for doctor
            String consultationFee = finalClaim.getOriginalConsultationFee() != null ?
                    finalClaim.getOriginalConsultationFee().toString() : "0";
            notificationService.sendToUser(
                    provider.getId(),
                    "✅ تم إرسال مطالبة زيارة متابعة بنجاح - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع)" +
                            (finalPatientName != null ? " - المريض " + finalPatientName + " يجب أن يدفع سعر الكشفية: " + consultationFee + " شيكل" :
                                    " - المريض يجب أن يدفع سعر الكشفية: " + consultationFee + " شيكل") +
                            " - في انتظار المراجعة الطبية",
                    "✅ Follow-up visit claim submitted successfully - Doctor amount: 0 ILS (Insurance does not pay)" +
                            (finalPatientName != null ? " - Patient " + finalPatientName + " must pay the consultation fee: " + consultationFee + " ILS" :
                                    " - Patient must pay the consultation fee: " + consultationFee + " ILS") +
                            " - Pending medical review",
                    finalClaim.getId(), "CLAIM"
            );
        } else {
            // Normal visit notification for provider
            notificationService.sendToUser(
                    provider.getId(),
                    "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + finalClaim.getAmount() + " شيكل" +
                            (finalPatientName != null ? " للمريض " + finalPatientName : "") +
                            " - في انتظار المراجعة الطبية",
                    "✅ Your claim has been submitted successfully - Amount: " + finalClaim.getAmount() + " ILS" +
                            (finalPatientName != null ? " for patient " + finalPatientName : "") +
                            " - Pending medical review",
                    finalClaim.getId(), "CLAIM"
            );
        }

        if (finalPatient != null) {
            // Check if follow-up for patient notification
            boolean isFollowUpForPatient = (claim.getIsFollowUp() != null && claim.getIsFollowUp()) ||
                    (provider.getRoles().stream().anyMatch(r -> r.getName() == RoleName.DOCTOR) && 
                     claim.getAmount() != null && claim.getAmount().compareTo(java.math.BigDecimal.ZERO) == 0);

            if (isFollowUpForPatient) {
                // Follow-up visit notification for patient
                String consultationFee = claim.getOriginalConsultationFee() != null ? 
                        claim.getOriginalConsultationFee().toString() : "0";
                notificationService.sendToUser(
                        finalPatient.getId(),
                        "📋 تم إنشاء مطالبة طبية لك من " + provider.getFullName() +
                                " - نوع الزيارة: زيارة متابعة (Follow-up Visit)" +
                                " - المبلغ للدكتور: 0 شيكل (التأمين لا يدفع)" +
                                " - يجب عليك دفع سعر الكشفية: " + consultationFee + " شيكل" +
                                " - في انتظار المراجعة",
                        "📋 A medical claim has been created for you by " + provider.getFullName() +
                                " - Visit type: Follow-up Visit" +
                                " - Doctor amount: 0 ILS (Insurance does not pay)" +
                                " - You must pay the consultation fee: " + consultationFee + " ILS" +
                                " - Pending review",
                        finalClaim.getId(), "CLAIM"
                );
            } else {
                // Normal visit notification for patient
                notificationService.sendToUser(
                        finalPatient.getId(),
                        "📋 تم إنشاء مطالبة طبية لك من " + provider.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " شيكل" +
                                " - في انتظار المراجعة",
                        "📋 A medical claim has been created for you by " + provider.getFullName() +
                                " - Amount: " + claim.getAmount() + " ILS" +
                                " - Pending review",
                        finalClaim.getId(), "CLAIM"
                );
            }
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        resultDto.setProviderEmployeeId(provider.getEmployeeId());
        resultDto.setProviderNationalId(provider.getNationalId());
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Create self-service claim by client
    public HealthcareProviderClaimDTO createClientClaim(
            UUID clientId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client client = clientRepo.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(client);
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        // Handle beneficiary: can be client themselves or a family member
        UUID beneficiaryId = dto.getClientId();
        String beneficiaryName = null;
        FamilyMember familyMember = null;

        if (beneficiaryId != null) {
            // First, try to find as a family member
            Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(beneficiaryId);
            
            if (familyMemberOpt.isPresent()) {
                familyMember = familyMemberOpt.get();
                // Verify that this family member belongs to the authenticated client
                if (familyMember.getClient() == null || !familyMember.getClient().getId().equals(clientId)) {
                    throw new BadRequestException("Family member does not belong to this client");
                }
                // Verify family member is approved
                if (familyMember.getStatus() != com.insurancesystem.Model.Entity.Enums.ProfileStatus.APPROVED) {
                    throw new BadRequestException("Family member is not approved");
                }
                claim.setClientId(beneficiaryId);
                claim.setClientName(familyMember.getFullName());
                beneficiaryName = familyMember.getFullName();
            } else {
                // Try to find as a client
                Optional<Client> beneficiaryClientOpt = clientRepo.findById(beneficiaryId);
                if (beneficiaryClientOpt.isPresent()) {
                    Client beneficiaryClient = beneficiaryClientOpt.get();
                    // Verify it's the authenticated client themselves
                    if (!beneficiaryClient.getId().equals(clientId)) {
                        throw new BadRequestException("Cannot create claim for another client");
                    }
                    claim.setClientId(clientId);
                    claim.setClientName(client.getFullName());
                    beneficiaryName = client.getFullName();
                } else {
                    throw new NotFoundException("Beneficiary not found");
                }
            }
        } else {
            // No beneficiary specified, use the authenticated client themselves
            claim.setClientId(clientId);
            claim.setClientName(client.getFullName());
            beneficiaryName = client.getFullName();
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        // Apply coverage rules before saving
        claim = claimEngineService.applyCoverageRules(claim);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // Notification message
        String notificationMessage = familyMember != null
            ? "📋 مطالبة جديدة من العميل " + client.getFullName() +
              " لعضو الأسرة " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل"
            : "📋 مطالبة جديدة من العميل " + client.getFullName() +
              " - المبلغ: " + claim.getAmount() + " شيكل";

        String englishNotificationMessage = familyMember != null
            ? "📋 New claim from client " + client.getFullName() +
              " for family member " + beneficiaryName +
              " - Amount: " + claim.getAmount() + " ILS"
            : "📋 New claim from client " + client.getFullName() +
              " - Amount: " + claim.getAmount() + " ILS";

        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        notificationMessage,
                        englishNotificationMessage,
                        savedClaim.getId(), "CLAIM"
                ));

        String clientNotificationMessage = familyMember != null
            ? "✅ تم إرسال مطالبة لعضو الأسرة " + beneficiaryName + " بنجاح - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية"
            : "✅ تم إرسال مطالبتك بنجاح - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية";

        String englishClientNotificationMessage = familyMember != null
            ? "✅ Claim for family member " + beneficiaryName + " submitted successfully - Amount: " + claim.getAmount() + " ILS" +
              " - Pending medical review"
            : "✅ Your claim has been submitted successfully - Amount: " + claim.getAmount() + " ILS" +
              " - Pending medical review";

        notificationService.sendToUser(
                client.getId(),
                clientNotificationMessage,
                englishClientNotificationMessage,
                savedClaim.getId(), "CLAIM"
        );

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        resultDto.setProviderEmployeeId(client.getEmployeeId());
        resultDto.setProviderNationalId(client.getNationalId());
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Get claims for provider or client (different logic for each)
    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID userId) {
        Client user = clientRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        boolean isClient = user.getRoles()
                .stream()
                .anyMatch(r -> r.getName() == RoleName.INSURANCE_CLIENT);

        List<HealthcareProviderClaim> claims;

        if (isClient) {
            // Client sees claims for themselves AND their family members
            List<UUID> allIds = new ArrayList<>();
            allIds.add(user.getId());
            List<FamilyMember> familyMembers = familyMemberRepo.findByClient_Id(user.getId());
            for (FamilyMember fm : familyMembers) {
                allIds.add(fm.getId());
            }
            claims = claimRepo.findByClientIdIn(allIds);
        } else {
            // Provider sees only claims they submitted (where they are the healthcare provider)
            claims = claimRepo.findByHealthcareProviderId(user.getId());
        }

        List<HealthcareProviderClaimDTO> result = new ArrayList<>();
        for (HealthcareProviderClaim claim : claims) {
            try {
                if (claim.getStatus() == null) {
                    claim.setStatus(ClaimStatus.PENDING_MEDICAL);
                }
                
                HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
                dto.setMedicalReviewerName(claim.getMedicalReviewerName());
                dto.setMedicalReviewedAt(claim.getMedicalReviewedAt());
                populatePatientInfo(claim, dto);
                result.add(dto);
            } catch (IllegalArgumentException e) {
                continue;
            } catch (Exception e) {
                continue;
            }
        }
        return result;
    }

    /**
     * Find the main client ID for notification purposes.
     * If clientId refers to a FamilyMember, returns the main client's ID.
     * If clientId refers to a Client, returns that ID directly.
     */
    private UUID resolveNotificationRecipientId(UUID clientId) {
        if (clientId == null) return null;
        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(clientId);
        if (familyMemberOpt.isPresent()) {
            Client mainClient = familyMemberOpt.get().getClient();
            return mainClient != null ? mainClient.getId() : null;
        }
        return clientRepo.findById(clientId).map(Client::getId).orElse(null);
    }

    private void populatePatientInfo(HealthcareProviderClaim claim, HealthcareProviderClaimDTO dto) {
        if (claim.getClientId() == null) {
            return;
        }

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        
        if (familyMemberOpt.isPresent()) {
            FamilyMember familyMember = familyMemberOpt.get();
            
            dto.setFamilyMemberId(familyMember.getId());
            dto.setFamilyMemberName(familyMember.getFullName());
            dto.setFamilyMemberRelation(familyMember.getRelation() != null ? familyMember.getRelation().toString() : null);
            dto.setFamilyMemberAge(calculateAge(familyMember.getDateOfBirth()));
            dto.setFamilyMemberGender(familyMember.getGender() != null ? familyMember.getGender().toString() : null);
            dto.setFamilyMemberInsuranceNumber(familyMember.getInsuranceNumber());
            dto.setFamilyMemberNationalId(familyMember.getNationalId());
            
            Client mainClient = familyMember.getClient();
            if (mainClient != null) {
                dto.setClientId(mainClient.getId());
                dto.setClientName(mainClient.getFullName());
                Integer mainClientAge = calculateAge(mainClient.getDateOfBirth());
                dto.setClientAge(mainClientAge);
                dto.setClientGender(mainClient.getGender() != null ? mainClient.getGender().toString() : null);
                dto.setClientEmployeeId(mainClient.getEmployeeId());
                dto.setClientNationalId(mainClient.getNationalId());
                dto.setClientFaculty(mainClient.getFaculty());
                dto.setClientDepartment(mainClient.getDepartment());
            }
        } else {
            clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                dto.setClientId(client.getId());
                dto.setClientName(client.getFullName());
                Integer clientAge = calculateAge(client.getDateOfBirth());
                dto.setClientAge(clientAge);
                dto.setClientGender(client.getGender() != null ? client.getGender().toString() : null);
                dto.setClientEmployeeId(client.getEmployeeId());
                dto.setClientNationalId(client.getNationalId());
                dto.setClientFaculty(client.getFaculty());
                dto.setClientDepartment(client.getDepartment());
            });
        }
    }

    private void populatePatientInfoForMedicalDTO(HealthcareProviderClaim claim, HealthcareProviderClaimMedicalDTO dto) {
        if (claim.getClientId() == null) {
            return;
        }

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        
        if (familyMemberOpt.isPresent()) {
            FamilyMember familyMember = familyMemberOpt.get();
            dto.setFamilyMemberId(familyMember.getId());
            dto.setFamilyMemberName(familyMember.getFullName());
            dto.setFamilyMemberRelation(familyMember.getRelation() != null ? familyMember.getRelation().toString() : null);
            dto.setFamilyMemberAge(calculateAge(familyMember.getDateOfBirth()));
            dto.setFamilyMemberGender(familyMember.getGender() != null ? familyMember.getGender().toString() : null);
            dto.setFamilyMemberInsuranceNumber(familyMember.getInsuranceNumber());
            dto.setFamilyMemberNationalId(familyMember.getNationalId());
            
            Client mainClient = familyMember.getClient();
            if (mainClient != null) {
                dto.setClientId(mainClient.getId());
                dto.setClientName(mainClient.getFullName());
                dto.setClientAge(calculateAge(mainClient.getDateOfBirth()));
                dto.setClientGender(mainClient.getGender() != null ? mainClient.getGender().toString() : null);
                dto.setEmployeeId(mainClient.getEmployeeId());
                dto.setClientNationalId(mainClient.getNationalId());
                dto.setClientFaculty(mainClient.getFaculty());
                dto.setClientDepartment(mainClient.getDepartment());
            }
        } else {
            clientRepo.findById(claim.getClientId()).ifPresent(client -> {
                dto.setClientId(client.getId());
                dto.setClientName(client.getFullName());
                dto.setClientAge(calculateAge(client.getDateOfBirth()));
                dto.setClientGender(client.getGender() != null ? client.getGender().toString() : null);
                dto.setEmployeeId(client.getEmployeeId());
                dto.setClientNationalId(client.getNationalId());
                dto.setClientFaculty(client.getFaculty());
                dto.setClientDepartment(client.getDepartment());
            });
        }
    }

    private Integer calculateAge(java.time.LocalDate dateOfBirth) {
        if (dateOfBirth == null) {
            return null;
        }
        java.time.LocalDate today = java.time.LocalDate.now();
        int age = today.getYear() - dateOfBirth.getYear();
        if (today.getMonthValue() < dateOfBirth.getMonthValue() ||
                (today.getMonthValue() == dateOfBirth.getMonthValue() && today.getDayOfMonth() < dateOfBirth.getDayOfMonth())) {
            age--;
        }
        return age > 0 ? age : null;
    }

    // Helper: Get provider role (handles self-service client claims)
    private String getProviderRole(HealthcareProviderClaim claim) {
        if (claim.getClientId() != null &&
                claim.getHealthcareProvider().getId().equals(claim.getClientId())) {
            return "INSURANCE_CLIENT";
        }
        // First try requestedRole (used by this system), then fall back to roles collection
        Client provider = claim.getHealthcareProvider();
        if (provider.getRequestedRole() != null) {
            return provider.getRequestedRole().name();
        }
        return provider.getRoles()
                .stream()
                .findFirst()
                .map(r -> r.getName().name())
                .orElse("UNKNOWN");
    }

    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimDTO> getAllClaims() {
        return claimRepo.findAllWithProvider()
                .stream()
                .map(claim -> {
                    HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
                    populatePatientInfo(claim, dto);
                    return dto;
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<HealthcareProviderClaimDTO> getAllClaims(Pageable pageable) {
        return claimRepo.findAllWithProvider(pageable)
                .map(claim -> {
                    HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
                    populatePatientInfo(claim, dto);
                    return dto;
                });
    }

    @Transactional(readOnly = true)
    public HealthcareProviderClaimDTO getClaim(UUID id, UUID requesterId, boolean isManager) {
        HealthcareProviderClaim claim = claimRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getHealthcareProvider().getId().equals(requesterId))
            throw new NotFoundException("Claim not found for this provider");

        HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
        populatePatientInfo(claim, dto);
        return dto;
    }

    // Medical admin rejects claim
    @Transactional
    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        // Accept both new and legacy status values for medical rejection
        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL &&
                claim.getStatus() != ClaimStatus.PENDING &&  // Legacy status
                claim.getStatus() != ClaimStatus.RETURNED_FOR_REVIEW) {
            throw new BadRequestException("Claim was already processed");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        "❌ تم رفض مطالبتك من المراجع الطبي " + reviewer.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " شيكل" +
                                (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : ""),
                        "❌ Your claim has been rejected by medical reviewer " + reviewer.getFullName() +
                                " - Amount: " + claim.getAmount() + " ILS" +
                                (reason != null && !reason.isEmpty() ? "\nReason: " + reason : ""),
                        claim.getId(), "CLAIM"
                );
            }

            if (claim.getClientId() != null) {
                UUID rejectPatientNotifyId = resolveNotificationRecipientId(claim.getClientId());
                if (rejectPatientNotifyId != null) {
                    String providerName = claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة";
                    notificationService.sendToUser(
                            rejectPatientNotifyId,
                            "❌ تم رفض مطالبتك الطبية من " + providerName +
                                    " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد"),
                            "❌ Your medical claim from " + providerName +
                                    " has been rejected - Reason: " + (reason != null && !reason.isEmpty() ? reason : "Not specified"),
                            claim.getId(), "CLAIM"
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send medical rejection notifications for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Medical admin approves claim (moves to coordination review)
    @Transactional
    public HealthcareProviderClaimDTO approveMedical(UUID claimId, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        // Accept both new and legacy status values for medical review
        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL &&
                claim.getStatus() != ClaimStatus.PENDING &&  // Legacy status
                claim.getStatus() != ClaimStatus.RETURNED_FOR_REVIEW) {
            throw new BadRequestException("Claim already processed");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setMedicalReviewerId(reviewerId);
        claim.setMedicalReviewerName(reviewer.getFullName());
        claim.setMedicalReviewedAt(Instant.now());
        claim.setStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 إشعار لمقدم الخدمة مع معلومات follow-up
        String notificationMessage = "✅ تمت الموافقة على مطالبتك من المراجع الطبي " + reviewer.getFullName();
        if (claim.getIsFollowUp() != null && claim.getIsFollowUp()) {
            notificationMessage += " - ⚠️ زيارة متابعة (Follow-up): المريض يجب أن يدفع سعر الكشفية (" + 
                    (claim.getOriginalConsultationFee() != null ? claim.getOriginalConsultationFee() : "0") + 
                    " شيكل). التأمين لا يدفع الكشفية في زيارة المتابعة.";
        } else {
            notificationMessage += " - المبلغ: " + claim.getAmount() + " شيكل";
        }
        notificationMessage += " - الآن في انتظار مراجعة المنسق الإداري";
        
        String englishProviderMessage = "✅ Your claim has been approved by medical reviewer " + reviewer.getFullName();
        if (claim.getIsFollowUp() != null && claim.getIsFollowUp()) {
            englishProviderMessage += " - ⚠️ Follow-up visit: Patient must pay the consultation fee (" +
                    (claim.getOriginalConsultationFee() != null ? claim.getOriginalConsultationFee() : "0") +
                    " ILS). Insurance does not pay for follow-up consultation.";
        } else {
            englishProviderMessage += " - Amount: " + claim.getAmount() + " ILS";
        }
        englishProviderMessage += " - Now pending coordination admin review";

        try {
            String providerName = claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة";

            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        notificationMessage,
                        englishProviderMessage,
                        claim.getId(), "CLAIM"
                );
            }

            // 🔔 إشعار للمنسقين الإداريين
            clientRepo.findByRoles_Name(RoleName.COORDINATION_ADMIN)
                    .forEach(coordinator ->
                            notificationService.sendToUser(
                                    coordinator.getId(),
                                    "🔔 مطالبة جديدة في انتظار المراجعة الإدارية\n" +
                                            "من: " + (claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة") + "\n" +
                                            "المبلغ: " + claim.getAmount() + " شيكل" +
                                            (claim.getClientName() != null ? "\nللمريض: " + claim.getClientName() : ""),
                                    "🔔 New claim pending administrative review\n" +
                                            "From: " + (claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "Provider") + "\n" +
                                            "Amount: " + claim.getAmount() + " ILS" +
                                            (claim.getClientName() != null ? "\nFor patient: " + claim.getClientName() : ""),
                                    claim.getId(), "CLAIM"
                            )
                    );

            if (claim.getClientId() != null) {
                UUID approvePatientNotifyId = resolveNotificationRecipientId(claim.getClientId());
                if (approvePatientNotifyId != null) {
                    notificationService.sendToUser(
                            approvePatientNotifyId,
                            "✅ تمت الموافقة الطبية على مطالبتك من " + providerName +
                                    " - الآن في انتظار مراجعة المنسق الإداري",
                            "✅ Your medical claim from " + providerName +
                                    " has been medically approved - Now pending coordination admin review",
                            claim.getId(), "CLAIM"
                    );
                }
            }
        } catch (Exception e) {
            log.warn("Failed to send medical approval notifications for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Get claims pending medical review (includes legacy PENDING status)
    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimMedicalDTO> getClaimsForMedicalReview() {
        List<HealthcareProviderClaim> claims = claimRepo.findByStatusInWithProvider(
                List.of(ClaimStatus.PENDING_MEDICAL, ClaimStatus.PENDING, ClaimStatus.RETURNED_FOR_REVIEW)
                );

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);

            dto.setDiagnosis(claim.getDiagnosis());
            dto.setTreatmentDetails(cleanTreatmentDetails(claim.getTreatmentDetails()));
            populatePatientInfoForMedicalDTO(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            populateProviderInfo(claim, dto);
            dto.setDescription(claim.getDescription());
            dto.setRoleSpecificData(claim.getRoleSpecificData());
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            return dto;
        }).toList();
    }

    // Get final decisions (approved/rejected by medical admin and coordination admin)
    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimMedicalDTO> getFinalDecisions() {
        List<ClaimStatus> statuses = List.of(
                ClaimStatus.APPROVED_MEDICAL,
                ClaimStatus.AWAITING_COORDINATION_REVIEW,
                ClaimStatus.APPROVED_FINAL,
                ClaimStatus.REJECTED_FINAL,
                ClaimStatus.REJECTED_MEDICAL,
                ClaimStatus.PAYMENT_PENDING,
                ClaimStatus.PAID
        );

        List<HealthcareProviderClaim> claims = claimRepo.findByStatusInWithProvider(statuses);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimMedicalDTO dto = claimMapper.toMedicalDto(claim);
            dto.setAmount(claim.getAmount() != null ? claim.getAmount().doubleValue() : null);
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            if (claim.getStatus() == ClaimStatus.RETURNED_FOR_REVIEW) {
                dto.setReturnedByCoordinator(true);
                dto.setCoordinatorNote(claim.getRejectionReason());
            } else {
                dto.setReturnedByCoordinator(false);
                dto.setCoordinatorNote(null);
            }
            
            populatePatientInfoForMedicalDTO(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            populateProviderInfo(claim, dto);
            dto.setDescription(claim.getDescription());
            dto.setRoleSpecificData(claim.getRoleSpecificData());
            dto.setIsFollowUp(claim.getIsFollowUp());
            dto.setOriginalConsultationFee(claim.getOriginalConsultationFee());

            return dto;
        }).toList();
    }

    private void populateProviderInfo(HealthcareProviderClaim claim, HealthcareProviderClaimMedicalDTO dto) {
        Client provider = claim.getHealthcareProvider();
        if (provider != null) {
            String employeeId = provider.getEmployeeId();
            String nationalId = provider.getNationalId();
            
            dto.setProviderEmployeeId(employeeId);
            dto.setProviderNationalId(nationalId);
            
            String role = dto.getProviderRole();
            
            // For client claims (outside network), extract provider and doctor name from roleSpecificData
            if (role != null && role.equals("INSURANCE_CLIENT") && claim.getRoleSpecificData() != null) {
                try {
                    java.util.Map<String, Object> roleData = objectMapper.readValue(
                            claim.getRoleSpecificData(), 
                            java.util.Map.class
                    );
                    String providerName = (String) roleData.get("providerName");
                    String doctorName = (String) roleData.get("doctorName");
                    
                    if (providerName != null && !providerName.trim().isEmpty()) {
                        dto.setProviderName(providerName);
                    } else {
                        dto.setProviderName(provider.getFullName());
                    }
                    
                    // Set doctor name if available
                    if (doctorName != null && !doctorName.trim().isEmpty()) {
                        dto.setDoctorName(doctorName);
                    }
                } catch (Exception e) {
                    // If parsing fails, use provider's name
                    dto.setProviderName(provider.getFullName());
                }
            } else {
                // For regular providers, use their name
                dto.setProviderName(provider.getFullName());
            }
            
            if (role != null) {
                if (role.equals("DOCTOR")) {
                    dto.setProviderSpecialization(provider.getSpecialization());
                } else if (role.equals("PHARMACIST")) {
                    dto.setProviderPharmacyCode(provider.getPharmacyCode() != null && !provider.getPharmacyCode().trim().isEmpty() 
                            ? provider.getPharmacyCode() : null);
                } else if (role.equals("LAB_TECH")) {
                    dto.setProviderLabCode(provider.getLabCode() != null && !provider.getLabCode().trim().isEmpty() 
                            ? provider.getLabCode() : null);
                } else if (role.equals("RADIOLOGIST")) {
                    dto.setProviderRadiologyCode(provider.getRadiologyCode() != null && !provider.getRadiologyCode().trim().isEmpty() 
                            ? provider.getRadiologyCode() : null);
                }
            }
        }
    }

    private String cleanTreatmentDetails(String treatmentDetails) {
        if (treatmentDetails == null || treatmentDetails.isEmpty()) {
            return treatmentDetails;
        }
        
        String cleaned = treatmentDetails.replaceAll("(?i)(\\r?\\n)?\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*?(?=\\r?\\n|$|\\z)", "");
        cleaned = cleaned.replaceAll("(?i)(\\r?\\n)?\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*$", "");
        cleaned = cleaned.replaceAll("(?i)^\\s*Family\\s+Member:.*?-\\s*Insurance:.*?-\\s*Age:.*?-\\s*Gender:.*?(?=\\r?\\n|$)", "");
        cleaned = cleaned.replaceAll("\\r?\\n\\r?\\n+", "\n");
        cleaned = cleaned.replaceAll("\\s+", " ");
        cleaned = cleaned.trim();
        
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String saveDocument(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return baseUrl + "/uploads/healthcare-claims/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }
    // 📤 Export Approved Claims as PDF
    public byte[] exportApprovedClaimsPdf() {
        List<HealthcareProviderClaim> claims = claimRepo.findAllApprovedClaims();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Approved Claims Report", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated by Coordination Admin"));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            table.addCell("Claim ID");
            table.addCell("Patient Name");
            table.addCell("Provider Name");
            table.addCell("Medical Reviewer");
            table.addCell("Amount");
            table.addCell("Service Date");

            for (HealthcareProviderClaim claim : claims) {
                table.addCell(claim.getId().toString());
                table.addCell(claim.getClientName() != null ? claim.getClientName() : "-");
                table.addCell(claim.getHealthcareProvider().getFullName());
                table.addCell(claim.getMedicalReviewerName() != null ? claim.getMedicalReviewerName() : "-");
                table.addCell(claim.getAmount() + " NIS");
                table.addCell(claim.getServiceDate().toString());
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private byte[] generatePdf(ReportType reportType, List<HealthcareProviderClaim> claims) {
        boolean hideClientName = reportType != ReportType.CLIENT;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);

            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph(reportType.name() + " Claims Report", titleFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Generated by Coordination Admin"));
            document.add(new Paragraph(" "));

            int columns = hideClientName ? 5 : 6;
            PdfPTable table = new PdfPTable(columns);
            table.setWidthPercentage(100);

            table.addCell("Claim ID");
            table.addCell("Provider Name");

            if (!hideClientName) {
                table.addCell("Client Name");
            }

            table.addCell("Amount");
            table.addCell("Status");
            table.addCell("Service Date");

            for (HealthcareProviderClaim claim : claims) {
                table.addCell(claim.getId().toString());
                table.addCell(claim.getHealthcareProvider().getFullName());

                if (!hideClientName) {
                    table.addCell(claim.getClientName() != null ? claim.getClientName() : "-");
                }

                table.addCell(claim.getAmount() + " NIS");
                table.addCell(claim.getStatus().name());
                table.addCell(claim.getServiceDate() != null ? claim.getServiceDate().toString() : "-");
            }

            document.add(table);
            document.close();

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    public byte[] exportReportPdf(ReportType reportType, ClaimStatus status, LocalDate from, LocalDate to) {
        RoleName roleFilter = switch (reportType) {
            case DOCTOR -> RoleName.DOCTOR;
            case PHARMACY -> RoleName.PHARMACIST;
            case LAB -> RoleName.LAB_TECH;
            case RADIOLOGY -> RoleName.RADIOLOGIST;
            case CLIENT -> null; // clients handled separately
        };

        List<HealthcareProviderClaim> claims = claimRepo.filterClaims(status, from, to, roleFilter);

        return generatePdf(reportType, claims);
    }

    public byte[] exportReportExcel(ReportType reportType, ClaimStatus status, LocalDate from, LocalDate to) {
        RoleName roleFilter = switch (reportType) {
            case DOCTOR -> RoleName.DOCTOR;
            case PHARMACY -> RoleName.PHARMACIST;
            case LAB -> RoleName.LAB_TECH;
            case RADIOLOGY -> RoleName.RADIOLOGIST;
            case CLIENT -> null;
        };

        List<HealthcareProviderClaim> claims = claimRepo.filterClaims(status, from, to, roleFilter);
        return generateExcel(reportType, claims);
    }

    private byte[] generateExcel(ReportType reportType, List<HealthcareProviderClaim> claims) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(reportType.name() + " Claims Report");

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Claim ID", "Provider Name", "Provider Type", "Diagnosis", "Description",
                    "Total Amount", "Insurance Covered", "Client Pay", "Status", "Service Date", "Submitted Date"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (HealthcareProviderClaim claim : claims) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(claim.getId().toString());
                row.createCell(1).setCellValue(claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "-");
                row.createCell(2).setCellValue(claim.getHealthcareProvider() != null && claim.getHealthcareProvider().getRequestedRole() != null
                        ? claim.getHealthcareProvider().getRequestedRole().name() : "-");
                row.createCell(3).setCellValue(claim.getDiagnosis() != null ? claim.getDiagnosis() : "-");
                row.createCell(4).setCellValue(claim.getDescription() != null ? claim.getDescription() : "-");
                row.createCell(5).setCellValue(claim.getAmount() != null ? claim.getAmount().doubleValue() : 0);
                row.createCell(6).setCellValue(claim.getInsuranceCoveredAmount() != null ? claim.getInsuranceCoveredAmount().doubleValue() : 0);
                row.createCell(7).setCellValue(claim.getClientPayAmount() != null ? claim.getClientPayAmount().doubleValue() : 0);
                row.createCell(8).setCellValue(claim.getStatus().name());
                row.createCell(9).setCellValue(claim.getServiceDate() != null ? claim.getServiceDate().toString() : "-");
                row.createCell(10).setCellValue(claim.getSubmittedAt() != null ? claim.getSubmittedAt().toString() : "-");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }

    // Coordination admin creates claim on behalf of client
    @Transactional
    public HealthcareProviderClaimDTO createClaimByCoordinationAdmin(
            UUID adminId,
            CreateHealthcareProviderClaimDTO dto,
            MultipartFile invoiceImage
    ) {
        Client admin = clientRepo.findById(adminId)
                .orElseThrow(() -> new NotFoundException("Coordination admin not found"));

        boolean isAuthorized = admin.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.COORDINATION_ADMIN || r.getName() == RoleName.INSURANCE_MANAGER
                        || r.getName() == RoleName.MEDICAL_ADMIN || r.getName() == RoleName.DOCTOR);

        if (!isAuthorized) {
            throw new BadRequestException("Not authorized to create claims this way");
        }

        if (dto.getClientId() == null) {
            throw new BadRequestException("Client ID is required for coordination admin claim");
        }

        // First, try to find as a family member
        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(dto.getClientId());
        Client client;
        FamilyMember familyMember = null;
        String beneficiaryName;
        
        if (familyMemberOpt.isPresent()) {
            // It's a family member
            familyMember = familyMemberOpt.get();
            // Verify family member is approved
            if (familyMember.getStatus() != com.insurancesystem.Model.Entity.Enums.ProfileStatus.APPROVED) {
                throw new BadRequestException("Family member is not approved");
            }
            client = familyMember.getClient();
            beneficiaryName = familyMember.getFullName();
        } else {
            // It's a regular client
            client = clientRepo.findById(dto.getClientId())
                    .orElseThrow(() -> new NotFoundException("Client not found"));
            beneficiaryName = client.getFullName();
        }

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        // Set healthcare provider to the client (not admin) so it shows in client's claims
        claim.setHealthcareProvider(client);
        
        if (familyMember != null) {
            // Claim is for family member
            claim.setClientId(familyMember.getId());
            claim.setClientName(familyMember.getFullName());
        } else {
            // Claim is for client themselves
            claim.setClientId(client.getId());
            claim.setClientName(client.getFullName());
        }
        
        // Coordinator admin creates claim - requires medical review like all other claims
        claim.setStatus(ClaimStatus.PENDING_MEDICAL);

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        // Apply coverage rules before saving
        claim = claimEngineService.applyCoverageRules(claim);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // Notification message for client
        String clientNotificationMessage = familyMember != null
            ? "📋 تم إنشاء مطالبة من المنسق الإداري " + admin.getFullName() +
              " لعضو الأسرة " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية"
            : "📋 تم إنشاء مطالبة من المنسق الإداري " + admin.getFullName() +
              " للعميل " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - في انتظار المراجعة الطبية";

        String englishClientNotifMsg = familyMember != null
            ? "📋 A claim has been created by coordination admin " + admin.getFullName() +
              " for family member " + beneficiaryName +
              " - Amount: " + claim.getAmount() + " ILS" +
              " - Pending medical review"
            : "📋 A claim has been created by coordination admin " + admin.getFullName() +
              " for client " + beneficiaryName +
              " - Amount: " + claim.getAmount() + " ILS" +
              " - Pending medical review";

        // Notify the client
        notificationService.sendToUser(
                client.getId(),
                clientNotificationMessage,
                englishClientNotifMsg,
                savedClaim.getId(), "CLAIM"
        );

        // Notify medical admins about the new claim created by coordinator that needs review
        String medicalAdminNotificationMessage = familyMember != null
            ? "📋 مطالبة جديدة من المنسق الإداري " + admin.getFullName() +
              " لعضو الأسرة " + beneficiaryName + " (العميل: " + client.getFullName() + ")" +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تحتاج مراجعة طبية"
            : "📋 مطالبة جديدة من المنسق الإداري " + admin.getFullName() +
              " للعميل " + beneficiaryName +
              " - المبلغ: " + claim.getAmount() + " شيكل" +
              " - تحتاج مراجعة طبية";

        String englishMedicalAdminNotifMsg = familyMember != null
            ? "📋 New claim from coordination admin " + admin.getFullName() +
              " for family member " + beneficiaryName + " (Client: " + client.getFullName() + ")" +
              " - Amount: " + claim.getAmount() + " ILS" +
              " - Requires medical review"
            : "📋 New claim from coordination admin " + admin.getFullName() +
              " for client " + beneficiaryName +
              " - Amount: " + claim.getAmount() + " ILS" +
              " - Requires medical review";

        clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                .forEach(medicalAdmin -> notificationService.sendToUser(
                        medicalAdmin.getId(),
                        medicalAdminNotificationMessage,
                        englishMedicalAdminNotifMsg,
                        savedClaim.getId(), "CLAIM"
                ));

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);

        if (claim.getHealthcareProvider() != null) {
            resultDto.setProviderEmployeeId(claim.getHealthcareProvider().getEmployeeId());
            resultDto.setProviderNationalId(claim.getHealthcareProvider().getNationalId());
        }
        
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Coordinator admin approves claim (sets to final approval)
    @Transactional
    public HealthcareProviderClaimDTO approveAdmin(UUID claimId, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW) {
            throw new BadRequestException("Claim is not awaiting coordination review");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        // Fix #13: Store coordinator reviewer info
        claim.setCoordinationReviewerId(reviewerId);
        claim.setCoordinationReviewerName(reviewer.getFullName());
        claim.setCoordinationReviewedAt(Instant.now());

        claim.setStatus(ClaimStatus.APPROVED_FINAL);
        claim.setApprovedAt(Instant.now());

        // Fix #66: Track usage BEFORE save to make it atomic with approval
        if (claim.getClientId() != null) {
            try {
                java.math.BigDecimal usageAmount = claim.getInsuranceCoveredAmount() != null
                        ? claim.getInsuranceCoveredAmount()
                        : (claim.getAmount() != null ? claim.getAmount() : java.math.BigDecimal.ZERO);
                clientUsageService.incrementUsage(claim.getClientId(), null, usageAmount);
            } catch (Exception e) {
                log.warn("Failed to track usage for client {}: {}", claim.getClientId(), e.getMessage());
            }
        }

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 Notifications — non-blocking (should never prevent approval)
        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        "✅ تمت الموافقة النهائية على مطالبتك من المنسق الإداري " + reviewer.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " شيكل" +
                                (claim.getClientName() != null ? " للمريض " + claim.getClientName() : "") +
                                " - تمت الموافقة بنجاح!",
                        "✅ Your claim has been finally approved by coordination admin " + reviewer.getFullName() +
                                " - Amount: " + claim.getAmount() + " ILS" +
                                (claim.getClientName() != null ? " for patient " + claim.getClientName() : "") +
                                " - Approved successfully!",
                        claim.getId(), "CLAIM"
                );
            }

            UUID patientNotifyId = resolveNotificationRecipientId(claim.getClientId());
            if (patientNotifyId != null) {
                String providerName = claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة";
                notificationService.sendToUser(
                        patientNotifyId,
                        "✅ تمت الموافقة النهائية على مطالبتك الطبية من " + providerName +
                                " - تمت الموافقة بنجاح!",
                        "✅ Your medical claim from " + providerName +
                                " has been finally approved - Approved successfully!",
                        claim.getId(), "CLAIM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send approval notifications for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    // Coordinator admin rejects claim
    @Transactional
    public HealthcareProviderClaimDTO rejectAdmin(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW) {
            throw new BadRequestException("Claim is not awaiting coordination review");
        }

        Client reviewer = clientRepo.findById(reviewerId)
                .orElseThrow(() -> new NotFoundException("Reviewer not found"));

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);

        HealthcareProviderClaim savedClaim = claimRepo.save(claim);

        // 🔔 Notifications — non-blocking (should never prevent rejection)
        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        "❌ تم رفض مطالبتك من المنسق الإداري " + reviewer.getFullName() +
                                " - المبلغ: " + claim.getAmount() + " شيكل" +
                                (reason != null && !reason.isEmpty() ? "\nالسبب: " + reason : ""),
                        "❌ Your claim has been rejected by coordination admin " + reviewer.getFullName() +
                                " - Amount: " + claim.getAmount() + " ILS" +
                                (reason != null && !reason.isEmpty() ? "\nReason: " + reason : ""),
                        claim.getId(), "CLAIM"
                );
            }

            UUID rejectPatientNotifyId = resolveNotificationRecipientId(claim.getClientId());
            if (rejectPatientNotifyId != null) {
                String providerName = claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة";
                notificationService.sendToUser(
                        rejectPatientNotifyId,
                        "❌ تم رفض مطالبتك الطبية من " + providerName +
                                " - السبب: " + (reason != null && !reason.isEmpty() ? reason : "غير محدد"),
                        "❌ Your medical claim from " + providerName +
                                " has been rejected - Reason: " + (reason != null && !reason.isEmpty() ? reason : "Not specified"),
                        claim.getId(), "CLAIM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send rejection notifications for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(savedClaim);
        populatePatientInfo(savedClaim, resultDto);
        return resultDto;
    }

    @Transactional
    public HealthcareProviderClaimDTO returnToMedical(UUID claimId, String reason, UUID coordinatorId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        // PAID status is final - cannot be changed
        if (claim.getStatus() == ClaimStatus.PAID) {
            throw new BadRequestException("Paid claims cannot be modified - payment is final");
        }

        // Allow returning claims that are awaiting coordination review OR already approved
        if (claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW &&
            claim.getStatus() != ClaimStatus.APPROVED_FINAL) {
            throw new BadRequestException("Only claims awaiting coordination review or approved claims can be returned for review");
        }

        boolean wasApproved = claim.getStatus() == ClaimStatus.APPROVED_FINAL;

        // Fix #12: Reverse usage tracking when approved claim is returned
        if (wasApproved && claim.getClientId() != null) {
            try {
                java.math.BigDecimal usageAmount = claim.getInsuranceCoveredAmount() != null
                        ? claim.getInsuranceCoveredAmount()
                        : (claim.getAmount() != null ? claim.getAmount() : java.math.BigDecimal.ZERO);
                clientUsageService.decrementUsage(claim.getClientId(), null, usageAmount);
            } catch (Exception e) {
                log.warn("Failed to reverse usage for client {}: {}", claim.getClientId(), e.getMessage());
            }
        }

        claim.setStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        claim.setRejectionReason(reason);
        claim.setRejectedAt(Instant.now());
        // Clear approval timestamp since it's being returned
        if (wasApproved) {
            claim.setApprovedAt(null);
        }

        HealthcareProviderClaim saved = claimRepo.save(claim);

        String medicalAdminMessage = wasApproved
            ? "🚨 مراجعة طبية عاجلة\n" +
              "تم إرجاع مطالبة موافق عليها مسبقاً للمراجعة.\n\n" +
              "📝 ملاحظة:\n" + reason
            : "🚨 مراجعة طبية عاجلة\n" +
              "تم إرجاع مطالبة من المنسق الإداري.\n\n" +
              "📝 ملاحظة:\n" + reason;

        String englishMedicalAdminMessage = wasApproved
            ? "🚨 Urgent medical review\n" +
              "A previously approved claim has been returned for review.\n\n" +
              "📝 Note:\n" + reason
            : "🚨 Urgent medical review\n" +
              "A claim has been returned by the coordination admin.\n\n" +
              "📝 Note:\n" + reason;

        try {
            clientRepo.findByRoles_Name(RoleName.MEDICAL_ADMIN)
                    .forEach(admin ->
                            notificationService.sendToUser(
                                    admin.getId(),
                                    medicalAdminMessage,
                                    englishMedicalAdminMessage,
                                    claim.getId(), "CLAIM"
                            )
                    );
        } catch (Exception e) {
            log.warn("Failed to send return-to-medical notifications to medical admins for claim {}: {}", claimId, e.getMessage());
        }

        String providerMessage = wasApproved
            ? "⚠️ تم إرجاع مطالبتك الموافق عليها للمراجعة الطبية مرة أخرى:\n" + reason
            : "⚠️ تمت إعادة مطالبتك للمراجعة الطبية بسبب ملاحظة إدارية:\n" + reason;

        String englishProviderMessage = wasApproved
            ? "⚠️ Your approved claim has been returned for medical review again:\n" + reason
            : "⚠️ Your claim has been returned for medical review due to an administrative note:\n" + reason;

        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        providerMessage,
                        englishProviderMessage,
                        claim.getId(), "CLAIM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send return-to-medical notification for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(saved);
        populatePatientInfo(saved, resultDto);
        return resultDto;
    }

    // Get claims approved by medical admin for coordination review
    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReview() {
        List<HealthcareProviderClaim> claims = claimRepo.findByStatusWithProvider(ClaimStatus.AWAITING_COORDINATION_REVIEW);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);

            populatePatientInfo(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            

            dto.setInvoiceImagePath(claim.getInvoiceImagePath());

            // ✅ تحديد دور مقدم الخدمة
            String role;
            if (claim.getClientId() != null &&
                    claim.getHealthcareProvider().getId().equals(claim.getClientId())) {

                // Self-service client claim
                role = RoleName.INSURANCE_CLIENT.name();

            } else {
                // Provider claim
                role = claim.getHealthcareProvider()
                        .getRoles()
                        .stream()
                        .findFirst()
                        .map(r -> r.getName().name())
                        .orElse("UNKNOWN");
            }

            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId())
                        .ifPresent(c -> dto.setEmployeeId(c.getEmployeeId()));
            }

            return dto;
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReviewPaginated(int page, int size) {
        // Two-step query to avoid Hibernate in-memory pagination with JOIN FETCH
        // Step 1: Get IDs with DB-level pagination (lightweight query)
        List<UUID> ids = claimRepo.findIdsByStatus(
                ClaimStatus.AWAITING_COORDINATION_REVIEW,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "submittedAt")));

        if (ids.isEmpty()) {
            return List.of();
        }

        // Step 2: Fetch full entities with JOIN FETCH using the IDs
        List<HealthcareProviderClaim> claims = claimRepo.findByIdsWithProvider(ids);

        return claims.stream().map(claim -> {
            HealthcareProviderClaimDTO dto = claimMapper.toDto(claim);
            populatePatientInfo(claim, dto);
            dto.setProviderRole(getProviderRole(claim));
            dto.setInvoiceImagePath(claim.getInvoiceImagePath());
            if (claim.getClientId() != null) {
                clientRepo.findById(claim.getClientId())
                        .ifPresent(c -> dto.setEmployeeId(c.getEmployeeId()));
            }
            return dto;
        }).toList();
    }

    // Return claim to provider for corrections
    @org.springframework.transaction.annotation.Transactional
    public HealthcareProviderClaimDTO returnToProvider(UUID claimId, String reason, UUID reviewerId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        // Fix #61: PAID status is final - cannot be modified
        if (claim.getStatus() == ClaimStatus.PAID) {
            throw new BadRequestException("Paid claims cannot be modified - payment is final");
        }

        // Validate status allows return
        if (claim.getStatus() != ClaimStatus.PENDING_MEDICAL &&
            claim.getStatus() != ClaimStatus.AWAITING_COORDINATION_REVIEW &&
            claim.getStatus() != ClaimStatus.RETURNED_FOR_REVIEW) {
            throw new IllegalStateException("Cannot return claim in status: " + claim.getStatus());
        }

        claim.setStatus(ClaimStatus.RETURNED_TO_PROVIDER);
        claim.setRejectionReason(reason);

        HealthcareProviderClaim saved = claimRepo.save(claim);

        // Notify the provider about the returned claim
        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        "⚠️ تمت إعادة مطالبتك للتصحيح:\n" + reason +
                                "\nيرجى مراجعة المطالبة وإجراء التصحيحات اللازمة",
                        "⚠️ Your claim has been returned for correction:\n" + reason +
                                "\nPlease review the claim and make the necessary corrections",
                        claim.getId(), "CLAIM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send return-to-provider notification for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(saved);
        populatePatientInfo(saved, resultDto);
        return resultDto;
    }

    // Mark claim as paid
    @Transactional
    public HealthcareProviderClaimDTO markAsPaid(UUID claimId, UUID adminId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.PAYMENT_PENDING &&
            claim.getStatus() != ClaimStatus.APPROVED_FINAL) {
            throw new IllegalStateException("Claim must be in PAYMENT_PENDING or APPROVED_FINAL status to mark as paid");
        }

        claim.setStatus(ClaimStatus.PAID);
        claim.setPaidAt(Instant.now());
        claim.setPaidBy(adminId);

        HealthcareProviderClaim saved = claimRepo.save(claim);

        // Notify provider and patient about the payment
        try {
            if (claim.getHealthcareProvider() != null) {
                notificationService.sendToUser(
                        claim.getHealthcareProvider().getId(),
                        "💰 تم دفع مطالبتك بنجاح - المبلغ: " + claim.getInsuranceCoveredAmount() + " شيكل" +
                                (claim.getClientName() != null ? " للمريض " + claim.getClientName() : ""),
                        "💰 Your claim has been paid successfully - Amount: " + claim.getInsuranceCoveredAmount() + " ILS" +
                                (claim.getClientName() != null ? " for patient " + claim.getClientName() : ""),
                        claim.getId(), "CLAIM"
                );
            }

            UUID paidPatientNotifyId = resolveNotificationRecipientId(claim.getClientId());
            if (paidPatientNotifyId != null) {
                String providerName = claim.getHealthcareProvider() != null ? claim.getHealthcareProvider().getFullName() : "مقدم الخدمة";
                notificationService.sendToUser(
                        paidPatientNotifyId,
                        "💰 تم دفع مطالبتك الطبية من " + providerName,
                        "💰 Your medical claim from " + providerName + " has been paid",
                        claim.getId(), "CLAIM"
                );
            }
        } catch (Exception e) {
            log.warn("Failed to send payment notifications for claim {}: {}", claimId, e.getMessage());
        }

        HealthcareProviderClaimDTO resultDto = claimMapper.toDto(saved);
        populatePatientInfo(saved, resultDto);
        return resultDto;
    }
}
