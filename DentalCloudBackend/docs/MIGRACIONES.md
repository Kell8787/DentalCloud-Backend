# Migraciones de base de datos

DentalCloud usa Flyway para versionar cambios de esquema. Hibernate queda en
`ddl-auto: validate`: valida que las entidades coincidan con la base de datos,
pero no crea ni modifica tablas.

## Esquema nuevo

Con PostgreSQL vacío, iniciar el backend normalmente. Flyway ejecuta
`V1__baseline_current_schema.sql`, después `V2__add_inventory_stock_controls.sql`, y registra ambas migraciones en
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

## Convención

- `V1__baseline_current_schema.sql`: esquema legado inicial.
- `V2__add_inventory_stock_controls.sql`: controles de stock y movimientos.
- `V3__<descripcion>.sql`: siguiente cambio de dominio posterior al baseline.
- No editar una migración que ya se ejecutó en un entorno compartido.
