package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.ProvidersReportDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Enums.RoleRequestStatus;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProvidersReportService {

    private final ClientRepository clientRepo;

    /**
     * Check if client has the specified role (from roles collection or requestedRole if approved)
     */
    private boolean hasRole(Client c, RoleName roleName) {
        // Check roles collection first
        if (c.getRoles() != null && c.getRoles().stream().anyMatch(r -> r.getName() == roleName)) {
            return true;
        }
        // Fallback: check requestedRole if approved
        return c.getRequestedRole() == roleName && c.getRoleRequestStatus() == RoleRequestStatus.APPROVED;
    }

    public ProvidersReportDto generateReport() {
        List<Client> clients = clientRepo.findAll();

        // Doctors
        List<String> doctors = clients.stream()
                .filter(c -> hasRole(c, RoleName.DOCTOR))
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // Pharmacists
        List<String> pharmacies = clients.stream()
                .filter(c -> hasRole(c, RoleName.PHARMACIST))
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // Lab Techs
        List<String> labs = clients.stream()
                .filter(c -> hasRole(c, RoleName.LAB_TECH))
                .map(Client::getFullName)
                .collect(Collectors.toList());

        // Radiologists
        List<String> radiologists = clients.stream()
                .filter(c -> hasRole(c, RoleName.RADIOLOGIST))
                .map(Client::getFullName)
                .collect(Collectors.toList());

        return ProvidersReportDto.builder()
                .totalProviders(doctors.size() + pharmacies.size() + labs.size() + radiologists.size())
                .doctorsCount(doctors.size())
                .pharmaciesCount(pharmacies.size())
                .labsCount(labs.size())
                .radiologistsCount(radiologists.size())
                .doctors(doctors)
                .pharmacies(pharmacies)
                .labs(labs)
                .radiologists(radiologists)
                .build();
    }
}
