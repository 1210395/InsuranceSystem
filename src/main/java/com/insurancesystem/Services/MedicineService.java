package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.MedicineDTO;
import com.insurancesystem.Model.Entity.Medicine;
import com.insurancesystem.Model.MapStruct.MedicineMapper;
import com.insurancesystem.Repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicineService {

    private final MedicineRepository medicineRepo;
    private final MedicineMapper medicineMapper;

    // ➕ إنشاء دواء جديد (Admin/Manager)
    public MedicineDTO create(MedicineDTO dto) {
        Medicine medicine = medicineMapper.toEntity(dto);
        medicine.setCreatedAt(Instant.now());
        medicine.setUpdatedAt(Instant.now());

        Medicine saved = medicineRepo.save(medicine);
        return medicineMapper.toDto(saved);
    }

    // 🔍 بحث بالاسم أو المصطلح العلمي
    public List<MedicineDTO> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }

        return medicineRepo.searchByNameOrScientific(query)
                .stream()
                .map(medicineMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 جميع الأدوية
    public List<MedicineDTO> getAll() {
        return medicineRepo.findAll()
                .stream()
                .map(medicineMapper::toDto)
                .collect(Collectors.toList());
    }

    // 📖 دواء واحد بالـ ID
    public MedicineDTO getById(UUID id) {
        Medicine medicine = medicineRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));
        return medicineMapper.toDto(medicine);
    }

    // ✏️ تعديل دواء
    public MedicineDTO update(UUID id, MedicineDTO dto) {
        Medicine medicine = medicineRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));

        medicine.setName(dto.getName());
        medicine.setScientificName(dto.getScientificName());
        medicine.setQuantity(dto.getQuantity());
        medicine.setUnionPrice(dto.getUnionPrice());
        medicine.setDescription(dto.getDescription());
        medicine.setUpdatedAt(Instant.now());

        Medicine updated = medicineRepo.save(medicine);
        return medicineMapper.toDto(updated);
    }

    // ❌ حذف دواء
    public void delete(UUID id) {
        Medicine medicine = medicineRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Medicine not found"));
        medicineRepo.delete(medicine);
    }
}