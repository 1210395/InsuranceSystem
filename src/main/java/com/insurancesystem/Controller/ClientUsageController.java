package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientUsageDTO;
import com.insurancesystem.Services.ClientUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/usage")
@RequiredArgsConstructor
public class ClientUsageController {

    private final ClientUsageService usageService;

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<ClientUsageDTO> getClientUsage(
            @PathVariable UUID clientId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        return ResponseEntity.ok(usageService.getClientUsage(clientId, year, month));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @GetMapping("/client/{clientId}/yearly")
    public ResponseEntity<ClientUsageDTO> getClientYearlyUsage(
            @PathVariable UUID clientId,
            @RequestParam(required = false) Integer year) {

        if (year == null) year = LocalDate.now().getYear();

        return ResponseEntity.ok(usageService.getClientYearlyUsage(clientId, year));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @GetMapping("/client/{clientId}/history")
    public ResponseEntity<List<ClientUsageDTO>> getClientUsageHistory(@PathVariable UUID clientId) {
        return ResponseEntity.ok(usageService.getClientUsageHistory(clientId));
    }

    @PreAuthorize("hasAnyRole('INSURANCE_MANAGER', 'MEDICAL_ADMIN')")
    @GetMapping("/client/{clientId}/check")
    public ResponseEntity<Map<String, Object>> checkClientLimitStatus(
            @PathVariable UUID clientId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        ClientUsageDTO usage = usageService.getClientUsage(clientId, year, month);
        ClientUsageDTO yearlyUsage = usageService.getClientYearlyUsage(clientId, year);

        return ResponseEntity.ok(Map.of(
            "clientId", clientId,
            "monthlyUsage", usage,
            "yearlyUsage", yearlyUsage
        ));
    }
}
