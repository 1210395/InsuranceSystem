package com.insurancesystem.Services;

import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.GlobalPolicyStatus;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

/**
 * ClaimEngineService - Updated to use GlobalPolicy system
 * Applies coverage rules from the active GlobalPolicy to healthcare claims
 */
@Service
@RequiredArgsConstructor
public class ClaimEngineService {

    private final GlobalPolicyRepository globalPolicyRepo;
    private final ServiceCoverageRepository serviceCoverageRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final HealthcareProviderClaimRepository claimRepo;

    /**
     * Apply coverage rules from GlobalPolicy to a healthcare claim
     */
    public HealthcareProviderClaim applyCoverageRules(HealthcareProviderClaim claim) {

        // 1) Get active GlobalPolicy
        Optional<GlobalPolicy> activePolicyOpt = globalPolicyRepo.findActivePolicy();
        if (activePolicyOpt.isEmpty()) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ No active insurance policy found");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }
        GlobalPolicy policy = activePolicyOpt.get();

        // 2) Find service coverage by matching description/service name
        String serviceName = claim.getDescription();
        Optional<ServiceCoverage> coverageOpt = serviceCoverageRepo.findByGlobalPolicyIdAndServiceNameIgnoreCase(
                policy.getId(), serviceName);

        if (coverageOpt.isEmpty()) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Service '" + serviceName + "' not covered under this policy");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }
        ServiceCoverage coverage = coverageOpt.get();

        // 3) Check if service is active
        if (!Boolean.TRUE.equals(coverage.getIsActive())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is currently inactive");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 4) Check coverage status
        if (coverage.getCoverageStatus() == CoverageStatusType.NOT_COVERED) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is not covered");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 5) Gender validation
        String patientGender = getPatientGender(claim);
        Integer patientAge = getPatientAge(claim);

        if (!isGenderAllowed(patientGender, coverage.getAllowedGender(), patientAge)) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is not available for patient's gender/age category");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 6) Age validation
        if (!isAgeAllowed(patientAge, coverage.getMinAge(), coverage.getMaxAge())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Patient age (" + patientAge + ") is outside coverage range (" +
                    (coverage.getMinAge() != null ? coverage.getMinAge() : "0") + "-" +
                    (coverage.getMaxAge() != null ? coverage.getMaxAge() : "unlimited") + ")");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 7) Referral requirement
        if (Boolean.TRUE.equals(coverage.getRequiresReferral()) && claim.getDoctorName() == null) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service requires a referral from doctor");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
            return claim;
        }

        // 8) Check client limits (visits/spending)
        if (claim.getClientId() != null) {
            ClientLimits clientLimits = policy.getClientLimits();
            if (clientLimits != null) {
                String limitCheck = checkClientLimits(claim.getClientId(), clientLimits, BigDecimal.valueOf(claim.getAmount()));
                if (limitCheck != null) {
                    claim.setIsCovered(false);
                    claim.setCoverageMessage(limitCheck);
                    claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
                    claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
                    return claim;
                }
            }
        }

        // 9) Check frequency limits
        if (coverage.getFrequencyLimit() != null && coverage.getFrequencyPeriod() != null && claim.getClientId() != null) {
            String freqCheck = checkFrequencyLimit(claim.getClientId(), serviceName,
                    coverage.getFrequencyLimit(), coverage.getFrequencyPeriod());
            if (freqCheck != null) {
                claim.setIsCovered(false);
                claim.setCoverageMessage(freqCheck);
                claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
                claim.setClientPayAmount(BigDecimal.valueOf(claim.getAmount()));
                return claim;
            }
        }

        BigDecimal amount = BigDecimal.valueOf(claim.getAmount());
        BigDecimal coveragePercent = coverage.getCoveragePercent();

        // 10) Emergency full coverage
        if (Boolean.TRUE.equals(claim.getEmergency())) {
            claim.setIsCovered(true);
            claim.setCoverageMessage("🚑 Emergency — fully covered");
            claim.setInsuranceCoveredAmount(amount);
            claim.setClientPayAmount(BigDecimal.ZERO);
            claim.setCoveragePercentUsed(BigDecimal.valueOf(100));
            claim.setMaxCoverageUsed(BigDecimal.ZERO);
            return claim;
        }

        // 11) Calculate normal coverage
        BigDecimal insurancePay = amount.multiply(coveragePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal clientPay = amount.subtract(insurancePay);

        // 12) Apply max coverage limit
        if (coverage.getMaxCoverageAmount() != null &&
                coverage.getMaxCoverageAmount().compareTo(BigDecimal.ZERO) > 0 &&
                insurancePay.compareTo(coverage.getMaxCoverageAmount()) > 0) {
            insurancePay = coverage.getMaxCoverageAmount();
            clientPay = amount.subtract(insurancePay);
        }

        // 13) Set final values
        claim.setIsCovered(true);

        String statusMsg = coverage.getCoverageStatus() == CoverageStatusType.PARTIAL
                ? "✔ Partially covered (" + coveragePercent + "%)"
                : "✔ Covered (" + coveragePercent + "%)";
        claim.setCoverageMessage(statusMsg);

        claim.setInsuranceCoveredAmount(insurancePay);
        claim.setClientPayAmount(clientPay);
        claim.setCoveragePercentUsed(coveragePercent);
        claim.setMaxCoverageUsed(coverage.getMaxCoverageAmount() != null ? coverage.getMaxCoverageAmount() : BigDecimal.ZERO);

        return claim;
    }

    private String checkClientLimits(java.util.UUID clientId, ClientLimits limits, BigDecimal claimAmount) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();

        // Check monthly visit limit
        if (limits.getMaxVisitsPerMonth() != null) {
            int monthlyVisits = claimRepo.countVisitsInMonth(clientId, currentYear, currentMonth);
            if (monthlyVisits >= limits.getMaxVisitsPerMonth()) {
                return "❌ Monthly visit limit (" + limits.getMaxVisitsPerMonth() + ") exceeded";
            }
        }

        // Check yearly visit limit
        if (limits.getMaxVisitsPerYear() != null) {
            int yearlyVisits = claimRepo.countVisitsInYear(clientId, currentYear);
            if (yearlyVisits >= limits.getMaxVisitsPerYear()) {
                return "❌ Yearly visit limit (" + limits.getMaxVisitsPerYear() + ") exceeded";
            }
        }

        // Check monthly spending limit
        if (limits.getMaxSpendingPerMonth() != null) {
            BigDecimal monthlySpending = claimRepo.sumSpendingInMonth(clientId, currentYear, currentMonth);
            if (monthlySpending == null) monthlySpending = BigDecimal.ZERO;
            if (monthlySpending.add(claimAmount).compareTo(limits.getMaxSpendingPerMonth()) > 0) {
                BigDecimal remaining = limits.getMaxSpendingPerMonth().subtract(monthlySpending);
                return "❌ Monthly spending limit exceeded. Remaining: ₪" + remaining.setScale(2, RoundingMode.HALF_UP);
            }
        }

        // Check yearly spending limit
        if (limits.getMaxSpendingPerYear() != null) {
            BigDecimal yearlySpending = claimRepo.sumSpendingInYear(clientId, currentYear);
            if (yearlySpending == null) yearlySpending = BigDecimal.ZERO;
            if (yearlySpending.add(claimAmount).compareTo(limits.getMaxSpendingPerYear()) > 0) {
                BigDecimal remaining = limits.getMaxSpendingPerYear().subtract(yearlySpending);
                return "❌ Yearly spending limit exceeded. Remaining: ₪" + remaining.setScale(2, RoundingMode.HALF_UP);
            }
        }

        return null; // No limit exceeded
    }

    private String checkFrequencyLimit(java.util.UUID clientId, String serviceName, Integer limit,
            com.insurancesystem.Model.Entity.Enums.FrequencyPeriod period) {
        LocalDate sinceDate = switch (period) {
            case DAILY -> LocalDate.now();
            case WEEKLY -> LocalDate.now().minusDays(7);
            case MONTHLY -> LocalDate.now().withDayOfMonth(1);
            case YEARLY -> LocalDate.now().withDayOfYear(1);
        };

        int usageCount = claimRepo.countServiceUsageSince(clientId, serviceName, sinceDate);
        if (usageCount >= limit) {
            return "❌ Service usage limit (" + limit + " per " + period.name().toLowerCase() + ") exceeded";
        }
        return null;
    }

    // ===========================
    // Helper Methods
    // ===========================

    private String getPatientGender(HealthcareProviderClaim claim) {
        if (claim.getClientId() == null) {
            return null;
        }

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        if (familyMemberOpt.isPresent()) {
            FamilyMember fm = familyMemberOpt.get();
            return fm.getGender() != null ? fm.getGender().toString() : null;
        }

        Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
        if (clientOpt.isPresent()) {
            return clientOpt.get().getGender();
        }

        return null;
    }

    private Integer getPatientAge(HealthcareProviderClaim claim) {
        if (claim.getClientId() == null) {
            return null;
        }

        LocalDate dateOfBirth = null;

        Optional<FamilyMember> familyMemberOpt = familyMemberRepo.findById(claim.getClientId());
        if (familyMemberOpt.isPresent()) {
            dateOfBirth = familyMemberOpt.get().getDateOfBirth();
        } else {
            Optional<Client> clientOpt = clientRepo.findById(claim.getClientId());
            if (clientOpt.isPresent()) {
                dateOfBirth = clientOpt.get().getDateOfBirth();
            }
        }

        if (dateOfBirth != null) {
            return Period.between(dateOfBirth, LocalDate.now()).getYears();
        }
        return null;
    }

    private boolean isGenderAllowed(String patientGender, AllowedGender allowedGender, Integer patientAge) {
        if (patientGender == null || allowedGender == null || allowedGender == AllowedGender.ALL) {
            return true;
        }
        return switch (allowedGender) {
            case MALE -> "MALE".equalsIgnoreCase(patientGender);
            case FEMALE -> "FEMALE".equalsIgnoreCase(patientGender);
            case CHILD -> patientAge != null && patientAge < 18;
            default -> true;
        };
    }

    private boolean isAgeAllowed(Integer patientAge, Integer minAge, Integer maxAge) {
        if (minAge == null && maxAge == null) {
            return true;
        }
        if (patientAge == null) {
            return true;
        }
        if (minAge != null && patientAge < minAge) {
            return false;
        }
        if (maxAge != null && patientAge > maxAge) {
            return false;
        }
        return true;
    }
}
