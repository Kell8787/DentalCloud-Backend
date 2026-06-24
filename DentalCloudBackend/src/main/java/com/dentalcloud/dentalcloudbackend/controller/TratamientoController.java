package com.dentalcloud.dentalcloudbackend.controller;

import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoResponseDTO;
import com.dentalcloud.dentalcloudbackend.services.TratamientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
@RequiredArgsConstructor
public class TratamientoController {

    private final TratamientoService tratamientoService;

    @GetMapping
    public ResponseEntity<List<TratamientoResponseDTO>> listarTratamientos() {
        return ResponseEntity.ok(tratamientoService.listarTratamientos());
    }

    @PostMapping
    public ResponseEntity<TratamientoResponseDTO> crearTratamiento(@Valid @RequestBody TratamientoRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tratamientoService.crearTratamiento(request));
    }
}