package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalDocumentResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.ClinicalDocumentUploadRequestDTO;
import com.dentalcloud.dentalcloudbackend.services.ClinicalDocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ClinicalDocumentController {
    private final ClinicalDocumentService documentService;

    @PostMapping(value = "/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ClinicalDocumentResponseDTO> upload(
            @Valid ClinicalDocumentUploadRequestDTO request,
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.upload(request, file, userDetails.getUsername()));
    }

    @GetMapping({"/documentos/mios", "/patients/me/documents"})
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<ClinicalDocumentResponseDTO>> own(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.listOwn(userDetails.getUsername()));
    }

    @GetMapping("/citas/{appointmentId}/documentos")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<ClinicalDocumentResponseDTO>> byAppointment(
            @PathVariable UUID appointmentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.listForAppointment(appointmentId, userDetails.getUsername()));
    }

    @GetMapping("/treatment-plans/{planId}/documentos")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<ClinicalDocumentResponseDTO>> byPlan(
            @PathVariable UUID planId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.listForPlan(planId, userDetails.getUsername()));
    }

    @GetMapping("/documentos/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ClinicalDocumentResponseDTO> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.get(id, userDetails.getUsername()));
    }

    @PatchMapping("/documentos/{id}/publicar")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<ClinicalDocumentResponseDTO> publish(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(documentService.publish(id, userDetails.getUsername()));
    }

    @GetMapping("/documentos/{id}/contenido")
    @PreAuthorize("permitAll()")
    public ResponseEntity<Resource> download(@PathVariable UUID id,
                                              @RequestParam long expiresAt,
                                              @RequestParam String signature) {
        var document = documentService.download(id, expiresAt, signature);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(document.mimeType()))
                .contentLength(document.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(document.filename()).build().toString())
                .body(document.resource());
    }
}
