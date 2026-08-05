-- Migra las citas existentes a un intervalo tipado y conserva las columnas
-- históricas para permitir una transición segura de clientes antiguos.
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS starts_at TIMESTAMP;
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS ends_at TIMESTAMP;
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS status VARCHAR(24);
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS source VARCHAR(24);
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS treatment_plan_id UUID;
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS rescheduled_from_id UUID;
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS cancellation_reason TEXT;
ALTER TABLE citas
    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

UPDATE citas
SET starts_at = COALESCE(starts_at, hora),
    ends_at = COALESCE(ends_at, hora_fin),
    status = COALESCE(status, CASE estado_cita
        WHEN 'PENDIENTE' THEN 'SOLICITADA'
        WHEN 'CONFIRMADA' THEN 'CONFIRMADA'
        WHEN 'FINALIZADA' THEN 'COMPLETADA'
        WHEN 'ELIMINADA' THEN 'CANCELADA'
        ELSE 'CANCELADA'
    END),
    source = COALESCE(source, 'STAFF_CREATED');

ALTER TABLE citas
    ALTER COLUMN starts_at SET NOT NULL;
ALTER TABLE citas
    ALTER COLUMN ends_at SET NOT NULL;
ALTER TABLE citas
    ALTER COLUMN status SET NOT NULL;
ALTER TABLE citas
    ALTER COLUMN source SET NOT NULL;

ALTER TABLE citas
    ADD CONSTRAINT ck_citas_typed_interval CHECK (starts_at < ends_at);
ALTER TABLE citas
    ADD CONSTRAINT ck_citas_status CHECK (status IN (
        'SOLICITADA', 'CONFIRMADA', 'RECHAZADA', 'CANCELADA',
        'INASISTENCIA', 'COMPLETADA'
    ));
ALTER TABLE citas
    ADD CONSTRAINT ck_citas_source CHECK (source IN ('PATIENT_REQUEST', 'STAFF_CREATED'));
ALTER TABLE citas
    ADD CONSTRAINT fk_citas_treatment_plan
        FOREIGN KEY (treatment_plan_id) REFERENCES patient_treatment_plans (id);
ALTER TABLE citas
    ADD CONSTRAINT fk_citas_rescheduled_from
        FOREIGN KEY (rescheduled_from_id) REFERENCES citas (id);

CREATE INDEX IF NOT EXISTS idx_citas_dentista_starts_at ON citas (dentista_id, starts_at);
CREATE INDEX IF NOT EXISTS idx_citas_patient_starts_at ON citas (user_id, starts_at);
CREATE INDEX IF NOT EXISTS idx_citas_status_starts_at ON citas (status, starts_at);
CREATE INDEX IF NOT EXISTS idx_citas_treatment_plan ON citas (treatment_plan_id);
