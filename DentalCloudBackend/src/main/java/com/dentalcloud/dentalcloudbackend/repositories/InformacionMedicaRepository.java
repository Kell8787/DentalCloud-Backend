package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.InformacionMedica;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InformacionMedicaRepository extends JpaRepository<InformacionMedica, UUID> {
    Optional<InformacionMedica> findByUser(User user);
}
