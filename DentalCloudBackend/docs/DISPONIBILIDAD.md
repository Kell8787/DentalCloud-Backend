# Disponibilidad de clínica

La disponibilidad usa la configuración interna creada por Flyway en
`clinic_schedules`, con un registro por día de la semana. No existe una pantalla
ni un endpoint administrativo para modificarla.

- `dayOfWeek`: ISO-8601, lunes `1` a domingo `7`.
- `opensAt` y `closesAt`: límites de atención.
- `enabled`: indica si la clínica atiende ese día.

La consulta de slots y la creación/edición de citas consultan el mismo registro.
Un día deshabilitado no devuelve slots y rechaza nuevas citas.

## Regla de franjas

- Los posibles inicios se generan cada 30 minutos desde la apertura (`:00` y
  `:30`).
- La duración real de la cita sigue determinada por el tratamiento.
- Solo se ofrece una franja cuando la cita completa termina dentro del horario
  de clínica; el cierre no tiene margen adicional.
- Una cita `SOLICITADA` o `CONFIRMADA` bloquea cualquier franja cuyo intervalo
  se solape con ella. Los estados terminales no bloquean disponibilidad.
- Cuando se consulta por plan de tratamiento, solo se devuelven franjas del
  doctor asignado al plan para conservar la continuidad clínica.
- La creación y el reagendamiento vuelven a validar la cuadrícula, el horario y
  los solapamientos para evitar reservas manipuladas o carreras concurrentes.

Con la configuración inicial, la clínica atiende de lunes a viernes de 8:00
a. m. a 4:00 p. m., permanece cerrada el sábado y atiende el domingo de 8:00
a. m. a 12:00 p. m.
