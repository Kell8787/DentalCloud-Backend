package com.dentalcloud.dentalcloudbackend.domain.entity;

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
public class Cita {
    @Id
    @GeneratedValue( strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(nullable = false)
    private String patientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detista_id",nullable = false)
    private Dentist dentist;

    @NotNull
    @FutureOrPresent
    @Column(nullable = false)
    private String appointmentDate; //Fecha de la cita

    @NotNull
    @Column(nullable = false)
    private LocalDateTime horaDesde;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime horaHasta;

    @NotBlank
    @Email
    @Column(nullable = false)
    private String patientEmail; // Correo

}
