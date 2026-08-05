CREATE TABLE IF NOT EXISTS appointment_status_events (
    id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    from_status VARCHAR(24),
    to_status VARCHAR(24) NOT NULL,
    reason TEXT,
    actor_id UUID,
    occurred_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_appointment_status_events PRIMARY KEY (id),
    CONSTRAINT fk_appointment_status_events_appointment
        FOREIGN KEY (appointment_id) REFERENCES citas (id),
    CONSTRAINT fk_appointment_status_events_actor
        FOREIGN KEY (actor_id) REFERENCES dental_users (id),
    CONSTRAINT ck_appointment_status_events_from CHECK (
        from_status IS NULL OR from_status IN (
            'SOLICITADA', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA',
            'INASISTENCIA', 'COMPLETADA'
        )
    ),
    CONSTRAINT ck_appointment_status_events_to CHECK (to_status IN (
        'SOLICITADA', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA',
        'INASISTENCIA', 'COMPLETADA'
    ))
);

CREATE INDEX IF NOT EXISTS idx_appointment_status_events_appointment_time
    ON appointment_status_events (appointment_id, occurred_at);
