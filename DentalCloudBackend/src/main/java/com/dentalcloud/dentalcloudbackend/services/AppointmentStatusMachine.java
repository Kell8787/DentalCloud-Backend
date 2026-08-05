package com.dentalcloud.dentalcloudbackend.services;

import com.dentalcloud.dentalcloudbackend.domain.enums.AppointmentStatus;

public final class AppointmentStatusMachine {
    private AppointmentStatusMachine() {
    }

    public static boolean isAllowed(AppointmentStatus from, AppointmentStatus to) {
        return switch (from) {
            case SOLICITADA -> to == AppointmentStatus.CONFIRMADA
                    || to == AppointmentStatus.RECHAZADA || to == AppointmentStatus.CANCELADA;
            case CONFIRMADA -> to == AppointmentStatus.CANCELADA
                    || to == AppointmentStatus.INASISTENCIA || to == AppointmentStatus.COMPLETADA;
            case RECHAZADA, CANCELADA, INASISTENCIA, COMPLETADA -> false;
        };
    }
}
