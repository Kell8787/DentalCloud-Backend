package com.dentalcloud.dentalcloudbackend.repositories;

import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClinicScheduleRepository extends JpaRepository<ClinicSchedule, UUID> {
    Optional<ClinicSchedule> findByDayOfWeek(Integer dayOfWeek);

    List<ClinicSchedule> findByEnabledTrueOrderByDayOfWeekAsc();
}
