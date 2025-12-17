package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.MedicalRecordDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.MedicalRecord;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.MedicalRecordMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.LabRequestRepository;
import com.insurancesystem.Repository.MedicalRecordRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordService {

    private final MedicalRecordRepository recordRepo;
    private final ClientRepository clientRepo;
    private final MedicalRecordMapper medicalRecordMapper;
    private final PrescriptionRepository prrepo;
    private final LabRequestRepository labrepo;
    private final ClientMapper clientMapper;


    // ➕ إنشاء سجل جديد (Doctor فقط)
    public MedicalRecordDTO createRecord(MedicalRecordDTO dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));


        Client member;
        if (dto.getMemberId() != null) {
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> new NotFoundException("Member not found"));
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> new NotFoundException("MEMBER_NOT_FOUND"));
        } else {
            throw new IllegalArgumentException("You must provide either memberId or memberName");
        }

        MedicalRecord record = medicalRecordMapper.toEntity(dto);
        record.setDoctor(doctor);
        record.setMember(member);
        record.setCreatedAt(Instant.now());
        record.setUpdatedAt(Instant.now());

        return medicalRecordMapper.toDto(recordRepo.save(record));
    }

    // 📖 جلب كل السجلات (Manager أو Admin فقط)
    public List<MedicalRecordDTO> getAll() {
        return recordRepo.findAll()
                .stream().map(medicalRecordMapper::toDto).collect(Collectors.toList());
    }

    // 📖 جلب سجلات الدكتور الحالي فقط
    public List<MedicalRecordDTO> getByCurrentDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByEmail(currentUsername.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));


        return recordRepo.findByDoctorId(doctor.getId())
                .stream().map(medicalRecordMapper::toDto).collect(Collectors.toList());
    }

    // 📖 جلب سجلات عضو محدد
    public List<MedicalRecordDTO> getByMember(UUID memberId) {
        Client member = clientRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        // 👤 إذا كان المستخدم الحالي Client لازم نتأكد انه يجيب سجلاته فقط
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_INSURANCE_CLIENT"))) {
            if (!member.getEmail().equalsIgnoreCase(currentUsername)) {
                throw new SecurityException("You can only view your own records!");
            }

        }

        return recordRepo.findByMemberId(memberId)
                .stream().map(medicalRecordMapper::toDto).collect(Collectors.toList());
    }

    // 📖 جلب سجل واحد
    public MedicalRecordDTO getById(UUID id) {
        MedicalRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        // 👤 إذا كان Client لازم يكون هو صاحب السجل
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth.getAuthorities().stream().anyMatch(r -> r.getAuthority().equals("ROLE_INSURANCE_CLIENT"))) {
            String currentUsername = auth.getName();
            if (!record.getMember().getEmail().equalsIgnoreCase(currentUsername)) {
                throw new SecurityException("You can only view your own record!");
            }

        }

        return medicalRecordMapper.toDto(record);
    }

    // ✏️ تحديث سجل (Doctor فقط)
    public MedicalRecordDTO updateRecord(UUID id, MedicalRecordDTO dto) {
        MedicalRecord record = recordRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medical record not found"));

        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());
        record.setUpdatedAt(Instant.now());

        return medicalRecordMapper.toDto(recordRepo.save(record));
    }

    // ❌ حذف سجل (Doctor فقط)
    public void deleteRecord(UUID id) {
        if (!recordRepo.existsById(id)) {
            throw new NotFoundException("Medical record not found");
        }
        recordRepo.deleteById(id);
    }

    public ClientDto updateProfile(String username, UpdateUserDTO dto, MultipartFile[] universityCard){
        Client client = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("User not found"));


        if (dto.getFullName() != null) client.setFullName(dto.getFullName());
        if (dto.getEmail() != null) client.setEmail(dto.getEmail());
        if (dto.getPhone() != null) client.setPhone(dto.getPhone());

        if (universityCard != null && universityCard.length > 0) {
            try {
                String uploadDir = "uploads/university-cards";
                Files.createDirectories(Paths.get(uploadDir));

                if (client.getUniversityCardImages() == null) {
                    client.setUniversityCardImages(new java.util.ArrayList<>());
                }

                for (MultipartFile file : universityCard) {
                    if (file == null || file.isEmpty()) continue;

                    String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, fileName);

                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    client.getUniversityCardImages().add("/" + uploadDir + "/" + fileName);
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to upload university card", e);
            }
        }


        clientRepo.save(client);

        return clientMapper.toDTO(client);


    }

    public Map<String, Long> getDoctorStats(String username) {
        Client doctor = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Doctor not found"));


        long prescriptionsCount = prrepo.countByDoctorId(doctor.getId());
        long labRequestsCount = labrepo.countByDoctorId(doctor.getId());
        long medicalRecordsCount = recordRepo.countByDoctorId(doctor.getId());

        long total = prescriptionsCount + labRequestsCount + medicalRecordsCount;

        Map<String, Long> stats = new HashMap<>();
        stats.put("prescriptions", prescriptionsCount);
        stats.put("labRequests", labRequestsCount);
        stats.put("medicalRecords", medicalRecordsCount);
        stats.put("total", total);

        return stats;
    }

}

