-- Local-only cleanup for data created by DentalCloud E2E runs.
-- Run only against the disposable local Docker database after reviewing the
-- preview queries below. This is intentionally not a Flyway migration.

BEGIN;

CREATE TEMP TABLE cleanup_e2e_users ON COMMIT DROP AS
SELECT id FROM dental_users WHERE lower(email) LIKE 'e2e.%';

CREATE TEMP TABLE cleanup_e2e_treatments ON COMMIT DROP AS
SELECT id FROM tratamientos WHERE nombre LIKE 'Flujo E2E %';

CREATE TEMP TABLE cleanup_e2e_products ON COMMIT DROP AS
SELECT id FROM inventory_products WHERE name LIKE 'E2E %';

CREATE TEMP TABLE cleanup_e2e_categories ON COMMIT DROP AS
SELECT id FROM inventory_categories WHERE name LIKE 'E2E-%';

CREATE TEMP TABLE cleanup_e2e_plans ON COMMIT DROP AS
SELECT id FROM patient_treatment_plans
WHERE patient_id IN (SELECT id FROM cleanup_e2e_users)
   OR treatment_id IN (SELECT id FROM cleanup_e2e_treatments);

CREATE TEMP TABLE cleanup_e2e_appointments ON COMMIT DROP AS
SELECT id FROM citas
WHERE user_id IN (SELECT id FROM cleanup_e2e_users)
   OR tratamiento_id IN (SELECT id FROM cleanup_e2e_treatments)
   OR treatment_plan_id IN (SELECT id FROM cleanup_e2e_plans);

-- Child clinical/audit rows first.
DELETE FROM clinical_note_audit
WHERE note_id IN (
    SELECT id FROM clinical_notes
    WHERE patient_id IN (SELECT id FROM cleanup_e2e_users)
       OR appointment_id IN (SELECT id FROM cleanup_e2e_appointments)
);
DELETE FROM clinical_documents
WHERE patient_id IN (SELECT id FROM cleanup_e2e_users)
   OR appointment_id IN (SELECT id FROM cleanup_e2e_appointments)
   OR plan_id IN (SELECT id FROM cleanup_e2e_plans);
DELETE FROM clinical_notes
WHERE patient_id IN (SELECT id FROM cleanup_e2e_users)
   OR appointment_id IN (SELECT id FROM cleanup_e2e_appointments);
DELETE FROM aftercare_instructions
WHERE patient_id IN (SELECT id FROM cleanup_e2e_users)
   OR appointment_id IN (SELECT id FROM cleanup_e2e_appointments);
DELETE FROM appointment_status_events
WHERE appointment_id IN (SELECT id FROM cleanup_e2e_appointments);
DELETE FROM citas WHERE id IN (SELECT id FROM cleanup_e2e_appointments);

DELETE FROM treatment_steps WHERE plan_id IN (SELECT id FROM cleanup_e2e_plans);
DELETE FROM patient_treatment_plans WHERE id IN (SELECT id FROM cleanup_e2e_plans);

DELETE FROM stock_movements
WHERE product_id IN (SELECT id FROM cleanup_e2e_products)
   OR actor_id IN (SELECT id FROM cleanup_e2e_users);
DELETE FROM inventory_products WHERE id IN (SELECT id FROM cleanup_e2e_products);
DELETE FROM inventory_categories WHERE id IN (SELECT id FROM cleanup_e2e_categories)
  AND NOT EXISTS (SELECT 1 FROM inventory_products p WHERE p.category_id = inventory_categories.id);

DELETE FROM tratamientos
WHERE id IN (SELECT id FROM cleanup_e2e_treatments)
  AND NOT EXISTS (SELECT 1 FROM citas c WHERE c.tratamiento_id = tratamientos.id)
  AND NOT EXISTS (SELECT 1 FROM patient_treatment_plans p WHERE p.treatment_id = tratamientos.id);

DELETE FROM user_alergias WHERE user_id IN (
    SELECT id FROM informacion_medica WHERE user_id IN (SELECT id FROM cleanup_e2e_users)
);
DELETE FROM user_medicamentos WHERE user_id IN (
    SELECT id FROM informacion_medica WHERE user_id IN (SELECT id FROM cleanup_e2e_users)
);
DELETE FROM informacion_medica WHERE user_id IN (SELECT id FROM cleanup_e2e_users);
DELETE FROM contacto_emergencia WHERE user_id IN (SELECT id FROM cleanup_e2e_users);
DELETE FROM dentists WHERE user_id IN (SELECT id FROM cleanup_e2e_users);
DELETE FROM dental_users
WHERE id IN (SELECT id FROM cleanup_e2e_users)
  AND NOT EXISTS (SELECT 1 FROM citas c WHERE c.user_id = dental_users.id)
  AND NOT EXISTS (SELECT 1 FROM appointment_status_events e WHERE e.actor_id = dental_users.id)
  AND NOT EXISTS (SELECT 1 FROM stock_movements m WHERE m.actor_id = dental_users.id);

COMMIT;
