package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.AftercareInstruction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AftercareInstructionRepository extends JpaRepository<AftercareInstruction, UUID> {

    List<AftercareInstruction> findByPatientIdAndPublishedAtIsNotNullOrderByPublishedAtDesc(UUID patientId);

    List<AftercareInstruction> findByPatientIdOrderByCreatedAtDesc(UUID patientId);

    List<AftercareInstruction> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
