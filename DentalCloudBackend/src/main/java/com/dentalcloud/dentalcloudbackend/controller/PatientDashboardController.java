package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.PatientDashboardResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.PatientDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class PatientDashboardController {
    private final PatientDashboardService dashboardService;

    @GetMapping("/patient")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PatientDashboardResponseDTO> get(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(dashboardService.get(userDetails.getUsername()));
    }
}
