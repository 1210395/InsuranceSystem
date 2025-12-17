package com.insurancesystem.Services;

import com.insurancesystem.Exception.NotFoundException;
import com.insurancesystem.Model.Dto.CreateFamilyMemberDTO;
import com.insurancesystem.Model.Dto.FamilyMemberDTO;
import com.insurancesystem.Model.Entity.Client;
import com.insurancesystem.Model.Entity.Enums.ProfileStatus;
import com.insurancesystem.Model.Entity.FamilyMember;
import com.insurancesystem.Model.MapStruct.FamilyMemberMapper;
import com.insurancesystem.Repository.ClientRepository;
import com.insurancesystem.Repository.FamilyMemberRepository;
import com.insurancesystem.Security.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyMemberService {

    private final FamilyMemberRepository familyRepo;
    private final ClientRepository clientRepo;
    private final FamilyMemberMapper familyMemberMapper;

    /* ===================== GET ===================== */

    @Transactional(readOnly = true)
    public List<FamilyMemberDTO> getFamilyForClient(String username) {
        Client client = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Client not found"));


        return familyRepo.findByClient_Id(client.getId())
                .stream()
                .map(familyMemberMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FamilyMemberDTO> getFamilyForClient(UUID clientId) {
        return familyRepo.findByClient_Id(clientId)
                .stream()
                .map(familyMemberMapper::toDto)
                .toList();
    }

    /* ===================== CREATE ===================== */

    public FamilyMemberDTO createFamilyMember(
            String username,
            CreateFamilyMemberDTO dto,
            MultipartFile[] documents
    ) {
        Client client = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Client not found"));


        if (familyRepo.existsByNationalId(dto.getNationalId())) {
            throw new IllegalStateException("National ID already exists");
        }

        long count = familyRepo.countByClient_Id(client.getId());

        if (client.getEmployeeId() == null) {
            throw new IllegalStateException("Client has no employee ID");
        }

        String insuranceNumber =
                client.getEmployeeId()
                        + "."
                        + String.format("%02d", count + 1);

        FamilyMember member = FamilyMember.builder()
                .client(client)
                .fullName(dto.getFullName())
                .nationalId(dto.getNationalId())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .relation(dto.getRelation())
                .insuranceNumber(insuranceNumber)
                .status(ProfileStatus.PENDING)
                .build();

        // ✅ حفظ أكثر من ملف
        if (documents != null && documents.length > 0) {
            for (MultipartFile file : documents) {
                if (file == null || file.isEmpty()) continue;

                String path = FileStorageUtil.save(file, "family");
                member.getDocumentImages().add(path);
            }
        }

        return familyMemberMapper.toDto(familyRepo.save(member));
    }

    /* ===================== UPDATE STATUS ===================== */

    public FamilyMemberDTO updateStatus(UUID memberId, ProfileStatus status) {
        FamilyMember member = familyRepo.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Family member not found"));

        member.setStatus(status);
        return familyMemberMapper.toDto(member);
    }

    /* ===================== DELETE ===================== */

    public void deleteFamilyMember(String username, UUID memberId) {
        Client client = clientRepo.findByEmail(username.toLowerCase())
                .orElseThrow(() -> new NotFoundException("Client not found"));


        familyRepo.deleteByIdAndClient_Id(memberId, client.getId());
    }
}
