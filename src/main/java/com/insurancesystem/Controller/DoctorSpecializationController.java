package com.insurancesystem.Controller;

import com.insurancesystem.Model.Entity.DoctorSpecialization;
import com.insurancesystem.Repository.DoctorSpecializationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/doctor-specializations")
@RequiredArgsConstructor
public class DoctorSpecializationController {

    private final DoctorSpecializationRepository specializationRepository;

    /**
     * Get all specializations (public endpoint for registration)
     */
    @GetMapping
    public ResponseEntity<List<DoctorSpecialization>> getAllSpecializations() {
        List<DoctorSpecialization> specializations = specializationRepository.findAllByOrderByDisplayNameAsc();
        return ResponseEntity.ok(specializations);
    }

    /**
     * Get all specializations with details (for doctor dashboard)
     */
    @GetMapping("/with-details")
    public ResponseEntity<List<DoctorSpecialization>> getSpecializationsWithDetails() {
        List<DoctorSpecialization> specializations = specializationRepository.findAllByOrderByDisplayNameAsc();
        return ResponseEntity.ok(specializations);
    }

    /**
     * Get specialization by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<DoctorSpecialization> getSpecializationById(@PathVariable Long id) {
        return specializationRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all specializations for manager
     */
    @GetMapping("/manager/all")
    public ResponseEntity<List<DoctorSpecialization>> getAllForManager() {
        List<DoctorSpecialization> specializations = specializationRepository.findAll();
        return ResponseEntity.ok(specializations);
    }
}
