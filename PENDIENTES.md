# Pendientes y estado del proyecto

**Fecha:** 26 de septiembre de 2026

> **Para levantar el proyecto** usa [GUIA_ENTREGA.md](GUIA_ENTREGA.md).
> **Para demostrar los endpoints** en Postman, [POSTMAN_DEMO_PASO_A_PASO.md](POSTMAN_DEMO_PASO_A_PASO.md).
> **Para ejecutarlo en otro PC**, [GUIA_OTRO_PC.md](GUIA_OTRO_PC.md).
> Este documento es el estado y la lista de lo que falta.

---

## 1. Estado actual

| | |
|---|---|
| **Backend** | ✅ Funcionando contra Google Cloud SQL |
| **Frontend** | ✅ Implementado en `frontend/` (Next.js 16), 15 páginas |
| **Base de datos** | ✅ `marketplace_db` (PostgreSQL 18.6), **10 tablas, 130 filas** |
| **Acceso a la red** | ✅ `0.0.0.0/0` autorizado: conecta desde cualquier PC y red |
| **Compilación backend** | ✅ `BUILD SUCCESSFUL` |
| **Tests backend** | ✅ 114 tests, 0 fallos |
| **Endpoints** | ✅ 31 operaciones, **39 aserciones end-to-end, 0 fallos** |
| **Tipos frontend** | ✅ `tsc --noEmit` sin errores |
| **Usuarios de prueba** | ✅ **16 cuentas** creadas y verificadas con login real |
| **Carpeta de imágenes** | ✅ `frontend/public/images/productos/` |

### El problema de la IP quedó resuelto

Cloud SQL solo aceptaba IPs autorizadas y la del equipo cambió
(`186.28.26.68` → `186.31.165.104`), lo que rompía el arranque con
`SocketTimeoutException: Connect timed out`.

**Solución aplicada:** se autorizó `0.0.0.0/0` en
*Cloud SQL → marketplace → Connections → Networking → Authorized networks*.

Ahora conecta desde cualquier PC y cualquier red sin tocar nada más.

> ⚠️ **Pendiente de seguridad:** quitar esa red cuando termines de presentar (ver §4).

### Datos en la base

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

---

## 2. Usuarios de prueba creados (26-sep-2026)

**Listado completo con contraseñas en [USUARIOS_PRUEBA.md](USUARIOS_PRUEBA.md).**

| Rol | Cantidad | Contraseña | Estado |
|---|---|---|---|
| Administrador | 1 | `Admin123!` | `admin.prueba@marketplace.com` |
| Vendedor | 5 | `Vendedor123!` | **Todos verificados** |
| Comprador | 10 | `Comprador123!` | Habilitados |

Los correos y nombres provienen del dataset de referencia (`marketplace_datos.csv`).
Se probó el login de las **16 cuentas: 16 OK / 0 fallos**.

Para recrearlos:

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\seed-users.ps1
```

---

## 3. Análisis del dataset `marketplace_datos.csv`

**Informe completo en [ANALISIS_CSV_VS_BD.md](ANALISIS_CSV_VS_BD.md).**

Resumen: el archivo **no es un CSV, es un Excel renombrado**. Tiene **16 hojas** con 778 filas, y su
encaje con las tablas de esta aplicación es parcial:

| Estado | Hojas | Filas | Nota |
|---|---|---|---|
| ✅ Cargable con conversión | `productos`, `categorias`, `envios`, `pagos`, `resenas` | 228 | Requiere transformar valores y remapear ids |
| ⚠️ Cargable con pérdida | `usuarios`, `pedidos`, `pedido_items`, `carritos`, `carrito_items` | 316 | Faltan columnas obligatorias |
| ❌ Sin tabla destino | `direcciones`, `vendedores`, `marcas`, `producto_imagenes`, `favoritos`, `cupones` | 234 | Este proyecto no tiene esas tablas |

**Dos hallazgos importantes:**

1. La columna `contrasena_hash` del Excel tiene valores como `hash_de_prueba_1`, que **no son
   BCrypt válidos**: esos usuarios no pueden iniciar sesión. Por eso se crearon las cuentas por la
   API con contraseñas nuevas.
2. Los **precios no declaran moneda** (son pesos colombianos) y la aplicación exige
   `currency_code`, así que habría que fijar `COP`.

**Lo más rentable** sería cargar la hoja `productos` (45 filas) **por la API**, para que quede
coherente el inventario. Está pendiente.

---

## 4. Pendientes

### 🔴 Importantes

| # | Pendiente | Detalle |
|---|---|---|
| 1 | **Subir los cambios a GitHub** | Hay **~15 archivos** sin subir: los 3 documentos nuevos, `USUARIOS_PRUEBA.md`, `ANALISIS_CSV_VS_BD.md`, `tools/seed-users.ps1`, `tools/SeedAdminUser.java`, `tools/DbReport.java`, `frontend/public/images/`, `README.md`, `PENDIENTES.md` |
| 2 | **Abrir el frontend en el navegador** | Está verificado que las 15 páginas se sirven con HTTP 200, pero **nunca se vio renderizado**. Falta confirmar visualmente que el CSS y el JavaScript funcionan |
| 3 | **Probar el proxy frontend → backend con ambos vivos** | La última prueba del proxy se hizo con el backend caído. Con la base ya accesible, hay que confirmar que `http://localhost:3000/api/v1/products` devuelve datos |
| 4 | **Quitar `0.0.0.0/0` de Cloud SQL** | Al terminar la presentación. Deja la base abierta a internet y la única defensa es la contraseña |

### 🟡 Cargar los datos de ejemplo

| # | Pendiente | Detalle |
|---|---|---|
| 5 | **Cargar productos del dataset** | 45 productos de la hoja `productos`, por la API. Requiere remapear `id_vendedor` a los 5 vendedores creados y resolver la categoría con la hoja `categorias` |
| 6 | **Productos "de presentación"** | Al menos uno con **1 unidad** (para demostrar el control de stock) y otro con **stock 0** |
| 7 | **Reseñas de ejemplo** | Las 37 del dataset no se pueden crear por la API (exigen compra despachada). Habría que insertarlas por SQL y quedarían como **no verificadas** |
| 8 | **Subir imágenes reales** | La carpeta está creada pero vacía. Ver su README para las convenciones (`/images/productos/nombre.jpg`) |

### 🟡 Funcionalidad que la especificación V2 pide y no está

| # | Pendiente | Impacto |
|---|---|---|
| 9 | **Endpoint para marcar `DELIVERED`** | Sin él una orden **nunca llega a `COMPLETED`**. El estado existe pero es inalcanzable |
| 10 | **Rate limiting (Bucket4j)** | Dependencia declarada, sin integrar. La spec lo pide por rol |
| 11 | **Resilience4j** | Dependencia declarada, sin integrar. Aplicable a la pasarela de pago |
| 12 | **Memorystore Redis** | No integrado. La spec lo pide para caché de catálogo |
| 13 | **Cloud Storage para imágenes** | Hoy el producto solo guarda una URL; no hay endpoint de subida |
| 14 | **MFA para ADMIN y SELLER** | La spec lo recomienda |
| 15 | **Webhook de confirmación de pago** | El `redirectUrl` está previsto en el DTO pero no hay callback |

### 🟢 Limpieza y deuda menor

| # | Pendiente | Detalle |
|---|---|---|
| 16 | **Consolidar documentación** | Hay 9 documentos con solapamiento: `EJECUCION_Y_DEMO.md` vs `GUIA_ENTREGA.md`, y `GUIA_POSTMAN_MANUAL.md` vs `POSTMAN_DEMO_PASO_A_PASO.md` |
| 17 | **Actualizar `task.md`** | Se escribió cuando el proyecto era solo backend: no menciona el frontend ni los usuarios de prueba |
| 18 | **`ddl-auto: update` → `validate`** | Con migraciones versionadas (Flyway o Liquibase) antes de producción |
| 19 | **`JWT_SECRET` a Secret Manager** | Hoy tiene un valor por defecto en `application.yml` |
| 20 | **Desactivar `BootstrapAdminRunner`** | En producción crea un admin con contraseña conocida si las variables están puestas |

---

## 5. Datos para no perderlos

| Dato | Valor |
|---|---|
| Repositorio | `https://github.com/juanriano8/Marketplace` |
| Instancia Cloud SQL | `marketplace-509503:us-central1:marketplace` |
| IP pública de la BD | `136.112.91.42` (puerto 5432) |
| Base de datos | `marketplace_db` (PostgreSQL 18.6) |
| Usuario de BD | `postgres` |
| Contraseñas | En `backend/.env` (ignorado por Git) |
| Admins | `admin@marketplace.com` y `admin.prueba@marketplace.com` |
| Panel web | <http://localhost:3000> |
| Swagger | <http://localhost:8080/swagger-ui.html> |

> ⚠️ **No apuntes la aplicación a la base `postgres`** de la instancia: contiene 23 tablas de otro
> proyecto con claves primarias `bigint`, incompatible con este modelo.

---

## 6. Herramientas incluidas

| Archivo | Para qué |
|---|---|
| `backend/run.ps1` | Arrancar la API (lee `.env`, localiza el JDK 21) |
| `backend/run.sh` | Lo mismo en Linux / macOS / WSL |
| `backend/setup.ps1` | Configuración inicial en un PC nuevo |
| `backend/tools/verify.ps1` | Comprobación completa antes de presentar |
| `backend/tools/seed-users.ps1` | Crea los 5 vendedores y 10 compradores (idempotente) |
| `backend/tools/SeedAdminUser.java` | Crea el administrador de prueba (no hay endpoint para eso) |
| `backend/tools/DbReport.java` | Informe de tablas, columnas y filas de la base |
| `backend/tools/AdminPasswordSync.java` | Sincronizar la contraseña del admin con la BD |
| `backend/tools/e2e-test.ps1` | Recorrido de los 27 endpoints (39 aserciones) |
| `backend/tools/stock-test.ps1` | Prueba de antisobreventa |
| `backend/tools/split-test.ps1` | Prueba de orden multi-vendedor y BOLA |
| `backend/postman/Marketplace-API.postman_collection.json` | 44 peticiones listas para Postman |
| `frontend/public/images/productos/` | Carpeta para las imágenes de los productos |

> Ejecuta los `.ps1` con `powershell -ExecutionPolicy Bypass -File .\ruta\script.ps1` si tu
> política de ejecución los bloquea.

---

## 7. Comandos del día a día

```powershell
# Arrancar (terminal 1)
cd backend
.\run.ps1

# Panel web (terminal 2)
cd frontend
npm run dev

# Verificar que todo funciona
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1

# Ver el estado de la base
java -cp "<postgresql.jar>" tools/DbReport.java 136.112.91.42 5432 postgres "<password>" marketplace_db

# Volver a crear los usuarios de prueba
powershell -ExecutionPolicy Bypass -File .\tools\seed-users.ps1
```
