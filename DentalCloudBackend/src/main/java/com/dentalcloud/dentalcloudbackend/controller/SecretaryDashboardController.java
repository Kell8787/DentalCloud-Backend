package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.SecretaryDashboardResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.SecretaryDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class SecretaryDashboardController {
    private final SecretaryDashboardService dashboardService;

    @GetMapping("/secretary")
    @PreAuthorize("hasAnyRole('SECRETARIA', 'ADMIN')")
    public ResponseEntity<SecretaryDashboardResponseDTO> get(
            @RequestParam(required = false) LocalDate weekOf) {
        return ResponseEntity.ok(dashboardService.get(weekOf));
    }
}
