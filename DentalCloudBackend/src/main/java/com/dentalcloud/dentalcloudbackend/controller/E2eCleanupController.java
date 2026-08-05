package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.services.E2eCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "e2e"})
@RequestMapping("/api/e2e")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class E2eCleanupController {
    private final E2eCleanupService cleanupService;

    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanup() {
        cleanupService.cleanup();
        return ResponseEntity.noContent().build();
    }
}
