package com.insurancesystem.Controller;

import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Services.MedicalAdminServices;
import com.insurancesystem.Services.MedicalAdminServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-admin")
@RequiredArgsConstructor
public class MedicalAdminController {

    private final MedicalAdminServices medicalAdminService;



    // ✅ تعطيل / تفعيل حساب مستخدم
    @PatchMapping("/toggle-status/{id}")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<String> toggleUserStatus(@PathVariable UUID id) {
        medicalAdminService.toggleUserStatus(id);
        return ResponseEntity.ok("✅ User status updated successfully");
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('MEDICAL_ADMIN')")
    public ResponseEntity<Map<String, Object>> getFullDashboardStats() {
        return ResponseEntity.ok(medicalAdminService.getFullDashboardStats());
    }




}
