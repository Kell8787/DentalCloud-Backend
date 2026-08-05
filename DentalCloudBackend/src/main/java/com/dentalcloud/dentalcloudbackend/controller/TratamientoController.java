package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoEstadoRequestDTO;
import com.dentalcloud.dentalcloudbackend.services.TratamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
@RequiredArgsConstructor
public class TratamientoController {

    private final TratamientoService tratamientoService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TratamientoResponseDTO>> listarTratamientos(
            @RequestParam(defaultValue = "false") boolean includeInactive,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (includeInactive && userDetails.getAuthorities().stream()
                .noneMatch(authority -> authority.getAuthority().matches("ROLE_(DOCTOR|SECRETARIA|ADMIN)"))) {
            throw new AccessDeniedException("Solo el personal puede consultar tratamientos inactivos");
        }
        return ResponseEntity.ok(tratamientoService.listarTratamientos(includeInactive));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<TratamientoResponseDTO> crearTratamiento(@Valid @RequestBody TratamientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tratamientoService.crearTratamiento(request));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('DOCTOR', 'SECRETARIA', 'ADMIN')")
    public ResponseEntity<TratamientoResponseDTO> cambiarEstado(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody TratamientoEstadoRequestDTO request) {
        return ResponseEntity.ok(tratamientoService.cambiarEstado(id, request.getActive()));
    }
}
