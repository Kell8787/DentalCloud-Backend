package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalRecordResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.ClinicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class ClinicalRecordController {
    private final ClinicalRecordService clinicalRecordService;

    @GetMapping("/{patientId}/clinical-record")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ClinicalRecordResponseDTO> get(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(clinicalRecordService.get(patientId, userDetails.getUsername()));
    }
}
