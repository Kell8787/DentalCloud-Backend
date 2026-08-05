# Esqueleto clínico (DentalCloud Backend)

Esta entrega prepara el modelo persistente para planes de tratamiento,
documentos clínicos e instrucciones post-cita. Todavía no expone endpoints ni
interfaz de usuario; el contrato definitivo se cerrará en `F0-X-01`.

## Entidades

- `PatientTreatmentPlan`: relaciona paciente, tratamiento del catálogo y
  dentista. Sus estados son `PLANNED`, `ACTIVE`, `PAUSED`, `COMPLETED` y
  `CANCELLED`.
- `TreatmentStep`: pasos ordenados de un plan. La posición es única dentro del
  plan y sus estados son `PENDING`, `COMPLETED` y `SKIPPED`.
- `ClinicalDocument`: solo guarda metadatos y una `objectKey` privada para el
  almacenamiento externo. Debe relacionarse con una cita o un plan y puede
  marcarse como visible para el paciente.
- `AftercareInstruction`: contenido posterior a una cita. Solo se considera
  publicado cuando `publishedAt` tiene valor; la prioridad acepta `LOW`,
  `NORMAL` o `HIGH`.

## Seguridad prevista

Los identificadores de paciente no sustituyen las comprobaciones de ownership
del servicio. El paciente solo podrá consultar sus propios planes,
documentos visibles e instrucciones publicadas. El doctor podrá gestionar el
contenido clínico de sus pacientes y Secretaría no tendrá acceso a notas ni a
documentos clínicos.

## Documentos

`ClinicalDocument` conserva `mimeType`, `sizeBytes`, `checksum` y `objectKey`
para que la futura capa de almacenamiento valide el archivo y emita URLs
firmadas. No se almacenan binarios grandes en PostgreSQL ni se publican rutas
permanentes.
