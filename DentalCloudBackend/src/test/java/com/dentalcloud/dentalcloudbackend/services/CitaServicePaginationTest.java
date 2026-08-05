package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.repositories.AppointmentStatusEventRepository;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServicePaginationTest {
    @Mock private CitasRepository citasRepository;
    @Mock private ClinicScheduleRepository clinicScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PatientTreatmentPlanRepository treatmentPlanRepository;
    @Mock private AppointmentStatusEventRepository appointmentStatusEventRepository;
    @InjectMocks private CitaService service;

    @Test
    void returnsStablePaginationEnvelope() {
        when(citasRepository.search(eq(AppointmentStatus.CONFIRMADA),
                eq(LocalDate.of(2026, 8, 1).atStartOfDay()),
                eq(LocalDate.of(2026, 8, 8).plusDays(1).atStartOfDay()), eq(null), any()))
                .thenReturn(new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(1, 20), 0));

        var response = service.listarPaginado(1, 20, AppointmentStatus.CONFIRMADA,
                LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 8), null);

        assertThat(response.getPage()).isEqualTo(1);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalItems()).isZero();
        assertThat(response.getItems()).isEmpty();
    }
}
