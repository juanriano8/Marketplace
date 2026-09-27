# Usuarios de prueba

**Fecha de creación:** 26 de septiembre de 2026
**Base de datos:** `marketplace_db` en Google Cloud SQL
**Estado:** ✅ los 16 accesos verificados con login real contra la API (16 OK / 0 fallos)

Los correos y nombres provienen del dataset de referencia
(`marketplace_datos.csv`, hoja `usuarios`), para que los datos de prueba coincidan con el modelo de
datos del proyecto.

---

## Resumen rápido de credenciales

| Rol | Cantidad | Contraseña |
|---|---|---|
| **Administrador** | 1 | `Admin123!` |
| **Vendedor** | 5 | `Vendedor123!` |
| **Comprador** | 10 | `Comprador123!` |

Los 5 vendedores están **verificados** (`seller_approved = true`), así que pueden publicar productos
y registrar despachos desde el primer momento.

---

## Administrador (1)

| Campo | Valor |
|---|---|
| **Nombre** | Administrador de prueba |
| **Correo** | `admin.prueba@marketplace.com` |
| **Contraseña** | `Admin123!` |
| **Rol** | `ROLE_ADMIN` |
| **Estado** | Habilitado |
| **ID** | `21d14a38-ce69-4687-991b-c5b0462b0942` |

### Qué puede hacer

- Aprobar o rechazar productos (`PATCH /api/v1/admin/products/{id}/approval`)
- Verificar o rechazar vendedores (`PATCH /api/v1/admin/sellers/{sellerId}/verify`)
- Ver todos los vendedores (`GET /api/v1/admin/sellers`)
- Consultar todas las transacciones (`GET /api/v1/admin/orders`)
- Auditar el inventario completo (`GET /api/v1/admin/inventory/audit`)
- Moderar reseñas (`GET` y `DELETE /api/v1/admin/reviews`)

### Otro administrador que ya existía

| Correo | Contraseña | Nota |
|---|---|---|
| `admin@marketplace.com` | La del `.env` (`BOOTSTRAP_ADMIN_PASSWORD`) | Se creó en el primer arranque. **No la cambié.** |

> Existen **2 administradores** en total. Si cambias `BOOTSTRAP_ADMIN_PASSWORD` en el `.env`, el
> segundo no se actualiza: el bootstrap solo crea el usuario si no existe.

---

## Vendedores (5)

Todos con la contraseña **`Vendedor123!`** y **verificados**.

| # | Nombre | Correo | ID en el CSV | Nombre de tienda (del CSV) |
|---|---|---|---|---|
| 1 | Diego Herrera | `diego.herrera3@marketplace.com` | 3 | PetShopOnline |
| 2 | Jorge Ramirez | `jorge.ramirez4@marketplace.com` | 4 | PetShopOnline |
| 3 | Santiago Flores | `santiago.flores9@marketplace.com` | 9 | BeautyPlus 3 |
| 4 | Daniel Herrera | `daniel.herrera17@marketplace.com` | 17 | — |
| 5 | Andres Medina | `andres.medina21@marketplace.com` | 21 | — |

> La columna **tienda** viene de la hoja `vendedores` del CSV. Es solo informativa: la aplicación
> **no tiene tabla de perfiles de vendedor**, así que ese dato no se guardó en la base.

### Qué puede hacer

- Ver y crear productos (`GET` y `POST /api/v1/seller/products`)
- Editar sus propios productos (`PUT /api/v1/seller/products/{id}`)
- Ver y ajustar su inventario (`GET /api/v1/seller/inventory`, `POST /api/v1/seller/inventory/adjust`)
- Ver sus sub-órdenes y registrar despachos (`GET /api/v1/seller/orders`, `PATCH .../ship`)

---

## Compradores (10)

Todos con la contraseña **`Comprador123!`**.

| # | Nombre | Correo | ID en el CSV |
|---|---|---|---|
| 1 | Tomas Rodriguez | `tomas.rodriguez1@marketplace.com` | 1 |
| 2 | Veronica Medina | `veronica.medina2@marketplace.com` | 2 |
| 3 | Daniela Rodriguez | `daniela.rodriguez6@marketplace.com` | 6 |
| 4 | Isabella Jimenez | `isabella.jimenez8@marketplace.com` | 8 |
| 5 | Miguel Torres | `miguel.torres10@marketplace.com` | 10 |
| 6 | Natalia Guzman | `natalia.guzman11@marketplace.com` | 11 |
| 7 | Juliana Ortiz | `juliana.ortiz12@marketplace.com` | 12 |
| 8 | Diana Perez | `diana.perez13@marketplace.com` | 13 |
| 9 | Julian Munoz | `julian.munoz14@marketplace.com` | 14 |
| 10 | Alejandra Rojas | `alejandra.rojas15@marketplace.com` | 15 |

### Qué puede hacer

- Ver el catálogo público y el detalle de cada producto
- Añadir productos al carrito y pagar (checkout)
- Ver su historial de compras y el estado de cada envío
- Dejar reseñas de productos que haya comprado y cuyo despacho se haya registrado

---

## Cómo entrar

### En el panel web

1. Arranca el backend (`.\run.ps1`) y el frontend (`npm run dev`)
2. Abre <http://localhost:3000/login>
3. Escribe el correo y la contraseña de la tabla
4. El sistema te lleva al panel que corresponde a tu rol automáticamente

### En Swagger UI

1. Abre <http://localhost:8080/swagger-ui.html>
2. Despliega **Authentication** → `POST /api/v1/auth/login` → **Try it out**
3. Pega el correo y la contraseña, y ejecuta
4. Copia el `accessToken` de la respuesta
5. Arriba a la derecha, **Authorize** → pega el token → **Authorize**

### En Postman

Petición `POST {{baseUrl}}/api/v1/auth/login` con este cuerpo:

```json
{
  "email": "diego.herrera3@marketplace.com",
  "password": "Vendedor123!"
}
```

Guarda el `accessToken` en la variable del rol (`sellerToken`, `buyerToken` o `adminToken`).

### Con curl

```powershell
curl.exe -s -X POST http://localhost:8080/api/v1/auth/login `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"diego.herrera3@marketplace.com\",\"password\":\"Vendedor123!\"}'
```

---

## Verificación realizada

Se probó el login de las **16 cuentas** contra la API real:

```
[OK] ADMIN      admin.prueba@marketplace.com        HTTP 200  ROLE_ADMIN
[OK] VENDEDOR   diego.herrera3@marketplace.com      HTTP 200  ROLE_SELLER
[OK] VENDEDOR   jorge.ramirez4@marketplace.com      HTTP 200  ROLE_SELLER
[OK] VENDEDOR   santiago.flores9@marketplace.com    HTTP 200  ROLE_SELLER
[OK] VENDEDOR   daniel.herrera17@marketplace.com    HTTP 200  ROLE_SELLER
[OK] VENDEDOR   andres.medina21@marketplace.com     HTTP 200  ROLE_SELLER
[OK] COMPRADOR  tomas.rodriguez1@marketplace.com    HTTP 200  ROLE_BUYER
[OK] COMPRADOR  veronica.medina2@marketplace.com    HTTP 200  ROLE_BUYER
[OK] COMPRADOR  daniela.rodriguez6@marketplace.com  HTTP 200  ROLE_BUYER
[OK] COMPRADOR  isabella.jimenez8@marketplace.com   HTTP 200  ROLE_BUYER
[OK] COMPRADOR  miguel.torres10@marketplace.com     HTTP 200  ROLE_BUYER
[OK] COMPRADOR  natalia.guzman11@marketplace.com    HTTP 200  ROLE_BUYER
[OK] COMPRADOR  juliana.ortiz12@marketplace.com     HTTP 200  ROLE_BUYER
[OK] COMPRADOR  diana.perez13@marketplace.com       HTTP 200  ROLE_BUYER
[OK] COMPRADOR  julian.munoz14@marketplace.com      HTTP 200  ROLE_BUYER
[OK] COMPRADOR  alejandra.rojas15@marketplace.com   HTTP 200  ROLE_BUYER

RESULTADO: 16 OK / 0 fallos
```

### Estado de la base de datos tras el sembrado

| Tabla | Filas |
|---|---|
| `users` | 34 (2 admin, 15 vendedores, 17 compradores) |
| `products` | 10 |
| `stock_items` | 10 |
| `stock_movements` | 34 |
| `carts` | 7 |
| `cart_items` | 10 |
| `orders` | 7 |
| `sub_orders` | 8 |
| `order_items` | 10 |
| `reviews` | 0 |

> Los vendedores aparecen como 15 y no 5 porque incluye los creados en las pruebas end-to-end
> anteriores. Los **15 están verificados**.

---

## Cómo volver a crearlos

Los 15 vendedores y compradores se crean con:

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\seed-users.ps1
```

El script es **idempotente**: si un usuario ya existe lo informa y continúa, y vuelve a verificar a
los vendedores.

El administrador de prueba se crea aparte, porque **no existe endpoint público de registro de
administradores**:

```powershell
# Instrucciones completas de classpath en el propio archivo
java -cp "<postgresql.jar>;<spring-security-crypto.jar>;<spring-jcl.jar>" `
  tools/SeedAdminUser.java 136.112.91.42 5432 postgres "<password>" marketplace_db `
  admin.prueba@marketplace.com "Admin123!"
```

También es idempotente: si el administrador ya existe, le restablece la contraseña documentada.

---

## Notas y limitaciones

**Los usuarios del CSV no se pudieron importar directamente.** La hoja `usuarios` del dataset trae
la columna `contrasena_hash` con valores del tipo `hash_de_prueba_1`, que **no son hashes BCrypt
válidos**: con ellos el login fallaría siempre. Por eso se crearon las cuentas por la API con
contraseñas nuevas.

**Los nombres y apellidos no se guardan.** La tabla `users` de esta aplicación solo tiene
`email`, `password`, `role`, `enabled` y `seller_approved`. No hay columnas para nombre, apellido ni
teléfono, así que esos datos del CSV no se conservan: solo se usaron los correos.

**El teléfono, la dirección y los datos de tienda tampoco se guardan.** No existen tablas para
ellos. Ver el detalle completo en [ANALISIS_CSV_VS_BD.md](ANALISIS_CSV_VS_BD.md).

**Contraseñas simples a propósito.** Son cuentas de prueba sobre una base de datos de práctica. No
las uses como referencia para producción.
