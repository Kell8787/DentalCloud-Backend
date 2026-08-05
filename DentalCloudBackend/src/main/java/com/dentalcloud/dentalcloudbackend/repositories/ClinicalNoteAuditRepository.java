package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicalNoteAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalNoteAuditRepository extends JpaRepository<ClinicalNoteAudit, UUID> {
    List<ClinicalNoteAudit> findByNoteIdOrderByOccurredAtAsc(UUID noteId);
}
