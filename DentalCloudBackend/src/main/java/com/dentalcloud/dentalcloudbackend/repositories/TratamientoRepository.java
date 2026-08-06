package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface TratamientoRepository extends JpaRepository<Tratamiento, UUID> {
    List<Tratamiento> findByActiveTrueOrderByNombreAsc();

    List<Tratamiento> findAllByOrderByNombreAsc();

    Optional<Tratamiento> findByNombre(String nombre);

    java.util.Optional<Tratamiento> findByIdAndActiveTrue(UUID id);
}
