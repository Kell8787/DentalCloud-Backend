package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.CitaService;
import com.dentalcloud.dentalcloudbackend.services.E2eAppointmentFixtureService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@Profile("e2e")
@RequestMapping("/api/e2e/fixtures")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class E2eFixtureController {
    private final E2eAppointmentFixtureService fixtureService;
    private final CitaService citaService;

    @PostMapping("/appointments/{id}/rewind")
    public ResponseEntity<CitaResponseDTO> rewindAppointment(@PathVariable UUID id) {
        fixtureService.rewindToCompletable(id);
        return ResponseEntity.ok(citaService.obtenerPorId(id));
    }
}
