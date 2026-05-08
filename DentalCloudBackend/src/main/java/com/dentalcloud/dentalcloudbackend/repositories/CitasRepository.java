package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CitasRepository extends JpaRepository<Citas, UUID> {

    boolean existsByDentistAndHoraLessThanAndHoraFinGreaterThanAndEstadoCitaIn(
            Dentist dentist,
            LocalDateTime horaFin,
            LocalDateTime hora,
            List<EstadoCita> estados
    );

    List<Citas> findByDentistAndFechaCita(Dentist dentist, String fechaCita);
}
