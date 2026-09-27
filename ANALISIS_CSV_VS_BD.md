# Análisis: `marketplace_datos.csv` frente a las tablas de Google Cloud

**Fecha:** 26 de septiembre de 2026
**Archivo analizado:** `marketplace_datos.csv` (50.302 bytes, en la raíz del proyecto)
**Base de datos:** `marketplace_db` en Google Cloud SQL (PostgreSQL 18.6)

---

## Conclusión en una línea

**El archivo no es un CSV: es un Excel (`.xlsx`) renombrado.** Y de sus **16 hojas, solo unas pocas
columnas son aprovechables** para las tablas de esta aplicación. La mayoría de las hojas
corresponden a tablas que **este proyecto no tiene** (direcciones, marcas, favoritos, cupones,
perfiles de vendedor, varias imágenes por producto), y los usuarios **no se pueden importar**
porque sus contraseñas son hashes de relleno que no son válidos.

---

## 1. Qué es realmente el archivo

Al intentar leerlo como texto falla. Sus primeros bytes son `50 4B 03 04` (`PK`), la firma de un
archivo **ZIP**, y contiene la estructura interna de un libro de Excel:

```
xl/workbook.xml                16 hojas
xl/sharedStrings.xml           34.961 bytes de textos
xl/worksheets/sheet1.xml ... sheet16.xml
docProps/...
```

**Está mal nombrado:** debería ser `.xlsx`. Se puede abrir con Excel o LibreOffice, pero cualquier
herramienta que espere un CSV de verdad (incluida una carga directa a PostgreSQL con `COPY`) lo
rechazará.

---

## 2. Las 16 hojas del Excel

| # | Hoja | Filas | Contenido |
|---|---|---|---|
| 1 | `usuarios` | 40 | id, nombre, apellido, correo, contrasena_hash, telefono, rol, estado |
| 2 | `vendedores` | 11 | id_vendedor, id_usuario, nombre_tienda, descripcion, calificacion_promedio, fecha_registro, estado |
| 3 | `direcciones` | 44 | id_direccion, id_usuario, calle, ciudad, departamento, codigo_postal, pais, es_principal |
| 4 | `marcas` | 30 | nombre, descripcion |
| 5 | `categorias` | 34 | id_categoria, nombre, descripcion, id_categoria_padre |
| 6 | `productos` | 45 | id_producto, nombre, descripcion, precio, stock, id_marca, id_categoria, id_vendedor, imagen_url, estado |
| 7 | `producto_imagenes` | 91 | id_imagen, id_producto, url, orden |
| 8 | `carritos` | 35 | id_carrito, id_usuario, fecha_creacion |
| 9 | `carrito_items` | 94 | id_item, id_carrito, id_producto, cantidad |
| 10 | `pedidos` | 40 | id_pedido, id_usuario, id_direccion, fecha, estado, total |
| 11 | `pedido_items` | 107 | id_detalle, id_pedido, id_producto, cantidad, precio_unitario |
| 12 | `pagos` | 40 | id_pago, id_pedido, metodo_pago, estado, monto, fecha_pago, referencia_transaccion |
| 13 | `envios` | 21 | id_envio, id_pedido, transportadora, numero_guia, estado, fecha_estimada, fecha_entrega |
| 14 | `resenas` | 37 | id_resena, id_producto, id_usuario, calificacion, comentario, fecha |
| 15 | `favoritos` | 34 | id_usuario, id_producto, fecha_agregado |
| 16 | `cupones` | 24 | id_cupon, codigo, tipo_descuento, valor, fecha_inicio, fecha_expiracion, usos_maximos, usos_actuales |

---

## 3. Las 10 tablas de la aplicación en Google Cloud

| Tabla | Columnas | Filas | Columnas principales |
|---|---|---|---|
| `users` | 9 | 34 | id, email, password, role, enabled, seller_approved |
| `products` | 14 | 10 | name, description, slug, price, currency_code, stock_quantity, image_url, status, seller_id, category |
| `stock_items` | 10 | 10 | product_id, seller_id, available_quantity, reserved_quantity, low_stock_threshold |
| `stock_movements` | 13 | 34 | product_id, seller_id, movement_type, quantity_delta, before, after, reason |
| `carts` | 6 | 7 | buyer_id, status |
| `cart_items` | 11 | 10 | cart_id, product_id, seller_id, product_name, quantity, unit_price, currency_code |
| `orders` | 11 | 7 | order_number, buyer_id, total_amount, currency_code, status, payment_reference, paid_at |
| `sub_orders` | 13 | 8 | order_id, seller_id, subtotal, currency_code, status, tracking_number, carrier |
| `order_items` | 10 | 10 | sub_order_id, product_id, product_name, quantity, unit_price, currency_code |
| `reviews` | 13 | 0 | product_id, seller_id, buyer_id, rating, title, comment, sub_order_id, verified_purchase |

**Total: 10 tablas, 130 filas.**

---

## 4. Veredicto hoja por hoja

### ✅ Aprovechable

#### `productos` (45 filas) → `products`

Es la hoja con mejor encaje. Conversión necesaria:

| Columna del Excel | Columna de la tabla | Transformación |
|---|---|---|
| `nombre` | `name` | Directo |
| `descripcion` | `description` | Directo |
| `precio` | `price` | Directo (viene sin decimales, en pesos) |
| `stock` | `stock_quantity` | Directo |
| `imagen_url` | `image_url` | Directo (son URLs externas de ejemplo) |
| `estado` | `status` | `activo` → `ACTIVE` |
| `id_categoria` | `category` | **Hay que resolver el nombre** desde la hoja `categorias` |
| `id_vendedor` | `seller_id` | **Hay que remapear** a un usuario con rol vendedor ya existente |
| `id_marca` | — | **No hay sitio**: se podría concatenar al nombre o a la descripción |
| — | `slug` | **Generar** (la aplicación lo hace: `nombre-aleatorio`) |
| — | `currency_code` | **Fijar** `COP` (el Excel no lo trae; los precios son pesos colombianos) |

> **Ojo con el estado:** los productos que se inserten directamente en la base con `status='ACTIVE'`
> aparecen en el catálogo **sin pasar por moderación**. Si quieres respetar el flujo, insértalos como
> `PENDING_APPROVAL` y apruébalos desde el panel de administración.

#### `categorias` (34 filas) → `products.category`

No hay tabla de categorías, pero el **nombre** sirve para rellenar `products.category`. La jerarquía
(`id_categoria_padre`) **se pierde**: la aplicación guarda la categoría como texto libre.

#### `resenas` (37 filas) → `reviews`

| Columna del Excel | Columna de la tabla | Transformación |
|---|---|---|
| `id_producto` | `product_id` | Remapear al producto insertado |
| `id_usuario` | `buyer_id` | Remapear a un comprador |
| `calificacion` | `rating` | Directo (1 a 5; hay que validar el rango) |
| `comentario` | `comment` | Directo |
| `fecha` | `created_at` | Directo |
| — | `seller_id` | **Deducir** del producto |
| — | `sub_order_id` | **Dejar en NULL** |
| — | `verified_purchase` | **`false`** (no hay compra asociada) |

⚠️ **Estas reseñas no se pueden crear por la API**: `POST /api/v1/buyer/reviews` exige una compra
despachada y rechazaría con `400`. Habría que insertarlas con SQL directo, y quedarían como
**no verificadas**.

#### `envios` (21 filas) → `sub_orders`

| Columna del Excel | Columna de la tabla |
|---|---|
| `numero_guia` | `tracking_number` |
| `transportadora` | `carrier` |
| `estado` | `status` (`entregado` → `DELIVERED`, `enviado` → `SHIPPED`) |
| `fecha_entrega` | `delivered_at` |

**Problema:** el Excel asocia envíos a un **pedido** (`id_pedido`), pero el modelo de esta
aplicación divide cada pedido en **sub-órdenes por vendedor**. Habría que decidir a qué sub-orden
corresponde cada envío, y el Excel no tiene esa información.

#### `pagos` (40 filas) → `orders`

| Columna del Excel | Columna de la tabla |
|---|---|
| `referencia_transaccion` | `payment_reference` |
| `fecha_pago` | `paid_at` |
| `estado` (`aprobado`) | `status` → `PAID` |
| `monto` | Debe cuadrar con `total_amount` |

**No existe tabla de pagos.** `metodo_pago` (PSE, tarjeta_débito…) **se pierde**.

### ⚠️ Aprovechable con conversión, pero se pierde información

#### `usuarios` (40 filas) → `users`

| Columna del Excel | Columna de la tabla | Observación |
|---|---|---|
| `correo` | `email` | ✅ Directo |
| `rol` (`cliente`/`vendedor`) | `role` | `cliente` → `ROLE_BUYER`, `vendedor` → `ROLE_SELLER` |
| `estado` (`activo`/`inactivo`) | `enabled` | `activo` → `true` |
| **`contrasena_hash`** | `password` | ❌ **INSERVIBLE**: son valores `hash_de_prueba_1`, no son BCrypt |
| `nombre`, `apellido`, `telefono` | — | ❌ **No hay columnas** para ellos |
| — | `seller_approved` | Hay que decidirlo (para vendedores) |

> **Este fue el bloqueo real:** los hashes de relleno no permiten iniciar sesión. Se solucionó
> creando las cuentas por la API con contraseñas nuevas (ver
> [USUARIOS_PRUEBA.md](USUARIOS_PRUEBA.md)), reutilizando los correos y nombres del Excel.

#### `pedidos` (40 filas) → `orders`

| Columna del Excel | Columna de la tabla | Observación |
|---|---|---|
| `total` | `total_amount` | Directo |
| `fecha` | `created_at` | Directo |
| `estado` | `status` | Hay que mapear: `entregado` → `COMPLETED`, `pagado` → `PAID`, y definir `PENDING_PAYMENT`, `CANCELLED` |
| `id_usuario` | `buyer_id` | Remapear |
| — | `order_number` | **Generar** (`ORD-...`) |
| — | `currency_code` | Fijar `COP` |
| `id_direccion` | — | ❌ **No hay columna de dirección** |

#### `pedido_items` (107 filas) → `order_items`

| Columna del Excel | Columna de la tabla | Observación |
|---|---|---|
| `cantidad` | `quantity` | Directo |
| `precio_unitario` | `unit_price` | Directo |
| `id_producto` | `product_id` | Remapear |
| — | `sub_order_id` | **Deducir** agrupando por vendedor |
| — | `product_name` | **Deducir** del producto |
| — | `currency_code` | Fijar `COP` |

#### `carritos` (35 filas) → `carts`

| Columna del Excel | Columna de la tabla | Observación |
|---|---|---|
| `id_usuario` | `buyer_id` | Remapear |
| `fecha_creacion` | `created_at` | Directo |
| — | `status` | **Fijar** `ACTIVE` o `CHECKED_OUT` (el Excel no lo trae) |

> **Conflicto de modelo:** esta aplicación asume **un carrito activo por comprador**, mientras que
> el Excel tiene varios carritos por usuario. Se pueden importar como histórico, pero solo uno por
> comprador debería quedar `ACTIVE`.

#### `carrito_items` (94 filas) → `cart_items`

Falta todo lo que la aplicación necesita además del producto y la cantidad:

| Columna del Excel | Columna de la tabla | Observación |
|---|---|---|
| `id_carrito` | `cart_id` | Remapear |
| `id_producto` | `product_id` | Remapear |
| `cantidad` | `quantity` | Directo |
| — | `seller_id` | **Deducir** del producto |
| — | `product_name` | **Deducir** del producto |
| — | `unit_price` | **Deducir** del producto |
| — | `currency_code` | Fijar `COP` |

### ❌ No aprovechable (no existe la tabla)

| Hoja | Filas | Por qué no sirve |
|---|---|---|
| `direcciones` | 44 | **No hay tabla de direcciones.** Las órdenes no guardan dirección de envío. |
| `vendedores` | 11 | **No hay perfil de vendedor.** Un vendedor es solo un `users` con rol y `seller_approved`. Se pierden `nombre_tienda`, `descripcion` y `calificacion_promedio`. |
| `marcas` | 30 | **No hay tabla de marcas.** El producto no tiene relación con marca. |
| `producto_imagenes` | 91 | **Solo se soporta una imagen por producto** (`products.image_url`). No hay tabla de imágenes. |
| `favoritos` | 34 | **No está implementado.** |
| `cupones` | 24 | **No está implementado.** Ni siquiera hay descuentos: `orders` no tiene `discount_amount`. |

---

## 5. Resumen del encaje

| Estado | Hojas | Filas |
|---|---|---|
| ✅ Se puede cargar con conversión razonable | `productos`, `categorias`, `envios`, `pagos`, `resenas` | 228 |
| ⚠️ Se puede cargar, con pérdida de información | `usuarios`, `pedidos`, `pedido_items`, `carritos`, `carrito_items` | 316 |
| ❌ No hay dónde meterlo | `direcciones`, `vendedores`, `marcas`, `producto_imagenes`, `favoritos`, `cupones` | 234 |

**De las 778 filas del dataset, unas 544 (el 70 %) podrían llegar a las tablas** aplicando
transformaciones, y 234 (el 30 %) no tienen tabla destino.

---

## 6. Qué haría falta para cargar los productos

Es lo más rentable, porque `productos` es la hoja que mejor encaja. Dos caminos:

### Opción A: por la API (recomendado)

Un script que recorra las 45 filas y llame a `POST /api/v1/seller/products` con un token de
vendedor. Ventajas: el `slug` se genera, el stock entra en el ledger de inventario y la categoría
queda igual. Inconveniente: los productos quedan en `PENDING_APPROVAL` y hay que aprobarlos.

**Requisito:** cada `id_vendedor` del Excel tiene que corresponder a un vendedor verificado que
exista en la base.

### Opción B: por SQL directo

Un `INSERT` masivo. Más rápido, pero hay que generar los `slug`, crear a mano las filas de
`stock_items` (si no, `GET /api/v1/inventory/check/{id}` devolvería `404`) y no queda rastro en
`stock_movements`.

### Diferencias entre el dataset y esta aplicación

Si vas a cargar los datos, ten en cuenta estas incompatibilidades de modelo:

| Tema | Dataset | Esta aplicación |
|---|---|---|
| Moneda | No la declara (precios en pesos) | `currency_code` obligatorio por producto |
| Imágenes | Varias por producto (91 filas) | Una por producto |
| Marca | Entidad aparte (30) | No existe |
| Categoría | Jerárquica, con categoría padre | Texto libre en el producto |
| Direcciones | Entidad aparte (44) | No existen |
| Cupones | Entidad aparte (24) | No existen |
| Favoritos | Entidad aparte (34) | No existen |
| Perfil de vendedor | Entidad aparte (11) | Solo `seller_approved` en `users` |
| Pago | Entidad aparte, con método | Solo `payment_reference` en `orders` |
| Envío | Uno por pedido | Uno por **sub-orden** (por vendedor) |
| Estados | `activo`, `pagado`, `entregado`, `enviado` | Enums en mayúsculas (`ACTIVE`, `PAID`, `COMPLETED`, `SHIPPED`) |

---

## 7. Recomendación

1. **No intentes cargar el dataset completo.** El 30 % no tiene tabla destino y otro 40 % requiere
   transformaciones que perderían datos.
2. **Usa los usuarios del dataset** (ya hecho): se crearon 15 cuentas con los correos y nombres
   reales, con contraseñas nuevas. Ver [USUARIOS_PRUEBA.md](USUARIOS_PRUEBA.md).
3. **Para los productos, usa la hoja `productos`** con la opción A (por API), que mantiene la
   coherencia del inventario.
4. **Si la entrega exige el modelo de datos del dataset** (con marcas, direcciones, cupones,
   favoritos, varias imágenes y perfiles de vendedor), eso implica **crear tablas nuevas** y
   modificar las entidades: es un cambio de alcance, no una carga de datos.
