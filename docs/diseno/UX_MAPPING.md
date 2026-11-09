# UX_MAPPING.md — Rutas de la API y su correspondencia con la interfaz

Contrato funcional entre backend y frontend. **44 operaciones en 39 rutas**, agrupadas por módulo.
Base local: `http://localhost:8080` · Base nube: `https://marketplace-api-805790031718.us-central1.run.app`.

**Auth:** el panel usa una cookie httpOnly y el proxy de Next añade
`Authorization: Bearer <accessToken>`; Postman/Swagger pueden usar el Bearer directamente.
Roles: `ADMIN`, `SELLER`, `BUYER`.
**Paginación:** `?page=0&size=20&sort=createdAt,desc` → `{content, totalElements, totalPages, number, size}`.
**Errores:** RFC 7807 `ProblemDetail` (`type`, `title`, `status`, `detail`, `instance`, `timestamp`).

---

## 1. Auth — `/api/v1/auth` · `/api/v1/admin/sellers`

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| POST | `/auth/register/buyer` | Público | `{email, password}` | `201 {accessToken, userId}` | `/registro` |
| POST | `/auth/register/seller` | Público | `{email, password}` | `201 {accessToken, userId}` · no aprobado | `/registro` |
| POST | `/auth/login` | Público | `{email, password}` | `200 {accessToken, tokenType, expiresIn, userId}` | `/login` |
| PATCH | `/admin/sellers/{sellerId}/verify` | ADMIN | `{approved: true}` | `200` | `/admin/vendedores` |
| GET | `/admin/sellers?page&size` | ADMIN | — | `200` paginado *(apoyo)* | `/admin/vendedores` |

## 2. Product — `/api/v1/products` · `/seller/products` · `/admin/products`

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/products?page&size&sort` | Público | — | `200` paginado | `/productos`, `/` |
| GET | `/products/{slug}` | Público | — | `200` · `404` | `/productos/[slug]` |
| GET | `/products/ranking?limit` | Público | — | `200` lista ordenada por rating y decaimiento | `/` |
| GET | `/seller/products?page&size` | SELLER | — | `200` paginado | `/vendedor` |
| POST | `/seller/products` | SELLER | `{name, description, price, currencyCode, stockQuantity, category, imageUrl}` | `201` | `/vendedor` |
| PUT | `/seller/products/{id}` | SELLER | mismos campos | `200` | `/vendedor` |
| PATCH | `/admin/products/{id}/approval` | ADMIN | `{approved: true}` | `200` | `/admin` |
| GET | `/admin/products?status=&page&size` | ADMIN | — | `200` paginado | `/admin` |

## 3. Inventory — `/api/v1/inventory` · `/seller/inventory` · `/admin/inventory`

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/inventory/check/{productId}` | Público | — | `200 {availableQuantity}` · `404` | `/productos/[slug]` |
| POST | `/seller/inventory/adjust` | SELLER | `{productId, quantityDelta}` **o** `{productId, physicalQuantity, reason}` | `200` | `/vendedor/inventario` |
| GET | `/seller/inventory?page&size` | SELLER | — | `200` paginado | `/vendedor/inventario` |
| GET | `/admin/inventory/audit?page&size` | ADMIN | — | `200` paginado | `/admin/inventario` |

## 4. Cart y órdenes

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/cart` | BUYER | — | `200 {items, total}` | `/carrito` |
| POST | `/cart/items` | BUYER | `{productId, quantity}` | `200` · `400` stock | `/productos/[slug]`, `/carrito` |
| PUT | `/cart/items/{productId}` | BUYER | `{quantity}` | `200` | `/carrito` |
| DELETE | `/cart/items/{productId}` | BUYER | — | `200` | `/carrito` |
| DELETE | `/cart` | BUYER | — | `200` | `/carrito` |
| POST | `/orders/checkout` | BUYER | `{notes}` | `201 {order, paymentCaptured, paymentReference}` | `/checkout` |
| GET | `/buyer/orders?page&size` | BUYER | — | `200` paginado | `/mis-compras` |
| GET | `/buyer/orders/{orderId}` | BUYER | — | `200` · `404` | `/mis-compras` |
| GET | `/seller/orders?status=&page&size` | SELLER | — | `200` paginado | `/vendedor/despachos` |
| PATCH | `/seller/orders/{subOrderId}/ship` | SELLER | `{trackingNumber, carrier}` | `200` | `/vendedor/despachos` |
| PATCH | `/seller/orders/{subOrderId}/deliver` | SELLER | — | `200` · cierra la orden a `COMPLETED` cuando todas las sub-órdenes están `DELIVERED` | `/vendedor/despachos` |
| GET | `/admin/orders?page&size&sort` | ADMIN | — | `200` paginado | `/admin/ordenes` |

## 5. Review

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| POST | `/buyer/reviews` | BUYER | `{productId, rating, title, comment}` | `201` | `/mis-compras`, `/productos/[slug]` |
| GET | `/reviews/product/{productId}?page&size` | Público | — | `200 {averageRating, totalReviews, reviews}` | `/productos/[slug]` |
| GET | `/admin/reviews?page&size` | ADMIN | — | `200` paginado | `/admin` |
| DELETE | `/admin/reviews/{reviewId}` | ADMIN | — | `204` · `404` | `/admin` |

## 6. Seller profile

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/sellers/{sellerId}/profile` | Público | — | `200 {storeName, logoUrl, ratingAvg, ratingCount}` | — (API) |
| GET | `/seller/profile` | SELLER | — | `200` perfil propio | — (API) |
| PUT | `/seller/profile` | SELLER | `{storeName, description, logoUrl}` | `200` | — (API) |

## 7. Notifications

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/notifications?page&size` | Autenticado | — | `200` paginado | `/notificaciones` |
| GET | `/notifications/unread-count` | Autenticado | — | `200 {count}` | campana de la navbar |
| PATCH | `/notifications/{id}/read` | Autenticado | — | `204` · `404` si no es tuya | `/notificaciones` |
| PATCH | `/notifications/read-all` | Autenticado | — | `200 {updated}` | `/notificaciones` |

## 8. Favorites

| Método | Ruta | Rol | Request | Response | Pantalla |
|---|---|---|---|---|---|
| GET | `/favorites` | BUYER | — | `200` lista de productos | `/favoritos` |
| GET | `/favorites/ids` | BUYER | — | `200 [uuid]` | corazones |
| POST | `/favorites/{productId}` | BUYER | — | `204` | catálogo/detalle |
| DELETE | `/favorites/{productId}` | BUYER | — | `204` | catálogo/detalle/favoritos |

## 9. Infraestructura

| Método | Ruta | Uso |
|---|---|---|
| GET | `/actuator/health` | Healthcheck (`{"status":"UP"}`) |
| GET | `/v3/api-docs` | OpenAPI JSON |
| GET | `/swagger-ui.html` | Swagger UI (redirige a `/swagger-ui/index.html`) |

---

## 10. Mapa pantalla → endpoints

| # | Pantalla | Endpoints que usa |
|---|---|---|
| 1 | `/` portada | `GET /products/ranking` (destacados) |
| 2 | `/productos` catálogo | `GET /products` |
| 3 | `/productos/[slug]` detalle | `GET /products/{slug}` · `GET /inventory/check/{id}` · `GET /reviews/product/{id}` · `POST /cart/items` |
| 4 | `/login` | `POST /auth/login` |
| 5 | `/registro` | `POST /auth/register/buyer` · `POST /auth/register/seller` |
| 6 | `/carrito` | `GET /cart` · `PUT /cart/items/{id}` · `DELETE /cart/items/{id}` · `DELETE /cart` |
| 7 | `/checkout` | `POST /orders/checkout` · `GET /cart` |
| 8 | `/mis-compras` | `GET /buyer/orders` · `GET /buyer/orders/{id}` · `POST /buyer/reviews` |
| 9 | `/vendedor` | `GET /seller/products` · `POST /seller/products` · `PUT /seller/products/{id}` |
| 10 | `/vendedor/inventario` | `GET /seller/inventory` · `POST /seller/inventory/adjust` |
| 11 | `/vendedor/despachos` | `GET /seller/orders` · `PATCH /seller/orders/{id}/ship` |
| 12 | `/admin` moderación | `GET /admin/products?status=` · `PATCH /admin/products/{id}/approval` · `GET /admin/reviews` · `DELETE /admin/reviews/{id}` |
| 13 | `/admin/vendedores` | `GET /admin/sellers` · `PATCH /admin/sellers/{id}/verify` |
| 14 | `/admin/ordenes` | `GET /admin/orders` |
| 15 | `/admin/inventario` | `GET /admin/inventory/audit` |
| 16 | `/notificaciones` | `GET /notifications` · `GET /notifications/unread-count` · `PATCH /notifications/{id}/read` · `PATCH /notifications/read-all` |
| 17 | `/favoritos` | `GET /favorites` · `DELETE /favorites/{id}` |

## 11. Los 4 estados por pantalla

| Pantalla | Carga | Vacío | Error + reintento |
|---|---|---|---|
| `/` | skeleton de hero y tarjetas | «Pronto habrá productos» | sí |
| `/productos` | skeleton de grid | «No hay productos» + «Ver todo» | sí |
| `/productos/[slug]` | skeleton de detalle | — | sí |
| `/login` | spinner en botón | — | sí |
| `/registro` | spinner en botón | — | sí |
| `/carrito` | skeleton de líneas | «Tu carrito está vacío» + «Ver catálogo» | sí |
| `/checkout` | skeleton | carrito vacío | sí |
| `/mis-compras` | skeleton de tarjetas | «Aún no tienes compras» | sí |
| `/vendedor` | skeleton de tabla | «Crea tu primer producto» | sí |
| `/vendedor/inventario` | skeleton | «Sin stock registrado» | sí |
| `/vendedor/despachos` | skeleton | «No hay despachos pendientes» | sí |
| `/admin` | skeleton | «Nada pendiente de moderar» | sí |
| `/admin/vendedores` | skeleton | «No hay vendedores» | sí |
| `/admin/ordenes` | skeleton | «No hay órdenes» | sí |
| `/admin/inventario` | skeleton | «Sin movimientos» | sí |
| `/notificaciones` | skeleton | «No tienes notificaciones» | sí |
| `/favoritos` | skeleton | «Todavía no tienes favoritos» | sí |

> El **botón «Reintentar»** debe repetir la misma petición que falló. Es el hueco más visible de la
> rúbrica ([PENDIENTES.md §2.2](../../PENDIENTES.md#L51)).

## 12. Códigos de error

| Código | Cuándo | Qué muestra la UI |
|---|---|---|
| `400` | Validación, stock insuficiente, vendedor no aprobado | Mensaje junto al campo / aviso |
| `401` | Sin token o token caducado | Redirige a `/login` |
| `403` | Rol incorrecto o recurso ajeno (BOLA) | «No tienes permiso» |
| `404` | Producto, orden o reseña inexistente | «No encontrado» + volver |
| `409` | Conflicto (p. ej. email ya registrado) | Mensaje específico |
| `500` | Fallo del servidor | «Algo salió mal» + **Reintentar** |

---

**Última actualización:** 2026-11-09 · 44 operaciones / 39 rutas.
