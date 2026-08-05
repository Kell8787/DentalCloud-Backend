-- Esqueleto clínico: planes, pasos, documentos e instrucciones post-cita.

CREATE TABLE patient_treatment_plans (
    id UUID NOT NULL,
    patient_id UUID NOT NULL,
    treatment_id UUID NOT NULL,
    dentist_id UUID NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PLANNED',
    started_at TIMESTAMP(6) WITH TIME ZONE,
    expected_end_at TIMESTAMP(6) WITH TIME ZONE,
    cancellation_reason TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_patient_treatment_plans PRIMARY KEY (id),
    CONSTRAINT fk_patient_treatment_plans_patient
        FOREIGN KEY (patient_id) REFERENCES dental_users (id),
    CONSTRAINT fk_patient_treatment_plans_treatment
        FOREIGN KEY (treatment_id) REFERENCES tratamientos (id),
    CONSTRAINT fk_patient_treatment_plans_dentist
        FOREIGN KEY (dentist_id) REFERENCES dentists (id),
    CONSTRAINT chk_patient_treatment_plans_status
        CHECK (status IN ('PLANNED', 'ACTIVE', 'PAUSED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_patient_treatment_plans_cancel_reason
        CHECK (status <> 'CANCELLED' OR cancellation_reason IS NOT NULL),
    CONSTRAINT chk_patient_treatment_plans_dates
        CHECK (expected_end_at IS NULL OR started_at IS NULL OR expected_end_at >= started_at)
);

CREATE TABLE treatment_steps (
    id UUID NOT NULL,
    plan_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    position INTEGER NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    completed_at TIMESTAMP(6) WITH TIME ZONE,
    completed_by UUID,
    observation TEXT,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_treatment_steps PRIMARY KEY (id),
    CONSTRAINT fk_treatment_steps_plan
        FOREIGN KEY (plan_id) REFERENCES patient_treatment_plans (id),
    CONSTRAINT fk_treatment_steps_completed_by
        FOREIGN KEY (completed_by) REFERENCES dental_users (id),
    CONSTRAINT uk_treatment_steps_plan_position UNIQUE (plan_id, position),
    CONSTRAINT chk_treatment_steps_position_positive CHECK (position > 0),
    CONSTRAINT chk_treatment_steps_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
    CONSTRAINT chk_treatment_steps_completion_data
        CHECK (status = 'PENDING' OR completed_at IS NOT NULL)
);

CREATE TABLE clinical_documents (
    id UUID NOT NULL,
    patient_id UUID NOT NULL,
    appointment_id UUID,
    plan_id UUID,
    document_type VARCHAR(32) NOT NULL,
    title VARCHAR(180) NOT NULL,
    object_key VARCHAR(512) NOT NULL,
    mime_type VARCHAR(128) NOT NULL,
    size_bytes BIGINT NOT NULL,
    checksum VARCHAR(128) NOT NULL,
    visible_to_patient BOOLEAN NOT NULL DEFAULT FALSE,
    created_by UUID NOT NULL,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_clinical_documents PRIMARY KEY (id),
    CONSTRAINT fk_clinical_documents_patient
        FOREIGN KEY (patient_id) REFERENCES dental_users (id),
    CONSTRAINT fk_clinical_documents_appointment
        FOREIGN KEY (appointment_id) REFERENCES citas (id),
    CONSTRAINT fk_clinical_documents_plan
        FOREIGN KEY (plan_id) REFERENCES patient_treatment_plans (id),
    CONSTRAINT fk_clinical_documents_created_by
        FOREIGN KEY (created_by) REFERENCES dental_users (id),
    CONSTRAINT uk_clinical_documents_object_key UNIQUE (object_key),
    CONSTRAINT chk_clinical_documents_size_positive CHECK (size_bytes > 0),
    CONSTRAINT chk_clinical_documents_parent
        CHECK (appointment_id IS NOT NULL OR plan_id IS NOT NULL)
);

CREATE TABLE aftercare_instructions (
    id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    author_id UUID NOT NULL,
    title VARCHAR(180) NOT NULL,
    body TEXT NOT NULL,
    priority VARCHAR(16) NOT NULL DEFAULT 'NORMAL',
    published_at TIMESTAMP(6) WITH TIME ZONE,
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_aftercare_instructions PRIMARY KEY (id),
    CONSTRAINT fk_aftercare_instructions_appointment
        FOREIGN KEY (appointment_id) REFERENCES citas (id),
    CONSTRAINT fk_aftercare_instructions_patient
        FOREIGN KEY (patient_id) REFERENCES dental_users (id),
    CONSTRAINT fk_aftercare_instructions_author
        FOREIGN KEY (author_id) REFERENCES dental_users (id),
    CONSTRAINT chk_aftercare_instructions_priority
        CHECK (priority IN ('LOW', 'NORMAL', 'HIGH'))
);

CREATE INDEX idx_patient_treatment_plans_patient_status
    ON patient_treatment_plans (patient_id, status);

CREATE INDEX idx_patient_treatment_plans_dentist_status
    ON patient_treatment_plans (dentist_id, status);

CREATE INDEX idx_treatment_steps_plan_position
    ON treatment_steps (plan_id, position);

CREATE INDEX idx_clinical_documents_patient_visible
    ON clinical_documents (patient_id, visible_to_patient, created_at);

CREATE INDEX idx_clinical_documents_plan
    ON clinical_documents (plan_id);

CREATE INDEX idx_clinical_documents_appointment
    ON clinical_documents (appointment_id);

CREATE INDEX idx_aftercare_instructions_patient_published
    ON aftercare_instructions (patient_id, published_at);

CREATE INDEX idx_aftercare_instructions_appointment
    ON aftercare_instructions (appointment_id);
