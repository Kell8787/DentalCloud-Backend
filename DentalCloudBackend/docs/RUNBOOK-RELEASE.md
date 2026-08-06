# Runbook de release y recuperación

Este runbook aplica al backend de DentalCloud desplegado con Docker Compose y
PostgreSQL. Se ejecuta desde `DentalCloud-Backend/DentalCloudBackend`. No se
deben copiar secretos a tickets, logs, commits ni comandos compartidos.

## 1. Preflight y variables

1. Confirmar la rama y el repositorio antes de tocar el entorno:

   ```bash
   git status --short --branch
   git branch --show-current
   ```

2. Preparar `.env` desde `.env.example` fuera de Git. Como mínimo deben existir
   `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`,
   `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`,
   `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET` y `JWT_EXPIRATION_MS`.
3. Verificar que `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` y que
   `SPRING_FLYWAY_BASELINE_ON_MIGRATE` solo sea `true` para la preparación
   controlada de una base legacy local.
4. Confirmar una ventana de mantenimiento, un responsable de rollback y espacio
   suficiente para el respaldo.

## 2. Verificación y despliegue

Ejecutar en una copia limpia de la versión que se publicará:

```bash
./mvnw -B verify
docker compose config --quiet
docker compose build backend_app
```

Antes de reemplazar el servicio, tomar respaldo (sección 3). Después:

```bash
docker compose up -d --no-deps backend_app
docker compose ps
docker logs --tail 200 dentalcloud_backend
```

La aplicación debe arrancar con Flyway aplicado y `ddl-auto: validate`. Validar
la autenticación con una cuenta de prueba sin imprimir el token:

```bash
curl -fsS -o /tmp/dentalcloud-login.json \
  -H 'Content-Type: application/json' \
  -d '{"email":"<cuenta-de-prueba>","password":"<secreto>"}' \
  http://localhost:8080/api/auth/login
test -s /tmp/dentalcloud-login.json
rm -f /tmp/dentalcloud-login.json
```

## 3. Backup y restore de PostgreSQL

El respaldo debe ser formato custom y almacenarse fuera del repositorio, con
permisos restringidos y retención definida por operaciones:

```bash
umask 077
backup_file="/secure/backups/dentalcloud-$(date +%Y%m%d-%H%M%S).dump"
docker compose exec -T postgres_db pg_dump \
  -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Fc > "$backup_file"
sha256sum "$backup_file" > "$backup_file.sha256"
```

Probar periódicamente la restauración en una base aislada, nunca sobre la base
activa:

```bash
docker compose exec -T postgres_db createdb -U "$POSTGRES_USER" dentalcloud_restore
cat "$backup_file" | docker compose exec -T postgres_db pg_restore \
  -U "$POSTGRES_USER" -d dentalcloud_restore --no-owner --exit-on-error
docker compose exec -T postgres_db dropdb -U "$POSTGRES_USER" dentalcloud_restore
```

La restauración productiva requiere detener la aplicación, confirmar el backup
seleccionado y ejecutar el procedimiento aprobado por operaciones. Nunca se
borra el volumen productivo como atajo.

## 4. Migraciones y rollback

Antes de migrar, revisar el diff de la migración y confirmar que es versionada,
reversible o que tiene un procedimiento de recuperación documentado. Flyway se
ejecuta al arrancar:

```bash
docker compose up -d --no-deps backend_app
docker logs dentalcloud_backend 2>&1 | grep -E 'Flyway|Successfully applied|ERROR'
```

Después validar la versión aplicada sin modificar la tabla de historial:

```bash
docker compose exec -T postgres_db psql \
  -U "$POSTGRES_USER" -d "$POSTGRES_DB" \
  -c 'select installed_rank, version, description, success from flyway_schema_history order by installed_rank;'
```

El rollback normal es volver a la imagen anterior de la aplicación. No se
retrocede una migración aplicada eliminando filas de `flyway_schema_history`.
Si la versión anterior no puede leer el esquema nuevo, detener el servicio y
restaurar el backup validado en una base aislada/producción siguiendo la ventana
aprobada; luego arrancar la imagen compatible y verificar los datos.

## 5. Creación segura de staff

1. El administrador crea una cuenta mediante el flujo autorizado de registro y
   entrega la contraseña inicial por un canal seguro. No se registra en logs ni
   se guarda en texto plano en tickets.
2. El administrador autentica la cuenta y cambia su rol usando
   `PATCH /api/users/{id}/rol` con `{"nuevoRol":"SECRETARIA"}` o
   `{"nuevoRol":"DOCTOR"}`. Solo `ADMIN` puede ejecutar ese endpoint; al crear
   un doctor se genera su relación `Dentist`.
3. Confirmar `active=true`, correo/DUIs únicos y el rol esperado. Probar login y
   luego cerrar la sesión de verificación.
4. Revocar o desactivar la cuenta cuando termine su relación laboral. No
   compartir JWT, contraseñas ni datos clínicos durante la verificación.

## 6. Criterios de salida y escalamiento

- `docker compose ps` muestra PostgreSQL saludable y backend activo.
- Flyway termina sin error y `ddl-auto: validate` no reporta diferencias.
- Login de prueba, endpoint protegido y una lectura no clínica responden según
  contrato.
- Se conserva el backup, su checksum y el registro de quién aprobó el cambio.

Ante error de arranque, migración o pérdida de datos: detener nuevas escrituras,
conservar logs sin secretos, no ejecutar comandos destructivos y escalar al
responsable de operaciones con el backup y `traceId` correspondiente.
