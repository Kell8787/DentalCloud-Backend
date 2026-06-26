package com.dentalcloud.dentalcloudbackend.controller;
import com.dentalcloud.dentalcloudbackend.domain.dto.*;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import com.dentalcloud.dentalcloudbackend.services.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitasController {

    private final CitaService citasService;

    @PostMapping
    public ResponseEntity<CitaResponseDTO> crearCitas(@Valid @RequestBody CrearCitasRequestDTO citaRequest) {
        CitaResponseDTO citaResponse = citasService.crearCita(citaRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(citaResponse);
    }

    @GetMapping("/slots-disponibles")
    public ResponseEntity<List<SlotDisponibleDTO>> obtenerSlotsDisponibles(
            @RequestParam LocalDate fecha,
            @RequestParam UUID tratamientoId) {
        return ResponseEntity.ok(citasService.obtenerSlotsDisponibles(fecha, tratamientoId));
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> aprobarCita(@PathVariable UUID id) {
        return ResponseEntity.ok(citasService.aprobarCita(id));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> rechazarCita(
            @PathVariable UUID id,
            @RequestBody(required = false) RechazarCitasRequestDTO request) {
        if (request == null) request = new RechazarCitasRequestDTO();
        return ResponseEntity.ok(citasService.rechazarCita(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> cancelarCita(
            @PathVariable UUID id,
            @RequestBody(required = false) CancelarCitaRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (request == null) request = new CancelarCitaRequestDTO();
        return ResponseEntity.ok(citasService.cancelarCita(id, userDetails.getUsername(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> editarCita(
            @PathVariable UUID id,
            @RequestBody EditarCitaRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.editarCita(id, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> eliminarCita(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        citasService.eliminarCita(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CitaResponseDTO>> obtenerMisCitas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerMisCitas(userDetails.getUsername()));
    }

    @GetMapping("/mi-agenda")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<CitaResponseDTO>> obtenerMiAgenda(
            @RequestParam(required = false) LocalDate fecha,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerAgendaDoctor(userDetails.getUsername(), fecha));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<List<CitaResponseDTO>> obtenerTodasLasCitas(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) EstadoCita estado) {
        return ResponseEntity.ok(citasService.obtenerTodasLasCitas(fecha, estado));
    }
}
