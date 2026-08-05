# Disponibilidad configurable

La disponibilidad ya no depende de horarios escritos en `CitaService`. Flyway
crea `clinic_schedules` con un registro por día de la semana:

- `dayOfWeek`: ISO-8601, lunes `1` a domingo `7`.
- `opensAt` y `closesAt`: límites de atención.
- `enabled`: permite cerrar un día sin desplegar código.
- `version`: prepara edición concurrente del horario.

La consulta de slots y la creación/edición de citas consultan el mismo registro.
Un día deshabilitado no devuelve slots y rechaza nuevas citas. El intervalo
permitido incluye el margen de 15 minutos conservado del comportamiento
anterior.
