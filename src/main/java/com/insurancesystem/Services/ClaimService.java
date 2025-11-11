package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.*;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Model.MapStruct.ClaimMapper;
import com.insurancesystem.Repository.*;
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
public class ClaimService {

    private final ClaimRepository claimRepo;
    private final ClientRepository clientRepo;
    private final PolicyRepository policyRepo;
    private final ClaimMapper claimMapper;
    private final NotificationService notificationService;

    private final String UPLOAD_DIR = "uploads/invoices/";

    // 🟢 إنشاء مطالبة جديدة
    public ClaimDTO createClaim(UUID memberId, CreateClaimDTO dto, MultipartFile invoiceImage) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        Policy policy = dto.getPolicyId() != null
                ? policyRepo.findById(dto.getPolicyId()).orElseThrow(() -> new NotFoundException("Policy not found"))
                : policyRepo.findByName(dto.getPolicyName()).orElseThrow(() -> new NotFoundException("Policy not found"));

        Claim claim = claimMapper.toEntity(dto);
        claim.setMember(member);
        claim.setPolicy(policy);
        claim.setStatus(ClaimStatus.PENDING);

        if (invoiceImage != null && !invoiceImage.isEmpty()) {
            claim.setInvoiceImagePath(saveInvoice(invoiceImage));
        }

        claimRepo.save(claim);

        // إشعار المراجع الطبي
        notificationService.sendToRole(
                RoleName.MEDICAL_ADMIN,
                "مطالبة جديدة بانتظار المراجعة الطبية من " + member.getFullName()
        );

        return claimMapper.toDto(claim);
    }

    // 🔹 استعلامات عامة
    public List<ClaimDTO> getMemberClaims(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));
        return claimRepo.findByMember(member).stream().map(claimMapper::toDto).toList();
    }

    public List<ClaimDTO> getAllClaims() {
        return claimRepo.findAll().stream().map(claimMapper::toDto).toList();
    }

    // 🔹 عرض مطالبة واحدة
    public ClaimDTO getClaim(UUID claimId, UUID requesterId, boolean isManager) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));
        if (!isManager && !claim.getMember().getId().equals(requesterId))
            throw new NotFoundException("Claim not found for this member");
        return claimMapper.toDto(claim);
    }

    // ✅ موافقة
    public ClaimDTO approveClaim(UUID claimId, RoleName role, UUID reviewerId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (role == RoleName.MEDICAL_ADMIN) {
            claim.setStatus(ClaimStatus.AWAITING_ADMIN_REVIEW);
            claim.setMedicalReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setMedicalReviewedAt(Instant.now());

            notificationService.sendToRole(
                    RoleName.INSURANCE_MANAGER,
                    "مطالبة جاهزة للمراجعة الإدارية من " + claim.getDoctorName()
            );

        } else if (role == RoleName.INSURANCE_MANAGER) {
            if (claim.getStatus() != ClaimStatus.AWAITING_ADMIN_REVIEW &&
                    claim.getStatus() != ClaimStatus.APPROVED_BY_MEDICAL)
                throw new NotFoundException("Medical review not completed yet");

            claim.setStatus(ClaimStatus.APPROVED);
            claim.setAdminReviewer(clientRepo.findById(reviewerId).orElse(null));
            claim.setAdminReviewedAt(Instant.now());
            claim.setApprovedAt(Instant.now());

            notificationService.sendToUser(
                    claim.getMember().getId(),
                    "تمت الموافقة النهائية على مطالبتك بمبلغ " + claim.getAmount()
            );
        }

        claimRepo.save(claim);
        return claimMapper.toDto(claim);
    }

    // ❌ رفض
    public ClaimDTO rejectClaim(UUID claimId, RejectClaimDTO dto, RoleName role, UUID reviewerId) {
        Claim claim = claimRepo.findById(claimId)
                .orElseThrow(() -> new NotFoundException("Claim not found"));

        if (role == RoleName.MEDICAL_ADMIN) {
            claim.setStatus(ClaimStatus.REJECTED_BY_MEDICAL);
            claim.setMedicalReviewer(clientRepo.findById(reviewerId).orElse(null));
        } else {
            claim.setStatus(ClaimStatus.REJECTED);
            claim.setAdminReviewer(clientRepo.findById(reviewerId).orElse(null));
        }

        claim.setRejectionReason(dto.getReason());
        claim.setRejectedAt(Instant.now());
        claimRepo.save(claim);

        notificationService.sendToUser(
                claim.getMember().getId(),
                "تم رفض مطالبتك. السبب: " + dto.getReason()
        );

        return claimMapper.toDto(claim);
    }

    // 🧾 حفظ الفاتورة
    private String saveInvoice(MultipartFile file) {
        try {
            Files.createDirectories(Path.of(UPLOAD_DIR));
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path path = Path.of(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());
            return "http://localhost:8080/uploads/invoices/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save invoice image", e);
        }
    }

    // 🔹 للمراجع الطبي
    public List<ClaimDTO> getClaimsForMedicalReview() {
        return claimRepo.findPendingMedicalClaims().stream().map(claimMapper::toDto).toList();
    }

    // 🔹 للإداري
    public List<ClaimDTO> getClaimsForAdminReview() {
        return claimRepo.findPendingAdminClaims().stream().map(claimMapper::toDto).toList();
    }
}
