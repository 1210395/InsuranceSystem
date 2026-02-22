package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.PoliciesReportDto;
import com.insurancesystem.Model.Entity.Enums.MemberStatus;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.GlobalPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PoliciesReportService {

    private final ClientRepository clientRepo;
    private final GlobalPolicyRepository globalPolicyRepo;

    public PoliciesReportDto generateReport() {
        long totalMembers = clientRepo.count();
        long activeMembers = clientRepo.countByStatus(MemberStatus.ACTIVE);
        long inactiveMembers = clientRepo.countByStatus(MemberStatus.INACTIVE);

        // Count active global policies (should be 1 in the new system)
        long activePolicies = globalPolicyRepo.countByStatus(GlobalPolicyStatus.ACTIVE);

        // With GlobalPolicy system, all active clients share the same policy
        Map<String, Long> membersPerPolicy = new HashMap<>();
        globalPolicyRepo.findActivePolicy().ifPresent(policy ->
            membersPerPolicy.put(policy.getName(), activeMembers)
        );

        return PoliciesReportDto.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .inactiveMembers(inactiveMembers)
                .activePolicies(activePolicies)
                .membersPerPolicy(membersPerPolicy)
                .build();
    }
}
