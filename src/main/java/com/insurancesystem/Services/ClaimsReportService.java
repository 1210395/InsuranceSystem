package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.ClaimsReportDto;
import com.insurancesystem.Model.Dto.HealthcareProviderClaimDTO;
import com.insurancesystem.Model.Entity.Enums.ClaimStatus;
import com.insurancesystem.Model.MapStruct.HealthcareProviderClaimMapper;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimsReportService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final HealthcareProviderClaimMapper claimMapper;

    public ClaimsReportDto generateReport() {
        long totalClaims = claimRepo.count();

        long approvedClaims =
                claimRepo.countByStatus(ClaimStatus.APPROVED_FINAL)
                + claimRepo.countByStatus(ClaimStatus.PAYMENT_PENDING)
                + claimRepo.countByStatus(ClaimStatus.PAID);

        long rejectedClaims =
                claimRepo.countByStatus(ClaimStatus.REJECTED_FINAL)
                + claimRepo.countByStatus(ClaimStatus.REJECTED_MEDICAL);

        long pendingClaims =
                claimRepo.countByStatus(ClaimStatus.PENDING_MEDICAL)
                + claimRepo.countByStatus(ClaimStatus.RETURNED_FOR_REVIEW)
                + claimRepo.countByStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                + claimRepo.countByStatus(ClaimStatus.APPROVED_MEDICAL)
                + claimRepo.countByStatus(ClaimStatus.PENDING_COORDINATION)
                + claimRepo.countByStatus(ClaimStatus.RETURNED_TO_PROVIDER);

        List<HealthcareProviderClaimDTO> approvedList =
                claimRepo.findByStatusInWithProvider(
                        List.of(ClaimStatus.APPROVED_FINAL, ClaimStatus.PAYMENT_PENDING, ClaimStatus.PAID)
                ).stream().map(claimMapper::toDto).toList();

        List<HealthcareProviderClaimDTO> rejectedList =
                claimRepo.findByStatusInWithProvider(
                        List.of(ClaimStatus.REJECTED_FINAL, ClaimStatus.REJECTED_MEDICAL)
                ).stream().map(claimMapper::toDto).toList();

        List<HealthcareProviderClaimDTO> pendingList =
                claimRepo.findByStatusInWithProvider(
                        List.of(
                                ClaimStatus.PENDING_MEDICAL,
                                ClaimStatus.RETURNED_FOR_REVIEW,
                                ClaimStatus.AWAITING_COORDINATION_REVIEW,
                                ClaimStatus.APPROVED_MEDICAL,
                                ClaimStatus.PENDING_COORDINATION,
                                ClaimStatus.RETURNED_TO_PROVIDER
                        )
                ).stream().map(claimMapper::toDto).toList();

        double totalApprovedAmount = approvedList.stream()
                .mapToDouble(c -> c.getInsuranceCoveredAmount() != null ? c.getInsuranceCoveredAmount().doubleValue() : 0)
                .sum();

        double totalRejectedAmount = rejectedList.stream()
                .mapToDouble(c -> c.getAmount() != null ? c.getAmount() : 0)
                .sum();

        return ClaimsReportDto.builder()
                .totalClaims(totalClaims)
                .approvedClaims(approvedClaims)
                .rejectedClaims(rejectedClaims)
                .pendingClaims(pendingClaims)
                .totalApprovedAmount(totalApprovedAmount)
                .totalRejectedAmount(totalRejectedAmount)
                .approvedList(approvedList)
                .rejectedList(rejectedList)
                .pendingList(pendingList)
                .build();
    }}
