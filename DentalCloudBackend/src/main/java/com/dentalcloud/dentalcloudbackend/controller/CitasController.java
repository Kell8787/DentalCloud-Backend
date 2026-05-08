package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CrearCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RechazarCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.SlotDisponibleDTO;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.services.CitaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<CitaResponseDTO> crearCitas(@RequestBody CrearCitasRequestDTO citaRequest) {
        CitaResponseDTO citaResponse = citasService.crearCita(citaRequest);
        return ResponseEntity.ok(citaResponse);
    }

    @GetMapping("/slots-disponibles")
    public ResponseEntity<List<SlotDisponibleDTO>> obtenerSlotsDisponibles(
            @RequestParam LocalDate fecha,
            @RequestParam UUID tratamientoId) {
        return ResponseEntity.ok(citasService.obtenerSlotsDisponibles(fecha, tratamientoId));
    }

    @PatchMapping("/{id}/aprobar")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> aprobarCita(@PathVariable UUID id) {
        return ResponseEntity.ok(citasService.aprobarCita(id));
    }

    @PatchMapping("/{id}/rechazar")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> rechazarCita(
            @PathVariable UUID id,
            @RequestBody(required = false) RechazarCitasRequestDTO request) {
        if (request == null) request = new RechazarCitasRequestDTO();
        return ResponseEntity.ok(citasService.rechazarCita(id, request));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<CitaResponseDTO> cancelarCita(@PathVariable UUID id) {
        return ResponseEntity.ok(citasService.cancelarCita(id));
    }

}
