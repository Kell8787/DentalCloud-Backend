package com.dentalcloud.dentalcloudbackend.domain.entity;

import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Citas")
public class Citas {
    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detista_id",nullable = false)
    private Dentist dentist;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tratamiento_id", nullable = false)
    private Tratamiento tratamiento;

    @NotNull
    @Column(nullable = false)
    private String fechaCita; //Fecha de la cita

    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String motivo;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime hora; // El inicio de la cita

    @NotNull
    @Column(nullable = false)
    private LocalDateTime horaFin; // El fin de la cita

    @Column(columnDefinition = "TEXT")
    private String motivoCancelacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCita estadoCita;
}
