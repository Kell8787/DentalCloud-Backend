# Esqueleto clínico (DentalCloud Backend)

Esta entrega prepara el modelo persistente para planes de tratamiento,
documentos clínicos e instrucciones post-cita. Los endpoints de planes y pasos
ya están disponibles para doctor, administrador y lectura propia del paciente.
Los documentos cuentan con almacenamiento privado local abstraído y URLs
firmadas de corta duración; las instrucciones post-cita quedan para F2-B2-03.

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

## Seguridad

Los identificadores de paciente no sustituyen las comprobaciones de ownership
del servicio. El paciente solo podrá consultar sus propios planes,
documentos visibles e instrucciones publicadas. El doctor podrá gestionar el
contenido clínico de sus pacientes y Secretaría no tiene acceso a planes ni
documentos clínicos. El porcentaje se calcula como pasos `COMPLETED` dividido
entre pasos totales; el cliente no puede enviarlo. Planes y pasos usan `version`
para detectar ediciones concurrentes.

## Documentos

`ClinicalDocument` conserva `mimeType`, `sizeBytes`, `checksum` y una `objectKey`
privada. `POST /api/documentos` recibe multipart, calcula el SHA-256 en el
servidor, valida PDF/JPEG/PNG hasta 10 MB y nunca acepta `objectKey` desde el
cliente. El adaptador local escribe fuera de PostgreSQL y puede sustituirse por
S3/MinIO; la respuesta entrega una URL firmada con expiración de 5 minutos.
`GET /api/patients/me/documents` solo devuelve metadatos visibles del paciente.
