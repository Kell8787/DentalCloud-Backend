package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
    List<ClinicalNote> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<ClinicalNote> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
