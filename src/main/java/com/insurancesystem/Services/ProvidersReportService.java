package com.insurancesystem.Services;

import com.insurancesystem.Model.Dto.ProvidersReportDto;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvidersReportService {

    private final ClientRepository clientRepo;

    public ProvidersReportDto generateReport() {
        List<String> doctors = clientRepo.findByRoleOrRequestedRole(RoleName.DOCTOR).stream()
                .map(Client::getFullName).toList();

        List<String> pharmacies = clientRepo.findByRoleOrRequestedRole(RoleName.PHARMACIST).stream()
                .map(Client::getFullName).toList();

        List<String> labs = clientRepo.findByRoleOrRequestedRole(RoleName.LAB_TECH).stream()
                .map(Client::getFullName).toList();

        List<String> radiologists = clientRepo.findByRoleOrRequestedRole(RoleName.RADIOLOGIST).stream()
                .map(Client::getFullName).toList();

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
