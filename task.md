# Tareas de Implementación del Backend Marketplace

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
  - [ ] Compilación y validación continua (`./gradlew build`).

## Endpoints adicionales implementados (soporte de los anteriores)

| Endpoint | Rol | Motivo |
| --- | --- | --- |
| `PUT /api/v1/cart/items/{productId}` | COMPRADOR | Fijar cantidad absoluta de una línea. |
| `DELETE /api/v1/cart/items/{productId}` | COMPRADOR | Quitar una línea del carrito. |
| `DELETE /api/v1/cart` | COMPRADOR | Vaciar el carrito antes del checkout. |
| `GET /api/v1/buyer/orders/{orderId}` | COMPRADOR | Detalle de una orden propia (con validación BOLA). |
| `GET /api/v1/seller/inventory` | VENDEDOR | Ver el stock propio antes de ajustarlo. |
| `GET /api/v1/admin/products?status=` | ADMINISTRADOR | Bandeja de moderación (`PENDING_APPROVAL` por defecto). |
| `GET /api/v1/admin/reviews` | ADMINISTRADOR | Moderación incluyendo reseñas ocultas. |

## Nota sobre la verificación

`./gradlew build` **no pudo ejecutarse en este entorno**: la máquina sólo tiene
`JRE 1.8` (`C:\Program Files\Java\jre1.8.0_461`) mientras el proyecto exige
`Java 21` (`build.gradle` → `JavaLanguageVersion.of(21)`), y el sandbox no tiene
salida de red (TLS bloqueado) para descargar un JDK 21 ni dependencias de Maven.
El código se validó de forma estática (imports, firmas de puertos vs. adaptadores,
balance de bloques) y con la suite de tests escrita, pero **queda pendiente
ejecutar `./gradlew build` en una máquina con JDK 21**.
