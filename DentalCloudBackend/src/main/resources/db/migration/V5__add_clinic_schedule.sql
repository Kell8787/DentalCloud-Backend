CREATE TABLE IF NOT EXISTS clinic_schedules (
    id UUID NOT NULL,
    day_of_week INTEGER NOT NULL,
    opens_at TIME NOT NULL,
    closes_at TIME NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_clinic_schedules PRIMARY KEY (id),
    CONSTRAINT uk_clinic_schedules_day UNIQUE (day_of_week),
    CONSTRAINT ck_clinic_schedules_day CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT ck_clinic_schedules_interval CHECK (opens_at < closes_at)
);

INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000001', 1, '08:00:00', '16:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 1);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000002', 2, '08:00:00', '16:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 2);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000003', 3, '08:00:00', '16:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 3);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000004', 4, '08:00:00', '16:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 4);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000005', 5, '08:00:00', '16:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 5);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000006', 6, '08:00:00', '12:00:00', FALSE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 6);
INSERT INTO clinic_schedules (id, day_of_week, opens_at, closes_at, enabled, version)
SELECT '51000000-0000-0000-0000-000000000007', 7, '08:00:00', '12:00:00', TRUE, 0
WHERE NOT EXISTS (SELECT 1 FROM clinic_schedules WHERE day_of_week = 7);

CREATE INDEX IF NOT EXISTS idx_clinic_schedules_enabled_day
    ON clinic_schedules (enabled, day_of_week);
