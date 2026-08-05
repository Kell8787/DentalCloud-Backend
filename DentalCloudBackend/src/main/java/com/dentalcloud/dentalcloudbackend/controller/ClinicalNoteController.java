package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteAmendmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteAuditResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalNoteResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.ClinicalNoteService;
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
@PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
public class ClinicalNoteController {
    private final ClinicalNoteService noteService;

    @GetMapping("/patients/{patientId}/clinical-notes")
    public ResponseEntity<List<ClinicalNoteResponseDTO>> listForPatient(
            @PathVariable UUID patientId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.listForPatient(patientId, userDetails.getUsername()));
    }

    @GetMapping("/clinical-notes/{id}")
    public ResponseEntity<ClinicalNoteResponseDTO> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.get(id, userDetails.getUsername()));
    }

    @PostMapping("/clinical-notes")
    public ResponseEntity<ClinicalNoteResponseDTO> create(
            @Valid @RequestBody ClinicalNoteRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(noteService.create(request, userDetails.getUsername()));
    }

    @PatchMapping("/clinical-notes/{id}")
    public ResponseEntity<ClinicalNoteResponseDTO> updateDraft(
            @PathVariable UUID id,
            @Valid @RequestBody ClinicalNoteRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.updateDraft(id, request, userDetails.getUsername()));
    }

    @PostMapping("/clinical-notes/{id}/finalize")
    public ResponseEntity<ClinicalNoteResponseDTO> finalize(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.finalize(id, userDetails.getUsername()));
    }

    @PostMapping("/clinical-notes/{id}/amend")
    public ResponseEntity<ClinicalNoteResponseDTO> amend(
            @PathVariable UUID id,
            @Valid @RequestBody ClinicalNoteAmendmentRequestDTO request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.amend(id, request, userDetails.getUsername()));
    }

    @GetMapping("/clinical-notes/{id}/audit")
    public ResponseEntity<List<ClinicalNoteAuditResponseDTO>> audit(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(noteService.audit(id, userDetails.getUsername()));
    }
}
