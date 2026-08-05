-- Notas clínicas versionadas: los finales no se sobrescriben; una enmienda crea una nueva nota.

CREATE TABLE clinical_notes (
    id UUID NOT NULL,
    patient_id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    author_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
    body TEXT NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finalized_at TIMESTAMP(6) WITH TIME ZONE,
    amended_from_id UUID,
    amendment_reason TEXT,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_clinical_notes PRIMARY KEY (id),
    CONSTRAINT fk_clinical_notes_patient FOREIGN KEY (patient_id) REFERENCES dental_users (id),
    CONSTRAINT fk_clinical_notes_appointment FOREIGN KEY (appointment_id) REFERENCES citas (id),
    CONSTRAINT fk_clinical_notes_author FOREIGN KEY (author_id) REFERENCES dental_users (id),
    CONSTRAINT fk_clinical_notes_amended_from FOREIGN KEY (amended_from_id) REFERENCES clinical_notes (id),
    CONSTRAINT chk_clinical_notes_status CHECK (status IN ('DRAFT', 'FINAL', 'AMENDED')),
    CONSTRAINT chk_clinical_notes_finalized CHECK (status = 'DRAFT' OR finalized_at IS NOT NULL),
    CONSTRAINT chk_clinical_notes_amendment CHECK (status <> 'AMENDED' OR (amended_from_id IS NOT NULL
        AND amendment_reason IS NOT NULL AND length(trim(amendment_reason)) > 0))
);

CREATE TABLE clinical_note_audit (
    id UUID NOT NULL,
    note_id UUID NOT NULL,
    action VARCHAR(24) NOT NULL,
    actor_id UUID NOT NULL,
    detail TEXT,
    occurred_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_clinical_note_audit PRIMARY KEY (id),
    CONSTRAINT fk_clinical_note_audit_note FOREIGN KEY (note_id) REFERENCES clinical_notes (id),
    CONSTRAINT fk_clinical_note_audit_actor FOREIGN KEY (actor_id) REFERENCES dental_users (id)
);

CREATE INDEX idx_clinical_notes_patient_created ON clinical_notes (patient_id, created_at DESC);
CREATE INDEX idx_clinical_notes_appointment ON clinical_notes (appointment_id, created_at DESC);
CREATE INDEX idx_clinical_note_audit_note_occurred ON clinical_note_audit (note_id, occurred_at ASC);
