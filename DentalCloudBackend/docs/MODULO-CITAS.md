# Modelo de citas tipado

F1-B1-01 migra las citas desde `fecha_cita`/`hora`/`hora_fin` hacia el
intervalo `starts_at`/`ends_at`. La API canónica devuelve ambos extremos en
ISO-8601 y el backend calcula `endsAt` usando la duración del tratamiento.

## Estados y origen

- `SOLICITADA`: solicitud creada por el paciente.
- `CONFIRMADA`: cita aceptada o creada directamente por personal.
- `RECHAZADA`: solicitud rechazada con motivo.
- `CANCELADA`: cita cancelada, conservando motivo cuando aplica.
- `INASISTENCIA`: el paciente no se presentó.
- `COMPLETADA`: atención finalizada.
- `PATIENT_REQUEST` y `STAFF_CREATED` identifican el origen.

Los registros existentes se migran sin borrar sus columnas históricas. Como el
esquema anterior no registraba el origen, los registros migrados se marcan
`STAFF_CREATED` y las nuevas operaciones ya deben indicar el origen real.

## Rutas canónicas iniciales

- `POST /api/citas/solicitudes`: paciente autenticado; el paciente se infiere
  del JWT y el plan determina tratamiento y doctor.
- `POST /api/citas`: Secretaría, doctor o administrador; recibe el paciente y
  doctor explícitos.
- `GET /api/citas/disponibilidad`: devuelve slots con doctor, `startsAt` y
  `endsAt`. Los inicios se alinean a intervalos de 30 minutos, la duración
  depende del tratamiento y las citas solicitadas o confirmadas que se solapan
  no se devuelven como disponibles.
- `GET /api/citas/mias`: devuelve las citas del paciente autenticado.

Las rutas históricas permanecen como aliases durante la migración, pero las
mutaciones nuevas deben usar las rutas canónicas y comprobar el rol y el
ownership en el servicio.

Las creaciones aceptan el header opcional `Idempotency-Key`. Para solicitudes
repetidas del mismo paciente se devuelve el mismo recurso; la clave no puede
reutilizarse para otro paciente. Las reservas por doctor se serializan dentro
de la transacción para que el mismo slot no se confirme dos veces por una
condición de carrera.

Cada creación y transición agrega un evento inmutable con actor, motivo y
timestamps. No se revive una cita terminal ni se registra inasistencia antes
del inicio de la franja.
