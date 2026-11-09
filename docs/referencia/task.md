# Tareas de Implementación del Backend Marketplace

Estado: **completado y verificado en ejecución contra Google Cloud SQL.**

- [x] **Módulo Auth (Autenticación y Gestión de Usuarios)**
  - [x] Implementar configuración JWT, entidades de usuario y persistencia.
  - [x] `POST /api/v1/auth/register/buyer` (Público) - Registro de compradores.
  - [x] `POST /api/v1/auth/register/seller` (Público) - Solicitud de registro de vendedores.
  - [x] `POST /api/v1/auth/login` (Público) - Autenticación y obtención de JWT.
  - [x] `PATCH /api/v1/admin/sellers/{sellerId}/verify` (ADMINISTRADOR) - Verificación de vendedores.

- [x] **Módulo Product (Catálogo de Productos)**
  - [x] `GET /api/v1/products` (Público) - Listado público paginado.
  - [x] `GET /api/v1/products/{slug}` (Público) - Detalle de producto.
  - [x] `GET /api/v1/seller/products` (VENDEDOR) - Consulta catálogo propio.
  - [x] `POST /api/v1/seller/products` (VENDEDOR) - Crear producto propio.
  - [x] `PUT /api/v1/seller/products/{id}` (VENDEDOR) - Actualizar producto.
  - [x] `PATCH /api/v1/admin/products/{id}/approval` (ADMINISTRADOR) - Modera productos.

- [x] **Módulo Inventory (Inventario y Stock)**
  - [x] `GET /api/v1/inventory/check/{productId}` (Público) - Consulta de disponibilidad.
  - [x] `POST /api/v1/seller/inventory/adjust` (VENDEDOR) - Ajustar existencias físicas.
  - [x] `GET /api/v1/admin/inventory/audit` (ADMINISTRADOR) - Auditoría global.

- [x] **Módulo Order (Carrito, Compras y Órdenes)**
  - [x] `GET /api/v1/cart` (COMPRADOR) - Obtener carrito activo.
  - [x] `POST /api/v1/cart/items` (COMPRADOR) - Agregar items al carrito.
  - [x] `POST /api/v1/orders/checkout` (COMPRADOR) - Crear orden e iniciar pago.
  - [x] `GET /api/v1/buyer/orders` (COMPRADOR) - Historial de compras.
  - [x] `GET /api/v1/seller/orders` (VENDEDOR) - Consulta de despachos pendientes.
  - [x] `PATCH /api/v1/seller/orders/{subOrderId}/ship` (VENDEDOR) - Actualizar despacho.
  - [x] `GET /api/v1/admin/orders` (ADMINISTRADOR) - Consulta global de transacciones.

- [x] **Módulo Review (Reseñas y Moderación)**
  - [x] `POST /api/v1/buyer/reviews` (COMPRADOR) - Crear reseña (compra verificada).
  - [x] `GET /api/v1/reviews/product/{productId}` (Público) - Opiniones del producto.
  - [x] `DELETE /api/v1/admin/reviews/{reviewId}` (ADMINISTRADOR) - Eliminar reseñas.

- [x] **Pruebas y Verificación**
  - [x] Configuración inicial de Tests de Seguridad RBAC.
  - [x] Compilación y validación continua (`./gradlew build`).

---

## Resultados de la verificación

| Comprobación | Resultado |
| --- | --- |
| Compilación (`./gradlew clean build`) | ✅ BUILD SUCCESSFUL |
| Suite de tests | ✅ **114 tests, 0 fallos, 0 errores** |
| Arranque contra Google Cloud SQL | ✅ `HikariPool - Start completed` en 12,25 s |
| Esquema generado por Hibernate | ✅ 10 tablas |
| Endpoints end-to-end contra Cloud SQL | ✅ **39 aserciones, 0 fallos** |
| Antisobreventa (bloqueo pesimista) | ✅ Verificado con 2 compradores y 1 unidad |
| División de orden por vendedor | ✅ Verificado (1 orden → 2 sub-órdenes) |
| BOLA entre vendedores | ✅ Verificado (400 al despachar sub-orden ajena) |
| RBAC (401 anónimo / 403 rol incorrecto) | ✅ Verificado |
| Validación de payloads y RFC 7807 | ✅ Verificado |

### Entorno verificado

- Instancia: `marketplace-509503:us-central1:marketplace` (PostgreSQL 18.6, IP pública `136.112.91.42`)
- Base de datos: `marketplace_db` (creada vacía; la base `postgres` de la instancia contiene 23
  tablas de un proyecto anterior con claves `bigint`, incompatible con este modelo)
- JDK: Temurin 21.0.12.1 · Gradle 8.10.2

### Endpoints adicionales implementados (soporte de los anteriores)

| Endpoint | Rol | Motivo |
| --- | --- | --- |
| `PUT /api/v1/cart/items/{productId}` | COMPRADOR | Fijar cantidad absoluta de una línea. |
| `DELETE /api/v1/cart/items/{productId}` | COMPRADOR | Quitar una línea del carrito. |
| `DELETE /api/v1/cart` | COMPRADOR | Vaciar el carrito antes del checkout. |
| `GET /api/v1/buyer/orders/{orderId}` | COMPRADOR | Detalle de una orden propia (con validación BOLA). |
| `GET /api/v1/seller/inventory` | VENDEDOR | Ver el stock propio antes de ajustarlo. |
| `GET /api/v1/admin/products?status=` | ADMINISTRADOR | Bandeja de moderación (`PENDING_APPROVAL` por defecto). |
| `GET /api/v1/admin/reviews` | ADMINISTRADOR | Moderación incluyendo reseñas ocultas. |

### Herramientas de verificación incluidas

| Archivo | Uso |
| --- | --- |
| `backend/tools/verify.ps1` | Comprobación rápida antes de la demo (JDK, tests, API, endpoints, BD, login). |
| `backend/tools/e2e-test.ps1` | Recorrido completo de los 27 endpoints (39 aserciones). |
| `backend/tools/stock-test.ps1` | Prueba de antisobreventa con dos compradores. |
| `backend/tools/split-test.ps1` | Prueba de división de orden multi-vendedor y BOLA. |
| `backend/tools/AdminPasswordSync.java` | Sincroniza el hash del admin si se cambia la contraseña en `.env`. |

### Incidencias encontradas y corregidas durante la verificación

1. `AuthControllerTest` fallaba (5 tests) porque `SecurityConfig` ahora requiere el bean
   `JwtAccessDeniedHandler` y el test no lo importaba. Corregido.
2. La base `postgres` de la instancia tenía un esquema incompatible (PK `bigint`). Se creó la base
   nueva `marketplace_db`.
3. Cambiar `BOOTSTRAP_ADMIN_PASSWORD` tras el primer arranque no actualiza el usuario existente,
   por lo que el login devolvía `401`. Documentado y resuelto con `AdminPasswordSync.java`.

### Pendiente para producción (no bloquea la demo)

- Sustituir la pasarela de pago *stub* por el proveedor real.
- Integrar Bucket4j (rate limiting) y Resilience4j (dependencias ya declaradas).
- Memorystore Redis para caché y blacklist de JWT.
- Pasar `ddl-auto` a `validate` con migraciones versionadas (Flyway/Liquibase).
- Mover `JWT_SECRET` y las credenciales a GCP Secret Manager.
