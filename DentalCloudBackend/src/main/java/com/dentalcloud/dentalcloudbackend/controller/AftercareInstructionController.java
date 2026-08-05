package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.AftercareInstructionRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.AftercareInstructionResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.AftercareInstructionService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AftercareInstructionController {
    private final AftercareInstructionService instructionService;

    @PostMapping("/aftercare")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AftercareInstructionResponseDTO> create(
            @Valid @RequestBody AftercareInstructionRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(instructionService.create(request, userDetails.getUsername()));
    }

    @GetMapping("/patients/me/aftercare")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<AftercareInstructionResponseDTO>> own(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(instructionService.listOwn(userDetails.getUsername()));
    }

    @GetMapping("/aftercare/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AftercareInstructionResponseDTO> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(instructionService.get(id, userDetails.getUsername()));
    }

    @PatchMapping("/aftercare/{id}/publicar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AftercareInstructionResponseDTO> publish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(instructionService.publish(id, userDetails.getUsername()));
    }
}
