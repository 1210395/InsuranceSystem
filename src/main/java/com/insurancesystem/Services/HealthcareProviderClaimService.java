package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.CreateHealthcareProviderClaimDTO;
import com.insurancesystem.Model.Dto.RejectClaimDTO;
import com.insurancesystem.Model.Entity.HealthcareProviderClaim;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.MapStruct.HealthcareProviderClaimMapper;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HealthcareProviderClaimService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final HealthcareProviderClaimMapper claimMapper;
    private final NotificationService notificationService;
    private final String UPLOAD_DIR = "uploads/healthcare-claims/";

    // ✅ إنشاء مطالبة
    public HealthcareProviderClaimDTO createClaim(UUID providerId, CreateHealthcareProviderClaimDTO dto, MultipartFile invoiceImage) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        HealthcareProviderClaim claim = claimMapper.toEntity(dto);
        claim.setHealthcareProvider(provider);
        claim.setStatus(ClaimStatus.PENDING);

        // ✅ حفظ معرف واسم المريض
        if (claim.getClientId() != null) {
            Client client = clientRepo.findById(claim.getClientId()).orElse(null);
            if (client != null) {
                claim.setClientName(client.getFullName());
            }
        }

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveDocument(invoiceImage));
        }

        claimRepo.save(claim);

        // إشعار المدير
        notificationService.sendToRole(
                RoleName.INSURANCE_MANAGER,
                "مطالبة جديدة من " + provider.getFullName() +
                        " بمبلغ " + dto.getAmount()
        );

        return claimMapper.toDto(claim);
    }

    public List<HealthcareProviderClaimDTO> getProviderClaims(UUID providerId) {
        Client provider = clientRepo.findById(providerId)
                .orElseThrow(() -> new NotFoundException("Healthcare provider not found"));

        List<HealthcareProviderClaim> claims = claimRepo.findByHealthcareProvider(provider);

        // ✅ ملء clientName من clientId إذا كان فارغ
        for (HealthcareProviderClaim claim : claims) {
            if ((claim.getClientName() == null || claim.getClientName().trim().isEmpty()) && claim.getClientId() != null) {
                Client client = clientRepo.findById(claim.getClientId()).orElse(null);
                if (client != null) {
                    claim.setClientName(client.getFullName());
                    claimRepo.save(claim);
                }
            }
        }

        return claims.stream()
                .map(claimMapper::toDto)
                .toList();
    }

    public List<HealthcareProviderClaimDTO> getAllClaims() {
        List<HealthcareProviderClaim> claims = claimRepo.findAll();

        // ✅ ملء clientName من clientId إذا كان فارغ
        for (HealthcareProviderClaim claim : claims) {
            if ((claim.getClientName() == null || claim.getClientName().trim().isEmpty()) && claim.getClientId() != null) {
                Client client = clientRepo.findById(claim.getClientId()).orElse(null);
                if (client != null) {
                    claim.setClientName(client.getFullName());
                    claimRepo.save(claim);
                }
            }
        }

        return claims.stream()
                .map(claimMapper::toDto)
                .toList();
    }

    public HealthcareProviderClaimDTO getClaim(UUID claimId, UUID requesterId, boolean isManager) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (!isManager && !claim.getHealthcareProvider().getId().equals(requesterId)) {
            throw new NotFoundException("Claim not found for this provider");
        }

        return claimMapper.toDto(claim);
    }

    // موافقة على مطالبة
    public HealthcareProviderClaimDTO approveClaim(UUID claimId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED);
        claim.setApprovedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "تمت الموافقة على مطالبتك بمبلغ " + claim.getAmount()
        );

        return claimMapper.toDto(claim);
    }

    // رفض مطالبة
    public HealthcareProviderClaimDTO rejectClaim(UUID claimId, RejectClaimDTO dto) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(dto.getReason());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "تم رفض مطالبتك. السبب: " + dto.getReason()
        );

        return claimMapper.toDto(claim);
    }

    private String saveDocument(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return "http://localhost:8080/uploads/healthcare-claims/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save document", e);
        }
    }

    // Get claims for medical review (PENDING or RETURNED_FOR_REVIEW)
    public List<HealthcareProviderClaimDTO> getClaimsForMedicalReview() {
        List<ClaimStatus> statuses = List.of(
                ClaimStatus.PENDING,
                ClaimStatus.PENDING_MEDICAL,
                ClaimStatus.RETURNED_FOR_REVIEW
        );
        return claimRepo.findByStatusIn(statuses).stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Get claims for coordination review
    public List<HealthcareProviderClaimDTO> getClaimsForCoordinationReview() {
        return claimRepo.findClaimsForCoordinationReview().stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Get final decisions
    public List<HealthcareProviderClaimDTO> getFinalDecisions() {
        return claimRepo.findFinalDecisions().stream()
                .map(claimMapper::toDto)
                .toList();
    }

    // Medical approve claim
    public HealthcareProviderClaimDTO approveMedical(UUID claimId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED_MEDICAL);
        claim.setMedicalReviewedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.COORDINATION_ADMIN,
                "Medical approved claim from " + claim.getHealthcareProvider().getFullName()
        );

        return claimMapper.toDto(claim);
    }

    // Medical reject claim
    public HealthcareProviderClaimDTO rejectMedical(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED_MEDICAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim was rejected by medical review. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }

    // Final approve claim (coordination admin)
    public HealthcareProviderClaimDTO approveFinal(UUID claimId) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.APPROVED_FINAL);
        claim.setApprovedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim of " + claim.getAmount() + " has been fully approved!"
        );

        return claimMapper.toDto(claim);
    }

    // Final reject claim (coordination admin)
    public HealthcareProviderClaimDTO rejectFinal(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.REJECTED_FINAL);
        claim.setRejectedAt(Instant.now());
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getHealthcareProvider().getId(),
                "Your claim was rejected. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }

    // Return to medical for review
    public HealthcareProviderClaimDTO returnToMedical(UUID claimId, String reason) {
        HealthcareProviderClaim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        claim.setStatus(ClaimStatus.RETURNED_FOR_REVIEW);
        claim.setRejectionReason(reason);
        claimRepo.save(claim);

        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "Claim returned for medical review. Reason: " + reason
        );

        return claimMapper.toDto(claim);
    }
}

