package com.insurancesystem.Services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Model.Entity.Enums.AllowedGender;
import com.insurancesystem.Model.Entity.Enums.CoverageStatusType;
import com.insurancesystem.Model.Entity.Enums.ProviderType;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

/**
 * ClaimEngineService - Updated to use GlobalPolicy system
 * Applies coverage rules from the active GlobalPolicy to healthcare claims
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimEngineService {

    private final GlobalPolicyRepository globalPolicyRepo;
    private final ServiceCoverageRepository serviceCoverageRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberRepository familyMemberRepo;
    private final HealthcareProviderClaimRepository claimRepo;
    private final PriceListRepository priceListRepo;
    private final ObjectMapper objectMapper;

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
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }
        GlobalPolicy policy = activePolicyOpt.get();

        // 1b) Validate policy is not expired
        if (policy.getEffectiveTo() != null && policy.getEffectiveTo().isBefore(LocalDate.now())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("Policy has expired on " + policy.getEffectiveTo());
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 1c) Validate policy effective date has started
        if (policy.getEffectiveFrom() != null && policy.getEffectiveFrom().isAfter(LocalDate.now())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("Policy is not yet effective. Starts on " + policy.getEffectiveFrom());
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 1d) Validate patient enrollment is active
        if (claim.getClientId() != null) {
            Optional<Client> patientOpt = clientRepo.findById(claim.getClientId());
            if (patientOpt.isPresent()) {
                Client patient = patientOpt.get();
                if (patient.getStatus() != null &&
                    patient.getStatus() != com.insurancesystem.Model.Entity.Enums.MemberStatus.ACTIVE) {
                    // Could be a family member - check
                    Optional<FamilyMember> fmOpt = familyMemberRepo.findById(claim.getClientId());
                    if (fmOpt.isEmpty()) {
                        claim.setIsCovered(false);
                        claim.setCoverageMessage("Patient enrollment is not active");
                        claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
                        claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
                        return claim;
                    }
                }
            }
        }

        // 2) Find service coverage by matching description/service name, then fall back to category
        String serviceName = claim.getDescription();
        Optional<ServiceCoverage> coverageOpt = serviceCoverageRepo.findByGlobalPolicyIdAndServiceNameIgnoreCase(
                policy.getId(), serviceName);

        if (coverageOpt.isEmpty()) {
            // Fall back: determine category from provider role
            String categoryName = resolveCategory(claim);
            if (categoryName != null) {
                List<ServiceCoverage> categoryCoverages = serviceCoverageRepo.findByGlobalPolicyIdAndCategoryName(
                        policy.getId(), categoryName);
                if (!categoryCoverages.isEmpty()) {
                    coverageOpt = Optional.of(categoryCoverages.get(0));
                }
            }
        }

        if (coverageOpt.isEmpty()) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Service '" + serviceName + "' not covered under this policy");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }
        ServiceCoverage coverage = coverageOpt.get();

        // 3) Check if service is active
        if (!Boolean.TRUE.equals(coverage.getIsActive())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is currently inactive");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 4) Check coverage status
        if (coverage.getCoverageStatus() == CoverageStatusType.NOT_COVERED) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is not covered");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 5) Gender validation
        String patientGender = getPatientGender(claim);
        Integer patientAge = getPatientAge(claim);

        if (!isGenderAllowed(patientGender, coverage.getAllowedGender(), patientAge)) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service is not available for patient's gender/age category");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 6) Age validation
        if (!isAgeAllowed(patientAge, coverage.getMinAge(), coverage.getMaxAge())) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ Patient age (" + patientAge + ") is outside coverage range (" +
                    (coverage.getMinAge() != null ? coverage.getMinAge() : "0") + "-" +
                    (coverage.getMaxAge() != null ? coverage.getMaxAge() : "unlimited") + ")");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 7) Referral requirement
        if (Boolean.TRUE.equals(coverage.getRequiresReferral()) && claim.getDoctorName() == null) {
            claim.setIsCovered(false);
            claim.setCoverageMessage("❌ This service requires a referral from doctor");
            claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
            claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
            return claim;
        }

        // 8) Check client limits (visits/spending)
        if (claim.getClientId() != null) {
            ClientLimits clientLimits = policy.getClientLimits();
            if (clientLimits != null) {
                String limitCheck = checkClientLimits(claim.getClientId(), clientLimits, claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
                if (limitCheck != null) {
                    claim.setIsCovered(false);
                    claim.setCoverageMessage(limitCheck);
                    claim.setInsuranceCoveredAmount(BigDecimal.ZERO);
                    claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
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
                claim.setClientPayAmount(claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO);
                return claim;
            }
        }

        BigDecimal amount = claim.getAmount() != null ? claim.getAmount() : BigDecimal.ZERO;
        BigDecimal coveragePercent = coverage.getCoveragePercent();

        // 10) Try per-item coverage calculation for pharmacy/lab/radiology claims
        String categoryName = resolveCategory(claim);
        if (categoryName != null && claim.getRoleSpecificData() != null) {
            PerItemCoverageResult perItemResult = calculatePerItemCoverage(claim, categoryName);
            if (perItemResult != null) {
                claim.setIsCovered(true);
                claim.setInsuranceCoveredAmount(perItemResult.insurancePay);
                claim.setClientPayAmount(perItemResult.clientPay);
                claim.setCoveragePercentUsed(perItemResult.effectivePercent);
                claim.setCoverageMessage(perItemResult.message);
                claim.setMaxCoverageUsed(coverage.getMaxCoverageAmount() != null ? coverage.getMaxCoverageAmount() : BigDecimal.ZERO);
                return claim;
            }
        }

        // 12) Calculate normal coverage (fallback for single-item or doctor claims)
        BigDecimal insurancePay = amount.multiply(coveragePercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal clientPay = amount.subtract(insurancePay);

        // 13) Apply max coverage limit (clamped to not exceed claim amount)
        if (coverage.getMaxCoverageAmount() != null &&
                coverage.getMaxCoverageAmount().compareTo(BigDecimal.ZERO) > 0 &&
                insurancePay.compareTo(coverage.getMaxCoverageAmount()) > 0) {
            insurancePay = coverage.getMaxCoverageAmount().min(amount);
            clientPay = amount.subtract(insurancePay);
        }

        // Ensure clientPay is never negative
        if (clientPay.compareTo(BigDecimal.ZERO) < 0) {
            clientPay = BigDecimal.ZERO;
            insurancePay = amount;
        }

        // 14) Set final values
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
                return "❌ Monthly spending limit exceeded. Remaining: " + remaining.setScale(2, RoundingMode.HALF_UP) + " JOD";
            }
        }

        // Check yearly spending limit
        if (limits.getMaxSpendingPerYear() != null) {
            BigDecimal yearlySpending = claimRepo.sumSpendingInYear(clientId, currentYear);
            if (yearlySpending == null) yearlySpending = BigDecimal.ZERO;
            if (yearlySpending.add(claimAmount).compareTo(limits.getMaxSpendingPerYear()) > 0) {
                BigDecimal remaining = limits.getMaxSpendingPerYear().subtract(yearlySpending);
                return "❌ Yearly spending limit exceeded. Remaining: " + remaining.setScale(2, RoundingMode.HALF_UP) + " JOD";
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

    private static class PerItemCoverageResult {
        BigDecimal insurancePay;
        BigDecimal clientPay;
        BigDecimal effectivePercent;
        String message;
    }

    private PerItemCoverageResult calculatePerItemCoverage(HealthcareProviderClaim claim, String categoryName) {
        try {
            JsonNode root = objectMapper.readTree(claim.getRoleSpecificData());

            ProviderType providerType = switch (categoryName) {
                case "Pharmacy" -> ProviderType.PHARMACY;
                case "Laboratory Tests" -> ProviderType.LAB;
                case "Radiology & Imaging" -> ProviderType.RADIOLOGY;
                default -> null;
            };
            if (providerType == null) return null;

            BigDecimal totalInsurance = BigDecimal.ZERO;
            BigDecimal totalClient = BigDecimal.ZERO;
            BigDecimal totalAmount = BigDecimal.ZERO;

            JsonNode items = root.get("items");
            if (items != null && items.isArray() && !items.isEmpty()) {
                // Multi-item claims (pharmacy prescriptions)
                for (JsonNode item : items) {
                    String itemName = item.has("name") ? item.get("name").asText() : null;
                    double itemPrice = item.has("price") ? item.get("price").asDouble()
                            : item.has("pharmacistPrice") ? item.get("pharmacistPrice").asDouble() : 0;

                    if (itemName == null || itemPrice <= 0) continue;

                    BigDecimal price = BigDecimal.valueOf(itemPrice);
                    totalAmount = totalAmount.add(price);

                    List<PriceList> priceEntries = priceListRepo.findByProviderTypeAndServiceName(providerType, itemName);
                    int coveragePct = 0;
                    BigDecimal approvedPrice = price;

                    if (!priceEntries.isEmpty()) {
                        PriceList pl = priceEntries.get(0);
                        coveragePct = pl.getCoveragePercentage() != null ? pl.getCoveragePercentage() : 0;
                        if (pl.getCoverageStatus() == com.insurancesystem.Model.Entity.Enums.CoverageStatus.NOT_COVERED) {
                            coveragePct = 0;
                        }
                        int qty = item.has("calculatedQuantity") ? item.get("calculatedQuantity").asInt(1) : 1;
                        BigDecimal definedPriceForQty = BigDecimal.valueOf(pl.getPrice()).multiply(BigDecimal.valueOf(qty));
                        approvedPrice = price.min(definedPriceForQty);
                    }

                    BigDecimal itemInsurance = approvedPrice.multiply(BigDecimal.valueOf(coveragePct))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal itemClient = price.subtract(itemInsurance);

                    totalInsurance = totalInsurance.add(itemInsurance);
                    totalClient = totalClient.add(itemClient);
                }
            } else if (root.has("testName")) {
                // Single-item claims (lab tests, radiology)
                String testName = root.get("testName").asText();
                double testPrice = root.has("finalPrice") ? root.get("finalPrice").asDouble()
                        : claim.getAmount() != null ? claim.getAmount().doubleValue() : 0;

                if (testName != null && testPrice > 0) {
                    BigDecimal price = BigDecimal.valueOf(testPrice);
                    totalAmount = price;

                    List<PriceList> priceEntries = priceListRepo.findByProviderTypeAndServiceName(providerType, testName);
                    int coveragePct = 0;
                    BigDecimal approvedPrice = price;

                    if (!priceEntries.isEmpty()) {
                        PriceList pl = priceEntries.get(0);
                        coveragePct = pl.getCoveragePercentage() != null ? pl.getCoveragePercentage() : 0;
                        if (pl.getCoverageStatus() == com.insurancesystem.Model.Entity.Enums.CoverageStatus.NOT_COVERED) {
                            coveragePct = 0;
                        }
                        BigDecimal definedPrice = BigDecimal.valueOf(pl.getPrice());
                        approvedPrice = price.min(definedPrice);
                    }

                    BigDecimal itemInsurance = approvedPrice.multiply(BigDecimal.valueOf(coveragePct))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal itemClient = price.subtract(itemInsurance);

                    totalInsurance = itemInsurance;
                    totalClient = itemClient;
                }
            } else {
                return null;
            }

            if (totalAmount.compareTo(BigDecimal.ZERO) == 0) return null;

            PerItemCoverageResult result = new PerItemCoverageResult();
            result.insurancePay = totalInsurance;
            result.clientPay = totalClient;
            result.effectivePercent = totalInsurance.multiply(BigDecimal.valueOf(100))
                    .divide(totalAmount, 2, RoundingMode.HALF_UP);

            if (result.effectivePercent.compareTo(BigDecimal.valueOf(100)) >= 0) {
                result.message = "✔ Fully covered";
            } else if (result.effectivePercent.compareTo(BigDecimal.ZERO) == 0) {
                result.message = "❌ Not covered";
            } else {
                result.message = "✔ Partially covered (" + result.effectivePercent.setScale(0, RoundingMode.HALF_UP) + "%)";
            }

            return result;
        } catch (Exception e) {
            log.warn("Per-item coverage calculation failed for claim, falling back to category-level: {}", e.getMessage());
            return null;
        }
    }

    private String resolveCategory(HealthcareProviderClaim claim) {
        Client provider = claim.getHealthcareProvider();
        if (provider == null) return null;

        // Determine provider role
        String role = null;
        if (provider.getRequestedRole() != null) {
            role = provider.getRequestedRole().name();
        } else if (provider.getRoles() != null) {
            role = provider.getRoles().stream()
                    .findFirst()
                    .map(r -> r.getName().name())
                    .orElse(null);
        }
        if (role == null) return null;

        // Map provider role to service category name
        return switch (role) {
            case "PHARMACIST" -> "Pharmacy";
            case "LAB_TECH" -> "Laboratory Tests";
            case "RADIOLOGIST" -> "Radiology & Imaging";
            case "DOCTOR" -> "General Consultation";
            default -> null;
        };
    }

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
