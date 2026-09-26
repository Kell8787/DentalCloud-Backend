package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ContactoEmergencia;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ContactoEmergenciaRepository extends JpaRepository<ContactoEmergencia, UUID> {

    Optional<ContactoEmergencia> findByUser(User user);
}
