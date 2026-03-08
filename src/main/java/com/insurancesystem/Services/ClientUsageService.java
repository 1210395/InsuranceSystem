package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientUsageDTO;
import com.insurancesystem.Model.Entity.*;
import com.insurancesystem.Repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ClientUsageService {

    private final ClientUsageRepository clientUsageRepository;
    private final ClientServiceUsageRepository clientServiceUsageRepository;
    private final ClientRepository clientRepository;
    private final ServiceCoverageRepository serviceCoverageRepository;
    private final ServiceCategoryRepository serviceCategoryRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional(readOnly = true)
    public ClientUsageDTO getClientUsage(UUID clientId, int year, int month) {
        ClientUsage usage = clientUsageRepository.findByClientIdAndYearAndMonth(clientId, year, month)
                .orElse(null);

        if (usage == null) {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new NotFoundException("Client not found"));
            return ClientUsageDTO.builder()
                    .clientId(clientId)
                    .clientName(client.getFullName())
                    .year(year)
                    .month(month)
                    .totalVisits(0)
                    .totalSpending(BigDecimal.ZERO)
                    .build();
        }

        return toDTO(usage);
    }

    @Transactional(readOnly = true)
    public List<ClientUsageDTO> getClientUsageHistory(UUID clientId) {
        return clientUsageRepository.findByClientId(clientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ClientUsageDTO getClientYearlyUsage(UUID clientId, int year) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new NotFoundException("Client not found"));

        Integer totalVisits = clientUsageRepository.getTotalVisitsForYear(clientId, year);
        BigDecimal totalSpending = clientUsageRepository.getTotalSpendingForYear(clientId, year);

        return ClientUsageDTO.builder()
                .clientId(clientId)
                .clientName(client.getFullName())
                .year(year)
                .month(null)
                .totalVisits(totalVisits != null ? totalVisits : 0)
                .totalSpending(totalSpending != null ? totalSpending : BigDecimal.ZERO)
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementUsage(UUID clientId, UUID serviceCoverageId, BigDecimal amount) {
        // Resolve FamilyMember ID → parent Client ID
        UUID resolvedClientId = resolveClientId(clientId);
        if (resolvedClientId == null) {
            log.warn("Cannot track usage: no client found for ID {}", clientId);
            return;
        }

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Update client monthly usage
        ClientUsage clientUsage = clientUsageRepository.findByClientIdAndYearAndMonth(resolvedClientId, year, month)
                .orElseGet(() -> {
                    Client client = clientRepository.findById(resolvedClientId)
                            .orElseThrow(() -> new NotFoundException("Client not found"));
                    return ClientUsage.builder()
                            .client(client)
                            .year(year)
                            .month(month)
                            .totalVisits(0)
                            .totalSpending(BigDecimal.ZERO)
                            .build();
                });

        clientUsage.setTotalVisits(clientUsage.getTotalVisits() + 1);
        clientUsage.setTotalSpending(clientUsage.getTotalSpending().add(amount));
        clientUsageRepository.save(clientUsage);

        // Update service-specific usage
        if (serviceCoverageId != null) {
            ServiceCoverage service = serviceCoverageRepository.findById(serviceCoverageId).orElse(null);

            ClientServiceUsage serviceUsage = clientServiceUsageRepository
                    .findByClientIdAndServiceCoverageIdAndYearAndMonth(clientId, serviceCoverageId, year, month)
                    .orElseGet(() -> {
                        Client client = clientRepository.findById(clientId)
                                .orElseThrow(() -> new NotFoundException("Client not found"));
                        ClientServiceUsage.ClientServiceUsageBuilder builder = ClientServiceUsage.builder()
                                .client(client)
                                .serviceCoverage(service)
                                .year(year)
                                .month(month)
                                .usageCount(0)
                                .amountUsed(BigDecimal.ZERO);

                        if (service != null && service.getCategory() != null) {
                            builder.category(service.getCategory());
                        }

                        return builder.build();
                    });

            serviceUsage.setUsageCount(serviceUsage.getUsageCount() + 1);
            serviceUsage.setAmountUsed(serviceUsage.getAmountUsed().add(amount));
            clientServiceUsageRepository.save(serviceUsage);
        }
    }

    /**
     * Resolve a clientId that might be a FamilyMember ID to the actual Client ID.
     */
    private UUID resolveClientId(UUID clientId) {
        if (clientId == null) return null;
        // Check if it's a direct client
        if (clientRepository.existsById(clientId)) return clientId;
        // Check if it's a family member → return parent client ID
        return familyMemberRepository.findById(clientId)
                .map(fm -> fm.getClient() != null ? fm.getClient().getId() : null)
                .orElse(null);
    }

    // Fix #12: Reverse usage when approved claim is returned
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decrementUsage(UUID clientId, UUID serviceCoverageId, BigDecimal amount) {
        UUID resolvedClientId = resolveClientId(clientId);
        if (resolvedClientId == null) {
            log.warn("Cannot reverse usage: no client found for ID {}", clientId);
            return;
        }

        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        clientUsageRepository.findByClientIdAndYearAndMonth(resolvedClientId, year, month)
                .ifPresent(clientUsage -> {
                    clientUsage.setTotalVisits(Math.max(0, clientUsage.getTotalVisits() - 1));
                    clientUsage.setTotalSpending(
                            clientUsage.getTotalSpending().subtract(amount).max(BigDecimal.ZERO));
                    clientUsageRepository.save(clientUsage);
                });

        if (serviceCoverageId != null) {
            clientServiceUsageRepository
                    .findByClientIdAndServiceCoverageIdAndYearAndMonth(clientId, serviceCoverageId, year, month)
                    .ifPresent(serviceUsage -> {
                        serviceUsage.setUsageCount(Math.max(0, serviceUsage.getUsageCount() - 1));
                        serviceUsage.setAmountUsed(
                                serviceUsage.getAmountUsed().subtract(amount).max(BigDecimal.ZERO));
                        clientServiceUsageRepository.save(serviceUsage);
                    });
        }
    }

    @Transactional(readOnly = true)
    public boolean checkClientLimitExceeded(UUID clientId, ClientLimits limits) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Check monthly limits
        ClientUsage monthlyUsage = clientUsageRepository
                .findByClientIdAndYearAndMonth(clientId, year, month)
                .orElse(null);

        if (monthlyUsage != null) {
            if (limits.getMaxVisitsPerMonth() != null &&
                monthlyUsage.getTotalVisits() >= limits.getMaxVisitsPerMonth()) {
                return true;
            }
            if (limits.getMaxSpendingPerMonth() != null &&
                monthlyUsage.getTotalSpending().compareTo(limits.getMaxSpendingPerMonth()) >= 0) {
                return true;
            }
        }

        // Check yearly limits
        Integer yearlyVisits = clientUsageRepository.getTotalVisitsForYear(clientId, year);
        BigDecimal yearlySpending = clientUsageRepository.getTotalSpendingForYear(clientId, year);

        if (limits.getMaxVisitsPerYear() != null && yearlyVisits != null &&
            yearlyVisits >= limits.getMaxVisitsPerYear()) {
            return true;
        }
        if (limits.getMaxSpendingPerYear() != null && yearlySpending != null &&
            yearlySpending.compareTo(limits.getMaxSpendingPerYear()) >= 0) {
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public boolean checkCategoryLimitExceeded(UUID clientId, UUID categoryId, CategoryLimits limits) {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        int month = now.getMonthValue();

        // Check monthly limits
        Integer monthlyVisits = clientServiceUsageRepository
                .getCategoryUsageCount(clientId, categoryId, year, month);
        BigDecimal monthlySpending = clientServiceUsageRepository
                .getCategorySpending(clientId, categoryId, year, month);

        if (limits.getMaxVisitsPerMonth() != null && monthlyVisits != null &&
            monthlyVisits >= limits.getMaxVisitsPerMonth()) {
            return true;
        }
        if (limits.getMaxSpendingPerMonth() != null && monthlySpending != null &&
            monthlySpending.compareTo(limits.getMaxSpendingPerMonth()) >= 0) {
            return true;
        }

        // Check yearly limits
        Integer yearlyVisits = clientServiceUsageRepository
                .getCategoryUsageCount(clientId, categoryId, year, null);
        BigDecimal yearlySpending = clientServiceUsageRepository
                .getCategorySpending(clientId, categoryId, year, null);

        if (limits.getMaxVisitsPerYear() != null && yearlyVisits != null &&
            yearlyVisits >= limits.getMaxVisitsPerYear()) {
            return true;
        }
        if (limits.getMaxSpendingPerYear() != null && yearlySpending != null &&
            yearlySpending.compareTo(limits.getMaxSpendingPerYear()) >= 0) {
            return true;
        }

        return false;
    }

    @Transactional(readOnly = true)
    public int getServiceUsageCount(UUID clientId, UUID serviceId, int year, Integer month) {
        Integer count = clientServiceUsageRepository.getServiceUsageCount(clientId, serviceId, year, month);
        return count != null ? count : 0;
    }

    private ClientUsageDTO toDTO(ClientUsage usage) {
        return ClientUsageDTO.builder()
                .id(usage.getId())
                .clientId(usage.getClient().getId())
                .clientName(usage.getClient().getFullName())
                .year(usage.getYear())
                .month(usage.getMonth())
                .totalVisits(usage.getTotalVisits())
                .totalSpending(usage.getTotalSpending())
                .lastUpdated(usage.getLastUpdated() != null ? usage.getLastUpdated().toString() : null)
                .build();
    }
}
