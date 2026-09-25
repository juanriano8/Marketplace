# Guía manual de peticiones para Postman

Referencia para **ingresar cada endpoint a mano** en Postman, con el cuerpo JSON, la respuesta
esperada y los códigos HTTP reales (todos verificados contra Google Cloud SQL).

> Si prefieres no escribir nada: importa
> `backend/postman/Marketplace-API.postman_collection.json` (44 peticiones ya listas).

---

## Índice

- [Configurar Postman](#configurar-postman)
- [Orden recomendado](#orden-recomendado-para-la-demo)
- [1. Health y documentación](#1-health-y-documentación)
- [2. Autenticación](#2-autenticación-público)
- [3. Administrador: vendedores](#3-administrador-vendedores)
- [4. Productos: público](#4-productos-público)
- [5. Productos: vendedor](#5-productos-vendedor)
- [6. Productos: administrador](#6-productos-administrador)
- [7. Inventario](#7-inventario)
- [8. Carrito](#8-carrito-comprador)
- [9. Órdenes](#9-órdenes)
- [10. Reseñas](#10-reseñas)
- [11. Pruebas de seguridad](#11-pruebas-de-seguridad)
- [Códigos de error](#códigos-de-error)

---

## Configurar Postman

### Variables de colección

Crea una colección llamada `Marketplace` y define estas variables (pestaña **Variables**):

| Variable | Valor inicial | Se rellena con |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | — (manual) |
| `token` | *(vacío)* | El `accessToken` del login del rol que vayas a usar |
| `adminToken` | *(vacío)* | Login del admin |
| `sellerToken` | *(vacío)* | Login del vendedor |
| `buyerToken` | *(vacío)* | Login del comprador |
| `sellerId` | *(vacío)* | `userId` de la respuesta de registro del vendedor |
| `buyerId` | *(vacío)* | `userId` del comprador |
| `productId` | *(vacío)* | `id` de la respuesta de crear producto |
| `productSlug` | *(vacío)* | `slug` de esa misma respuesta |
| `orderId` | *(vacío)* | `order.orderId` del checkout |
| `subOrderId` | *(vacío)* | `order.subOrders[0].subOrderId` del checkout |
| `reviewId` | *(vacío)* | `reviewId` de la respuesta de crear reseña |

### Autenticación

Para los endpoints protegidos, en la pestaña **Auth** de cada petición:

- Type: **Bearer Token**
- Token: `{{adminToken}}` o `{{sellerToken}}` o `{{buyerToken}}` según el rol

También sirve añadir a mano la cabecera `Authorization: Bearer <token>`.

### Cómo automatizar el token (opcional)

En la petición de login, pestaña **Tests**, escribe:

```javascript
const j = pm.response.json();
pm.collectionVariables.set('adminToken', j.accessToken);   // o buyerToken / sellerToken
pm.test('200 OK', () => pm.response.to.have.status(200));
```

Así el token se guarda solo y no tienes que copiarlo.

---

## Orden recomendado para la demo

Cada paso habilita el siguiente. Duración: ~3 minutos.

```
1.  Login admin                      -> guarda adminToken
2.  Registrar vendedor               -> guarda sellerId
3.  (probar) Crear producto          -> 400: no está verificado
4.  Verificar vendedor (admin)       -> sellerApproved = true
5.  Login vendedor                   -> guarda sellerToken
6.  Crear producto                   -> PENDING_APPROVAL
7.  Listar productos público         -> NO aparece todavía
8.  Aprobar producto (admin)         -> ACTIVE
9.  Listar productos público         -> AHORA sí aparece
10. Registrar + login comprador      -> guarda buyerToken
11. Agregar al carrito               -> 200
12. (probar) Cantidad 9999           -> 400 control de stock
13. Checkout                         -> 201, orden PAID
14. Despachos del vendedor           -> ve su sub-orden
15. Registrar despacho               -> SHIPPED
16. Crear reseña                     -> verifiedPurchase: true
17. Sin token en /admin/orders       -> 401
18. Token de comprador en /admin     -> 403
```

---

## 1. Health y documentación

### `GET {{baseUrl}}/actuator/health` — Público

Sin cabeceras ni cuerpo.

**200 OK**
```json
{ "status": "UP" }
```

### `GET {{baseUrl}}/v3/api-docs` — Público

Devuelve el OpenAPI completo. Útil para importar la colección entera en Postman:
**Import → Link → `http://localhost:8080/v3/api-docs`**.

---

## 2. Autenticación (público)

### `POST {{baseUrl}}/api/v1/auth/register/buyer`

**Auth:** No Auth · **Body:** raw → JSON

```json
{
  "email": "comprador@marketplace.com",
  "password": "Password123!"
}
```

**201 Created**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "email": "comprador@marketplace.com",
  "role": "ROLE_BUYER",
  "sellerApproved": null
}
```

| Código | Cuándo |
|---|---|
| `201` | Registrado correctamente |
| `400` | Email inválido, contraseña < 8 caracteres o email ya registrado |

> Guarda `userId` en `{{buyerId}}` y `accessToken` en `{{buyerToken}}`.

---

### `POST {{baseUrl}}/api/v1/auth/register/seller`

Mismo cuerpo que el comprador, cambiando el email.

```json
{
  "email": "vendedor@marketplace.com",
  "password": "Password123!"
}
```

**201 Created**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "email": "vendedor@marketplace.com",
  "role": "ROLE_SELLER",
  "sellerApproved": false
}
```

> **`sellerApproved: false`** — el vendedor **no puede publicar** hasta que un administrador lo
> verifique. Guarda `userId` en `{{sellerId}}`.

---

### `POST {{baseUrl}}/api/v1/auth/login`

```json
{
  "email": "admin@marketplace.com",
  "password": "TU_CONTRASEÑA_DEL_ENV"
}
```

**200 OK** — misma estructura que el registro.

| Código | Cuándo |
|---|---|
| `200` | Credenciales correctas |
| `400` | Falta email o contraseña |
| `401` | Credenciales incorrectas |

> **Error típico:** si el admin da `401` con la contraseña del `.env`, es porque el administrador
> se creó con otra contraseña. Ver §10.3.1 de [EJECUCION_Y_DEMO.md](EJECUCION_Y_DEMO.md).

---

## 3. Administrador: vendedores

### `GET {{baseUrl}}/api/v1/admin/sellers?approved=false`

**Auth:** Bearer `{{adminToken}}` · **200 OK**

Bandeja de verificación. `approved=false` devuelve los pendientes, `approved=true` los ya
verificados, y si omites el parámetro devuelve todos los vendedores.

```json
{
  "content": [
    {
      "id": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "email": "vendedor@marketplace.com",
      "role": "ROLE_SELLER",
      "enabled": true,
      "sellerApproved": false,
      "createdAt": "2026-09-23T22:55:10Z"
    }
  ],
  "totalElements": 1
}
```

| Código | Cuándo |
|---|---|
| `200` | Devuelto |
| `401` | Sin token |
| `403` | El token no es de ADMIN |

> Este endpoint existe para que el panel de administración pueda mostrar a quién verificar; sin él
> habría que conocer el `sellerId` de antemano.

### `PATCH {{baseUrl}}/api/v1/admin/sellers/{{sellerId}}/verify`

**Auth:** Bearer `{{adminToken}}`

```json
{ "approved": true }
```

**200 OK**
```json
{
  "id": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "email": "vendedor@marketplace.com",
  "role": "ROLE_SELLER",
  "enabled": true,
  "sellerApproved": true,
  "createdAt": "2026-09-23T22:55:10Z"
}
```

| Código | Cuándo |
|---|---|
| `200` | Verificado |
| `400` | El usuario no es vendedor, o payload inválido |
| `403` | El token no es de ADMIN |
| `404` | No existe ese `sellerId` |

> `{"approved": false}` **rechaza** al vendedor y además deshabilita su cuenta
> (`enabled: false`), por lo que ya no podrá iniciar sesión.

---

## 4. Productos: público

### `GET {{baseUrl}}/api/v1/products?page=0&size=20&sort=createdAt,desc`

**Auth:** No Auth

**200 OK**
```json
{
  "content": [
    {
      "id": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "name": "Audifonos Inalambricos Pro",
      "description": "Cancelacion de ruido activa, 40h de bateria",
      "slug": "audifonos-inalambricos-pro-a1b2c3d4",
      "price": 139.99,
      "currencyCode": "USD",
      "stockQuantity": 30,
      "imageUrl": "https://storage.googleapis.com/mi-bucket/audifonos.jpg",
      "status": "ACTIVE",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "category": "Electronics",
      "createdAt": "2026-09-23T22:56:01Z",
      "updatedAt": "2026-09-23T22:56:01Z"
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "empty": false,
  "numberOfElements": 1,
  "size": 20,
  "number": 0
}
```

> **Solo aparecen productos `ACTIVE`.** `stockQuantity` es el stock **vendible real** (del módulo de
> inventario), no el valor declarado al crear el producto.

**Parámetros:** `page` (0), `size` (20), `sort` (`campo,direccion`).

---

### `GET {{baseUrl}}/api/v1/products/{{productSlug}}`

**Auth:** No Auth · **200 OK** — el mismo objeto producto.

| Código | Cuándo |
|---|---|
| `200` | Encontrado y `ACTIVE` |
| `404` | No existe, o existe pero no está aprobado |

---

## 5. Productos: vendedor

### `GET {{baseUrl}}/api/v1/seller/products?page=0&size=20`

**Auth:** Bearer `{{sellerToken}}` · **200 OK** — página de productos propios, incluidos los
`PENDING_APPROVAL`.

---

### `POST {{baseUrl}}/api/v1/seller/products`

**Auth:** Bearer `{{sellerToken}}`

```json
{
  "name": "Audifonos Inalambricos Pro",
  "description": "Cancelacion de ruido activa, 40h de bateria",
  "price": 149.99,
  "currencyCode": "USD",
  "stockQuantity": 25,
  "category": "Electronics",
  "imageUrl": "https://storage.googleapis.com/mi-bucket/audifonos.jpg"
}
```

**201 Created** — el producto recién creado.

| Código | Cuándo |
|---|---|
| `201` | Creado (nace en `PENDING_APPROVAL`) |
| `400` | Payload inválido, **o el vendedor no está verificado** |
| `403` | El token no es de SELLER |

Campos obligatorios: `name`, `price`, `stockQuantity`, `category`. `currencyCode` por defecto `USD`.

**Errores de validación (400):**
```json
{
  "type": "https://api.marketplace.com/errors/validation-error",
  "title": "Invalid Request Content",
  "status": 400,
  "detail": "Validation failed for one or more fields",
  "instance": "/api/v1/seller/products",
  "timestamp": "2026-09-23T22:56:00Z",
  "invalidParams": {
    "price": "Price must be greater than zero",
    "category": "Category is required"
  }
}
```

> Guarda `id` en `{{productId}}` y `slug` en `{{productSlug}}`.

---

### `PUT {{baseUrl}}/api/v1/seller/products/{{productId}}`

**Auth:** Bearer `{{sellerToken}}` · Todos los campos son opcionales.

```json
{
  "name": "Audifonos Inalambricos Pro v2",
  "price": 139.99,
  "description": "Version mejorada con estuche de carga"
}
```

**200 OK** — producto actualizado.

| Código | Cuándo |
|---|---|
| `200` | Actualizado |
| `400` | Payload inválido, **o el producto es de otro vendedor** (BOLA) |
| `404` | No existe |

> `stockQuantity` **se ignora** aquí. El stock se cambia con el endpoint de inventario.

---

## 6. Productos: administrador

### `GET {{baseUrl}}/api/v1/admin/products?status=PENDING_APPROVAL`

**Auth:** Bearer `{{adminToken}}` · **200 OK**

`status` acepta `PENDING_APPROVAL` (por defecto), `ACTIVE`, `REJECTED`, `INACTIVE`.

---

### `PATCH {{baseUrl}}/api/v1/admin/products/{{productId}}/approval`

**Auth:** Bearer `{{adminToken}}`

```json
{ "approved": true }
```

**200 OK** — el producto con `status: "ACTIVE"`.

| Código | Cuándo |
|---|---|
| `200` | Aplicado |
| `400` | El producto no está en `PENDING_APPROVAL` |
| `404` | No existe |

> `{"approved": false}` lo pasa a `REJECTED`.

---

## 7. Inventario

### `GET {{baseUrl}}/api/v1/inventory/check/{{productId}}` — Público

**200 OK**
```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "availableQuantity": 28,
  "reservedQuantity": 0,
  "sellableQuantity": 28,
  "lowStockThreshold": 10,
  "lowStock": false,
  "available": true,
  "lastAdjustedAt": "2026-09-23T22:58:12Z"
}
```

| Código | Cuándo |
|---|---|
| `200` | Devuelto |
| `404` | No hay registro de stock para ese producto |

**Fórmula:** `sellableQuantity = availableQuantity − reservedQuantity`.

---

### `POST {{baseUrl}}/api/v1/seller/inventory/adjust`

**Auth:** Bearer `{{sellerToken}}`

Envía **uno solo** de estos dos campos:

**a) Delta relativo** (suma o resta unidades):
```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "quantityDelta": -2,
  "reason": "2 unidades dañadas en bodega"
}
```

**b) Recuento absoluto** (fija el stock físico):
```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "physicalQuantity": 50,
  "reason": "Recuento fisico trimestral"
}
```

**200 OK** — snapshot del stock del vendedor.

| Código | Cuándo |
|---|---|
| `200` | Ajustado |
| `400` | Enviaste los dos campos, ninguno, o el ajuste dejaría stock negativo, **o el producto es de otro vendedor** |
| `404` | No hay registro de stock |

---

### `GET {{baseUrl}}/api/v1/seller/inventory?page=0&size=20`

**Auth:** Bearer `{{sellerToken}}` · **200 OK** — stock de todos los productos propios.

---

### `GET {{baseUrl}}/api/v1/admin/inventory/audit?page=0&size=50`

**Auth:** Bearer `{{adminToken}}` · Añade `&productId={{productId}}` para filtrar.

**200 OK**
```json
{
  "content": [
    {
      "movementId": "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
      "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "movementType": "ADJUSTMENT",
      "quantityDelta": -2,
      "quantityBefore": 30,
      "quantityAfter": 28,
      "reason": "2 unidades dañadas en bodega",
      "performedBy": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "reference": null,
      "occurredAt": "2026-09-23T22:58:12Z"
    }
  ],
  "totalElements": 3
}
```

**Tipos de movimiento:** `INITIAL` (al crear el producto), `ADJUSTMENT` (ajuste del vendedor),
`RESERVATION` (reserva de un checkout), `SALE` (venta confirmada), `CANCELLATION` (liberación).

---

## 8. Carrito (comprador)

### `GET {{baseUrl}}/api/v1/cart`

**Auth:** Bearer `{{buyerToken}}` · **200 OK** — si no existe, se crea uno vacío.

```json
{
  "cartId": "e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
  "buyerId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "ACTIVE",
  "items": [
    {
      "itemId": "f6a7b8c9-0d1e-2f3a-4b5c-6d7e8f9a0b1c",
      "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "productName": "Audifonos Inalambricos Pro",
      "quantity": 2,
      "unitPrice": 139.99,
      "lineTotal": 279.98,
      "currencyCode": "USD"
    }
  ],
  "totalItemCount": 2,
  "subtotal": 279.98,
  "currencyCode": "USD",
  "updatedAt": "2026-09-23T22:59:01Z"
}
```

---

### `POST {{baseUrl}}/api/v1/cart/items`

**Auth:** Bearer `{{buyerToken}}`

```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "quantity": 2
}
```

**200 OK** — el carrito actualizado.

| Código | Cuándo |
|---|---|
| `200` | Agregado (si ya estaba, **suma** cantidades) |
| `400` | Producto no `ACTIVE`, cantidad ≤ 0, cantidad > 999 o **stock insuficiente** |
| `404` | El producto no existe |

**Error de stock:**
```json
{
  "type": "https://api.marketplace.com/errors/domain-error",
  "title": "Domain Business Rule Violation",
  "status": 400,
  "detail": "Insufficient stock available for product c3d4e5f6-...",
  "timestamp": "2026-09-23T22:59:10Z"
}
```

---

### `PUT {{baseUrl}}/api/v1/cart/items/{{productId}}`

```json
{ "quantity": 3 }
```

Cantidad **absoluta**. `{"quantity": 0}` elimina la línea. **200 OK** o **400**.

---

### `DELETE {{baseUrl}}/api/v1/cart/items/{{productId}}`

Quita una línea. **200 OK** con el carrito actualizado.

### `DELETE {{baseUrl}}/api/v1/cart`

Vacía el carrito. **200 OK**.

---

## 9. Órdenes

### `POST {{baseUrl}}/api/v1/orders/checkout`

**Auth:** Bearer `{{buyerToken}}`

```json
{ "notes": "Entregar por la tarde" }
```

El cuerpo es **opcional** (puede ir vacío).

**201 Created**
```json
{
  "order": {
    "orderId": "a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
    "orderNumber": "ORD-EE7A1F1E9F724315",
    "buyerId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
    "totalAmount": 350.00,
    "currencyCode": "USD",
    "status": "PAID",
    "paymentReference": "stub-a7b8c9d0-...-4f2a1b3c",
    "paidAt": "2026-09-23T23:00:05Z",
    "createdAt": "2026-09-23T23:00:04Z",
    "subOrders": [
      {
        "subOrderId": "b8c9d0e1-2f3a-4b5c-6d7e-8f9a0b1c2d3e",
        "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
        "subtotal": 200.00,
        "currencyCode": "USD",
        "status": "PROCESSING",
        "trackingNumber": null,
        "carrier": null,
        "shippedAt": null,
        "deliveredAt": null,
        "items": [
          {
            "orderItemId": "c9d0e1f2-3a4b-5c6d-7e8f-9a0b1c2d3e4f",
            "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
            "productName": "Audifonos Inalambricos Pro",
            "quantity": 2,
            "unitPrice": 100.00,
            "lineTotal": 200.00,
            "currencyCode": "USD"
          }
        ]
      }
    ]
  },
  "paymentCaptured": true,
  "paymentReference": "stub-a7b8c9d0-...-4f2a1b3c",
  "chargedAmount": 350.00,
  "redirectUrl": null,
  "paymentMessage": "Captured by stub gateway"
}
```

**Puntos clave:**
- **Una orden, varias `subOrders`**: una por cada vendedor distinto en el carrito.
- `status: "PAID"` y `paymentCaptured: true` → el pago se capturó (pasarela simulada).
- La orden pasa de `PENDING_PAYMENT` a `PAID`, y cada sub-orden de `PENDING` a `PROCESSING`.
- Si el pago fallara, la orden quedaría en `PAYMENT_FAILED` y las sub-órdenes en `PENDING`.
- El carrito queda en `CHECKED_OUT` y se crea uno nuevo vacío en la siguiente operación.

| Código | Cuándo |
|---|---|
| `201` | Orden creada |
| `400` | Carrito vacío, producto ya no disponible, monedas mezcladas o stock insuficiente |
| `404` | El comprador no existe |
| `409` | Otro checkout modificó el stock a la vez (reintentar) |

> Guarda `order.orderId` y `order.subOrders[0].subOrderId`.

---

### `GET {{baseUrl}}/api/v1/buyer/orders?page=0&size=20`

**Auth:** Bearer `{{buyerToken}}` · **200 OK** — historial de compras propias.

### `GET {{baseUrl}}/api/v1/buyer/orders/{{orderId}}`

**200 OK** o **404** (si la orden es de otro comprador, devuelve 404 a propósito: no revela que existe).

---

### `GET {{baseUrl}}/api/v1/seller/orders?status=PROCESSING&page=0&size=20`

**Auth:** Bearer `{{sellerToken}}`

**200 OK** — página de **sub-órdenes**, no de órdenes:
```json
{
  "content": [
    {
      "subOrderId": "b8c9d0e1-2f3a-4b5c-6d7e-8f9a0b1c2d3e",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "subtotal": 200.00,
      "status": "PROCESSING",
      "trackingNumber": null,
      "carrier": null,
      "items": [ "..." ]
    }
  ],
  "totalElements": 1
}
```

`status` acepta `PENDING`, `PROCESSING`, `SHIPPED`, `DELIVERED`, `CANCELLED`. Si lo omites, devuelve
todas las del vendedor.

---

### `PATCH {{baseUrl}}/api/v1/seller/orders/{{subOrderId}}/ship`

**Auth:** Bearer `{{sellerToken}}`

```json
{
  "trackingNumber": "TRK-9938472635",
  "carrier": "DHL"
}
```

**200 OK** — la sub-orden con `status: "SHIPPED"` y `shippedAt` relleno.

| Código | Cuándo |
|---|---|
| `200` | Despachada |
| `400` | Sin `trackingNumber`, la sub-orden no está en `PROCESSING`, **o es de otro vendedor** (BOLA) |
| `404` | No existe |

> Este paso es **requisito** para que el comprador pueda dejar una reseña verificada.

---

### `GET {{baseUrl}}/api/v1/admin/orders?page=0&size=20&sort=createdAt,desc`

**Auth:** Bearer `{{adminToken}}` · **200 OK** — todas las órdenes del marketplace con sus sub-órdenes.

---

## 10. Reseñas

### `POST {{baseUrl}}/api/v1/buyer/reviews`

**Auth:** Bearer `{{buyerToken}}`

```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "rating": 5,
  "title": "Excelente calidad",
  "comment": "La cancelacion de ruido funciona muy bien y la bateria dura toda la semana."
}
```

**201 Created**
```json
{
  "reviewId": "d0e1f2a3-4b5c-6d7e-8f9a-0b1c2d3e4f5a",
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "buyerId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "rating": 5,
  "title": "Excelente calidad",
  "comment": "La cancelacion de ruido funciona muy bien...",
  "verifiedPurchase": true,
  "visible": true,
  "createdAt": "2026-09-23T23:02:30Z"
}
```

| Código | Cuándo |
|---|---|
| `201` | Creada |
| `400` | **Sin compra despachada de ese producto**, ya reseñaste ese producto, o `rating` fuera de 1-5 |
| `404` | El producto no existe |

**Mensaje si no has comprado:**
`"Only buyers with a dispatched or delivered order for this product can review it"`

---

### `GET {{baseUrl}}/api/v1/reviews/product/{{productId}}?page=0&size=10` — Público

**200 OK**
```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "averageRating": 5.0,
  "totalReviews": 1,
  "reviews": { "content": [ "..." ], "totalElements": 1 }
}
```

---

### `GET {{baseUrl}}/api/v1/admin/reviews?page=0&size=20`

**Auth:** Bearer `{{adminToken}}` · **200 OK** — incluye reseñas ocultas.

### `DELETE {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}`

**Auth:** Bearer `{{adminToken}}` · Sin cuerpo.

| Código | Cuándo |
|---|---|
| `204` | Eliminada (sin cuerpo en la respuesta) |
| `404` | No existe |

---

## 11. Pruebas de seguridad

Estas cuatro peticiones demuestran el control de acceso. Son las que más lucen en una presentación.

### Sin token → `401 Unauthorized`

`GET {{baseUrl}}/api/v1/admin/orders` con **Auth: No Auth**

```json
{
  "type": "https://api.marketplace.com/errors/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource",
  "instance": "/api/v1/admin/orders",
  "timestamp": "2026-09-23T23:03:00Z"
}
```

### Rol incorrecto → `403 Forbidden`

`GET {{baseUrl}}/api/v1/admin/orders` con **Bearer `{{buyerToken}}`**

```json
{
  "type": "https://api.marketplace.com/errors/access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "Your account does not have the required role to perform this action",
  "instance": "/api/v1/admin/orders"
}
```

### Vendedor sin verificar → `400`

`POST {{baseUrl}}/api/v1/seller/products` con `{{sellerToken}}` **antes** de que el admin lo verifique:

```json
{
  "type": "https://api.marketplace.com/errors/domain-error",
  "title": "Domain Business Rule Violation",
  "status": 400,
  "detail": "Your seller account is not verified yet; an administrator must approve it first"
}
```

### BOLA: tocar datos de otro → `400`

`PUT {{baseUrl}}/api/v1/seller/products/{{productId}}` con un `{{sellerToken}}` que **no** es el dueño:

```json
{
  "detail": "You are not authorized to update this product"
}
```

Lo mismo al intentar despachar una sub-orden ajena:
`"You are not authorized to update this sub-order"`

---

## Códigos de error

Todas las respuestas de error usan **RFC 7807 `ProblemDetail`** con estos campos:
`type`, `title`, `status`, `detail`, `instance`, `timestamp` (y `invalidParams` en validaciones).

| Código | `type` | Causa habitual |
|---|---|---|
| `400` | `/validation-error` | Campos inválidos → mira `invalidParams` |
| `400` | `/domain-error` | Regla de negocio: stock insuficiente, vendedor sin verificar, BOLA, estado inválido |
| `400` | `/malformed-body` | Cuerpo ausente o JSON mal formado |
| `401` | `/unauthorized` | Sin token, token caducado o firma inválida |
| `401` | `/invalid-credentials` | Email o contraseña incorrectos en el login |
| `403` | `/access-denied` | Token válido pero rol incorrecto |
| `404` | `/not-found` | El recurso no existe **o no es tuyo** (BOLA devuelve 404 en órdenes) |
| `409` | `/concurrent-modification` | Dos checkouts simultáneos sobre el mismo stock → reintentar |
| `409` | `/data-integrity` | Violación de restricción única |
| `500` | `/internal-error` | Error inesperado (mira el log del servidor) |

---

## Chuleta rápida de roles

| Ruta | Rol necesario |
|---|---|
| `/api/v1/auth/**` | Público |
| `GET /api/v1/products/**` | Público |
| `GET /api/v1/inventory/check/**` | Público |
| `GET /api/v1/reviews/product/**` | Público |
| `/api/v1/admin/**` | **ADMIN** |
| `/api/v1/seller/**` | **SELLER** |
| `/api/v1/cart/**` | **BUYER** |
| `/api/v1/buyer/**` | **BUYER** |
| `POST /api/v1/orders/checkout` | **BUYER** |
