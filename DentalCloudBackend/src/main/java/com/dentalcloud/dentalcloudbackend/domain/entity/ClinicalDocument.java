package com.dentalcloud.dentalcloudbackend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ClinicalDocuments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @Column(name = "appointment_id")
    private UUID appointmentId;

    @Column(name = "plan_id")
    private UUID planId;

    @NotBlank
    @Size(max = 32)
    @Column(name = "document_type", nullable = false, length = 32)
    private String documentType;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @NotBlank
    @Size(max = 512)
    @Column(name = "object_key", nullable = false, unique = true, length = 512)
    private String objectKey;

    @NotBlank
    @Size(max = 128)
    @Column(name = "mime_type", nullable = false, length = 128)
    private String mimeType;

    @NotNull
    @Positive
    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @NotBlank
    @Size(max = 128)
    @Column(nullable = false, length = 128)
    private String checksum;

    @Builder.Default
    @Column(name = "visible_to_patient", nullable = false)
    private boolean visibleToPatient = false;

    @NotNull
    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @NotNull
    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
