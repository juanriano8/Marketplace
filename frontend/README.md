# Frontend — Panel web del Marketplace

Aplicación **Next.js 16** (App Router) + **TypeScript** + **Tailwind CSS 4** que consume la API
Spring Boot del backend. Un solo proyecto, tres paneles según el rol autenticado.

---

## Índice

- [Arrancarlo](#arrancarlo)
- [Cómo se conecta con el backend](#cómo-se-conecta-con-el-backend)
- [Estructura](#estructura)
- [Qué se puede hacer en cada panel](#qué-se-puede-hacer-en-cada-panel)
- [Estado de verificación](#estado-de-verificación)
- [Solución de problemas](#solución-de-problemas)

---

## Arrancarlo

**Requisitos:** Node.js 20 o superior (tienes v24) y el **backend arrancado** en el puerto 8080.

```powershell
# Terminal 1 — backend
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1

# Terminal 2 — frontend
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm install          # la primera vez
npm run dev
```

Abre **<http://localhost:3000>**

### Comandos disponibles

| Comando | Para qué |
|---|---|
| `npm run dev` | Servidor de desarrollo con recarga automática (puerto 3000) |
| `npm run build` | Build de producción |
| `npm start` | Sirve el build de producción |
| `npm run typecheck` | Comprueba los tipos sin generar nada |

---

## Cómo se conecta con el backend

El navegador **solo habla con Next.js** (puerto 3000). Las peticiones a `/api/v1/**` se reenvían al
backend mediante un `rewrite` definido en `next.config.ts`:

```
Navegador  →  http://localhost:3000/api/v1/products
                    ↓  (rewrite de Next, del lado del servidor)
              http://localhost:8080/api/v1/products   (Spring Boot)
```

Esto tiene tres ventajas:

- **No hay CORS**: la petición al backend la hace el servidor de Next, no el navegador.
- No hay que configurar dominios permitidos en `SecurityConfig`.
- Si el backend está en otra máquina, solo hay que definir una variable de entorno:

```powershell
$env:MARKETPLACE_API_URL = "http://136.112.91.42:8080"
npm run dev
```

### Autenticación

El login devuelve un **JWT**. El frontend lo guarda en `localStorage` (clave
`marketplace.session`) y lo envía en la cabecera `Authorization: Bearer …` en cada petición.
El rol contenido en el token decide qué panel se muestra.

- `ROLE_BUYER` → catálogo, carrito, checkout, mis compras
- `ROLE_SELLER` → mi catálogo, inventario, despachos
- `ROLE_ADMIN` → moderación, vendedores, órdenes, auditoría de inventario

---

## Estructura

```
frontend/
├── next.config.ts              rewrite /api/v1/** -> backend
├── .npmrc                      caché de npm dentro del proyecto
├── src/
│   ├── app/
│   │   ├── layout.tsx          layout raíz con AuthProvider y Navbar
│   │   ├── page.tsx            portada con los tres roles
│   │   ├── globals.css         Tailwind 4 + componentes (.btn, .card, .badge, .table)
│   │   ├── login/              inicio de sesión
│   │   ├── registro/           alta de comprador o vendedor
│   │   ├── productos/          catálogo público (paginado)
│   │   │   └── [slug]/         detalle + reseñas
│   │   ├── carrito/            carrito del comprador
│   │   ├── checkout/           pago y resultado
│   │   ├── mis-compras/        historial con estado de cada envío
│   │   ├── vendedor/           catálogo propio (crear, editar)
│   │   │   ├── inventario/     ajustes de stock
│   │   │   └── despachos/      registro de guías
│   │   └── admin/              moderación de productos
│   │       ├── vendedores/     verificación de vendedores
│   │       ├── ordenes/        todas las transacciones
│   │       └── inventario/     auditoría del ledger
│   ├── components/
│   │   ├── Navbar.tsx          navegación según rol
│   │   ├── CartCount.tsx       contador del carrito
│   │   ├── RequireAuth.tsx     protección de rutas por rol
│   │   ├── ProductForm.tsx     formulario reutilizable de producto
│   │   ├── feedback.tsx        avisos, estados vacíos, carga
│   │   └── ui.tsx              badges, formato de moneda y fechas, estrellas
│   └── lib/
│       ├── api.ts              cliente HTTP + manejo de errores RFC 7807
│       ├── auth.tsx            contexto de sesión
│       └── types.ts            tipos que reflejan los DTO del backend
```

---

## Qué se puede hacer en cada panel

### Comprador

| Pantalla | Acciones |
|---|---|
| Catálogo | Ver productos aprobados, paginar, añadir al carrito |
| Detalle | Ver descripción, stock real, opiniones y **dejar reseña** si compró |
| Carrito | Cambiar cantidades, quitar líneas, vaciar, ver subtotal |
| Checkout | Confirmar pedido, pagar, ver el resultado y las sub-órdenes por vendedor |
| Mis compras | Historial con estado de pago y de cada despacho (guía y transportadora) |

### Vendedor

| Pantalla | Acciones |
|---|---|
| Mi catálogo | Ver todos sus productos con estado, **crear** y **editar** |
| Inventario | Ver disponible / reservado / vendible y **ajustar stock** (delta o recuento) |
| Despachos | Filtrar por estado y **registrar la guía** de cada sub-orden |

### Administrador

| Pantalla | Acciones |
|---|---|
| Moderación | Aprobar o rechazar productos pendientes |
| Vendedores | Ver pendientes y verificados, **verificar** o **rechazar** cuentas |
| Órdenes | Ver todas las transacciones con detalle de sub-órdenes y guías |
| Inventario | Auditoría completa del ledger de movimientos de stock |

---

## Estado de verificación

| Comprobación | Resultado |
|---|---|
| `npm install` | ✅ 41 paquetes + actualización a Next 16.3.6 |
| `npm audit` | ✅ 0 vulnerabilidades |
| `tsc --noEmit` (tipos) | ✅ **0 errores** |
| `next build` (compilación) | ✅ `Compiled successfully` — 12 rutas compiladas |
| Renderizado en navegador | ⚠️ **no ejecutado** (ver abajo) |
| Proxy `/api/v1/**` → backend | ⚠️ no ejecutado en vivo |
| API del backend | ✅ verificada aparte: 39 aserciones end-to-end contra Cloud SQL |

### Por qué no pude abrir la interfaz desde el entorno de desarrollo

El agente trabaja dentro de un *sandbox* que **bloquea la creación de procesos hijos con tuberías**.
Next.js necesita ese mecanismo para dos cosas:

1. `next dev` lanza el servidor con `fork` → **`Error: spawn EPERM`**
2. `next build` lanza su comprobador de tipos como proceso hijo → **`spawn EPERM`** al final, por eso
   el build no escribe `BUILD_ID` ni los manifests y `next start` no puede arrancar

Lo que sí quedó verificado: el código **compila por completo** (las 12 rutas se generaron en
`.next/server/app/`) y **los tipos están correctos** (`tsc` sin errores), que es la parte que
depende del código.

**Esto no te afecta:** en tu terminal no hay sandbox, así que `npm run dev` y `npm run build`
funcionarán con normalidad. Simplemente no pude comprobarlo yo desde aquí.

---

## Solución de problemas

| Síntoma | Causa y solución |
|---|---|
| La página carga pero todo sale vacío | El **backend no está arrancado**. Ejecuta `.\run.ps1` en `backend/` y recarga. |
| `401 Unauthorized` en todas las llamadas | La sesión caducó (24 h) o cambió `JWT_SECRET` en el backend. Cierra sesión y vuelve a entrar. |
| El catálogo está vacío | No hay productos **aprobados**. Entra como admin → Moderación → aprueba alguno. |
| No puedo crear productos como vendedor | La cuenta no está verificada: admin → Vendedores → Verificar. |
| No puedo reseñar | Necesitas una compra **despachada** de ese producto (el vendedor debe registrar la guía). |
| `Error: spawn EPERM` al arrancar | Solo ocurre en entornos con sandbox. En una terminal normal no pasa. |
| El puerto 3000 está ocupado | `npm run dev -- -p 3001` |
| Quiero apuntar a otro backend | `$env:MARKETPLACE_API_URL = "http://otra-ip:8080"` antes de `npm run dev` |
| Tras cambiar el backend no veo cambios | Reinicia el backend; Next solo proxya, no cachea la API. |
