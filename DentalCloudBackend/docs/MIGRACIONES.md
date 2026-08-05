# Migraciones de base de datos

DentalCloud usa Flyway para versionar cambios de esquema. Hibernate queda en
`ddl-auto: validate`: valida que las entidades coincidan con la base de datos,
pero no crea ni modifica tablas.

## Esquema nuevo

Con PostgreSQL vacío, iniciar el backend normalmente. Flyway ejecuta
`V1__baseline_current_schema.sql`, después `V2__add_inventory_stock_controls.sql`,
`V3__add_clinical_plan_documents_instructions.sql` y
`V4__migrate_appointments_to_typed_schedule.sql`, y registra todas las migraciones en
`flyway_schema_history`.

```bash
cp .env.example .env
docker compose up -d --build
```

El perfil `test` usa una base H2 efímera para validar el arranque y la
compatibilidad básica entre Flyway y las entidades. La validación definitiva
de PostgreSQL se ejecuta con Testcontainers:

```bash
./mvnw -B -Dtest=PostgreSqlFlywayIntegrationTest test
```

La prueba crea un contenedor efímero de PostgreSQL 16, ejecuta el baseline,
valida las entidades con `ddl-auto: validate` y comprueba el seed de doctores.
Docker debe estar disponible antes de ejecutar el comando.

## Base existente creada por Hibernate

Antes de migrar una base existente:

1. Crear un respaldo de PostgreSQL.
2. Comparar sus tablas con `V1__baseline_current_schema.sql`.
3. Iniciar una vez con el perfil `local`, que usa `baseline-on-migrate: true` y
   registra la estructura actual como versión `1` sin borrar datos.
4. Revisar `flyway_schema_history` y confirmar que la aplicación arranca con
   `ddl-auto: validate`.
5. Para producción, mantener `baseline-on-migrate: false` y ejecutar el
   baseline de forma controlada durante la preparación del entorno.

El baseline no corrige diferencias entre una base existente y el esquema
esperado. Cualquier cambio posterior debe agregarse como una migración nueva,
y debe ser reversible o contar con un procedimiento de rollback documentado.

## V2 — controles de inventario

`V2__add_inventory_stock_controls.sql` agrega `minimum_stock`, `unit` y
`version` a `inventory_products`, inicializando los registros existentes con
`0`, `unidad` y `0`, respectivamente. También crea `stock_movements` con
claves foráneas a producto y actor, checks de tipo/cantidad e índices de
consulta.

Si se necesita revertir V2 antes de usar los nuevos campos o movimientos, debe
hacerse con respaldo y una ventana controlada: eliminar primero los índices y
la tabla `stock_movements`, después retirar las restricciones y las tres
columnas agregadas de `inventory_products`, y finalmente eliminar la entrada
V2 de `flyway_schema_history`. No se debe editar una migración ya aplicada ni
ejecutar este rollback si ya existen datos que dependan de esos campos.

## V3 — esqueleto clínico

`V3__add_clinical_plan_documents_instructions.sql` crea los planes de
tratamiento, sus pasos, los metadatos de documentos clínicos y las
instrucciones post-cita. Las claves foráneas conservan la relación con
pacientes, dentistas, tratamientos y citas existentes; los binarios no se
guardan en estas tablas.

## V4 — citas con intervalo tipado

`V4__migrate_appointments_to_typed_schedule.sql` agrega `starts_at`, `ends_at`,
`status`, `source`, `treatment_plan_id`, `rescheduled_from_id`,
`cancellation_reason` y `version` a `citas`. Los valores existentes se
transforman de forma conservadora: `PENDIENTE` pasa a `SOLICITADA`,
`FINALIZADA` a `COMPLETADA` y los estados históricos no activos a
`CANCELADA`. Las columnas históricas se conservan durante la transición, pero
el código nuevo solo usa el intervalo tipado.

La fuente de registros anteriores no puede inferirse del esquema legado, por
lo que se inicializa como `STAFF_CREATED` y queda documentada para auditoría.
Los checks de intervalo, estado y origen, junto con los índices por doctor,
paciente y fecha, protegen el nuevo modelo sin eliminar datos existentes.

## V5 — horario configurable

`V5__add_clinic_schedule.sql` crea `clinic_schedules` con un registro por día,
semilla el horario actual de la clínica y permite deshabilitar días sin tocar
el código de disponibilidad. La semilla conserva lunes-viernes de 08:00 a
16:00, sábado cerrado y domingo de 08:00 a 12:00.

## V6 — idempotencia de citas

`V6__add_appointment_idempotency.sql` agrega una clave opcional única para
reintentos seguros de creación. Una misma clave para el mismo paciente devuelve
la cita ya creada; reutilizarla desde otro paciente responde conflicto. La
creación también bloquea la fila del doctor durante la comprobación y el
guardado para serializar reservas concurrentes.

## V7 — historial de estados de cita

`V7__add_appointment_status_events.sql` crea un historial inmutable de cada
transición con estado anterior, estado nuevo, motivo, actor y timestamp. La
creación registra el estado inicial; las acciones posteriores solo pueden
seguir la máquina de estados del contrato.

## Convención

- `V1__baseline_current_schema.sql`: esquema legado inicial.
- `V2__add_inventory_stock_controls.sql`: controles de stock y movimientos.
- `V3__add_clinical_plan_documents_instructions.sql`: esqueleto clínico.
- `V4__migrate_appointments_to_typed_schedule.sql`: intervalo, estado y origen de citas.
- `V5__add_clinic_schedule.sql`: horario semanal configurable.
- `V6__add_appointment_idempotency.sql`: reintentos y reservas concurrentes.
- `V7__add_appointment_status_events.sql`: historial inmutable de transiciones.
- `V8__<descripcion>.sql`: siguiente cambio de dominio posterior al baseline.
- No editar una migración que ya se ejecutó en un entorno compartido.
