package com.dentalcloud.dentalcloudbackend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_note_audit")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalNoteAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Column(nullable = false, length = 24)
    private String action;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(columnDefinition = "TEXT")
    private String detail;

    @Column(name = "occurred_at", nullable = false)
    @Builder.Default
    private Instant occurredAt = Instant.now();
}
