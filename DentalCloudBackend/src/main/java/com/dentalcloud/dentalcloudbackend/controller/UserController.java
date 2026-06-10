package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.CambiarRolRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.DoctorResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UserResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/doctors")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DoctorResponseDTO>> obtenerDoctores() {
        return ResponseEntity.ok(userService.obtenerDoctores());
    }

    @PatchMapping("/{id}/rol")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<UserResponseDTO> cambiarRol(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarRolRequestDTO request) {
        return ResponseEntity.ok(userService.cambiarRol(id, request));
    }
}
