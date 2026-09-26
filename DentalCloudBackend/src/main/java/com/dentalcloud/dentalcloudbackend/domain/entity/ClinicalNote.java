package com.dentalcloud.dentalcloudbackend.domain.entity;

import com.dentalcloud.dentalcloudbackend.domain.enums.ClinicalNoteStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "clinical_notes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalNote {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private ClinicalNoteStatus status = ClinicalNoteStatus.DRAFT;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "amended_from_id")
    private UUID amendedFromId;

    @Column(name = "amendment_reason", columnDefinition = "TEXT")
    private String amendmentReason;

    @Version
    @Column(nullable = false)
    private Long version;
}
