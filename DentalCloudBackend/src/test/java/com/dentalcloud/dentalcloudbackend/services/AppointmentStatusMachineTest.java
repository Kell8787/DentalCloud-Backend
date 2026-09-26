package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentStatusMachineTest {
    @Test
    void acceptsEveryContractedTransition() {
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.SOLICITADA, AppointmentStatus.CONFIRMADA)).isTrue();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.SOLICITADA, AppointmentStatus.RECHAZADA)).isTrue();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.SOLICITADA, AppointmentStatus.CANCELADA)).isTrue();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.CONFIRMADA, AppointmentStatus.CANCELADA)).isTrue();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.CONFIRMADA, AppointmentStatus.INASISTENCIA)).isTrue();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.CONFIRMADA, AppointmentStatus.COMPLETADA)).isTrue();
    }

    @Test
    void rejectsTerminalStatesAndRevival() {
        for (AppointmentStatus terminal : new AppointmentStatus[]{
                AppointmentStatus.RECHAZADA,
                AppointmentStatus.CANCELADA,
                AppointmentStatus.INASISTENCIA,
                AppointmentStatus.COMPLETADA
        }) {
            for (AppointmentStatus target : AppointmentStatus.values()) {
                assertThat(AppointmentStatusMachine.isAllowed(terminal, target)).isFalse();
            }
        }
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.SOLICITADA, AppointmentStatus.COMPLETADA)).isFalse();
        assertThat(AppointmentStatusMachine.isAllowed(AppointmentStatus.CONFIRMADA, AppointmentStatus.RECHAZADA)).isFalse();
    }
}
