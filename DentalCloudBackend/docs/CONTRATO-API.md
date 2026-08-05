# Contrato API inicial

F0-X-01 congela el vocabulario y las reglas comunes que usarán backend y
frontend durante las fases 1–3. La especificación ejecutable está en
[`openapi.yaml`](openapi.yaml).

## Reglas no negociables

- La API usa rutas `/api/...`, JSON y timestamps ISO-8601.
- La zona horaria de negocio es `America/El_Salvador`; la interfaz puede
  presentar reloj de 12 horas.
- Los pacientes nunca envían `pacienteId` en una solicitud propia: el backend
  lo infiere del JWT.
- El ownership se comprueba dentro del servicio, además del filtro de rutas.
- `401` significa sesión ausente o inválida; `403`, sesión sin privilegio;
  `404` puede ocultar recursos ajenos; `409`, conflicto de slot o versión; y
  `422`, validación de dominio o transición inválida.
- Los errores tienen `status`, `code`, `message`, `fieldErrors`, `traceId` y
  `timestamp`.
- Las colecciones paginadas tienen `items`, `page`, `size`, `totalItems` y
  `totalPages`.

## Estados congelados

| Dominio | Estados de contrato |
|---|---|
| Cita | `SOLICITADA`, `CONFIRMADA`, `RECHAZADA`, `CANCELADA`, `INASISTENCIA`, `COMPLETADA` |
| Plan | `PLANNED`, `ACTIVE`, `PAUSED`, `COMPLETED`, `CANCELLED` |
| Paso | `PENDING`, `COMPLETED`, `SKIPPED` |
| Instrucción | `LOW`, `NORMAL`, `HIGH` como prioridad; publicada cuando `publishedAt` no es nulo |
| Inventario | `SIN_STOCK`, `BAJO`, `DISPONIBLE` como estados derivados |
| Origen de cita | `PATIENT_REQUEST`, `STAFF_CREATED` |

Las citas creadas por pacientes nacen en `SOLICITADA`; las creadas por
personal nacen en `CONFIRMADA`. Reagendar siempre crea una cita nueva y
conserva el vínculo con la anterior. Una cita cancelada no vuelve a estar
confirmada.

## Compatibilidad con el backend actual

Las rutas históricas (`/api/citas`, `/api/citas/slots-disponibles`,
`/api/citas/mis-citas` y `/api/patients/profile`) se mantienen durante la
migración. Las rutas canónicas nuevas aparecen en `openapi.yaml` y se
implementarán por vertical slice; no se deben crear nombres alternos desde el
frontend.

Este documento no introduce todavía endpoints de almacenamiento binario ni
expone `objectKey`: los documentos siguen siendo metadatos privados y las
descargas futuras devolverán URLs firmadas de corta duración.
