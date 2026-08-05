package com.dentalcloud.dentalcloudbackend.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "AftercareInstructions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AftercareInstruction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "appointment_id", nullable = false)
    private UUID appointmentId;

    @NotNull
    @Column(name = "patient_id", nullable = false)
    private UUID patientId;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @NotBlank
    @Size(max = 180)
    @Column(nullable = false, length = 180)
    private String title;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String body;

    @NotBlank
    @Builder.Default
    @Column(nullable = false, length = 16)
    private String priority = "NORMAL";

    @Column(name = "published_at")
    private Instant publishedAt;

    @NotNull
    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}
