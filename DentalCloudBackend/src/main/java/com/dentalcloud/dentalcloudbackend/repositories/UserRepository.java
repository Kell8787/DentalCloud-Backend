package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    Optional<User> findByDui(String dui);
    Optional<User> findByPhoneNumber(String phoneNumber);
    List<User> findByRole(Rol role);
    boolean existsByDui(String dui);
    boolean existsByPhoneNumber(String phoneNumber);
}
