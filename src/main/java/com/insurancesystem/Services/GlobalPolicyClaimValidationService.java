package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.FrequencyPeriod;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import com.insurancesystem.Repository.*;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GlobalPolicyClaimValidationService {

    private final GlobalPolicyRepository policyRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final CategoryLimitsRepository categoryLimitsRepository;
    private final ClientRepository clientRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final HealthcareProviderClaimRepository claimRepository;

    @Data
    @Builder
    public static class ClaimValidationRequest {
        private UUID clientId;
        private UUID familyMemberId;  // Optional, for dependents
        private String serviceName;
        private BigDecimal claimAmount;
        private boolean isEmergency;
        private boolean hasReferral;
        private String doctorName;
    }

    @Data
    @Builder
    public static class ClaimValidationResult {
        private boolean covered;
        private String status;
        private String message;
        private BigDecimal insurancePayAmount;
        private BigDecimal clientPayAmount;
        private BigDecimal coveragePercentApplied;
        private BigDecimal maxCoverageApplied;

        // Limit warnings
        private boolean nearVisitLimit;
        private boolean nearSpendingLimit;
        private Integer visitsUsed;
        private Integer visitsRemaining;
        private BigDecimal spendingUsed;
        private BigDecimal spendingRemaining;
    }

    @Transactional(readOnly = true)
    public ClaimValidationResult validateClaim(ClaimValidationRequest request) {
        // 1. Get active policy
        Optional<GlobalPolicy> policyOpt = policyRepository.findByStatus(GlobalPolicyStatus.ACTIVE).stream().findFirst();
        if (policyOpt.isEmpty()) {
            return notCovered("NO_POLICY", "No active insurance policy found");
        }
        GlobalPolicy policy = policyOpt.get();

        // 2. Find service coverage
        Optional<ServiceCoverage> serviceOpt = serviceCoverageRepository.findByGlobalPolicyIdAndServiceNameIgnoreCase(
                policy.getId(), request.getServiceName());

        if (serviceOpt.isEmpty()) {
            return notCovered("SERVICE_NOT_FOUND", "Service '" + request.getServiceName() + "' is not in the policy");
        }
        ServiceCoverage service = serviceOpt.get();

        // 3. Check if service is active
        if (!Boolean.TRUE.equals(service.getIsActive())) {
            return notCovered("SERVICE_INACTIVE", "Service is currently inactive");
        }

        // 4. Check coverage status
        if (service.getCoverageStatus() == CoverageStatusType.NOT_COVERED) {
            return notCovered("NOT_COVERED", "This service is not covered under the current policy");
        }

        // 5. Validate patient eligibility (age/gender)
        ClaimValidationResult eligibilityCheck = validatePatientEligibility(request, service);
        if (!eligibilityCheck.isCovered()) {
            return eligibilityCheck;
        }

        // 6. Check referral requirement
        if (Boolean.TRUE.equals(service.getRequiresReferral()) && !request.isHasReferral()) {
            return notCovered("REFERRAL_REQUIRED", "This service requires a referral from a doctor");
        }

        // 7. Check client limits
        ClientLimits clientLimits = policy.getClientLimits();
        if (clientLimits != null) {
            ClaimValidationResult limitsCheck = validateClientLimits(request.getClientId(), clientLimits, request.getClaimAmount());
            if (!limitsCheck.isCovered()) {
                return limitsCheck;
            }
        }

        // 8. Check category limits if service has a category
        if (service.getCategory() != null) {
            ClaimValidationResult categoryCheck = validateCategoryLimits(
                    request.getClientId(), policy.getId(), service.getCategory().getId(), request.getClaimAmount());
            if (!categoryCheck.isCovered()) {
                return categoryCheck;
            }
        }

        // 9. Check service frequency limits
        if (service.getFrequencyLimit() != null && service.getFrequencyPeriod() != null) {
            ClaimValidationResult freqCheck = validateFrequencyLimit(
                    request.getClientId(), service.getServiceName(), service.getFrequencyLimit(), service.getFrequencyPeriod());
            if (!freqCheck.isCovered()) {
                return freqCheck;
            }
        }

        // 10. Calculate coverage amounts
        BigDecimal claimAmount = request.getClaimAmount();
        BigDecimal coveragePercent = service.getCoveragePercent();

        // Emergency override - full coverage
        if (request.isEmergency()) {
            coveragePercent = BigDecimal.valueOf(100);
        }

        BigDecimal insurancePay = claimAmount.multiply(coveragePercent).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        // Apply max coverage limit
        if (service.getMaxCoverageAmount() != null && insurancePay.compareTo(service.getMaxCoverageAmount()) > 0) {
            insurancePay = service.getMaxCoverageAmount();
        }

        BigDecimal clientPay = claimAmount.subtract(insurancePay);

        // Build result with remaining limits info
        ClaimValidationResult.ClaimValidationResultBuilder resultBuilder = ClaimValidationResult.builder()
                .covered(true)
                .status("APPROVED")
                .message(request.isEmergency() ? "Emergency claim - fully covered" : "Claim approved")
                .insurancePayAmount(insurancePay)
                .clientPayAmount(clientPay)
                .coveragePercentApplied(coveragePercent)
                .maxCoverageApplied(service.getMaxCoverageAmount());

        // Add usage info if limits exist
        if (clientLimits != null) {
            addUsageInfo(resultBuilder, request.getClientId(), clientLimits);
        }

        return resultBuilder.build();
    }

    private ClaimValidationResult validatePatientEligibility(ClaimValidationRequest request, ServiceCoverage service) {
        Integer patientAge = getPatientAge(request.getClientId(), request.getFamilyMemberId());
        String patientGender = getPatientGender(request.getClientId(), request.getFamilyMemberId());

        // Age validation
        if (patientAge != null) {
            if (service.getMinAge() != null && patientAge < service.getMinAge()) {
                return notCovered("AGE_BELOW_MIN",
                        "Patient age (" + patientAge + ") is below minimum age (" + service.getMinAge() + ") for this service");
            }
            if (service.getMaxAge() != null && patientAge > service.getMaxAge()) {
                return notCovered("AGE_ABOVE_MAX",
                        "Patient age (" + patientAge + ") exceeds maximum age (" + service.getMaxAge() + ") for this service");
            }
        }

        // Gender validation
        if (service.getAllowedGender() != null && service.getAllowedGender() != AllowedGender.ALL && patientGender != null) {
            boolean genderMatch = switch (service.getAllowedGender()) {
                case MALE -> "MALE".equalsIgnoreCase(patientGender);
                case FEMALE -> "FEMALE".equalsIgnoreCase(patientGender);
                case CHILD -> patientAge != null && patientAge < 18;
                default -> true;
            };
            if (!genderMatch) {
                return notCovered("GENDER_RESTRICTION",
                        "This service is restricted to " + service.getAllowedGender() + " patients");
            }
        }

        return ClaimValidationResult.builder().covered(true).build();
    }

    private ClaimValidationResult validateClientLimits(UUID clientId, ClientLimits limits, BigDecimal claimAmount) {
        LocalDateTime now = LocalDateTime.now();
        int currentMonth = now.getMonthValue();
        int currentYear = now.getYear();

        // Check monthly visit limit
        if (limits.getMaxVisitsPerMonth() != null) {
            int monthlyVisits = claimRepository.countVisitsInMonth(clientId, currentYear, currentMonth);
            if (monthlyVisits >= limits.getMaxVisitsPerMonth()) {
                return notCovered("MONTHLY_VISITS_EXCEEDED",
                        "Monthly visit limit (" + limits.getMaxVisitsPerMonth() + ") exceeded");
            }
        }

        // Check yearly visit limit
        if (limits.getMaxVisitsPerYear() != null) {
            int yearlyVisits = claimRepository.countVisitsInYear(clientId, currentYear);
            if (yearlyVisits >= limits.getMaxVisitsPerYear()) {
                return notCovered("YEARLY_VISITS_EXCEEDED",
                        "Yearly visit limit (" + limits.getMaxVisitsPerYear() + ") exceeded");
            }
        }

        // Check monthly spending limit
        if (limits.getMaxSpendingPerMonth() != null) {
            BigDecimal monthlySpending = claimRepository.sumSpendingInMonth(clientId, currentYear, currentMonth);
            if (monthlySpending == null) monthlySpending = BigDecimal.ZERO;
            if (monthlySpending.add(claimAmount).compareTo(limits.getMaxSpendingPerMonth()) > 0) {
                BigDecimal remaining = limits.getMaxSpendingPerMonth().subtract(monthlySpending);
                return notCovered("MONTHLY_SPENDING_EXCEEDED",
                        "Monthly spending limit exceeded. Remaining: ₪" + remaining.setScale(2, RoundingMode.HALF_UP));
            }
        }

        // Check yearly spending limit
        if (limits.getMaxSpendingPerYear() != null) {
            BigDecimal yearlySpending = claimRepository.sumSpendingInYear(clientId, currentYear);
            if (yearlySpending == null) yearlySpending = BigDecimal.ZERO;
            if (yearlySpending.add(claimAmount).compareTo(limits.getMaxSpendingPerYear()) > 0) {
                BigDecimal remaining = limits.getMaxSpendingPerYear().subtract(yearlySpending);
                return notCovered("YEARLY_SPENDING_EXCEEDED",
                        "Yearly spending limit exceeded. Remaining: ₪" + remaining.setScale(2, RoundingMode.HALF_UP));
            }
        }

        return ClaimValidationResult.builder().covered(true).build();
    }

    private ClaimValidationResult validateCategoryLimits(UUID clientId, UUID policyId, UUID categoryId, BigDecimal claimAmount) {
        Optional<CategoryLimits> limitsOpt = categoryLimitsRepository.findByGlobalPolicyIdAndCategoryId(policyId, categoryId);
        if (limitsOpt.isEmpty()) {
            return ClaimValidationResult.builder().covered(true).build();
        }

        // Category limits validation is tracked through ClientUsage table
        // For now, we allow if no specific tracking is set up
        // TODO: Implement detailed category tracking when ClientUsage is enhanced

        return ClaimValidationResult.builder().covered(true).build();
    }

    private ClaimValidationResult validateFrequencyLimit(UUID clientId, String serviceName, Integer limit, FrequencyPeriod period) {
        LocalDate sinceDate = switch (period) {
            case DAILY -> LocalDate.now();
            case WEEKLY -> LocalDate.now().minusDays(7);
            case MONTHLY -> LocalDate.now().withDayOfMonth(1);
            case YEARLY -> LocalDate.now().withDayOfYear(1);
        };

        int usageCount = claimRepository.countServiceUsageSince(clientId, serviceName, sinceDate);
        if (usageCount >= limit) {
            return notCovered("FREQUENCY_LIMIT_EXCEEDED",
                    "Service usage limit (" + limit + " per " + period.name().toLowerCase() + ") exceeded");
        }

        return ClaimValidationResult.builder().covered(true).build();
    }

    private void addUsageInfo(ClaimValidationResult.ClaimValidationResultBuilder builder, UUID clientId, ClientLimits limits) {
        LocalDateTime now = LocalDateTime.now();
        int currentYear = now.getYear();

        if (limits.getMaxVisitsPerYear() != null) {
            int used = claimRepository.countVisitsInYear(clientId, currentYear);
            int remaining = limits.getMaxVisitsPerYear() - used;
            builder.visitsUsed(used);
            builder.visitsRemaining(remaining);
            builder.nearVisitLimit(remaining <= 5);
        }

        if (limits.getMaxSpendingPerYear() != null) {
            BigDecimal used = claimRepository.sumSpendingInYear(clientId, currentYear);
            if (used == null) used = BigDecimal.ZERO;
            BigDecimal remaining = limits.getMaxSpendingPerYear().subtract(used);
            builder.spendingUsed(used);
            builder.spendingRemaining(remaining);
            builder.nearSpendingLimit(remaining.compareTo(BigDecimal.valueOf(500)) <= 0);
        }
    }

    private Integer getPatientAge(UUID clientId, UUID familyMemberId) {
        LocalDate dob = null;

        if (familyMemberId != null) {
            Optional<FamilyMember> fm = familyMemberRepository.findById(familyMemberId);
            if (fm.isPresent()) {
                dob = fm.get().getDateOfBirth();
            }
        } else if (clientId != null) {
            Optional<Client> client = clientRepository.findById(clientId);
            if (client.isPresent()) {
                dob = client.get().getDateOfBirth();
            }
        }

        if (dob != null) {
            return Period.between(dob, LocalDate.now()).getYears();
        }
        return null;
    }

    private String getPatientGender(UUID clientId, UUID familyMemberId) {
        if (familyMemberId != null) {
            Optional<FamilyMember> fm = familyMemberRepository.findById(familyMemberId);
            if (fm.isPresent() && fm.get().getGender() != null) {
                return fm.get().getGender().toString();
            }
        } else if (clientId != null) {
            Optional<Client> client = clientRepository.findById(clientId);
            if (client.isPresent()) {
                return client.get().getGender();
            }
        }
        return null;
    }

    private ClaimValidationResult notCovered(String status, String message) {
        return ClaimValidationResult.builder()
                .covered(false)
                .status(status)
                .message(message)
                .insurancePayAmount(BigDecimal.ZERO)
                .clientPayAmount(BigDecimal.ZERO)
                .build();
    }
}
