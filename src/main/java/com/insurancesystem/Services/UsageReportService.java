package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.UsageReportDto;
import com.insurancesystem.Model.Entity.Enums.*;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsageReportService {

    private final HealthcareProviderClaimRepository claimRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRepo;
    private final EmergencyRequestRepository emergencyRepo;
    private final MedicalRecordRepository medicalRecordRepository;

    public UsageReportDto generateReport() {
        return UsageReportDto.builder()
                // Claims - count ALL workflow statuses in each category
                .totalClaims(claimRepo.count())
                .approvedClaims(
                        claimRepo.countByStatus(ClaimStatus.APPROVED_FINAL)
                                + claimRepo.countByStatus(ClaimStatus.PAYMENT_PENDING)
                                + claimRepo.countByStatus(ClaimStatus.PAID)
                )
                .rejectedClaims(
                        claimRepo.countByStatus(ClaimStatus.REJECTED_FINAL)
                                + claimRepo.countByStatus(ClaimStatus.REJECTED_MEDICAL)
                )
                .pendingClaims(
                        claimRepo.countByStatus(ClaimStatus.PENDING_MEDICAL)
                                + claimRepo.countByStatus(ClaimStatus.RETURNED_FOR_REVIEW)
                                + claimRepo.countByStatus(ClaimStatus.AWAITING_COORDINATION_REVIEW)
                                + claimRepo.countByStatus(ClaimStatus.APPROVED_MEDICAL)
                                + claimRepo.countByStatus(ClaimStatus.PENDING_COORDINATION)
                                + claimRepo.countByStatus(ClaimStatus.RETURNED_TO_PROVIDER)
                )

                // Prescriptions - use countByStatus instead of loading entities
                .totalPrescriptions(prescriptionRepo.count())
                .verifiedPrescriptions(prescriptionRepo.countByStatus(PrescriptionStatus.VERIFIED))
                .rejectedPrescriptions(prescriptionRepo.countByStatus(PrescriptionStatus.REJECTED))
                .pendingPrescriptions(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))

                // Lab Requests - use countByStatus instead of loading entities
                .totalLabRequests(labRepo.count())
                .completedLabRequests(labRepo.countByStatus(LabRequestStatus.COMPLETED))
                .pendingLabRequests(labRepo.countByStatus(LabRequestStatus.PENDING))

                // Emergency Requests - use countByStatus instead of loading entities
                .totalEmergencyRequests(emergencyRepo.count())
                .approvedEmergencyRequests(
                        emergencyRepo.countByStatus(EmergencyStatus.APPROVED_BY_MEDICAL)
                                + emergencyRepo.countByStatus(EmergencyStatus.APPROVED)
                )
                .rejectedEmergencyRequests(
                        emergencyRepo.countByStatus(EmergencyStatus.REJECTED_BY_MEDICAL)
                                + emergencyRepo.countByStatus(EmergencyStatus.REJECTED)
                )
                .pendingEmergencyRequests(
                        emergencyRepo.countByStatus(EmergencyStatus.PENDING_MEDICAL)
                                + emergencyRepo.countByStatus(EmergencyStatus.PENDING)
                )

                // Medical Records
                .totalMedicalRecords(medicalRecordRepository.count())

                .build();
    }
}
