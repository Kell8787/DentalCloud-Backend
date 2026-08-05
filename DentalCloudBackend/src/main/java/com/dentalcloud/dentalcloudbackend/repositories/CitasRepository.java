package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CitasRepository extends JpaRepository<Citas, UUID> {
    @Query("""
            select c from Citas c
            where (:status is null or c.status = :status)
              and (:fromAt is null or c.startsAt >= :fromAt)
              and (:toAt is null or c.startsAt < :toAt)
              and (:patientId is null or c.user.id = :patientId)
            order by c.startsAt asc
            """)
    Page<Citas> search(@Param("status") AppointmentStatus status,
                       @Param("fromAt") LocalDateTime fromAt,
                       @Param("toAt") LocalDateTime toAt,
                       @Param("patientId") UUID patientId,
                       Pageable pageable);

    List<Citas> findByUserAndStatusInOrderByStartsAtAsc(User user, Collection<AppointmentStatus> statuses);

    List<Citas> findByUserAndStartsAtGreaterThanEqualAndStatusInOrderByStartsAtAsc(
            User user, LocalDateTime startsAt, Collection<AppointmentStatus> statuses);

    List<Citas> findByUserAndEndsAtLessThanAndStatusInOrderByStartsAtDesc(
            User user, LocalDateTime endsAt, Collection<AppointmentStatus> statuses);

    List<Citas> findByDentistAndStartsAtLessThanAndEndsAtGreaterThanOrderByStartsAtAsc(
            Dentist dentist, LocalDateTime endsAt, LocalDateTime startsAt);

    List<Citas> findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
            Dentist dentist, LocalDateTime from, LocalDateTime to);

    List<Citas> findByStatusInOrderByStartsAtAsc(Collection<AppointmentStatus> statuses);

    List<Citas> findByStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
            LocalDateTime from, LocalDateTime to);

    List<Citas> findByStatusOrderByStartsAtAsc(AppointmentStatus status);

    Optional<Citas> findByIdAndUser(UUID id, User user);

    Optional<Citas> findByIdempotencyKey(String idempotencyKey);

    boolean existsByDentistAndStartsAtLessThanAndEndsAtGreaterThanAndStatusIn(
            Dentist dentist,
            LocalDateTime endsAt,
            LocalDateTime startsAt,
            Collection<AppointmentStatus> statuses
    );

    boolean existsByDentistAndStartsAtLessThanAndEndsAtGreaterThanAndStatusInAndIdNot(
            Dentist dentist,
            LocalDateTime endsAt,
            LocalDateTime startsAt,
            Collection<AppointmentStatus> statuses,
            UUID id
    );
}
