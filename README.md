# 🦷 DentalCloud Backend

Backend REST API para la gestión de una clínica dental. Construido con **Spring Boot 3**, **PostgreSQL** y contenedores **Docker**.

---

## 📋 Tabla de contenidos

- [Stack tecnológico](#-stack-tecnológico)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Levantar el proyecto con Docker](#-levantar-el-proyecto-con-docker)
- [Variables de entorno](#-variables-de-entorno)
- [Autenticación (JWT)](#-autenticación-jwt)
- [Endpoints — Autenticación](#-endpoints--autenticación)
- [Endpoints — Inventario](#-endpoints--inventario)
- [Endpoints — Citas](#-endpoints--citas)
- [Roles y permisos](#-roles-y-permisos)
- [Migraciones](#-migraciones)
- [Pruebas](#-pruebas)
- [Colección Insomnia](#-colección-insomnia)

---

## 🛠 Stack tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.x |
| Spring Security | JWT (JJWT 0.12.6) |
| PostgreSQL | 16 |
| Maven | 3.9.9 |
| Docker / Docker Compose | — |
| Lombok | — |

---

## 📁 Estructura del proyecto

```
DentalCloud-Backend/
└── DentalCloudBackend/
    ├── Dockerfile
    ├── docker-compose.yml
    ├── pom.xml
    ├── docs/
    │   └── MODULO-INVENTARIO.md      # Documentación detallada del módulo inventario
    ├── insomnia/
    │   ├── dentalcloud_inventory.yaml # Colección original del módulo inventario
    │   └── dentalcloud_completo.yaml  # Colección COMPLETA (Auth, Inventario y Citas)
    └── src/main/java/com/dentalcloud/dentalcloudbackend/
        ├── config/          # SecurityConfig, CorsConfig
        ├── controller/      # AuthController, InventoryController, CitasController
        ├── domain/
        │   ├── dto/         # Request y Response DTOs
        │   ├── entity/      # Entidades JPA
        │   └── enums/       # EstadoCita, Genero, Parentesco
        ├── handlers/        # GlobalExceptionHandler
        ├── repositories/    # Spring Data JPA repositories
        ├── security/        # JwtService, JwtAuthenticationFilter
        └── services/        # AuthService, InventoryService, CitaService
```

---

## 🐳 Levantar el proyecto con Docker

> **Requisito previo:** tener instalado [Docker Desktop](https://www.docker.com/products/docker-desktop/).

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd DentalCloud-Backend/DentalCloudBackend
```

### 2. Construir y levantar los contenedores

Crear el archivo local de variables a partir del ejemplo. El archivo `.env`
no debe subirse al repositorio.

```bash
cp .env.example .env
```

Reemplaza los valores de contraseña y `JWT_SECRET` antes de usar el entorno.

```bash
docker compose up -d
```

Esto levanta dos servicios:
- **`postgres_db`** — PostgreSQL 16 en el puerto `5432`
- **`dentalcloud_backend`** — API Spring Boot en el puerto `8080`

> El backend espera a que PostgreSQL esté saludable antes de arrancar (healthcheck configurado).

### 3. Verificar que los contenedores están corriendo

```bash
docker ps
```

### 4. Ver los logs en tiempo real

```bash
docker logs -f dentalcloud_backend
```

Para ver los logs de la base de datos:

```bash
docker logs -f postgres_db
```

### 5. Detener los contenedores

```bash
docker compose down
```

Para detener **y eliminar los volúmenes** (borra la base de datos):

```bash
docker compose down -v
```

---

## ⚙️ Variables de entorno

Las variables se cargan desde `.env` mediante Docker Compose. Usa
`.env.example` como plantilla y no subas `.env`.

| Variable | Valor de ejemplo / por defecto | Descripción |
|---|---|---|
| `POSTGRES_USER` | `DentalAdmin` | Usuario de PostgreSQL |
| `POSTGRES_PASSWORD` | *(definida en `.env`)* | Contraseña de PostgreSQL |
| `POSTGRES_DB` | `DentalCloudDB` | Base de datos de PostgreSQL |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<host>:5432/<db_name>` | URL de conexión a PostgreSQL |
| `SPRING_DATASOURCE_USERNAME` | *(usuario configurado)* | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | *(contraseña configurada)* | Contraseña de la base de datos |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | `validate` | Hibernate valida el esquema; Flyway aplica las migraciones |
| `SPRING_FLYWAY_BASELINE_ON_MIGRATE` | `true` solo para local | Registra una base legacy sin borrar datos |
| `SPRING_FLYWAY_BASELINE_VERSION` | `1` | Versión usada para el baseline inicial |
| `JWT_SECRET` | *(definida en `.env`)* | Clave secreta para firmar los JWT |
| `JWT_EXPIRATION_MS` | `1296000000` | Expiración del token en ms (15 días) |

## 🗃 Migraciones

La estructura de PostgreSQL se versiona con Flyway. Hibernate usa
`ddl-auto: validate`, por lo que valida las tablas pero no las crea ni las
modifica automáticamente.

- Una base nueva ejecuta `V1__baseline_current_schema.sql` al arrancar.
- Una base existente debe respaldarse y arrancar una vez con el perfil `local`
  para registrar el baseline sin borrar datos.
- Los cambios posteriores deben agregarse como migraciones nuevas y no se deben
  editar después de ejecutarse en un entorno compartido.

La guía completa está en
[`docs/MIGRACIONES.md`](./DentalCloudBackend/docs/MIGRACIONES.md).

## ✅ Pruebas

Desde `DentalCloud-Backend/DentalCloudBackend`:

```bash
# Suite completa: contexto con H2 y PostgreSQL/Flyway con Testcontainers
./mvnw -B verify
```

Si el archivo perdió el permiso de ejecución después de descargarlo, puede
usarse el equivalente portable:

```bash
bash mvnw -B verify
```

La prueba `PostgreSqlFlywayIntegrationTest` necesita Docker disponible. Levanta
un PostgreSQL efímero, ejecuta la migración `V1__baseline_current_schema.sql`,
valida el esquema con Hibernate (`ddl-auto: validate`) y comprueba que el seed
de doctores se puede crear sobre PostgreSQL real. No reutiliza el contenedor de
desarrollo ni modifica la base local.

---

## 🔐 Autenticación JWT

La API usa **JSON Web Tokens (JWT)** con el algoritmo `HS256`.

### Endpoints públicos (sin token)

- `POST /api/auth/register`
- `POST /api/auth/login`

### Todos los demás endpoints requieren el header:

```http
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

### Flujo típico

1. Registrar un usuario con `POST /api/auth/register`
2. Obtener el token con `POST /api/auth/login`
3. Copiar el valor del campo `token` de la respuesta
4. Incluirlo en todas las peticiones posteriores como `Authorization: Bearer <token>`

---

## 📡 Endpoints — Autenticación

Base URL: `http://localhost:8080`

---

### `POST /api/auth/register` — Registrar paciente

Registra un nuevo usuario con rol `CUSTOMER`.

**Body (JSON):**

```json
{
  "firstName": "Rodrigo",
  "secondName": "Antonio",
  "lastName": "Iraheta",
  "secondLastName": "Lopez",
  "genero": "MASCULINO",
  "dui": "12345678-9",
  "birthDate": "11-02-2003",
  "email": "rodri@test.com",
  "password": "123456",
  "confirmPassword": "123456",
  "phoneNumber": "7777-7777",
  "direccion": "San Salvador",
  "contactoEmergencia": {
    "nombreCompleto": "Maria Lopez",
    "email": "maria@test.com",
    "phoneNumber": "8888-8888",
    "parentesco": "MADRE"
  },
  "informacionMedica": {
    "alergias": ["Penicilina", "Polvo"],
    "medicamentos": ["Ibuprofeno"],
    "antecedentesMedicos": "Asma leve"
  }
}
```

| Campo | Tipo | Requerido | Notas |
|---|---|---|---|
| `firstName` | string | ✅ | — |
| `secondName` | string | ❌ | Opcional |
| `lastName` | string | ✅ | — |
| `secondLastName` | string | ❌ | Opcional |
| `genero` | enum | ✅ | `MASCULINO` \| `FEMENINO` |
| `dui` | string | ✅ | Formato: `12345678-9` |
| `birthDate` | string | ✅ | Formato: `dd-MM-yyyy` |
| `email` | string | ✅ | Debe ser email válido |
| `password` | string | ✅ | — |
| `confirmPassword` | string | ✅ | Debe coincidir con `password` |
| `phoneNumber` | string | ✅ | — |
| `direccion` | string | ✅ | — |
| `contactoEmergencia` | objeto | ❌ | Ver tabla siguiente |
| `informacionMedica` | objeto | ❌ | Ver tabla siguiente |

**`contactoEmergencia`:**

| Campo | Tipo | Notas |
|---|---|---|
| `nombreCompleto` | string | — |
| `email` | string | — |
| `phoneNumber` | string | — |
| `parentesco` | enum | `MADRE` \| `PADRE` \| `HERMANO` \| `HERMANA` \| `PAREJA` \| `TUTOR` \| `OTRO` |

**`informacionMedica`:**

| Campo | Tipo | Notas |
|---|---|---|
| `alergias` | string[] | Lista de alergias |
| `medicamentos` | string[] | Lista de medicamentos |
| `antecedentesMedicos` | string | Texto libre |

**Respuesta `200 OK`:**

```json
{
  "message": "Usuario registrado exitosamente"
}
```

---

### `POST /api/auth/login` — Iniciar sesión

**Body (JSON):**

```json
{
  "email": "rodri@test.com",
  "password": "123456"
}
```

**Respuesta `200 OK`:**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "uuid",
    "firstName": "Rodrigo",
    "secondName": "Antonio",
    "lastName": "Iraheta",
    "secondLastName": "Lopez",
    "direccion": "San Salvador",
    "genero": "MASCULINO",
    "dui": "12345678-9",
    "birthDate": "2003-02-11",
    "email": "rodri@test.com",
    "phoneNumber": "7777-7777",
    "role": "CUSTOMER"
  },
  "message": "Login exitoso"
}
```

---

### `GET /api/auth/validate` — Validar token

Requiere autenticación.

**Header:**
```http
Authorization: Bearer <token_jwt>
```

**Respuesta `200 OK`:**

```json
{
  "valid": true
}
```

---

## 📦 Endpoints — Inventario

Todos los endpoints de inventario requieren autenticación JWT.

Base path: `/api/inventory`

---

### Resumen de endpoints

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/api/inventory/categories` | Lista todas las categorías |
| `POST` | `/api/inventory/category` | Crea una categoría |
| `GET` | `/api/inventory` | Lista productos (con filtros opcionales) |
| `GET` | `/api/inventory/{id}` | Detalle de un producto por UUID |
| `GET` | `/api/inventory/category/{categoryId}` | Productos de una categoría |
| `POST` | `/api/inventory` | Crea un producto |
| `PUT` | `/api/inventory/{id}` | Actualiza un producto |
| `PUT` | `/api/inventory/{id}/purchase` | Suma stock (compra) |
| `PUT` | `/api/inventory/{id}/sale` | Resta stock (venta) |
| `DELETE` | `/api/inventory/{id}` | Elimina un producto (soft delete) |

---

### `GET /api/inventory/categories` — Listar categorías

**Respuesta `200 OK`:**

```json
[
  {
    "id": "uuid-categoria",
    "name": "Insumos"
  }
]
```

---

### `POST /api/inventory/category` — Crear categoría

**Body (JSON):**

```json
{
  "name": "Insumos"
}
```

**Respuesta `200 OK`:**

```json
{
  "id": "uuid-categoria",
  "name": "Insumos"
}
```

---

### `GET /api/inventory` — Listar / buscar productos

Sin parámetros devuelve todos los productos activos.

**Query params opcionales (combinables):**

| Parámetro | Descripción |
|---|---|
| `name` | Fragmento del nombre del producto (insensible a mayúsculas) |
| `categoryName` | Nombre exacto de la categoría (insensible a mayúsculas) |

**Ejemplos:**

```http
GET /api/inventory
GET /api/inventory?name=guante
GET /api/inventory?categoryName=Insumos
GET /api/inventory?categoryName=Insumos&name=latex
```

**Respuesta `200 OK`:** lista de objetos `InventoryResponseDTO` (ver estructura abajo).

---

### `GET /api/inventory/{id}` — Detalle de producto

```http
GET /api/inventory/b2c3d4e5-f6a7-8901-bcde-f12345678901
```

**Respuesta `200 OK`:**

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "Guantes nitrilo M",
  "description": "Caja 100 uds",
  "purchasePrice": 12.5,
  "salePrice": 22.0,
  "categoryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "categoryName": "Insumos",
  "quantity": 60
}
```

---

### `GET /api/inventory/category/{categoryId}` — Productos por categoría

```http
GET /api/inventory/category/a1b2c3d4-e5f6-7890-abcd-ef1234567890
```

**Respuesta `200 OK`:** lista de objetos `InventoryResponseDTO`.

---

### `POST /api/inventory` — Crear producto

**Body (JSON):**

```json
{
  "name": "Guantes nitrilo M",
  "description": "Caja 100 uds",
  "purchasePrice": 12.50,
  "salePrice": 22.00,
  "categoryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "quantity": 50
}
```

| Campo | Tipo | Requerido | Reglas |
|---|---|---|---|
| `name` | string | ✅ | No vacío |
| `description` | string | ✅ | No vacío |
| `purchasePrice` | número | ✅ | ≥ 0 |
| `salePrice` | número | ✅ | ≥ 0 |
| `categoryId` | UUID | ✅ | Debe existir en BD |
| `quantity` | entero | ✅ | ≥ 0 |

**Respuesta `200 OK`:** objeto `InventoryResponseDTO`.

---

### `PUT /api/inventory/{id}` — Actualizar producto

Mismo body que `POST /api/inventory`.

---

### `PUT /api/inventory/{id}/purchase` — Compra / ingreso de stock

**Body (JSON):**

```json
{
  "quantity": 10
}
```

Suma `quantity` al stock actual. Mínimo: `1`.

**Respuesta `200 OK`:** objeto `InventoryResponseDTO` con el nuevo stock.

---

### `PUT /api/inventory/{id}/sale` — Venta / salida de stock

**Body (JSON):**

```json
{
  "quantity": 3
}
```

Resta `quantity` del stock actual. Si la cantidad pedida supera el stock disponible, devuelve `400 Bad Request` con mensaje de error.

---

### `DELETE /api/inventory/{id}` — Eliminar producto (soft delete)

No requiere body. El producto se marca como eliminado (`deleted = true`) pero permanece en la base de datos.

**Respuesta `200 OK`:**

```json
{
  "message": "Producto eliminado correctamente"
}
```

---

## 📅 Endpoints — Citas

Todos los endpoints de citas requieren autenticación JWT.

Base path: `/api/citas`

---

### Resumen de endpoints

| Método | Ruta | Roles | Descripción |
|---|---|---|---|
| `POST` | `/api/citas` | Autenticado | Crear una cita |
| `GET` | `/api/citas/slots-disponibles` | Autenticado | Consultar horarios disponibles |
| `PATCH` | `/api/citas/{id}/aprobar` | `SECRETARIA`, `ADMIN` | Aprobar una cita |
| `PATCH` | `/api/citas/{id}/rechazar` | `SECRETARIA`, `ADMIN` | Rechazar una cita |
| `PATCH` | `/api/citas/{id}/cancelar` | `CUSTOMER`, `SECRETARIA`, `ADMIN` | Cancelar una cita |

---

### `POST /api/citas` — Crear cita

**Body (JSON):**

```json
{
  "pacienteId": "uuid-del-paciente",
  "dentistaId": "uuid-del-dentista",
  "tratamientoId": "uuid-del-tratamiento",
  "fecha": "2025-06-15",
  "horaInicio": "09:00",
  "motivo": "Limpieza dental de rutina"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `pacienteId` | UUID | ID del paciente |
| `dentistaId` | UUID | ID del dentista |
| `tratamientoId` | UUID | ID del tratamiento |
| `fecha` | date | Fecha de la cita (`yyyy-MM-dd`) |
| `horaInicio` | time | Hora de inicio (`HH:mm`) |
| `motivo` | string | Motivo o descripción de la cita |

**Respuesta `200 OK`:**

```json
{
  "id": "uuid-de-la-cita",
  "pacienteId": "uuid-del-paciente",
  "dentistaId": "uuid-del-dentista",
  "tratamientoId": "uuid-del-tratamiento",
  "fecha": "2025-06-15",
  "horaInicio": "09:00",
  "duracionMinutos": 30,
  "precio": 25.00,
  "motivo": "Limpieza dental de rutina",
  "estadoCita": "PENDIENTE"
}
```

---

### `GET /api/citas/slots-disponibles` — Consultar slots disponibles

**Query params:**

| Parámetro | Tipo | Descripción |
|---|---|---|
| `fecha` | date | Fecha consultada (`yyyy-MM-dd`) |
| `tratamientoId` | UUID | ID del tratamiento |

**Ejemplo:**

```http
GET /api/citas/slots-disponibles?fecha=2025-06-15&tratamientoId=uuid-tratamiento
```

**Respuesta `200 OK`:**

```json
[
  {
    "dentistaId": "uuid-dentista",
    "dentistaNombre": "Dr. Martínez",
    "horasDisponibles": ["09:00", "09:30", "10:00"]
  }
]
```

---

### `PATCH /api/citas/{id}/aprobar` — Aprobar cita

Requiere rol `SECRETARIA` o `ADMIN`. Sin body.

**Respuesta `200 OK`:** objeto `CitaResponseDTO` con `estadoCita: "CONFIRMADA"`.

---

### `PATCH /api/citas/{id}/rechazar` — Rechazar cita

Requiere rol `SECRETARIA` o `ADMIN`.

**Body (JSON, opcional):**

```json
{
  "motivo": "El dentista no está disponible ese día"
}
```

**Respuesta `200 OK`:** objeto `CitaResponseDTO` con `estadoCita: "CANCELADA"`.

---

### `PATCH /api/citas/{id}/cancelar` — Cancelar cita

Requiere rol `CUSTOMER`, `SECRETARIA` o `ADMIN`. Sin body.

**Respuesta `200 OK`:** objeto `CitaResponseDTO` con `estadoCita: "CANCELADA"`.

---

## 🎭 Roles y permisos

| Rol | Descripción |
|---|---|
| `CUSTOMER` | Paciente registrado. Puede crear y cancelar sus citas. |
| `SECRETARIA` | Puede aprobar, rechazar y cancelar citas. |
| `ADMIN` | Acceso total a todos los endpoints. |

**Estados posibles de una cita (`EstadoCita`):**

| Estado | Descripción |
|---|---|
| `PENDIENTE` | Cita creada, esperando confirmación |
| `CONFIRMADA` | Cita aprobada por secretaria/admin |
| `CANCELADA` | Cita cancelada o rechazada |
| `FINALIZADA` | Cita completada |

---

## 🔧 Errores comunes

| Código | Causa |
|---|---|
| `400 Bad Request` | Campos inválidos, vender más stock del disponible, contraseñas no coinciden |
| `401 Unauthorized` | Token JWT no enviado o inválido |
| `403 Forbidden` | El rol del usuario no tiene permiso para la operación |
| `404 Not Found` | Recurso no encontrado (producto, categoría, cita, etc.) |

---

## 📂 Colección Insomnia

Dispones de dos archivos de colección listos para importar en [Insomnia](https://insomnia.rest/):

* **`insomnia/dentalcloud_completo.yaml` (Recomendado):** Contiene todos los módulos desarrollados: Autenticación, Inventario y Citas.
* **`insomnia/dentalcloud_inventory.yaml`:** Colección original que contiene únicamente Autenticación e Inventario.

### Cómo importar

1. Abrir Insomnia.
2. Ir a **File → Import**.
3. Seleccionar el archivo `insomnia/dentalcloud_completo.yaml` (o el de inventario si solo deseas ese módulo).
4. En la colección importada, editar el entorno (**Base Environment**):
   - `base_url`: `http://localhost:8080`
   - `login_email` y `login_password` con tus credenciales.
   - Completar las variables de ejemplo (como `paciente_id`, `dentista_id`, `tratamiento_id`, etc.) con UUIDs reales para facilitar tus pruebas de Citas.
5. Ejecutar la petición **Login (POST)** en la carpeta *Autenticación* para obtener el JWT.
6. Copiar el valor devuelto y colocarlo en la variable `token` de tu entorno. ¡Listo! Todas las peticiones autenticadas utilizarán este token automáticamente.

---

## 📖 Documentación adicional

- [`DentalCloudBackend/docs/MODULO-INVENTARIO.md`](./DentalCloudBackend/docs/MODULO-INVENTARIO.md) — Documentación detallada del módulo de inventario con reglas de negocio, validaciones y ejemplos de respuesta.
