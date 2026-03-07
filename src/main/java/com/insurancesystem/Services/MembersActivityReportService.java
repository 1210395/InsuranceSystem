package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.MembersActivityReportDto;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MembersActivityReportService {

    private final ClientRepository clientRepo;
    private final HealthcareProviderClaimRepository claimRepo;
    private final PrescriptionRepository prescriptionRepo;
    private final LabRequestRepository labRepo;
    private final EmergencyRequestRepository emergencyRepo;
    private final MedicalRecordRepository medicalRecordRepository;

    public MembersActivityReportDto generateReport() {
        long totalMembers = clientRepo.count();

        // Count distinct clients/members (not entity IDs)
        long membersWithClaims = claimRepo.countDistinctClients();

        long membersWithPrescriptions = prescriptionRepo.countDistinctMembers();

        long membersWithLabRequests = labRepo.countDistinctMembers();

        long membersWithEmergencyRequests = emergencyRepo.countDistinctMembers();

        long membersWithMedicalRecords = medicalRecordRepository.countDistinctMembers();

        return MembersActivityReportDto.builder()
                .totalMembers(totalMembers)
                .membersWithClaims(membersWithClaims)
                .membersWithPrescriptions(membersWithPrescriptions)
                .membersWithLabRequests(membersWithLabRequests)
                .membersWithEmergencyRequests(membersWithEmergencyRequests)
                .membersWithMedicalRecords(membersWithMedicalRecords)
                .build();
    }
}
