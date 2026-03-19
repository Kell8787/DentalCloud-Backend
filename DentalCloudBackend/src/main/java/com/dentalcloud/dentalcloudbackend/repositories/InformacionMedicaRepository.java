package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InformacionMedicaRepository extends JpaRepository<InformacionMedica, UUID> {
}
