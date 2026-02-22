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

        // Calculate totals
        int totalClaims = 0;
        BigDecimal totalClaimAmount = BigDecimal.ZERO;
        BigDecimal totalInsurancePaid = BigDecimal.ZERO;
        Set<UUID> uniqueClientIds = new HashSet<>();

        for (Object[] stat : claimStats) {
            UUID clientId = (UUID) stat[0];
            Long count = (Long) stat[2];
            BigDecimal insurancePaid = (BigDecimal) stat[3];

            uniqueClientIds.add(clientId);
            totalClaims += count.intValue();
            totalInsurancePaid = totalInsurancePaid.add(insurancePaid);
        }

        // Build top services list
        List<ServiceUsageStat> topServices = serviceStats.stream()
                .limit(10)
                .map(stat -> ServiceUsageStat.builder()
                        .serviceName((String) stat[0])
                        .usageCount(((Long) stat[1]).intValue())
                        .insurancePaid((BigDecimal) stat[2])
                        .build())
                .collect(Collectors.toList());

        // Build top clients list
        List<ClientUsageStat> topClients = claimStats.stream()
                .limit(10)
                .map(stat -> ClientUsageStat.builder()
                        .clientId((UUID) stat[0])
                        .clientName((String) stat[1])
                        .claimCount(((Long) stat[2]).intValue())
                        .totalSpent((BigDecimal) stat[3])
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
                .totalInsurancePaid(totalInsurancePaid)
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
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate startOfYear = now.withDayOfYear(1);

        Map<String, Object> stats = new HashMap<>();

        // This month's stats
        int monthClaims = claimRepository.countVisitsInMonth(null, now.getYear(), now.getMonthValue());
        BigDecimal monthSpending = claimRepository.sumSpendingInMonth(null, now.getYear(), now.getMonthValue());

        // This year's stats
        int yearClaims = claimRepository.countVisitsInYear(null, now.getYear());
        BigDecimal yearSpending = claimRepository.sumSpendingInYear(null, now.getYear());

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
