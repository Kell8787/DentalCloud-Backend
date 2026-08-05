ALTER TABLE tratamientos
    ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

CREATE INDEX IF NOT EXISTS idx_tratamientos_active_name
    ON tratamientos (active, nombre);
