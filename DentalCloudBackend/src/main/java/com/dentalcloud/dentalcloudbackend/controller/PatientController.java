package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarContactoEmergenciaRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ActualizarPerfilRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ContactoEmergenciaDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> obtenerPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(patientService.obtenerPerfil(userDetails.getUsername()));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> actualizarPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ActualizarPerfilRequestDTO request) {
        return ResponseEntity.ok(patientService.actualizarPerfil(userDetails.getUsername(), request));
    }

    @GetMapping("/emergency-contact")
    public ResponseEntity<ContactoEmergenciaDTO> obtenerContactoEmergencia(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(patientService.obtenerContactoEmergencia(userDetails.getUsername()));
    }

    @PutMapping("/emergency-contact")
    public ResponseEntity<Void> actualizarContactoEmergencia(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ActualizarContactoEmergenciaRequestDTO request) {
        patientService.actualizarContactoEmergencia(userDetails.getUsername(), request);
        return ResponseEntity.noContent().build();
    }


}