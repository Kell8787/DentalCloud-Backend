package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PatientTreatmentPlanRepository extends JpaRepository<PatientTreatmentPlan, UUID> {

    List<PatientTreatmentPlan> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<PatientTreatmentPlan> findByPatientIdAndStatusOrderByCreatedAtDesc(
            UUID patientId,
            TreatmentPlanStatus status
    );
}
