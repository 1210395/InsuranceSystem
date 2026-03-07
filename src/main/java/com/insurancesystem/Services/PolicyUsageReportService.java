package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import com.insurancesystem.Model.Entity.GlobalPolicy;
import com.insurancesystem.Repository.GlobalPolicyRepository;
import com.insurancesystem.Repository.HealthcareProviderClaimRepository;
import com.insurancesystem.Repository.ServiceCoverageRepository;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyUsageReportService {

    private final GlobalPolicyRepository policyRepository;
    private final HealthcareProviderClaimRepository claimRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;

    @Data
    @Builder
    public static class PolicyUsageSummary {
        private String policyName;
        private String policyVersion;
        private LocalDate reportPeriodStart;
        private LocalDate reportPeriodEnd;

        // Overall Statistics
        private int totalClaims;
        private int approvedClaims;
        private int rejectedClaims;
        private BigDecimal totalClaimAmount;
        private BigDecimal totalInsurancePaid;
        private BigDecimal totalClientPaid;
        private BigDecimal approvalRate;

        // Service Coverage Stats
        private int totalServicesInPolicy;
        private int servicesUsed;
        private List<ServiceUsageStat> topServices;

        // Client Stats
        private int uniqueClients;
        private List<ClientUsageStat> topClients;

        // Trend Data
        private List<MonthlyTrend> monthlyTrends;
    }

    @Data
    @Builder
    public static class ServiceUsageStat {
        private String serviceName;
        private int usageCount;
        private BigDecimal totalAmount;
        private BigDecimal insurancePaid;
    }

    @Data
    @Builder
    public static class ClientUsageStat {
        private UUID clientId;
        private String clientName;
        private int claimCount;
        private BigDecimal totalSpent;
    }

    @Data
    @Builder
    public static class MonthlyTrend {
        private int year;
        private int month;
        private String monthName;
        private int claimCount;
        private BigDecimal totalAmount;
        private BigDecimal insurancePaid;
    }

    @Transactional(readOnly = true)
    public PolicyUsageSummary generateReport(LocalDate fromDate, LocalDate toDate) {
        // Get active policy
        Optional<GlobalPolicy> policyOpt = policyRepository.findByStatus(GlobalPolicyStatus.ACTIVE).stream().findFirst();
        if (policyOpt.isEmpty()) {
            return PolicyUsageSummary.builder()
                    .policyName("No Active Policy")
                    .totalClaims(0)
                    .build();
        }
        GlobalPolicy policy = policyOpt.get();

        // Get claim statistics
        List<Object[]> claimStats = claimRepository.getClientUsageSummary(fromDate, toDate);
        List<Object[]> serviceStats = claimRepository.getServiceUsageBreakdown(fromDate, toDate);

        // Calculate totals from query: [clientId, clientName, count, insurancePaid, totalAmount, clientPaid]
        int totalClaims = 0;
        BigDecimal totalClaimAmount = BigDecimal.ZERO;
        BigDecimal totalInsurancePaid = BigDecimal.ZERO;
        BigDecimal totalClientPaid = BigDecimal.ZERO;
        Set<UUID> uniqueClientIds = new HashSet<>();

        for (Object[] stat : claimStats) {
            UUID clientId = (UUID) stat[0];
            Long count = (Long) stat[2];
            BigDecimal insurancePaid = (BigDecimal) stat[3];
            BigDecimal claimAmount = (BigDecimal) stat[4];
            BigDecimal clientPaid = (BigDecimal) stat[5];

            uniqueClientIds.add(clientId);
            totalClaims += count.intValue();
            totalInsurancePaid = totalInsurancePaid.add(insurancePaid);
            totalClaimAmount = totalClaimAmount.add(claimAmount);
            totalClientPaid = totalClientPaid.add(clientPaid);
        }

        // Count rejected claims in the same period
        long rejectedCount = claimRepository.countByStatus(com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_FINAL)
                + claimRepository.countByStatus(com.insurancesystem.Model.Entity.Enums.ClaimStatus.REJECTED_MEDICAL);

        // Calculate approval rate
        BigDecimal approvalRate = BigDecimal.ZERO;
        long totalForRate = totalClaims + rejectedCount;
        if (totalForRate > 0) {
            approvalRate = BigDecimal.valueOf(totalClaims)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalForRate), 2, RoundingMode.HALF_UP);
        }

        // Build top services list: [description, count, insurancePaid, totalAmount]
        List<ServiceUsageStat> topServices = serviceStats.stream()
                .limit(10)
                .map(stat -> ServiceUsageStat.builder()
                        .serviceName((String) stat[0])
                        .usageCount(((Long) stat[1]).intValue())
                        .insurancePaid((BigDecimal) stat[2])
                        .totalAmount((BigDecimal) stat[3])
                        .build())
                .collect(Collectors.toList());

        // Build top clients list
        List<ClientUsageStat> topClients = claimStats.stream()
                .limit(10)
                .map(stat -> ClientUsageStat.builder()
                        .clientId((UUID) stat[0])
                        .clientName((String) stat[1])
                        .claimCount(((Long) stat[2]).intValue())
                        .totalSpent((BigDecimal) stat[4])
                        .build())
                .collect(Collectors.toList());

        // Get services count
        long totalServicesInPolicy = serviceCoverageRepository.countByPolicyId(policy.getId());
        int servicesUsed = serviceStats.size();

        return PolicyUsageSummary.builder()
                .policyName(policy.getName())
                .policyVersion(policy.getVersion())
                .reportPeriodStart(fromDate)
                .reportPeriodEnd(toDate)
                .totalClaims(totalClaims)
                .approvedClaims(totalClaims)
                .rejectedClaims((int) rejectedCount)
                .totalClaimAmount(totalClaimAmount)
                .totalInsurancePaid(totalInsurancePaid)
                .totalClientPaid(totalClientPaid)
                .approvalRate(approvalRate)
                .totalServicesInPolicy((int) totalServicesInPolicy)
                .servicesUsed(servicesUsed)
                .topServices(topServices)
                .uniqueClients(uniqueClientIds.size())
                .topClients(topClients)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuickStats() {
        LocalDate now = LocalDate.now();

        Map<String, Object> stats = new HashMap<>();

        // This month's stats (system-wide, no client filter)
        int monthClaims = claimRepository.countAllVisitsInMonth(now.getYear(), now.getMonthValue());
        BigDecimal monthSpending = claimRepository.sumAllSpendingInMonth(now.getYear(), now.getMonthValue());

        // This year's stats (system-wide, no client filter)
        int yearClaims = claimRepository.countAllVisitsInYear(now.getYear());
        BigDecimal yearSpending = claimRepository.sumAllSpendingInYear(now.getYear());

        stats.put("monthClaims", monthClaims);
        stats.put("monthSpending", monthSpending != null ? monthSpending : BigDecimal.ZERO);
        stats.put("yearClaims", yearClaims);
        stats.put("yearSpending", yearSpending != null ? yearSpending : BigDecimal.ZERO);

        // Active policy info
        policyRepository.findByStatus(GlobalPolicyStatus.ACTIVE).stream().findFirst().ifPresent(policy -> {
            stats.put("policyName", policy.getName());
            stats.put("policyVersion", policy.getVersion());
            stats.put("totalServices", serviceCoverageRepository.countByPolicyId(policy.getId()));
        });

        return stats;
    }
}
