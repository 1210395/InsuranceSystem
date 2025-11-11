package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.ClientDto;
import com.insurancesystem.Model.Dto.PrescriptionDTO;
import com.insurancesystem.Model.Dto.PrescriptionItemDTO;
import com.insurancesystem.Model.Dto.UpdateUserDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Medicine;
import com.insurancesystem.Model.Entity.Enums.PrescriptionStatus;
import com.insurancesystem.Model.Entity.Enums.RoleName;
import com.insurancesystem.Model.Entity.Prescription;
import com.insurancesystem.Model.Entity.PrescriptionItem;
import com.insurancesystem.Model.MapStruct.ClientMapper;
import com.insurancesystem.Model.MapStruct.PrescriptionMapper;
import com.insurancesystem.Model.MapStruct.PrescriptionItemMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.MedicineRepository;
import com.insurancesystem.Repository.PrescriptionRepository;
import com.insurancesystem.Repository.PrescriptionItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepo;
    private final PrescriptionItemRepository prescriptionItemRepo;
    private final ClientRepository clientRepo;
    private final MedicineRepository medicineRepo;
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ClientMapper clientMapper;
    private final NotificationService notificationService;

    // ➕ Doctor ينشئ وصفة
    @Transactional
    public PrescriptionDTO create(PrescriptionDTO dto) {
        log.info("🔹 Starting prescription creation...");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        log.info("🔹 Current user: {}", currentUsername);

        // 🧑‍⚕️ الدكتور
        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> {
                    log.error("❌ Doctor not found: {}", currentUsername);
                    return new NotFoundException("DOCTOR_NOT_FOUND");
                });
        log.info("✅ Doctor found: {}", doctor.getFullName());

        // 👤 المريض
        Client member;
        if (dto.getMemberId() != null) {
            log.info("🔹 Searching member by ID: {}", dto.getMemberId());
            member = clientRepo.findById(dto.getMemberId())
                    .orElseThrow(() -> {
                        log.error("❌ Member not found by ID: {}", dto.getMemberId());
                        return new NotFoundException("MEMBER_NOT_FOUND");
                    });
        } else if (dto.getMemberName() != null && !dto.getMemberName().isBlank()) {
            log.info("🔹 Searching member by name: {}", dto.getMemberName());
            member = clientRepo.findByFullName(dto.getMemberName())
                    .orElseThrow(() -> {
                        log.error("❌ Member not found by name: {}", dto.getMemberName());
                        return new NotFoundException("MEMBER_NOT_FOUND");
                    });
        } else {
            log.error("❌ No member info provided");
            throw new IllegalArgumentException("MEMBER_INFO_REQUIRED");
        }
        log.info("✅ Member found: {}", member.getFullName());

        // ✅ التحقق من صحة الوصفة (عدم تكرار الأدوية)
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            log.error("❌ No medicines in prescription");
            throw new IllegalArgumentException("PRESCRIPTION_MUST_HAVE_MEDICINES");
        }
        log.info("✅ Prescription has {} medicines", dto.getItems().size());

        // 🔍 التحقق: هل المريض عنده دواء من نفس الأدوية (PENDING أو VERIFIED)؟
        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            log.info("🔹 Checking medicine: {}", itemDto.getMedicineId());

            Medicine med = medicineRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> {
                        log.error("❌ Medicine not found: {}", itemDto.getMedicineId());
                        return new NotFoundException("MEDICINE_NOT_FOUND");
                    });
            log.info("✅ Medicine found: {}", med.getName());

            // البحث عن PENDING prescriptions
            List<Prescription> pendingPrescriptions = prescriptionRepo.findByMemberIdAndStatus(
                    member.getId(),
                    PrescriptionStatus.PENDING
            );
            log.info("🔹 Found {} pending prescriptions for member", pendingPrescriptions.size());

            // ✋ التحقق من PENDING (ممنوع فوري)
            for (Prescription prescription : pendingPrescriptions) {
                for (PrescriptionItem item : prescription.getItems()) {
                    if (item.getMedicine().getId().equals(itemDto.getMedicineId())) {
                        log.warn("⚠️ PENDING prescription exists for medicine: {}", med.getName());
                        throw new IllegalArgumentException(
                                "PENDING_PRESCRIPTION_EXISTS|" + med.getName()
                        );
                    }
                }
            }

            // البحث عن VERIFIED prescriptions
            List<Prescription> verifiedPrescriptions = prescriptionRepo.findByMemberIdAndStatus(
                    member.getId(),
                    PrescriptionStatus.VERIFIED
            );
            log.info("🔹 Found {} verified prescriptions for member", verifiedPrescriptions.size());

            // ⏳ التحقق من VERIFIED (ممنوع إذا لم تنتهِ الجرعة)
            for (Prescription prescription : verifiedPrescriptions) {
                for (PrescriptionItem item : prescription.getItems()) {
                    if (item.getMedicine().getId().equals(itemDto.getMedicineId())) {
                        // تحقق من أن الوصفة لم تنتهِ بعد
                        if (item.getExpiryDate() != null && item.getExpiryDate().isAfter(Instant.now())) {
                            log.warn("⚠️ ACTIVE prescription exists for medicine: {}", med.getName());
                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm");
                            String expiryDateStr = sdf.format(java.util.Date.from(item.getExpiryDate()));

                            throw new IllegalArgumentException(
                                    "ACTIVE_PRESCRIPTION_EXISTS|" + med.getName() + "|" + expiryDateStr
                            );
                        }
                    }
                }
            }
        }

        log.info("✅ All medicines validated successfully");

        // 📝 بناء الوصفة
        Prescription prescription = Prescription.builder()
                .doctor(doctor)
                .member(member)
                .status(PrescriptionStatus.PENDING)
                .notes(dto.getNotes())
                .totalPrice(0.0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        Prescription savedPrescription = prescriptionRepo.save(prescription);
        log.info("✅ Prescription saved with ID: {}", savedPrescription.getId());

        // 💊 بناء الأدوية في الوصفة
        List<PrescriptionItem> items = new ArrayList<>();
        for (PrescriptionItemDTO itemDto : dto.getItems()) {
            Medicine medicine = medicineRepo.findById(itemDto.getMedicineId())
                    .orElseThrow(() -> new NotFoundException("MEDICINE_NOT_FOUND"));

            // 📅 حساب تاريخ انتهاء الدواء
            int dailyConsumption = itemDto.getDosage() * itemDto.getTimesPerDay();
            int daysOfSupply = medicine.getQuantity() / dailyConsumption;
            Instant expiryDate = Instant.now().plus(daysOfSupply, ChronoUnit.DAYS);

            PrescriptionItem item = PrescriptionItem.builder()
                    .prescription(savedPrescription)
                    .medicine(medicine)
                    .dosage(itemDto.getDosage())
                    .timesPerDay(itemDto.getTimesPerDay())
                    .expiryDate(expiryDate)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            items.add(prescriptionItemRepo.save(item));
        }

        savedPrescription.setItems(items);
        log.info("✅ Added {} items to prescription", items.size());

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                member.getId(),
                "تم إنشاء وصفة طبية جديدة لك من الدكتور " + doctor.getFullName()
        );

        // 🔔 إشعار لجميع الصيادلة
        List<Client> pharmacists = clientRepo.findByRoles_Name(RoleName.PHARMACIST);
        for (Client pharmacist : pharmacists) {
            notificationService.sendToUser(
                    pharmacist.getId(),
                    "وصفة طبية جديدة متاحة من الدكتور " + doctor.getFullName() +
                            " للمريض " + member.getFullName()
            );
        }
        log.info("✅ Notifications sent to {} pharmacists", pharmacists.size());

        log.info("✅ Prescription created successfully!");
        return prescriptionMapper.toDto(savedPrescription);
    }

    // 📖 Member يشوف وصفاته
    public List<PrescriptionDTO> getMyPrescriptions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();
        Client member = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Member not found"));

        return prescriptionRepo.findByMemberId(member.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 Pharmacist يشوف الوصفات المعلقة
    public List<PrescriptionDTO> getPending() {
        return prescriptionRepo.findByStatus(PrescriptionStatus.PENDING)
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ✅ Pharmacist يوافق على وصفة (مع إدخال الأسعار)
    @Transactional
    public PrescriptionDTO verify(UUID id, List<PrescriptionItemDTO> itemsWithPrices) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.VERIFIED);
        prescription.setPharmacist(pharmacist);
        prescription.setUpdatedAt(Instant.now());

        // 💰 تحديث الأسعار لكل دواء
        double totalPrice = 0.0;
        for (PrescriptionItemDTO itemDto : itemsWithPrices) {
            PrescriptionItem item = prescriptionItemRepo.findById(itemDto.getId())
                    .orElseThrow(() -> new NotFoundException("Prescription item not found"));

            Double pharmacistPrice = itemDto.getPharmacistPrice();
            Double unionPrice = item.getMedicine().getUnionPrice();

            // ✅ السعر النهائي = الأقل بين سعر الصيدلي وسعر النقابة
            Double finalPrice = Math.min(pharmacistPrice, unionPrice);

            item.setPharmacistPrice(pharmacistPrice);
            item.setFinalPrice(finalPrice);
            item.setUpdatedAt(Instant.now());

            prescriptionItemRepo.save(item);

            totalPrice += finalPrice;
        }

        prescription.setTotalPrice(totalPrice);

        Prescription saved = prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                saved.getMember().getId(),
                "تمت الموافقة على وصفتك الطبية من الصيدلية. المجموع: " + totalPrice + " دينار"
        );

        return prescriptionMapper.toDto(saved);
    }

    // ❌ Pharmacist يرفض وصفة
    @Transactional
    public PrescriptionDTO reject(UUID id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        prescription.setStatus(PrescriptionStatus.REJECTED);
        prescription.setPharmacist(pharmacist);
        prescription.setUpdatedAt(Instant.now());

        Prescription saved = prescriptionRepo.save(prescription);

        // 🔔 إشعار للمريض
        notificationService.sendToUser(
                saved.getMember().getId(),
                "تم رفض وصفتك الطبية."
        );

        return prescriptionMapper.toDto(saved);
    }

    // ✏️ Doctor يعدل وصفة
    @Transactional
    public PrescriptionDTO update(UUID id, PrescriptionDTO dto) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        // يمكن التعديل فقط إذا كانت PENDING
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("Cannot update prescription that is not PENDING");
        }

        prescription.setNotes(dto.getNotes());
        prescription.setUpdatedAt(Instant.now());

        // تحديث الأدوية إذا لزم الأمر
        if (dto.getItems() != null && !dto.getItems().isEmpty()) {
            // حذف الأدوية القديمة
            prescriptionItemRepo.deleteAll(prescription.getItems());
            prescription.getItems().clear();

            // إضافة الأدوية الجديدة
            for (PrescriptionItemDTO itemDto : dto.getItems()) {
                Medicine medicine = medicineRepo.findById(itemDto.getMedicineId())
                        .orElseThrow(() -> new NotFoundException("Medicine not found"));

                int dailyConsumption = itemDto.getDosage() * itemDto.getTimesPerDay();
                int daysOfSupply = medicine.getQuantity() / dailyConsumption;
                Instant expiryDate = Instant.now().plus(daysOfSupply, ChronoUnit.DAYS);

                PrescriptionItem item = PrescriptionItem.builder()
                        .prescription(prescription)
                        .medicine(medicine)
                        .dosage(itemDto.getDosage())
                        .timesPerDay(itemDto.getTimesPerDay())
                        .expiryDate(expiryDate)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build();

                prescription.getItems().add(prescriptionItemRepo.save(item));
            }
        }

        return prescriptionMapper.toDto(prescriptionRepo.save(prescription));
    }

    // ❌ Doctor يحذف وصفة
    public void delete(UUID id) {
        Prescription prescription = prescriptionRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Prescription not found"));

        // يمكن الحذف فقط إذا كانت PENDING
        if (prescription.getStatus() != PrescriptionStatus.PENDING) {
            throw new IllegalStateException("Cannot delete prescription that is not PENDING");
        }

        prescriptionRepo.delete(prescription);
    }

    // 📊 Doctor stats
    public PrescriptionDTO getDoctorStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return PrescriptionDTO.builder()
                .total(prescriptionRepo.countByDoctorId(doctor.getId()))
                .pending(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByDoctorIdAndStatus(doctor.getId(), PrescriptionStatus.REJECTED))
                .build();
    }

    // 📊 Pharmacist stats
    public PrescriptionDTO getPharmacistStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        return PrescriptionDTO.builder()
                .pending(prescriptionRepo.countByStatus(PrescriptionStatus.PENDING))
                .verified(prescriptionRepo.countByPharmacistIdAndStatus(
                        pharmacist.getId(), PrescriptionStatus.VERIFIED))
                .rejected(prescriptionRepo.countByPharmacistIdAndStatus(
                        pharmacist.getId(), PrescriptionStatus.REJECTED))
                .total(
                        prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.PENDING)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.VERIFIED)
                                + prescriptionRepo.countByPharmacistIdAndStatus(pharmacist.getId(), PrescriptionStatus.REJECTED)
                )
                .build();
    }

    // 👤 Pharmacist يحدّث بروفايله
    public ClientDto updatePharmacistProfile(String username, UpdateUserDTO dto, MultipartFile universityCard) {
        Client pharmacist = clientRepo.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            pharmacist.setFullName(dto.getFullName());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            pharmacist.setEmail(dto.getEmail());
        }

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            pharmacist.setPhone(dto.getPhone());
        }

        // ✅ تحديث الصورة
        if (universityCard != null && !universityCard.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + universityCard.getOriginalFilename();
                Path uploadPath = Paths.get("uploads/pharmacists");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(universityCard.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                pharmacist.setUniversityCardImage("/uploads/pharmacists/" + fileName);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save pharmacist image", e);
            }
        }

        pharmacist.setUpdatedAt(Instant.now());

        Client updated = clientRepo.save(pharmacist);

        return clientMapper.toDTO(updated);
    }

    // 📖 Doctor يشوف وصفاته
    public List<PrescriptionDTO> getByDoctor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client doctor = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));

        return prescriptionRepo.findByDoctorId(doctor.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 جميع الوصفات (عام)
    public List<PrescriptionDTO> getAll() {
        return prescriptionRepo.findAll()
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 الصيدلي يشوف كل الوصفات الخاصة فيه
    public List<PrescriptionDTO> getAllForCurrentPharmacist() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = auth.getName();

        Client pharmacist = clientRepo.findByUsername(currentUsername)
                .orElseThrow(() -> new NotFoundException("Pharmacist not found"));

        return prescriptionRepo.findByPharmacistId(pharmacist.getId())
                .stream()
                .map(prescriptionMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 إرجاع كل الصيادلة
    public List<ClientDto> getAllPharmacists() {
        return clientRepo.findByRoles_Name(RoleName.PHARMACIST)
                .stream()
                .map(clientMapper::toDTO)
                .collect(Collectors.toList());
    }

    // ✅ التحقق من الوصفات النشطة (PENDING و VERIFIED) + تاريخ انتهاء الدواء الحالي
    public Map<String, Object> checkActivePrescription(String memberName, UUID medicineId) {
        Map<String, Object> response = new HashMap<>();
        response.put("active", false);

        try {
            // البحث عن المريض
            Client member = clientRepo.findByFullName(memberName)
                    .orElse(null);

            if (member == null) {
                return response;
            }

            // البحث عن PENDING prescriptions
            List<Prescription> pendingPrescriptions = prescriptionRepo.findByMemberIdAndStatus(
                    member.getId(),
                    PrescriptionStatus.PENDING
            );

            // ✋ التحقق من PENDING (ممنوع فوري)
            for (Prescription p : pendingPrescriptions) {
                for (PrescriptionItem item : p.getItems()) {
                    if (item.getMedicine().getId().equals(medicineId)) {
                        response.put("active", true);
                        response.put("medicineName", item.getMedicine().getName());
                        response.put("status", "PENDING");
                        response.put("reason", "pending");
                        return response;
                    }
                }
            }

            // البحث عن VERIFIED prescriptions
            List<Prescription> verifiedPrescriptions = prescriptionRepo.findByMemberIdAndStatus(
                    member.getId(),
                    PrescriptionStatus.VERIFIED
            );

            // ⏳ التحقق من VERIFIED (ممنوع إذا لم تنتهِ الجرعة)
            for (Prescription p : verifiedPrescriptions) {
                for (PrescriptionItem item : p.getItems()) {
                    if (item.getMedicine().getId().equals(medicineId)) {
                        // تحقق من أن الوصفة لم تنتهِ بعد
                        if (item.getExpiryDate() != null && item.getExpiryDate().isAfter(Instant.now())) {
                            response.put("active", true);
                            response.put("medicineName", item.getMedicine().getName());
                            response.put("expiryDate", item.getExpiryDate());
                            response.put("status", "VERIFIED");
                            response.put("reason", "expiry");
                            return response;
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }
}
