# Tareas de Implementación del Backend Marketplace

- [ ] **Módulo Auth (Autenticación y Gestión de Usuarios)**
  - [x] Entidad de dominio User y UserRole creada.
  - [ ] Implementar configuración JWT (JwtService / JwtTokenProvider), filtro de seguridad y persistencia.
  - [ ] Repositorio de usuarios (UserRepositoryPort y UserJpaRepositoryAdapter).
  - [ ] DTOs y Mappers (MapStruct) para autenticación y registro.
  - [ ] `POST /api/v1/auth/register/buyer` (Público) - Registro de compradores.
  - [ ] `POST /api/v1/auth/register/seller` (Público) - Solicitud de registro de vendedores.
  - [ ] `POST /api/v1/auth/login` (Público) - Autenticación y obtención de JWT.
  - [ ] `PATCH /api/v1/admin/sellers/{sellerId}/verify` (ADMINISTRADOR) - Verificación de vendedores.

- [ ] **Módulo Product (Catálogo de Productos)**
  - [ ] `GET /api/v1/products` (Público) - Listado público paginado.
  - [ ] `GET /api/v1/products/{slug}` (Público) - Detalle de producto.
  - [ ] `GET /api/v1/seller/products` (VENDEDOR) - Consulta catálogo propio.
  - [ ] `POST /api/v1/seller/products` (VENDEDOR) - Crear producto propio.
  - [ ] `PUT /api/v1/seller/products/{id}` (VENDEDOR) - Actualizar producto.
  - [ ] `PATCH /api/v1/admin/products/{id}/approval` (ADMINISTRADOR) - Modera productos.

- [ ] **Módulo Inventory (Inventario y Stock)**
  - [ ] `GET /api/v1/inventory/check/{productId}` (Público) - Consulta de disponibilidad.
  - [ ] `POST /api/v1/seller/inventory/adjust` (VENDEDOR) - Ajustar existencias físicas.
  - [ ] `GET /api/v1/admin/inventory/audit` (ADMINISTRADOR) - Auditoría global.

- [ ] **Módulo Order (Carrito, Compras y Órdenes)**
  - [ ] `GET /api/v1/cart` (COMPRADOR) - Obtener carrito activo.
  - [ ] `POST /api/v1/cart/items` (COMPRADOR) - Agregar items al carrito.
  - [ ] `POST /api/v1/orders/checkout` (COMPRADOR) - Crear orden e iniciar pago.
  - [ ] `GET /api/v1/buyer/orders` (COMPRADOR) - Historial de compras.
  - [ ] `GET /api/v1/seller/orders` (VENDEDOR) - Consulta de despachos pendientes.
  - [ ] `PATCH /api/v1/seller/orders/{subOrderId}/ship` (VENDEDOR) - Actualizar despacho.
  - [ ] `GET /api/v1/admin/orders` (ADMINISTRADOR) - Consulta global de transacciones.

- [ ] **Módulo Review (Reseñas y Moderación)**
  - [ ] `POST /api/v1/buyer/reviews` (COMPRADOR) - Crear reseña (compra verificada).
  - [ ] `GET /api/v1/reviews/product/{productId}` (Público) - Opiniones del producto.
  - [ ] `DELETE /api/v1/admin/reviews/{reviewId}` (ADMINISTRADOR) - Eliminar reseñas.

- [ ] **Pruebas y Verificación**
  - [ ] Configuración inicial de Tests de Seguridad RBAC.
  - [ ] Compilación y validación continua (`./gradlew build`).
