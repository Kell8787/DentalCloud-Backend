package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalDocumentRepository extends JpaRepository<ClinicalDocument, UUID> {

    List<ClinicalDocument> findByPatientIdAndVisibleToPatientTrueOrderByCreatedAtDesc(UUID patientId);

    List<ClinicalDocument> findByPlanIdOrderByCreatedAtDesc(UUID planId);

    List<ClinicalDocument> findByAppointmentIdOrderByCreatedAtDesc(UUID appointmentId);
}
