package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.dto.CitaResponseDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.CrearCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.RechazarCitasRequestDTO;
import com.dentalcloud.dentalcloudbackend.domain.dto.SlotDisponibleDTO;
import com.dentalcloud.dentalcloudbackend.domain.entity.Citas;
import com.dentalcloud.dentalcloudbackend.domain.entity.Dentist;
import com.dentalcloud.dentalcloudbackend.domain.entity.Tratamiento;
import com.dentalcloud.dentalcloudbackend.domain.entity.User;
import com.dentalcloud.dentalcloudbackend.domain.enums.EstadoCita;
import com.dentalcloud.dentalcloudbackend.exceptions.BusinessException;
import com.dentalcloud.dentalcloudbackend.exceptions.ResourceNotFoundException;
import com.dentalcloud.dentalcloudbackend.repositories.CitasRepository;
import com.dentalcloud.dentalcloudbackend.repositories.DentistRepository;
import com.dentalcloud.dentalcloudbackend.repositories.TratamientoRepository;
import com.dentalcloud.dentalcloudbackend.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
@RequiredArgsConstructor
@Slf4j
public class CitaService {
    private final CitasRepository citasRepository;
    private final UserRepository userRepository;
    private final TratamientoRepository tratamientoRepository;
    private final DentistRepository dentistRepository;

    @Transactional
    public CitaResponseDTO crearCita(CrearCitasRequestDTO cita) {
        User paciente = userRepository.findById(cita.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));

        Dentist dentista = dentistRepository.findById(cita.getDentistaId())
                .orElseThrow(() -> new ResourceNotFoundException("Dentista no encontrado"));

        Tratamiento tratamiento = tratamientoRepository.findById(cita.getTratamientoId())
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));

        LocalDate fechaCita = cita.getFecha();
        LocalTime horaInicio = cita.getHoraInicio();
        LocalDateTime fechaHoraInicio = LocalDateTime.of(fechaCita, horaInicio);

        // Validar la fecha que no sea pasada
        if (fechaHoraInicio.isBefore(LocalDateTime.now())) {
            throw new BusinessException("No se puede crear una cita en una fecha u hora pasada");
        }

        // Validar horario laboral
        LocalTime horaInicioLaboral = LocalTime.of(8, 0);
        LocalTime horaFinLaboral;

        DayOfWeek dia = fechaCita.getDayOfWeek();

        if(dia == DayOfWeek.SUNDAY){
            horaFinLaboral = LocalTime.of(12, 0);
        } else if (dia == DayOfWeek.SATURDAY){
            throw new BusinessException("No se pueden crear citas los sábados");
        } else {
            horaFinLaboral = LocalTime.of(16, 0);
        }

        // Validar que este en el horario laboral
        if(horaInicio.isBefore(horaInicioLaboral) || horaInicio.isAfter(horaFinLaboral)){
            throw new BusinessException("La hora de la cita debe estar dentro del horario laboral");
        }

        // Calcular fecha y hora fin segun el tratamiento
        LocalDateTime fechaHoraFin = fechaHoraInicio.plusMinutes(tratamiento.getDuracionMinutos());

        // Validar que no termine fuera del horario laboral
        if (fechaHoraFin.toLocalTime().isAfter(horaFinLaboral.plusMinutes(15))) {
            throw new BusinessException("La cita excede el horario laboral");
        }

        boolean hayConflicto = citasRepository
                .existsByDentistAndHoraLessThanAndHoraFinGreaterThanAndEstadoCitaIn(
                        dentista,
                        fechaHoraFin,
                        fechaHoraInicio,
                        List.of(EstadoCita.PENDIENTE, EstadoCita.CONFIRMADA)
                );

        if(hayConflicto){
            throw new BusinessException("El dentista tiene otra cita en ese horario");
        }

        Citas nuevaCita = Citas.builder()
                .user(paciente)
                .dentist(dentista)
                .tratamiento(tratamiento)
                .fechaCita(fechaCita.toString())
                .hora(fechaHoraInicio)
                .horaFin(fechaHoraFin)
                .estadoCita(EstadoCita.PENDIENTE)
                .motivo(cita.getMotivo())
                .build();

        Citas citaGuardada = citasRepository.save(nuevaCita);

        return mapearCitaAResponse(citaGuardada);
    }

    public List<SlotDisponibleDTO> obtenerSlotsDisponibles(LocalDate fecha, UUID tratamientoId) {

        // Validar que no sea sábado
        if (fecha.getDayOfWeek() == DayOfWeek.SATURDAY) {
            throw new BusinessException("No hay citas los sábados");
        }

        // Validar que no sea fecha pasada
        if (fecha.isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha no puede ser en el pasado");
        }

        // Obtener tratamiento para saber la duración
        Tratamiento tratamiento = tratamientoRepository.findById(tratamientoId)
                .orElseThrow(() -> new ResourceNotFoundException("Tratamiento no encontrado"));

        int duracion = tratamiento.getDuracionMinutos();

        // Horario laboral según el día
        LocalTime inicioLaboral = LocalTime.of(8, 0);
        LocalTime finLaboral = fecha.getDayOfWeek() == DayOfWeek.SUNDAY
                ? LocalTime.of(12, 0)
                : LocalTime.of(16, 0);

        // Obtener todos los dentistas
        List<Dentist> dentistas = dentistRepository.findAll();

        List<SlotDisponibleDTO> resultado = new ArrayList<>();

        for (Dentist dentista : dentistas) {

            // Obtener citas del dentista en esa fecha
            List<Citas> citasDelDia = citasRepository
                    .findByDentistAndFechaCita(dentista, fecha.toString());

            List<LocalTime> horasDisponibles = new ArrayList<>();
            LocalTime slot = inicioLaboral;

            LocalTime finLaboralConMargen = finLaboral.plusMinutes(15);
            while (!slot.plusMinutes(duracion).isAfter(finLaboralConMargen)) {
                LocalDateTime slotInicio = LocalDateTime.of(fecha, slot);
                LocalDateTime slotFin = slotInicio.plusMinutes(duracion);

                // Verificar si el slot se solapa con alguna cita existente
                boolean ocupado = citasDelDia.stream()
                        .filter(c -> c.getEstadoCita() == EstadoCita.PENDIENTE
                                || c.getEstadoCita() == EstadoCita.CONFIRMADA)
                        .anyMatch(c -> slotInicio.isBefore(c.getHoraFin())
                                && slotFin.isAfter(c.getHora()));

                if (!ocupado) {
                    horasDisponibles.add(slot);
                }

                slot = slot.plusMinutes(duracion);
            }

            // Solo incluir dentistas con al menos un slot disponible
            if (!horasDisponibles.isEmpty()) {
                resultado.add(SlotDisponibleDTO.builder()
                        .dentistaId(dentista.getId())
                        .dentistaNombre(dentista.getName())
                        .horasDisponibles(horasDisponibles)
                        .build());
            }
        }

        return resultado;
    }

    @Transactional
    // Aprobar Una Cita
    public CitaResponseDTO aprobarCita(UUID citaId){

        Citas cita = citasRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        // Validar que la Cita este en "PENDIENTE"
        if(cita.getEstadoCita() != EstadoCita.PENDIENTE){
            throw new BusinessException("Solo se pueden aprobar citas en estado PENDIENTE");
        }

        cita.setEstadoCita(EstadoCita.CONFIRMADA);

        Citas citaActualizada = citasRepository.save(cita);

        return mapearCitaAResponse(citaActualizada);
    }

    @Transactional
    // Rechazar Una Cita
    public CitaResponseDTO rechazarCita(UUID citaId, RechazarCitasRequestDTO request){
        Citas cita = citasRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        if(cita.getEstadoCita() != EstadoCita.PENDIENTE){
            throw new BusinessException("Solo se pueden rechazar citas en estado PENDIENTE");
        }

        cita.setEstadoCita(EstadoCita.CANCELADA);

        if(request.getMotivo() != null && !request.getMotivo().isBlank()){
            cita.setMotivo(request.getMotivo());
        }

        Citas citaActualizada = citasRepository.save(cita);

        return mapearCitaAResponse(citaActualizada);
    }

    @Transactional
    // Cancelar Cita (Paciente)
    public CitaResponseDTO cancelarCita(UUID citaId, String emailUsuario) {
        Citas cita = citasRepository.findById(citaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));

        // Validar que sea el dueño de la cita (solo aplica para CUSTOMER)
        if (!cita.getUser().getEmail().equals(emailUsuario)) {
            throw new BusinessException("No tienes permiso para cancelar esta cita");
        }

        // Validar que la cita esté en estado PENDIENTE o CONFIRMADA
        if (cita.getEstadoCita() != EstadoCita.PENDIENTE &&
                cita.getEstadoCita() != EstadoCita.CONFIRMADA) {
            throw new BusinessException("No se puede cancelar una cita en estado " + cita.getEstadoCita());
        }

        if (cita.getHora().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BusinessException("No se puede cancelar una cita con menos de 24 horas de anticipación");
        }

        cita.setEstadoCita(EstadoCita.CANCELADA);
        Citas citaActualizada = citasRepository.save(cita);
        return mapearCitaAResponse(citaActualizada);
    }

    public List<CitaResponseDTO> obtenerMisCitas(String email) {
        User paciente = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        return citasRepository.findByUser(paciente)
                .stream()
                .map(this::mapearCitaAResponse)
                .toList();
    }

    public List<CitaResponseDTO> obtenerTodasLasCitas(LocalDate fecha) {
        List<Citas> citas;

        if (fecha != null) {
            citas = citasRepository.findByFechaCita(fecha.toString());
        } else {
            citas = citasRepository.findAll();
        }

        return citas.stream()
                .map(this::mapearCitaAResponse)
                .toList();
    }

    private CitaResponseDTO mapearCitaAResponse(Citas cita) {
        return CitaResponseDTO.builder()
                .id(cita.getId())
                .pacienteId(cita.getUser().getId())
                .pacienteNombre(cita.getUser().getFirstName() + " " + cita.getUser().getLastName())
                .dentistaId(cita.getDentist().getId())
                .dentistaNombre(cita.getDentist().getName())
                .tratamientoId(cita.getTratamiento().getId())
                .tratamientoNombre(cita.getTratamiento().getNombre())
                .fecha(LocalDate.parse(cita.getFechaCita()))
                .horaInicio(cita.getHora().toLocalTime())
                .horaFin(cita.getHoraFin().toLocalTime())
                .duracionMinutos(cita.getTratamiento().getDuracionMinutos())
                .precio(cita.getTratamiento().getPrecio())
                .estadoCita(cita.getEstadoCita())
                .motivo(cita.getMotivo())
                .build();
    }
}
