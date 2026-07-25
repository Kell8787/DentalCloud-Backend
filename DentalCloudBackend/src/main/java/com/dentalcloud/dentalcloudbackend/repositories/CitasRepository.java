package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import org.springframework.data.jpa.repository.JpaRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface CitasRepository extends JpaRepository<Citas, UUID> {
    List<Citas> findAll();

    List<Citas> findByUserAndEstadoCitaNot(User user, EstadoCita estadoCita);

    List<Citas> findByFechaCita(String fechaCita);

    boolean existsByDentistAndHoraLessThanAndHoraFinGreaterThanAndEstadoCitaIn(
            Dentist dentist,
            LocalDateTime horaFin,
            LocalDateTime hora,
            List<EstadoCita> estados
    );

    List<Citas> findByUser(User user);

    List<Citas> findByDentistAndFechaCita(Dentist dentist, String fechaCita);

    List<Citas> findByDentist(Dentist dentist);

    boolean existsByDentistAndHoraLessThanAndHoraFinGreaterThanAndEstadoCitaInAndIdNot(
            Dentist dentist,
            LocalDateTime horaFin,
            LocalDateTime hora,
            List<EstadoCita> estados,
            UUID id
    );
    List<Citas> findByFechaCitaAndEstadoCita(String fechaCita, EstadoCita estado);

    List<Citas> findByEstadoCita(EstadoCita estado);

    List<Citas> findByEstadoCitaNot(EstadoCita estado);

    List<Citas> findByFechaCitaAndEstadoCitaNot(String fechaCita, EstadoCita estado);
}
