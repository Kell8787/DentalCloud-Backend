package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.TratamientoResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TratamientoService {

    private final TratamientoRepository tratamientoRepository;

    public List<TratamientoResponseDTO> listarTratamientos() {
        return tratamientoRepository.findAll()
                .stream()
                .map(this::mapearAResponse)
                .toList();
    }

    public TratamientoResponseDTO crearTratamiento(TratamientoRequestDTO request) {
        Tratamiento tratamiento = Tratamiento.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .duracionMinutos(request.getDuracionMinutos())
                .precio(request.getPrecio())
                .build();
        return mapearAResponse(tratamientoRepository.save(tratamiento));
    }

    private TratamientoResponseDTO mapearAResponse(Tratamiento t) {
        return TratamientoResponseDTO.builder()
                .id(t.getId())
                .nombre(t.getNombre())
                .descripcion(t.getDescripcion())
                .duracionMinutos(t.getDuracionMinutos())
                .precio(t.getPrecio())
                .build();
    }
}