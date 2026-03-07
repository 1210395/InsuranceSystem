package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    long countByDoctorId(UUID doctorId);

    List<MedicalRecord> findByMember_Id(UUID memberId);

    @Query("SELECT COUNT(DISTINCT r.member.id) FROM MedicalRecord r")
    long countDistinctMembers();

}
