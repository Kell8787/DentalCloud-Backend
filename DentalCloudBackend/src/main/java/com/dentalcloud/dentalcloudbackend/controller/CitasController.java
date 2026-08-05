package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentPageResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AppointmentStatusEventResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AvailabilitySlotDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CancelarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CrearCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.EditarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RechazarCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ReagendarCitaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.SlotDisponibleDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.StaffAppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.MotivoCancelacion;
import com.dentalcloud.dentalcloudbackend.services.CitaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitasController {

    private final CitaService citasService;

    @PostMapping("/solicitudes")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CitaResponseDTO> solicitarCita(
            @Valid @RequestBody AppointmentRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citasService.solicitarCita(request, userDetails.getUsername(), idempotencyKey));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> crearCita(
            @Valid @RequestBody StaffAppointmentRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(citasService.crearCitaStaff(request, idempotencyKey, userDetails.getUsername()));
    }

    /** Ruta histórica conservada para clientes internos durante la migración. */
    @PostMapping("/legacy")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> crearCitaLegacy(
            @Valid @RequestBody CrearCitasRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citasService.crearCita(request));
    }

    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SECRETARIA', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AvailabilitySlotDTO>> disponibilidad(
            @RequestParam LocalDate fecha,
            @RequestParam(required = false) UUID planId,
            @RequestParam(required = false) UUID tratamientoId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerDisponibilidadPorPlan(
                fecha, planId, tratamientoId, userDetails.getUsername()));
    }

    @GetMapping("/slots-disponibles")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SECRETARIA', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SlotDisponibleDTO>> slotsDisponibles(
            @RequestParam LocalDate fecha,
            @RequestParam UUID tratamientoId) {
        return ResponseEntity.ok(citasService.obtenerSlotsDisponibles(fecha, tratamientoId));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CitaResponseDTO>> misCitas(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerMisCitas(userDetails.getUsername()));
    }

    @GetMapping("/mis-citas")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CitaResponseDTO>> misCitasLegacy(
            @AuthenticationPrincipal UserDetails userDetails) {
        return misCitas(userDetails);
    }

    @GetMapping("/mi-agenda")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<CitaResponseDTO>> agendaDoctor(
            @RequestParam(required = false) LocalDate fecha,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerAgendaDoctor(userDetails.getUsername(), fecha));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<List<CitaResponseDTO>> listarCitas(
            @RequestParam(required = false) LocalDate fecha,
            @RequestParam(required = false) EstadoCita estado) {
        return ResponseEntity.ok(citasService.obtenerTodasLasCitas(fecha, estado));
    }

    @GetMapping("/paginadas")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<AppointmentPageResponseDTO> listarPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(name = "from", required = false) LocalDate from,
            @RequestParam(name = "to", required = false) LocalDate to,
            @RequestParam(name = "patientId", required = false) UUID patientId) {
        return ResponseEntity.ok(citasService.listarPaginado(page, size, status, from, to, patientId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> detalle(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerPorId(id, userDetails.getUsername()));
    }

    @GetMapping("/{id}/historial")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<List<AppointmentStatusEventResponseDTO>> historial(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.obtenerHistorial(id, userDetails.getUsername()));
    }

    @PatchMapping({"/{id}/aceptar", "/{id}/aprobar"})
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> aceptar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.aprobarCita(id, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> rechazar(
            @PathVariable UUID id,
            @Valid @RequestBody RechazarCitasRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.rechazarCita(id, request, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> cancelar(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarCitaRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.cancelarCita(id, userDetails.getUsername(), request));
    }

    @PatchMapping("/{id}/reagendar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> reagendar(
            @PathVariable UUID id,
            @Valid @RequestBody ReagendarCitaRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.reagendarCita(id, request, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/inasistencia")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> inasistencia(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.marcarInasistencia(id, userDetails.getUsername()));
    }

    @PatchMapping("/{id}/completar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> completar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.completarCita(id, userDetails.getUsername()));
    }

    @GetMapping("/motivos-cancelacion")
    public ResponseEntity<List<MotivoCancelacion>> motivosCancelacion() {
        return ResponseEntity.ok(List.of(MotivoCancelacion.values()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> editar(
            @PathVariable UUID id,
            @RequestBody EditarCitaRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(citasService.editarCita(id, userDetails.getUsername(), request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> eliminar(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        citasService.eliminarCita(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}
