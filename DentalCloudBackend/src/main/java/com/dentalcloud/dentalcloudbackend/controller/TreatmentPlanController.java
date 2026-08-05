package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentPlanRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CreateTreatmentStepRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.PatientTreatmentPlanResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TreatmentStepResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentPlanRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateTreatmentStepRequestDTO;
import com.dentalcloud.dentalcloudbackend.services.TreatmentPlanService;
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
@RequiredArgsConstructor
public class TreatmentPlanController {
    private final TreatmentPlanService treatmentPlanService;

    @GetMapping("/api/patients/me/treatment-plans")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PatientTreatmentPlanResponseDTO>> ownPlans(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(treatmentPlanService.listOwn(userDetails.getUsername()));
    }

    @GetMapping("/api/patients/{patientId}/treatment-plans")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PatientTreatmentPlanResponseDTO>> patientPlans(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(treatmentPlanService.listForPatient(patientId, userDetails.getUsername()));
    }

    @PostMapping("/api/patients/{patientId}/treatment-plans")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<PatientTreatmentPlanResponseDTO> createPlan(
            @PathVariable UUID patientId,
            @Valid @RequestBody CreateTreatmentPlanRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(treatmentPlanService.create(patientId, request, userDetails.getUsername()));
    }

    @GetMapping("/api/treatment-plans/{planId}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<PatientTreatmentPlanResponseDTO> getPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(treatmentPlanService.get(planId, userDetails.getUsername()));
    }

    @PatchMapping("/api/treatment-plans/{planId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<PatientTreatmentPlanResponseDTO> updatePlan(
            @PathVariable UUID planId,
            @Valid @RequestBody UpdateTreatmentPlanRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(treatmentPlanService.update(planId, request, userDetails.getUsername()));
    }

    @PostMapping("/api/treatment-plans/{planId}/steps")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<TreatmentStepResponseDTO> addStep(
            @PathVariable UUID planId,
            @Valid @RequestBody CreateTreatmentStepRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(treatmentPlanService.addStep(planId, request, userDetails.getUsername()));
    }

    @PatchMapping("/api/treatment-plans/{planId}/steps/{stepId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<TreatmentStepResponseDTO> updateStep(
            @PathVariable UUID planId,
            @PathVariable UUID stepId,
            @Valid @RequestBody UpdateTreatmentStepRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(treatmentPlanService.updateStep(
                planId, stepId, request, userDetails.getUsername()));
    }
}
