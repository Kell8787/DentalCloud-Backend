package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.PatientAdminCreateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientAdminUpdateRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserActivationRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/patients")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
public class PatientAdminController {
    private final PatientService patientService;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(patientService.buscarPacientes(search));
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create(@Valid @RequestBody PatientAdminCreateRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.crearPacienteAdministrativo(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody PatientAdminUpdateRequestDTO request) {
        return ResponseEntity.ok(patientService.actualizarPacienteAdministrativo(id, request));
    }

    @PatchMapping("/{id}/activation")
    public ResponseEntity<UserResponseDTO> activation(
            @PathVariable UUID id,
            @Valid @RequestBody UserActivationRequestDTO request) {
        return ResponseEntity.ok(patientService.activarPaciente(id, request));
    }
}
