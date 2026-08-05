package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicScheduleResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateClinicScheduleRequestDTO;
import com.dentalcloud.dentalcloudbackend.services.ClinicScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clinic-schedules")
@RequiredArgsConstructor
public class ClinicScheduleController {
    private final ClinicScheduleService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<List<ClinicScheduleResponseDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @PutMapping("/{dayOfWeek}")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<ClinicScheduleResponseDTO> update(
            @PathVariable int dayOfWeek,
            @Valid @RequestBody UpdateClinicScheduleRequestDTO request) {
        return ResponseEntity.ok(service.update(dayOfWeek, request));
    }
}
