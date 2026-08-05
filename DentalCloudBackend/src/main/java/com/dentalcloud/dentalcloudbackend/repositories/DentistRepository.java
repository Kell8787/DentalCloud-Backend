package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.UUID;

public interface DentistRepository extends JpaRepository<Dentist, UUID> {
    Optional<Dentist> findByUserEmail(String email);
    Optional<Dentist> findByUser(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Dentist d where d.id = :id")
    Optional<Dentist> findByIdForUpdate(@Param("id") UUID id);
}
