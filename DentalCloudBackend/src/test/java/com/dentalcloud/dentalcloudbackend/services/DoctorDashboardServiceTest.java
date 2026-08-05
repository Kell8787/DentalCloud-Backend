package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorDashboardServiceTest {
    @Mock private DentistRepository dentistRepository;
    @Mock private CitaService citaService;
    @InjectMocks private DoctorDashboardService service;

    @Test
    void defaultsToCurrentWeekAndOnlyReturnsOwnAgendaGroupedByDay() {
        UUID dentistId = UUID.randomUUID();
        User doctor = User.builder().id(UUID.randomUUID()).firstName("Ana").lastName("Dental").build();
        Dentist dentist = Dentist.builder().id(dentistId).Name("Ana Dental").user(doctor).build();
        when(dentistRepository.findByUserEmail("doctor@test.com")).thenReturn(java.util.Optional.of(dentist));
        CitaResponseDTO appointment = CitaResponseDTO.builder()
                .startsAt(LocalDateTime.now().plusHours(2)).status(AppointmentStatus.CONFIRMADA).build();
        when(citaService.obtenerAgendaDoctorPorRango(eq("doctor@test.com"), eq(LocalDate.now().with(java.time.DayOfWeek.MONDAY)),
                eq(LocalDate.now().with(java.time.DayOfWeek.MONDAY).plusDays(6))))
                .thenReturn(List.of(appointment));

        var response = service.get("doctor@test.com", null, null);

        assertThat(response.getDoctorId()).isEqualTo(dentistId);
        assertThat(response.getCalendar()).hasSize(7);
        assertThat(response.getNextAppointments()).containsExactly(appointment);
    }

    @Test
    void rejectsRangesLongerThanThirtyOneDays() {
        UUID dentistId = UUID.randomUUID();
        Dentist dentist = Dentist.builder().id(dentistId).Name("Ana Dental").build();
        when(dentistRepository.findByUserEmail("doctor@test.com")).thenReturn(java.util.Optional.of(dentist));

        assertThatThrownBy(() -> service.get("doctor@test.com", LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1)))
                .isInstanceOf(com.dentalcloud.dentalcloudbackend.exceptions.BusinessException.class);
    }
}
