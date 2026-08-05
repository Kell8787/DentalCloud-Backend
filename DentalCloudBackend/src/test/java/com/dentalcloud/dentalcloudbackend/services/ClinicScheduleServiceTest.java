package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.UpdateClinicScheduleRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClinicScheduleServiceTest {
    @Mock private ClinicScheduleRepository repository;
    @InjectMocks private ClinicScheduleService service;

    @Test
    void updatesDayAndKeepsItsOptimisticVersion() {
        ClinicSchedule schedule = ClinicSchedule.builder()
                .id(UUID.randomUUID()).dayOfWeek(1).opensAt(LocalTime.of(8, 0))
                .closesAt(LocalTime.of(16, 0)).enabled(true).version(2L).build();
        UpdateClinicScheduleRequestDTO request = new UpdateClinicScheduleRequestDTO();
        request.setOpensAt(LocalTime.of(9, 0));
        request.setClosesAt(LocalTime.of(17, 0));
        request.setEnabled(true);
        request.setVersion(2L);
        when(repository.findByDayOfWeek(1)).thenReturn(Optional.of(schedule));
        when(repository.save(schedule)).thenReturn(schedule);

        var response = service.update(1, request);

        assertThat(response.getOpensAt()).isEqualTo(LocalTime.of(9, 0));
        assertThat(response.getClosesAt()).isEqualTo(LocalTime.of(17, 0));
        assertThat(response.getVersion()).isEqualTo(2L);
    }

    @Test
    void rejectsAnIntervalWithClosingBeforeOpening() {
        UpdateClinicScheduleRequestDTO request = new UpdateClinicScheduleRequestDTO();
        request.setOpensAt(LocalTime.of(17, 0));
        request.setClosesAt(LocalTime.of(9, 0));
        request.setEnabled(true);

        assertThatThrownBy(() -> service.update(1, request))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class)
                .hasMessageContaining("apertura");
    }
}
