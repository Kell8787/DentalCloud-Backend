package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.AvailabilitySlotDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.StaffAppointmentRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.ClinicSchedule;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.PatientTreatmentPlan;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import com.dentalcloud.dentalcloudbackend.domain.enums.Rol;
import com.dentalcloud.dentalcloudbackend.domain.enums.TreatmentPlanStatus;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.repositories.AppointmentStatusEventRepository;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.ClinicScheduleRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.PatientTreatmentPlanRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CitaServiceAvailabilityTest {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/El_Salvador");

    @Mock private CitasRepository citasRepository;
    @Mock private ClinicScheduleRepository clinicScheduleRepository;
    @Mock private UserRepository userRepository;
    @Mock private TratamientoRepository tratamientoRepository;
    @Mock private DentistRepository dentistRepository;
    @Mock private PatientTreatmentPlanRepository treatmentPlanRepository;
    @Mock private AppointmentStatusEventRepository appointmentStatusEventRepository;
    @InjectMocks private CitaService service;

    private final UUID treatmentId = UUID.randomUUID();
    private final UUID dentistId = UUID.randomUUID();
    private final LocalDate date = LocalDate.now(BUSINESS_ZONE).plusDays(7);
    private Dentist dentist;

    @BeforeEach
    void setUp() {
        dentist = Dentist.builder().id(dentistId).Name("Dra. Ana Martínez").build();
    }

    private void configureAvailability(int durationMinutes) {
        ClinicSchedule schedule = ClinicSchedule.builder()
                .dayOfWeek(date.getDayOfWeek().getValue())
                .opensAt(LocalTime.of(8, 0))
                .closesAt(LocalTime.of(11, 0))
                .enabled(true)
                .build();
        when(tratamientoRepository.findByIdAndActiveTrue(treatmentId))
                .thenReturn(Optional.of(Tratamiento.builder().id(treatmentId).duracionMinutos(durationMinutes).active(true).build()));
        when(clinicScheduleRepository.findByDayOfWeek(date.getDayOfWeek().getValue()))
                .thenReturn(Optional.of(schedule));
    }

    @Test
    void generatesStartsEveryThirtyMinutesAndOmitsEveryOverlappingSlot() {
        configureAvailability(60);
        Citas occupied = Citas.builder()
                .dentist(dentist)
                .startsAt(LocalDateTime.of(date, LocalTime.of(9, 0)))
                .endsAt(LocalDateTime.of(date, LocalTime.of(10, 0)))
                .status(AppointmentStatus.CONFIRMADA)
                .build();
        when(dentistRepository.findAll()).thenReturn(List.of(dentist));
        when(citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                any(), any(), any())).thenReturn(List.of(occupied));

        List<AvailabilitySlotDTO> slots = service.obtenerDisponibilidad(date, treatmentId);

        assertThat(slots).extracting(slot -> slot.getStartsAt().toLocalTime())
                .containsExactly(LocalTime.of(8, 0), LocalTime.of(10, 0));
        assertThat(slots).allSatisfy(slot -> assertThat(slot.getEndsAt())
                .isEqualTo(slot.getStartsAt().plusMinutes(60)));
    }

    @Test
    void doesNotOfferAppointmentsThatWouldFinishAfterClosing() {
        configureAvailability(45);
        when(dentistRepository.findAll()).thenReturn(List.of(dentist));
        when(citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                any(), any(), any())).thenReturn(List.of());

        List<AvailabilitySlotDTO> slots = service.obtenerDisponibilidad(date, treatmentId);

        assertThat(slots).extracting(slot -> slot.getStartsAt().toLocalTime())
                .containsExactly(
                        LocalTime.of(8, 0), LocalTime.of(8, 30), LocalTime.of(9, 0),
                        LocalTime.of(9, 30), LocalTime.of(10, 0));
        assertThat(slots).noneSatisfy(slot -> assertThat(slot.getEndsAt().toLocalTime())
                .isAfter(LocalTime.of(11, 0)));
    }

    @Test
    void rejectsManuallySubmittedTimesOutsideTheThirtyMinuteGrid() {
        UUID patientId = UUID.randomUUID();
        User patient = User.builder().id(patientId).build();
        StaffAppointmentRequestDTO request = new StaffAppointmentRequestDTO();
        request.setPatientId(patientId);
        request.setDoctorId(dentistId);
        request.setTreatmentId(treatmentId);
        request.setStartsAt(LocalDateTime.of(date, LocalTime.of(8, 15)));
        request.setReason("Control");
        when(userRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(dentistRepository.findByIdForUpdate(dentistId)).thenReturn(Optional.of(dentist));
        when(tratamientoRepository.findByIdAndActiveTrue(treatmentId))
                .thenReturn(Optional.of(Tratamiento.builder().id(treatmentId).duracionMinutos(60).active(true).build()));

        assertThatThrownBy(() -> service.crearCitaStaff(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("intervalo de 30 minutos");
    }

    @Test
    void availabilityForAPlanOnlyIncludesItsAssignedDentist() {
        configureAvailability(60);
        UUID planId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        Dentist otherDentist = Dentist.builder().id(UUID.randomUUID()).Name("Dr. Carlos Pérez").build();
        PatientTreatmentPlan plan = PatientTreatmentPlan.builder()
                .id(planId)
                .patientId(patientId)
                .dentistId(dentistId)
                .treatmentId(treatmentId)
                .status(TreatmentPlanStatus.ACTIVE)
                .build();
        when(treatmentPlanRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(userRepository.findByEmail("patient@example.com"))
                .thenReturn(Optional.of(User.builder().id(patientId).role(Rol.CUSTOMER).build()));
        when(dentistRepository.findAll()).thenReturn(List.of(dentist, otherDentist));
        when(citasRepository.findByDentistAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                any(), any(), any())).thenReturn(List.of());

        List<AvailabilitySlotDTO> slots = service.obtenerDisponibilidadPorPlan(
                date, planId, null, "patient@example.com");

        assertThat(slots).isNotEmpty().allMatch(slot -> slot.getDoctorId().equals(dentistId));
    }
}
