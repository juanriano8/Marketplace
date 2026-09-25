# Demostración paso a paso en Postman

Guía para **ejecutar cada endpoint a mano** y demostrar que funciona. Cada paso incluye la URL, la
autenticación, el código a pegar y el resultado que debes ver.

> **Requisito:** el backend debe estar arrancado y respondiendo. Ver
> [GUIA_ENTREGA.md](GUIA_ENTREGA.md). Comprueba con
> `curl.exe http://localhost:8080/actuator/health` y debe dar `{"status":"UP"}`.

## Índice

- [Parte 0: preparar Postman](#parte-0-preparar-postman)
- [Parte 1: módulo Auth](#parte-1-módulo-auth)
- [Parte 2: módulo Product](#parte-2-módulo-product)
- [Parte 3: módulo Inventory](#parte-3-módulo-inventory)
- [Parte 4: módulo Order](#parte-4-módulo-order)
- [Parte 5: módulo Review](#parte-5-módulo-review)
- [Parte 6: pruebas de seguridad](#parte-6-pruebas-de-seguridad)
- [Tabla de verificación final](#tabla-de-verificación-final)

---

# Parte 0: preparar Postman

## 0.1 Crear la colección

1. Abre Postman y ve a **Collections** (panel izquierdo)
2. Pulsa **+** y elige **Blank collection**
3. Ponle de nombre `Marketplace`

## 0.2 Crear las variables

1. Pasa el ratón sobre la colección `Marketplace`, pulsa los tres puntos y elige **Edit**
2. Ve a la pestaña **Variables**
3. Añade estas 11 variables. Deja vacías todas menos `baseUrl`:

| Variable | Valor inicial | Se rellena en |
|---|---|---|
| `baseUrl` | `http://localhost:8080` | (ya lo pones tú) |
| `adminToken` | | Paso 2 |
| `sellerToken` | | Paso 4 |
| `buyerToken` | | Paso 20 |
| `sellerId` | | Paso 3 |
| `buyerId` | | Paso 19 |
| `productId` | | Paso 8 |
| `productSlug` | | Paso 8 |
| `orderId` | | Paso 24 |
| `subOrderId` | | Paso 24 |
| `reviewId` | | Paso 30 |

4. Pulsa **Save**

> En las URLs escribe las variables con **doble llave**: `{{baseUrl}}`. Postman las sustituye por su
> valor automáticamente.

## 0.3 Cómo crear cada petición

Para cada paso de esta guía repite siempre lo mismo:

1. Pasa el ratón sobre la colección y pulsa los tres puntos, luego **Add request**
2. Ponle el nombre del paso, por ejemplo `1. Health`
3. Elige el **método** en el desplegable de la izquierda de la URL (GET, POST, PATCH, PUT, DELETE)
4. Pega la **URL** que indica el paso
5. Si el paso lleva **cuerpo**: pestaña **Body**, marca **raw**, y en el desplegable de la derecha
   elige **JSON**. Luego pega el código
6. Si el paso lleva **token**: pestaña **Authorization**, en **Type** elige **Bearer Token**, y en
   **Token** escribe `{{adminToken}}` (o el que corresponda)
7. Pulsa **Save** y después **Send**

## 0.4 Atajo opcional: capturar el token automáticamente

En la petición de login, ve a la pestaña **Scripts**, sección **Post-response**, y pega esto:

```javascript
const datos = pm.response.json();
pm.collectionVariables.set('adminToken', datos.accessToken);
```

Cambia `adminToken` por `sellerToken` o `buyerToken` según el login. Así no tienes que copiar el
token a mano.

---

# Parte 1: módulo Auth

## Paso 1: health check

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/actuator/health` |
| **Auth** | No Auth |
| **Body** | ninguno |

**Resultado esperado: `200 OK`**

```json
{ "status": "UP" }
```

**Qué demuestra:** que el servidor está vivo y conectado a la base de datos.

> Si sale `Could not get response`, el backend está apagado. Vuelve a
> [GUIA_ENTREGA.md](GUIA_ENTREGA.md#paso-2-levantar-el-backend).

---

## Paso 2: login del administrador

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/auth/login` |
| **Auth** | No Auth |
| **Body** | raw, JSON |

```json
{
  "email": "admin@marketplace.com",
  "password": "Admin123."
}
```

> Cambia la contraseña por la que tengas en `backend/.env`, campo `BOOTSTRAP_ADMIN_PASSWORD`.

**Resultado esperado: `200 OK`**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUk9MRV9BRE1JTiIs...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "f149df2d-b288-4d27-add0-7694fd848a63",
  "email": "admin@marketplace.com",
  "role": "ROLE_ADMIN",
  "sellerApproved": null
}
```

**Qué demuestra:** autenticación con JWT y roles. Fíjate en `role: ROLE_ADMIN`.

**Guarda:** copia el valor de `accessToken` en la variable `adminToken`.

**Códigos posibles:** `200` correcto, `400` falta un campo, `401` credenciales incorrectas.

**Si da `401`:** la contraseña del `.env` no coincide con la de la base. Ver
[GUIA_ENTREGA.md](GUIA_ENTREGA.md#el-login-del-administrador-da-401).

---

## Paso 3: registrar un vendedor

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/auth/register/seller` |
| **Auth** | No Auth |
| **Body** | raw, JSON |

```json
{
  "email": "vendedor.demo@marketplace.com",
  "password": "Password123!"
}
```

**Resultado esperado: `201 Created`**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "userId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "email": "vendedor.demo@marketplace.com",
  "role": "ROLE_SELLER",
  "sellerApproved": false
}
```

**Qué demuestra:** el vendedor nace **sin verificar** (`sellerApproved: false`).

**Guarda:** copia `userId` en la variable `sellerId`.

**Códigos posibles:** `201` correcto, `400` email ya registrado o contraseña corta.

> Cada email sólo se puede registrar una vez. Si repites la demo, cambia el correo (por ejemplo
> `vendedor.demo2@marketplace.com`). Si repites con el mismo recibirás `400` con
> *"Email is already registered"*, que es el comportamiento correcto.

---

## Paso 4: login del vendedor

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/auth/login` |
| **Auth** | No Auth |
| **Body** | raw, JSON |

```json
{
  "email": "vendedor.demo@marketplace.com",
  "password": "Password123!"
}
```

**Resultado esperado: `200 OK`** con `"role": "ROLE_SELLER"`.

**Guarda:** copia `accessToken` en la variable `sellerToken` (y su `userId` en `sellerId`).

---

## Paso 5: el vendedor sin verificar NO puede publicar

Este paso es una de las demostraciones más contundentes.

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/seller/products` |
| **Auth** | Bearer `{{sellerToken}}` |
| **Body** | raw, JSON |

```json
{
  "name": "Producto de prueba",
  "price": 50.00,
  "stockQuantity": 5,
  "category": "Test"
}
```

**Resultado esperado: `400 Bad Request`**

```json
{
  "type": "https://api.marketplace.com/errors/domain-error",
  "title": "Domain Business Rule Violation",
  "status": 400,
  "detail": "Your seller account is not verified yet; an administrator must approve it first",
  "instance": "/api/v1/seller/products",
  "timestamp": "2026-09-24T..."
}
```

**Qué demuestra:** una **regla de negocio** en el dominio: no se publica sin verificación previa
del administrador, aunque el token y el rol sean correctos.

---

## Paso 6: el admin lista los vendedores pendientes

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/sellers?approved=false` |
| **Auth** | Bearer `{{adminToken}}` |
| **Body** | ninguno |

**Resultado esperado: `200 OK`**

```json
{
  "content": [
    {
      "id": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "email": "vendedor.demo@marketplace.com",
      "role": "ROLE_SELLER",
      "enabled": true,
      "sellerApproved": false,
      "createdAt": "2026-09-24T..."
    }
  ],
  "totalElements": 1
}
```

**Qué demuestra:** la bandeja de verificación. Sin este endpoint el administrador tendría que
conocer el `sellerId` de antemano.

**Guarda:** confirma que aparece tu vendedor y copia su `id` en `sellerId`.

> Usa `?approved=true` para ver sólo los ya verificados, o quita el parámetro para ver todos.

---

## Paso 7: verificar al vendedor (spec 2.1)

| | |
|---|---|
| **Método y URL** | `PATCH {{baseUrl}}/api/v1/admin/sellers/{{sellerId}}/verify` |
| **Auth** | Bearer `{{adminToken}}` |
| **Body** | raw, JSON |

```json
{
  "approved": true
}
```

**Resultado esperado: `200 OK`**

```json
{
  "id": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "email": "vendedor.demo@marketplace.com",
  "role": "ROLE_SELLER",
  "enabled": true,
  "sellerApproved": true,
  "createdAt": "2026-09-24T..."
}
```

**Qué demuestra:** `sellerApproved` pasa a `true`. A partir de aquí sí puede publicar.

**Códigos posibles:** `200` correcto, `400` el usuario no es vendedor, `403` no eres admin,
`404` no existe ese id.

> Si envías `{"approved": false}` se **rechaza** al vendedor y además se **deshabilita** su cuenta
> (`enabled: false`), así que ya no podrá iniciar sesión.

---

# Parte 2: módulo Product

## Paso 8: crear un producto (spec 2.2)

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/seller/products` |
| **Auth** | Bearer `{{sellerToken}}` |
| **Body** | raw, JSON |

```json
{
  "name": "Audifonos Inalambricos Pro",
  "description": "Cancelacion de ruido activa, 40 horas de bateria",
  "price": 149.99,
  "currencyCode": "USD",
  "stockQuantity": 25,
  "category": "Electronics",
  "imageUrl": "https://storage.googleapis.com/mi-bucket/audifonos.jpg"
}
```

**Resultado esperado: `201 Created`**

```json
{
  "id": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "name": "Audifonos Inalambricos Pro",
  "description": "Cancelacion de ruido activa, 40 horas de bateria",
  "slug": "audifonos-inalambricos-pro-a1b2c3d4",
  "price": 149.99,
  "currencyCode": "USD",
  "stockQuantity": 25,
  "imageUrl": "https://storage.googleapis.com/mi-bucket/audifonos.jpg",
  "status": "PENDING_APPROVAL",
  "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "category": "Electronics",
  "createdAt": "2026-09-24T...",
  "updatedAt": "2026-09-24T..."
}
```

**Qué demuestra:** el producto nace en **`PENDING_APPROVAL`** y el `slug` se genera
automáticamente a partir del nombre.

**Guarda:** copia `id` en `productId` y `slug` en `productSlug`.

**Códigos posibles:** `201` correcto, `400` payload inválido o vendedor no verificado,
`403` no eres vendedor.

---

## Paso 9: el catálogo público todavía NO lo muestra (spec 2.2)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/products?page=0&size=20` |
| **Auth** | No Auth |
| **Body** | ninguno |

**Resultado esperado: `200 OK`**

```json
{
  "content": [ ... ],
  "totalElements": 6,
  "totalPages": 1,
  "pageable": { "pageNumber": 0, "pageSize": 20 },
  "last": true,
  "first": true
}
```

**Qué demuestra:** el catálogo público sólo expone productos **`ACTIVE`**. Busca
`Audifonos Inalambricos Pro` en la respuesta: **no aparece**. Ese es el control de moderación
funcionando.

> El número de `totalElements` depende de los datos que ya haya en la base.

---

## Paso 10: el admin lista los productos pendientes

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/products?status=PENDING_APPROVAL` |
| **Auth** | Bearer `{{adminToken}}` |
| **Body** | ninguno |

**Resultado esperado: `200 OK`** con una página que incluye el producto del paso 8.

**Guarda:** copia el `id` del producto en `productId` y su `slug` en `productSlug`.

> Otros valores de `status`: `ACTIVE`, `REJECTED`, `INACTIVE`.

---

## Paso 11: aprobar el producto (spec 2.2)

| | |
|---|---|
| **Método y URL** | `PATCH {{baseUrl}}/api/v1/admin/products/{{productId}}/approval` |
| **Auth** | Bearer `{{adminToken}}` |
| **Body** | raw, JSON |

```json
{
  "approved": true
}
```

**Resultado esperado: `200 OK`** con el producto y `"status": "ACTIVE"`.

**Qué demuestra:** la moderación. Ahora el producto es visible para el público.

**Códigos posibles:** `200` correcto, `400` el producto no está en `PENDING_APPROVAL`,
`403` no eres admin, `404` no existe.

---

## Paso 12: el producto ya aparece en el catálogo (spec 2.2)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/products?page=0&size=20` |
| **Auth** | No Auth |

**Resultado esperado: `200 OK`** y ahora **sí** aparece `Audifonos Inalambricos Pro`.

**Qué demuestra:** el ciclo completo vendedor, moderación y publicación.

---

## Paso 13: detalle del producto por slug (spec 2.2)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/products/{{productSlug}}` |
| **Auth** | No Auth |

**Resultado esperado: `200 OK`** con el objeto producto completo.

**Códigos posibles:** `200` correcto, `404` no existe o no está `ACTIVE`.

---

## Paso 14: listar el catálogo propio del vendedor (spec 2.2)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/seller/products?page=0&size=20` |
| **Auth** | Bearer `{{sellerToken}}` |

**Resultado esperado: `200 OK`** con todos tus productos, **incluidos los que siguen en
`PENDING_APPROVAL`** (al contrario que el catálogo público).

**Qué demuestra:** cada vendedor ve sólo su catálogo.

---

## Paso 15: editar el producto (spec 2.2)

| | |
|---|---|
| **Método y URL** | `PUT {{baseUrl}}/api/v1/seller/products/{{productId}}` |
| **Auth** | Bearer `{{sellerToken}}` |
| **Body** | raw, JSON |

```json
{
  "name": "Audifonos Inalambricos Pro v2",
  "price": 139.99,
  "description": "Version mejorada con estuche de carga"
}
```

**Resultado esperado: `200 OK`** con el precio actualizado.

**Qué demuestra:** el vendedor sólo puede editar **sus** productos. Si usas el token de otro
vendedor devuelve `400` con *"You are not authorized to update this product"*. Es una validación
**BOLA** (Broken Object Level Authorization).

> El campo `stockQuantity` se ignora aquí a propósito: el stock se cambia desde el módulo de
> inventario.

**Códigos posibles:** `200` correcto, `400` payload inválido o producto de otro vendedor,
`403` no eres vendedor, `404` no existe.

---

# Parte 3: módulo Inventory

## Paso 16: consultar disponibilidad (spec 2.3, público)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/inventory/check/{{productId}}` |
| **Auth** | No Auth |
| **Body** | ninguno |

**Resultado esperado: `200 OK`**

```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
  "availableQuantity": 25,
  "reservedQuantity": 0,
  "sellableQuantity": 25,
  "lowStockThreshold": 10,
  "lowStock": false,
  "available": true,
  "lastAdjustedAt": "2026-09-24T..."
}
```

**Qué demuestra:** la separación entre stock físico, reservado y vendible.
La fórmula es `sellableQuantity = availableQuantity - reservedQuantity`.

**Códigos posibles:** `200` correcto, `404` no hay registro de stock para ese producto.

---

## Paso 17: ajustar existencias con un delta (spec 2.3)

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/seller/inventory/adjust` |
| **Auth** | Bearer `{{sellerToken}}` |
| **Body** | raw, JSON |

```json
{
  "productId": "{{productId}}",
  "quantityDelta": -2,
  "reason": "2 unidades danadas en bodega"
}
```

**Resultado esperado: `200 OK`** con `availableQuantity` bajando de 25 a **23**.

**Qué demuestra:** el ajuste **relativo**. El motivo queda registrado en la auditoría.

---

## Paso 18: ajustar con un recuento absoluto (spec 2.3)

Misma URL y token, cambiando el cuerpo. Recuerda: se envía **sólo uno** de los dos campos.

```json
{
  "productId": "{{productId}}",
  "physicalQuantity": 30,
  "reason": "Recuento fisico trimestral"
}
```

**Resultado esperado: `200 OK`** con `availableQuantity` en **30**.

**Qué demuestra:** los dos modos de ajuste (relativo y absoluto).

**Códigos posibles:** `200` correcto, `400` enviaste los dos campos, ninguno, o el ajuste dejaría
el stock negativo, o el producto es de otro vendedor, `404` no hay registro de stock.

> **Demostración del guardarraíl:** envía `"quantityDelta": -9999` y verás `400` con
> *"Adjustment would leave a negative stock level"*.

---

## Paso 19: ver el stock propio

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/seller/inventory?page=0&size=20` |
| **Auth** | Bearer `{{sellerToken}}` |

**Resultado esperado: `200 OK`** con una página con el stock de todos tus productos.

---

## Paso 20: auditoría global de inventario (spec 2.3)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/inventory/audit?page=0&size=50` |
| **Auth** | Bearer `{{adminToken}}` |

**Resultado esperado: `200 OK`**

```json
{
  "content": [
    {
      "movementId": "d4e5f6a7-8b9c-0d1e-2f3a-4b5c6d7e8f9a",
      "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "movementType": "ADJUSTMENT",
      "quantityDelta": -2,
      "quantityBefore": 25,
      "quantityAfter": 23,
      "reason": "2 unidades danadas en bodega",
      "performedBy": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "reference": null,
      "occurredAt": "2026-09-24T..."
    }
  ],
  "totalElements": 3
}
```

**Qué demuestra:** el **ledger append-only**. Aparecen los movimientos generados en los pasos 8,
17 y 18: un `INITIAL` y dos `ADJUSTMENT`.

> Añade `&productId={{productId}}` para filtrar por un producto concreto.

**Tipos de movimiento:** `INITIAL`, `ADJUSTMENT`, `RESERVATION`, `SALE`, `CANCELLATION`.

---

# Parte 4: módulo Order

## Paso 21: registrar un comprador

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/auth/register/buyer` |
| **Auth** | No Auth |
| **Body** | raw, JSON |

```json
{
  "email": "comprador.demo@marketplace.com",
  "password": "Password123!"
}
```

**Resultado esperado: `201 Created`** con `"role": "ROLE_BUYER"` y `"sellerApproved": null`.

**Guarda:** copia `userId` en `buyerId` y `accessToken` en `buyerToken`.

---

## Paso 22: login del comprador

Si perdiste el token, repite el login:

```json
{
  "email": "comprador.demo@marketplace.com",
  "password": "Password123!"
}
```

**Resultado esperado: `200 OK`**. Guarda `accessToken` en `buyerToken`.

---

## Paso 23: obtener el carrito (spec 2.4)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/cart` |
| **Auth** | Bearer `{{buyerToken}}` |

**Resultado esperado: `200 OK`**

```json
{
  "cartId": "e5f6a7b8-9c0d-1e2f-3a4b-5c6d7e8f9a0b",
  "buyerId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "ACTIVE",
  "items": [],
  "totalItemCount": 0,
  "subtotal": 0,
  "currencyCode": "USD",
  "updatedAt": "2026-09-24T..."
}
```

**Qué demuestra:** el carrito se crea vacío automáticamente la primera vez que se consulta.

---

## Paso 24: añadir el producto al carrito (spec 2.4)

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/cart/items` |
| **Auth** | Bearer `{{buyerToken}}` |
| **Body** | raw, JSON |

```json
{
  "productId": "{{productId}}",
  "quantity": 2
}
```

**Resultado esperado: `200 OK`**

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
  "updatedAt": "2026-09-24T..."
}
```

**Qué demuestra:** se valida que el producto esté `ACTIVE` y que haya stock suficiente antes de
aceptar el ítem.

**Códigos posibles:** `200` correcto, `400` producto no disponible, cantidad inválida o stock
insuficiente, `404` el producto no existe.

> Ejecútalo otra vez con `"quantity": 1` y verás que **fusiona** las cantidades en la misma línea
> (pasa a 3) en lugar de crear una línea nueva.

---

## Paso 25: no se puede comprar más de lo que hay

Misma URL, cambiando la cantidad. Otra demostración muy visual.

```json
{
  "productId": "{{productId}}",
  "quantity": 9999
}
```

**Resultado esperado: `400 Bad Request`**

```json
{
  "type": "https://api.marketplace.com/errors/domain-error",
  "title": "Domain Business Rule Violation",
  "status": 400,
  "detail": "Insufficient stock available for product c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "timestamp": "2026-09-24T..."
}
```

**Qué demuestra:** el **control de stock**. El sistema rechaza la operación en lugar de aceptar un
carrito que nunca se podría pagar.

---

## Paso 26: cambiar la cantidad de una línea

| | |
|---|---|
| **Método y URL** | `PUT {{baseUrl}}/api/v1/cart/items/{{productId}}` |
| **Auth** | Bearer `{{buyerToken}}` |
| **Body** | raw, JSON |

```json
{
  "quantity": 2
}
```

**Resultado esperado: `200 OK`** con el carrito actualizado.

> Con `{"quantity": 0}` se elimina la línea.

---

## Paso 27: checkout y pago (spec 2.4)

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/orders/checkout` |
| **Auth** | Bearer `{{buyerToken}}` |
| **Body** | raw, JSON |

```json
{
  "notes": "Entregar por la tarde"
}
```

> El cuerpo es opcional, puedes dejarlo vacío.

**Resultado esperado: `201 Created`**

```json
{
  "order": {
    "orderId": "a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d",
    "orderNumber": "ORD-EE7A1F1E9F724315",
    "buyerId": "a1b2c3d4-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
    "totalAmount": 279.98,
    "currencyCode": "USD",
    "status": "PAID",
    "paymentReference": "stub-a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d-4f2a1b3c",
    "paidAt": "2026-09-24T...",
    "createdAt": "2026-09-24T...",
    "subOrders": [
      {
        "subOrderId": "b8c9d0e1-2f3a-4b5c-6d7e-8f9a0b1c2d3e",
        "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
        "subtotal": 279.98,
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
            "unitPrice": 139.99,
            "lineTotal": 279.98,
            "currencyCode": "USD"
          }
        ]
      }
    ]
  },
  "paymentCaptured": true,
  "paymentReference": "stub-a7b8c9d0-1e2f-3a4b-5c6d-7e8f9a0b1c2d-4f2a1b3c",
  "chargedAmount": 279.98,
  "redirectUrl": null,
  "paymentMessage": "Captured by stub gateway"
}
```

**Qué demuestra, y es lo más importante de la demo:**

- **Una orden con varias `subOrders`**: se crea una sub-orden por cada vendedor distinto que haya
  en el carrito. El comprador ve **un solo pago**; cada vendedor verá **sólo su parte**.
- `status: PAID` y `paymentCaptured: true` significan que el pago se capturó con la pasarela
  simulada.
- El stock se **reserva con bloqueo pesimista** antes de cobrar y luego se descuenta.
- El carrito pasa a `CHECKED_OUT`.

**Guarda:** copia `order.orderId` en `orderId` y `order.subOrders[0].subOrderId` en `subOrderId`.

**Códigos posibles:** `201` correcto, `400` carrito vacío, producto no disponible, monedas
mezcladas o stock insuficiente, `403` no eres comprador, `409` otro checkout tocó el stock a la
vez (reintentar).

> **Demostración opcional muy potente:** crea un producto con **1 sola unidad**, cómpralo con este
> comprador y luego intenta comprarlo con **otro** comprador. El segundo recibirá `400`: el sistema
> **no sobrevende**.

---

## Paso 28: historial de compras (spec 2.4)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/buyer/orders?page=0&size=20` |
| **Auth** | Bearer `{{buyerToken}}` |

**Resultado esperado: `200 OK`** con una página de órdenes, la del paso 27 la primera.

---

## Paso 29: detalle de una orden propia

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/buyer/orders/{{orderId}}` |
| **Auth** | Bearer `{{buyerToken}}` |

**Resultado esperado: `200 OK`** con la orden completa y sus sub-órdenes.

**Qué demuestra:** si usas la orden de **otro** comprador devuelve `404` a propósito: no revela ni
siquiera que existe. Es una validación **BOLA**.

---

## Paso 30: despachos pendientes del vendedor (spec 2.4)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/seller/orders?status=PROCESSING&page=0&size=20` |
| **Auth** | Bearer `{{sellerToken}}` |

**Resultado esperado: `200 OK`**

```json
{
  "content": [
    {
      "subOrderId": "b8c9d0e1-2f3a-4b5c-6d7e-8f9a0b1c2d3e",
      "sellerId": "b2c3d4e5-6f7a-8b9c-0d1e-2f3a4b5c6d7e",
      "subtotal": 279.98,
      "currencyCode": "USD",
      "status": "PROCESSING",
      "trackingNumber": null,
      "carrier": null,
      "shippedAt": null,
      "deliveredAt": null,
      "items": [ ... ]
    }
  ],
  "totalElements": 1
}
```

**Qué demuestra:** el vendedor recibe **sólo su parte** de la orden, no la orden completa del
comprador.

**Guarda:** copia `subOrderId` en la variable `subOrderId`.

> Otros valores de `status`: `PENDING`, `SHIPPED`, `DELIVERED`, `CANCELLED`. Si quitas el
> parámetro, devuelve todas las sub-órdenes del vendedor.

---

## Paso 31: registrar el despacho (spec 2.4)

| | |
|---|---|
| **Método y URL** | `PATCH {{baseUrl}}/api/v1/seller/orders/{{subOrderId}}/ship` |
| **Auth** | Bearer `{{sellerToken}}` |
| **Body** | raw, JSON |

```json
{
  "trackingNumber": "TRK-9938472635",
  "carrier": "DHL"
}
```

**Resultado esperado: `200 OK`** con la sub-orden y `"status": "SHIPPED"`, más `shippedAt`
relleno.

**Qué demuestra:** la máquina de estados del despacho, de `PROCESSING` a `SHIPPED`.

**Códigos posibles:** `200` correcto, `400` sin `trackingNumber`, la sub-orden no está en
`PROCESSING`, o es de otro vendedor, `404` no existe.

> **Este paso es requisito** para que el comprador pueda dejar una reseña verificada. Prueba a
> enviar `"trackingNumber": ""` y verás un `400` de validación.

---

## Paso 32: consulta global de transacciones (spec 2.4)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/orders?page=0&size=20&sort=createdAt,desc` |
| **Auth** | Bearer `{{adminToken}}` |

**Resultado esperado: `200 OK`** con **todas** las órdenes del marketplace, sus sub-órdenes,
estados de despacho y referencias de pago.

**Qué demuestra:** la vista de administración sobre las transacciones.

---

# Parte 5: módulo Review

## Paso 33: crear una reseña (spec 2.5)

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/buyer/reviews` |
| **Auth** | Bearer `{{buyerToken}}` |
| **Body** | raw, JSON |

```json
{
  "productId": "{{productId}}",
  "rating": 5,
  "title": "Excelente calidad",
  "comment": "La cancelacion de ruido funciona muy bien y la bateria dura toda la semana."
}
```

**Resultado esperado: `201 Created`**

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
  "createdAt": "2026-09-24T..."
}
```

**Qué demuestra:** **`verifiedPurchase: true`**. El sistema comprobó que el comprador tiene una
sub-orden despachada de ese producto. Sólo se reseña lo que se ha comprado.

**Guarda:** copia `reviewId` en la variable `reviewId`.

**Si sale `400`:**

| Mensaje | Causa |
|---|---|
| *"Only buyers with a dispatched or delivered order for this product can review it"* | Falta el paso 31 (registrar el despacho) |
| *"You have already reviewed this product"* | Sólo se permite una reseña por comprador y producto |
| Con `invalidParams` sobre `rating` | El valor debe estar entre 1 y 5 |

> Pruébalo con `"rating": 9` para ver la validación.

---

## Paso 34: opiniones del producto (spec 2.5, público)

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/reviews/product/{{productId}}?page=0&size=10` |
| **Auth** | No Auth |

**Resultado esperado: `200 OK`**

```json
{
  "productId": "c3d4e5f6-7a8b-9c0d-1e2f-3a4b5c6d7e8f",
  "averageRating": 5.0,
  "totalReviews": 1,
  "reviews": {
    "content": [
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
        "createdAt": "2026-09-24T..."
      }
    ],
    "totalElements": 1
  }
}
```

**Qué demuestra:** la nota media agregada y que sólo se exponen reseñas visibles.

**Códigos posibles:** `200` correcto, `404` el producto no existe.

---

## Paso 35: el admin lista todas las reseñas

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/reviews?page=0&size=20` |
| **Auth** | Bearer `{{adminToken}}` |

**Resultado esperado: `200 OK`** e incluye también las reseñas ocultas por moderación.

---

## Paso 36: eliminar una reseña (spec 2.5)

| | |
|---|---|
| **Método y URL** | `DELETE {{baseUrl}}/api/v1/admin/reviews/{{reviewId}}` |
| **Auth** | Bearer `{{adminToken}}` |
| **Body** | ninguno |

**Resultado esperado: `204 No Content`**, es decir, respuesta **sin cuerpo**.

**Qué demuestra:** la moderación de contenido. Vuelve a ejecutar el paso 34 y verás que
`totalReviews` bajó.

**Códigos posibles:** `204` correcto, `403` no eres admin, `404` no existe.

---

# Parte 6: pruebas de seguridad

Estas pruebas son las que mejor lucen, porque demuestran el control de acceso.

## Paso 37: sin token devuelve 401

| | |
|---|---|
| **Método y URL** | `GET {{baseUrl}}/api/v1/admin/orders` |
| **Auth** | **No Auth** (desactiva la pestaña Authorization) |

**Resultado esperado: `401 Unauthorized`**

```json
{
  "type": "https://api.marketplace.com/errors/unauthorized",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource",
  "instance": "/api/v1/admin/orders",
  "timestamp": "2026-09-24T..."
}
```

**Qué demuestra:** los endpoints protegidos rechazan peticiones anónimas.

---

## Paso 38: rol incorrecto devuelve 403

Misma URL, pero con **Auth: Bearer `{{buyerToken}}`** (un comprador).

**Resultado esperado: `403 Forbidden`**

```json
{
  "type": "https://api.marketplace.com/errors/access-denied",
  "title": "Forbidden",
  "status": 403,
  "detail": "Your account does not have the required role to perform this action",
  "instance": "/api/v1/admin/orders",
  "timestamp": "2026-09-24T..."
}
```

**Qué demuestra:** el token es válido, pero el **rol** no alcanza. Es la diferencia entre `401`
(no autenticado) y `403` (autenticado sin permiso).

> Repítelo con el mismo token de comprador contra `GET {{baseUrl}}/api/v1/seller/products`, que es
> área de vendedor: también dará `403`.

---

## Paso 39: validación de datos devuelve 400 con detalle

| | |
|---|---|
| **Método y URL** | `POST {{baseUrl}}/api/v1/auth/register/buyer` |
| **Auth** | No Auth |
| **Body** | raw, JSON |

```json
{
  "email": "no-es-un-email",
  "password": "123"
}
```

**Resultado esperado: `400 Bad Request`**

```json
{
  "type": "https://api.marketplace.com/errors/validation-error",
  "title": "Invalid Request Content",
  "status": 400,
  "detail": "Validation failed for one or more fields",
  "instance": "/api/v1/auth/register/buyer",
  "timestamp": "2026-09-24T...",
  "invalidParams": {
    "email": "Invalid email format",
    "password": "Password must be at least 8 characters long"
  }
}
```

**Qué demuestra:** validación de entrada con el detalle campo por campo en `invalidParams`, y todo
en formato **RFC 7807**.

---

## Paso 40: BOLA, un vendedor no puede tocar lo de otro

**Preparación:** registra un **segundo** vendedor (como en el paso 3, con otro correo),
verifícalo como admin (paso 7) y haz login para obtener su token en una variable nueva
`sellerToken2`.

Después usa ese token contra un producto del **primer** vendedor:

| | |
|---|---|
| **Método y URL** | `PUT {{baseUrl}}/api/v1/seller/products/{{productId}}` |
| **Auth** | Bearer `{{sellerToken2}}` |
| **Body** | raw, JSON |

```json
{
  "price": 1.00
}
```

**Resultado esperado: `400 Bad Request`**

```json
{
  "type": "https://api.marketplace.com/errors/domain-error",
  "title": "Domain Business Rule Violation",
  "status": 400,
  "detail": "You are not authorized to update this product",
  "timestamp": "2026-09-24T..."
}
```

**Qué demuestra:** **BOLA** (Broken Object Level Authorization). El rol es correcto, es un
vendedor, pero el recurso no es suyo. La autorización no se queda en el rol: también comprueba la
propiedad.

> Lo mismo ocurre con `PATCH /api/v1/seller/orders/{{subOrderId}}/ship` si usas la sub-orden de
> otro vendedor: devuelve *"You are not authorized to update this sub-order"*.

---

# Tabla de verificación final

Marca cada endpoint de la especificación cuando lo hayas ejecutado. Todas las casillas deben
quedar marcadas.

## Módulo Auth (spec 2.1)

| # | Endpoint | Paso | Esperado | Hecho |
|---|---|---|---|---|
| 1 | `POST /api/v1/auth/register/buyer` | 21 | `201` | [ ] |
| 2 | `POST /api/v1/auth/register/seller` | 3 | `201` | [ ] |
| 3 | `POST /api/v1/auth/login` | 2 | `200` | [ ] |
| 4 | `PATCH /api/v1/admin/sellers/{sellerId}/verify` | 7 | `200` | [ ] |

## Módulo Product (spec 2.2)

| # | Endpoint | Paso | Esperado | Hecho |
|---|---|---|---|---|
| 5 | `GET /api/v1/products` | 9 y 12 | `200` | [ ] |
| 6 | `GET /api/v1/products/{slug}` | 13 | `200` | [ ] |
| 7 | `GET /api/v1/seller/products` | 14 | `200` | [ ] |
| 8 | `POST /api/v1/seller/products` | 8 | `201` | [ ] |
| 9 | `PUT /api/v1/seller/products/{id}` | 15 | `200` | [ ] |
| 10 | `PATCH /api/v1/admin/products/{id}/approval` | 11 | `200` | [ ] |

## Módulo Inventory (spec 2.3)

| # | Endpoint | Paso | Esperado | Hecho |
|---|---|---|---|---|
| 11 | `GET /api/v1/inventory/check/{productId}` | 16 | `200` | [ ] |
| 12 | `POST /api/v1/seller/inventory/adjust` | 17 y 18 | `200` | [ ] |
| 13 | `GET /api/v1/admin/inventory/audit` | 20 | `200` | [ ] |

## Módulo Order (spec 2.4)

| # | Endpoint | Paso | Esperado | Hecho |
|---|---|---|---|---|
| 14 | `GET /api/v1/cart` | 23 | `200` | [ ] |
| 15 | `POST /api/v1/cart/items` | 24 | `200` | [ ] |
| 16 | `POST /api/v1/orders/checkout` | 27 | `201` | [ ] |
| 17 | `GET /api/v1/buyer/orders` | 28 | `200` | [ ] |
| 18 | `GET /api/v1/seller/orders` | 30 | `200` | [ ] |
| 19 | `PATCH /api/v1/seller/orders/{subOrderId}/ship` | 31 | `200` | [ ] |
| 20 | `GET /api/v1/admin/orders` | 32 | `200` | [ ] |

## Módulo Review (spec 2.5)

| # | Endpoint | Paso | Esperado | Hecho |
|---|---|---|---|---|
| 21 | `POST /api/v1/buyer/reviews` | 33 | `201` | [ ] |
| 22 | `GET /api/v1/reviews/product/{productId}` | 34 | `200` | [ ] |
| 23 | `DELETE /api/v1/admin/reviews/{reviewId}` | 36 | `204` | [ ] |

Con esto quedan cubiertas **las 23 rutas de la especificación**.

## Pruebas de seguridad

| Prueba | Paso | Esperado | Hecho |
|---|---|---|---|
| Petición anónima a endpoint protegido | 37 | `401` | [ ] |
| Rol incorrecto | 38 | `403` | [ ] |
| Validación de entrada | 39 | `400` con `invalidParams` | [ ] |
| Vendedor sin verificar no publica | 5 | `400` | [ ] |
| Stock insuficiente | 25 | `400` | [ ] |
| BOLA entre vendedores | 40 | `400` | [ ] |

---

# Alternativa: la colección automática

Si prefieres no crear las peticiones a mano, importa
`backend/postman/Marketplace-API.postman_collection.json` en Postman. Trae **44 peticiones** ya
organizadas y **captura sola** los tokens y los IDs.

> **Atención:** la colección trae `Admin123!` como contraseña del admin por defecto. Cámbiala en
> las variables de la colección, campo `adminPassword`, por la que tengas en `backend/.env`.

En resumen: usa esta guía para **explicar** cada endpoint en la presentación, y la colección para
**ejecutar** rápido.
