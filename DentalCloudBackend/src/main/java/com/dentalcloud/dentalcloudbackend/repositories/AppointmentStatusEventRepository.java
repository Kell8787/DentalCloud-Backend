package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.AppointmentStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AppointmentStatusEventRepository extends JpaRepository<AppointmentStatusEvent, UUID> {
    List<AppointmentStatusEvent> findByAppointmentIdOrderByOccurredAtAsc(UUID appointmentId);
}
