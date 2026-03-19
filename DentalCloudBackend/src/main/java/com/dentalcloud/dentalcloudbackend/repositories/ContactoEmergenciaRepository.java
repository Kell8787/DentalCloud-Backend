package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ContactoEmergencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, UUID> {
}
