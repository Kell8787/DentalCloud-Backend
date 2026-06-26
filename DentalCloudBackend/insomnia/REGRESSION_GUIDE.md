# Guía de Regresión — DentalCloud API

Suite automatizada de 10 flujos que cubre los caminos principales de la API.
Se ejecuta localmente contra `http://localhost:8080`.

---

## Requisitos

| Herramienta | Versión mínima | Instalación |
|---|---|---|
| Node.js | 18+ | [nodejs.org](https://nodejs.org) |
| inso CLI | 9+ | `npm install -g insomnia-inso` |
| Backend corriendo | — | `./mvnw spring-boot:run` |

Verificar versión de inso:
```bash
inso --version
```

---

## Pre-condiciones en la base de datos

Antes de correr los tests, estos dos usuarios deben existir en la BD:

| Usuario | Password | Rol |
|---|---|---|
| `admin@dentalcloud.com` | `Admin123!` | ADMIN |
| `doctor1@dentalcloud.com` | `Doctor123!` | DOCTOR |

Los demás usuarios (`test.paciente@...`, `test.secretaria@...`) los crean los tests automáticamente.

---

## Ejecutar la suite

```bash
# Desde la raíz del proyecto
inso run test insomnia/regression_tests.yaml --env Test
```

Correr un flujo específico:
```bash
inso run test insomnia/regression_tests.yaml --env Test --testNamePattern "Test 5"
```

Exportar reporte en formato JUnit (para CI):
```bash
inso run test insomnia/regression_tests.yaml --env Test --reporter junit --output test-results.xml
```

---

## Flujos y dependencias

Los tests deben correr **en orden** porque cada uno produce datos que el siguiente consume.

```
Test 1 → admin_token
   ├── Test 2  verifica dentistId de doctores
   ├── Test 3  crea tratamiento → tratamiento_id
   │      └── Test 5  paciente crea cita → cita_id
   │             └── Test 7  secretaria aprueba cita
   ├── Test 4  crea paciente → paciente_token, paciente_id
   ├── Test 6  crea secretaria → secretaria_token, secretaria_id
   ├── Test 9  crea categorías → cat_*_id
   │      └── Test 10 crea 9 productos
   └── Test 8  doctor lista agenda (independiente)
```

---

## Descripción de cada test

### Test 1 — Login Admin
Autentica con `admin@dentalcloud.com`. Guarda `admin_token` para todos los tests siguientes.

### Test 2 — Verificar doctores
Lista `/api/users/doctors`. Si algún doctor no tiene `dentistId` (vínculo con entidad Dentist), hace PATCH de rol para forzar la vinculación automática. Verifica que todos terminan con `dentistId`.

### Test 3 — Crear tratamiento
Crea el tratamiento **"Limpieza Regresion"** vía `/api/tratamientos`. Guarda `tratamiento_id`. Verifica que aparece en el listado.

### Test 4 — Crear paciente
Registra `test.paciente@dentalcloud.com` (DUI: `99999901-0`). Si ya existe, continúa. Hace login y guarda `paciente_token` y `paciente_id`.

### Test 5 — Paciente crea cita
Con el token del paciente:
1. Confirma que el tratamiento de regresión existe.
2. Consulta slots disponibles para `2026-07-07` con el `tratamiento_id`.
3. Crea la cita con el primer slot disponible.
4. Verifica que el estado es `PENDIENTE`. Guarda `cita_id`.

### Test 6 — Crear secretaria
Registra `test.secretaria@dentalcloud.com` (DUI: `99999902-0`). Si ya existe, continúa. Hace login, guarda `secretaria_token` y `secretaria_id`. Admin hace PATCH para asignar rol `SECRETARIA`.

### Test 7 — Secretaria aprueba cita
Con el token de secretaria:
1. Lista todas las citas y encuentra la del test 5 en estado `PENDIENTE`.
2. Aprueba la cita vía `/api/citas/{id}/aprobar`.
3. Verifica que el estado pasó a `CONFIRMADA`.

### Test 8 — Doctor lista agenda
Login con `doctor1@dentalcloud.com`. Llama `/api/citas/mi-agenda` y verifica que responde 200 con una lista. **Independiente del resto de los flujos.**

### Test 9 — Crear categorías de inventario
Crea tres categorías: **Materiales**, **Productos**, **Medicinas**. Guarda los IDs de cada una. Verifica que aparecen en el listado.

### Test 10 — Crear productos
Crea 3 productos por cada categoría del test 9 (9 en total):

| Materiales | Productos | Medicinas |
|---|---|---|
| Guantes de Latex | Resina Compuesta A2 | Lidocaina 2% |
| Mascarillas Quirurgicas | Cemento de Ionomero | Ibuprofeno 400mg |
| Agujas Dentales | Amalgama Dental | Clindamicina 300mg |

Verifica que el inventario tiene al menos 9 productos.

---

## Variables de entorno

Todas viven en el bloque `environments.data` del archivo `regression_tests.yaml`.

| Variable | Valor inicial | Quién la llena |
|---|---|---|
| `base_url` | `http://localhost:8080` | manual |
| `admin_email` | `admin@dentalcloud.com` | manual |
| `admin_password` | `Admin123!` | manual |
| `doctor_email` | `doctor1@dentalcloud.com` | manual |
| `doctor_password` | `Doctor123!` | manual |
| `paciente_email` | `test.paciente@dentalcloud.com` | manual |
| `secretaria_email` | `test.secretaria@dentalcloud.com` | manual |
| `cita_fecha` | `2026-07-07` | manual |
| `admin_token` | _(vacío)_ | Test 1 |
| `paciente_token` / `paciente_id` | _(vacío)_ | Test 4 |
| `secretaria_token` / `secretaria_id` | _(vacío)_ | Test 6 |
| `doctor_token` | _(vacío)_ | Test 8 |
| `tratamiento_id` | _(vacío)_ | Test 3 |
| `cita_id` | _(vacío)_ | Test 5 |
| `cat_*_id` | _(vacío)_ | Test 9 |

Para cambiar el ambiente (por ejemplo apuntar a staging), solo editar `base_url`.

---

## Comportamiento ante BD limpia vs. BD con datos

| Situación | Comportamiento |
|---|---|
| BD borrada (ambiente fresco) | Los tests crean todos los usuarios y datos desde cero |
| BD con datos previos de regresión | Tests 4 y 6 reciben error en register (DUI/email duplicado), lo ignoran y continúan con login |
| `doctor1` sin rol DOCTOR | Test 8 falla en la aserción de `body.user.role` |
| Sin doctores en el sistema | Test 2 falla en `expect(doctors.length).toBeGreaterThan(0)` |

---

## Archivos en esta carpeta

```
insomnia/
  dentalcloud_completo.yaml   Colección de desarrollo (uso en Insomnia UI)
  dentalcloud_inventory.yaml  Colección de inventario
  regression_tests.yaml       Suite de regresión automatizada
  REGRESSION_GUIDE.md         Esta guía
```
