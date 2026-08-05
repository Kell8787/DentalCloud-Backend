# Módulo de inventario (DentalCloud Backend)

Documentación del API bajo el prefijo **`/api/inventory`**: modelo de datos, endpoints, cuerpos JSON esperados y uso típico desde el cliente.

---

## Qué incluye el módulo

- **Categorías** (`ProductCategory`): solo `name`. Sirven para agrupar productos.
- **Productos** (`InventoryProduct`): nombre, descripción, precio de compra, precio de venta, **cantidad en inventario**, stock mínimo (`minimumStock`), unidad (`unit`), versión de concurrencia, enlace a una categoría y **eliminación lógica** (`deleted`). No existe entidad de proveedor.
- **Movimientos** (`StockMovement`): entradas, salidas y ajustes inmutables con producto, cantidad, motivo, actor y fecha.
- **Reglas de negocio**:
  - **Compra** (`PUT .../purchase`): suma unidades al producto.
  - **Venta** (`PUT .../sale`): resta unidades; si la cantidad solicitada es mayor que la disponible, la API responde con error (ver más abajo).
  - **Concurrencia**: los cambios de producto usan versionado optimista; si otro usuario actualizó el mismo producto, la operación responde con conflicto.

Las alertas visuales (bajo stock, crítico, etc.) se asumen en el **frontend**; el backend solo expone el campo numérico `quantity`.

---

## Autenticación

Por defecto, todo lo de **`/api/inventory`** requiere usuario autenticado (JWT), según `SecurityConfig`. Los únicos endpoints públicos relacionados con usuarios son **`POST /api/auth/register`** y **`POST /api/auth/login`**.

**Cabecera en cada petición protegida:**

```http
Authorization: Bearer <token_jwt>
Content-Type: application/json
```

Obtén el token con `POST /api/auth/login` y un cuerpo del estilo:

```json
{
  "email": "usuario@ejemplo.com",
  "password": "tu_contraseña"
}
```

---

## Base URL

En desarrollo local suele ser:

```text
http://localhost:8080
```

(Ajusta si usas otro host o puerto.)

---

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/inventory/categories` | Lista **todas** las categorías. |
| `POST` | `/api/inventory/category` | Crea una categoría. |
| `POST` | `/api/inventory` | Crea un producto. |
| `GET` | `/api/inventory` | Lista productos no eliminados; admite filtros opcionales por query (ver abajo). |
| `GET` | `/api/inventory/{id}` | Detalle de un producto por UUID. |
| `GET` | `/api/inventory/category/{categoryId}` | Productos de una categoría por **UUID** de categoría. |
| `PUT` | `/api/inventory/{id}` | Actualiza un producto completo (mismos campos que el alta). |
| `PUT` | `/api/inventory/{id}/purchase` | Suma cantidad (compra / ingreso de stock). |
| `PUT` | `/api/inventory/{id}/sale` | Resta cantidad (venta / salida de stock). |
| `DELETE` | `/api/inventory/{id}` | Baja **lógica** del producto (`deleted = true`). |

---

## Listado y búsqueda (`GET /api/inventory`)

Sin parámetros devuelve todos los productos activos (no eliminados).

Filtros opcionales (query string); se pueden **combinar**:

| Parámetro | Significado |
|-----------|-------------|
| `categoryName` | Filtro por **nombre de categoría**, comparación **insensible a mayúsculas** (implementado en PostgreSQL con `ILIKE`). Para nombres “normales” sin `%` ni `_` en la base de datos se comporta como una coincidencia práctica de igualdad; esos caracteres en el nombre guardado actúan como comodines de patrón en SQL. |
| `name` | Fragmento del **nombre del producto**: debe **contener** el texto indicado (insensible a mayúsculas; comodines `%` / `_` en el parámetro tienen el significado habitual de `LIKE`). |

**Ejemplos:**

```http
GET /api/inventory
GET /api/inventory?categoryName=Insumos
GET /api/inventory?name=guante
GET /api/inventory?categoryName=Insumos&name=latex
```

Codifica el valor en la URL si lleva espacios o caracteres especiales (p. ej. `Insumos%20dental`).

---

## Cuerpos JSON de entrada

### Crear categoría — `POST /api/inventory/category`

Validación: `name` obligatorio y no vacío.

```json
{
  "name": "Insumos"
}
```

**Respuesta** (`CategoryResponseDTO`): `id`, `name`.

---

### Crear producto — `POST /api/inventory`

Mismo DTO que la actualización: `InventoryUpdateRequestDTO`.

| Campo | Tipo | Reglas |
|-------|------|--------|
| `name` | string | Obligatorio, no blanco. |
| `description` | string | Obligatorio, no blanco. |
| `purchasePrice` | número | Obligatorio, ≥ 0. |
| `salePrice` | número | Obligatorio, ≥ 0. |
| `categoryId` | UUID | Obligatorio; debe existir una categoría con ese id. |
| `quantity` | entero | Obligatorio, ≥ 0. |
| `minimumStock` | entero | Opcional, ≥ 0; por defecto `0`. |
| `unit` | string | Opcional, máximo 32 caracteres; por defecto `unidad`. |

```json
{
  "name": "Guantes nitrilo M",
  "description": "Caja 100 uds",
  "purchasePrice": 12.50,
  "salePrice": 22.00,
  "categoryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "quantity": 50,
  "minimumStock": 10,
  "unit": "caja"
}
```

**Respuesta** (`InventoryResponseDTO`): ver sección siguiente.

---

### Actualizar producto — `PUT /api/inventory/{id}`

Mismo cuerpo que `POST /api/inventory`. El `{id}` es el UUID del producto.

---

### Compra / ingreso de stock — `PUT /api/inventory/{id}/purchase`

| Campo | Tipo | Reglas |
|-------|------|--------|
| `quantity` | entero | Obligatorio, **mínimo 1**. |

```json
{
  "quantity": 10
}
```

Se **suma** `quantity` al inventario actual del producto.

---

### Venta / salida de stock — `PUT /api/inventory/{id}/sale`

Mismo cuerpo que compra. Se **resta** la cantidad. Si `quantity` es mayor que el stock actual, se lanza un error de negocio (normalmente **400** con mensaje descriptivo vía el manejador global).

---

## Respuesta de producto (`InventoryResponseDTO`)

Ejemplo de cuerpo devuelto en alta, detalle, listado, actualización, compra o venta (cada ítem en listados sigue esta forma):

```json
{
  "id": "b2c3d4e5-f6a7-8901-bcde-f12345678901",
  "name": "Guantes nitrilo M",
  "description": "Caja 100 uds",
  "purchasePrice": 12.5,
  "salePrice": 22.0,
  "categoryId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "categoryName": "Insumos",
  "quantity": 60,
  "minimumStock": 10,
  "unit": "caja",
  "version": 1
}
```

---

## Eliminar producto — `DELETE /api/inventory/{id}`

No lleva cuerpo. Respuesta típica:

```json
{
  "message": "Producto eliminado correctamente"
}
```

Los productos eliminados **no** deben aparecer en listados ni en detalle (consultas filtran `deleted = false`).

---

## Errores y validación

- Los DTOs usan **Jakarta Bean Validation** (`@Valid`). Si falla la validación, suele responder **400** con detalle de campos (según `GlobalExceptionHandler`).
- **Entidad no encontrada** (`EntityNotFoundException`): suele ser **404** con mensaje en el cuerpo.
- **Argumentos ilegales** (p. ej. vender más de lo disponible): **400** con mensaje de error.

---

## Flujo recomendado (frontend u otro cliente)

1. `POST /api/auth/login` → guardar `token`.
2. `POST /api/inventory/category` (o usar categorías ya existentes).
3. Opcional: `GET /api/inventory/categories` para mostrar selector por nombre/id.
4. `POST /api/inventory` con `categoryId` tomado del listado de categorías.
5. Para movimientos de stock: `PUT .../purchase` o `PUT .../sale` solo con `{ "quantity": N }`.
6. Para buscar en UI: `GET /api/inventory?name=...` y/o `?categoryName=...`.
7. Opcional: `DELETE /api/inventory/{id}` para baja lógica.

---

## Colección Insomnia

En el repositorio hay exports de ejemplo:

- `insomnia/DentalCloud-Inventory.insomnia.json`
- `insomnia/DentalCloud-Inventory-Environments.insomnia.json`

Importa ambos en Insomnia (o solo el de entorno si ya tienes la colección). Configura `base_url`, credenciales de login y pega el JWT en `token` para las peticiones protegidas.

---

## Nota sobre base de datos (PostgreSQL)

Las columnas de texto (`name`, `description`) deben ser **`text` o `varchar`**, no `bytea`. Si aparecieran errores de tipo al buscar o listar, revisa el tipo real de las columnas en PostgreSQL y alinéalo con el esquema JPA (`columnDefinition = "TEXT"` en entidades) o con una migración manual.
