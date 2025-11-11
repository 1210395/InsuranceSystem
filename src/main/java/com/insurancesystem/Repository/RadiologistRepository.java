package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.RadiologyRequest;
import com.insurancesystem.Model.Entity.Enums.LabRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RadiologistRepository extends JpaRepository<RadiologyRequest, UUID> {

    // ✅ جميع طلبات الأشعة لراديولوجي معين
    List<RadiologyRequest> findByRadiologistId(UUID radiologistId);

    // ✅ جميع طلبات الأشعة لطبيب معين
    List<RadiologyRequest> findByDoctorId(UUID doctorId);

    // ✅ جميع طلبات الأشعة لعضو معين (المريض)
    List<RadiologyRequest> findByMemberId(UUID memberId);

    // ✅ طلبات الأشعة المعلقة (بدون تخصيص راديولوجي محدد)
    List<RadiologyRequest> findByStatusAndRadiologistIsNull(LabRequestStatus status);

    // ✅ عدد طلبات الأشعة المعلقة لراديولوجي معين
    long countByStatusAndRadiologistId(LabRequestStatus status, UUID radiologistId);

    // ✅ جميع طلبات الأشعة بحالة معينة
    List<RadiologyRequest> findByStatus(LabRequestStatus status);

}
