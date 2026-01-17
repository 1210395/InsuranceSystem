package com.insurancesystem.Repository;

import com.insurancesystem.Model.Entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Long> {

    Optional<DoctorSpecialization> findByDisplayName(String displayName);

    List<DoctorSpecialization> findAllByOrderByDisplayNameAsc();
}
