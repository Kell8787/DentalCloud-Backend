-- Índices y lecturas agrupadas para dashboards y agendas.
CREATE INDEX IF NOT EXISTS idx_citas_starts_at ON citas (starts_at);
