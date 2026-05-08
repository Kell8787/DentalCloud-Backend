package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TratamientoRepository extends JpaRepository<Tratamiento, UUID> {
}
