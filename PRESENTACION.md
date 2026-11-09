# PRESENTACIÓN — Guion y todo lo necesario

> **Documento para el día de la entrega.** Está todo desplegado en Google Cloud
> (Cloud Run) con la base de datos en **Neon**.
> Última verificación: **2026-11-09**, commit `a1f32a3`.

---

## 1. Enlaces (abrir en este orden)

| Qué | URL |
|---|---|
| **Panel (lo que se presenta)** | <https://marketplace-web-805790031718.us-east1.run.app> |
| API — salud | <https://marketplace-api-805790031718.us-east1.run.app/actuator/health> |
| API — Swagger | <https://marketplace-api-805790031718.us-east1.run.app/swagger-ui/index.html> |
| API — catálogo público | <https://marketplace-api-805790031718.us-east1.run.app/api/v1/products> |
| Neon (base de datos) | <https://console.neon.tech> |

> Guarda el primer enlace. **Es todo lo que necesitas para presentar.**

---

## 2. Cuentas de prueba

> **Las tres están verificadas contra el entorno desplegado** (`2026-11-09`):
> login correcto, rol correcto y vendedor ya aprobado. Se pueden usar tal cual.

### 2.1 Las tres cuentas de la demo

| Rol | Correo | Contraseña | Qué hacer con ella en la demo |
|---|---|---|---|
| 🛒 **Comprador** | `tomas.rodriguez1@marketplace.com` | `Comprador123!` | Catálogo, carrito, checkout, favoritos, reseñas, notificaciones |
| 🏪 **Vendedor** | `diego.herrera3@marketplace.com` | `Vendedor123!` | Publicar producto, ajustar inventario, despachar pedidos |
| 🛡️ **Administrador** | `admin@marketplace.com` | `Admin123.` | Verificar vendedores, aprobar productos, ver órdenes, moderar reseñas |

**Copiar y pegar:**

```
Comprador      tomas.rodriguez1@marketplace.com      Comprador123!
Vendedor       diego.herrera3@marketplace.com        Vendedor123!
Administrador  admin@marketplace.com                 Admin123.
```

> ⚠️ La contraseña del administrador termina en **punto**, no en exclamación.
> Es el error más habitual al copiarla.

### 2.2 Cuentas de respaldo (si alguna falla o se bloquea)

**Vendedores** — todos con `Vendedor123!`, todos ya verificados:

| Correo |
|---|
| `jorge.ramirez4@marketplace.com` |
| `santiago.flores9@marketplace.com` |
| `daniel.herrera17@marketplace.com` |
| `andres.medina21@marketplace.com` |

**Compradores** — todos con `Comprador123!`:

| Correo |
|---|
| `veronica.medina2@marketplace.com` |
| `daniela.rodriguez6@marketplace.com` |
| `isabella.jimenez8@marketplace.com` |
| `miguel.torres10@marketplace.com` |
| `natalia.guzman11@marketplace.com` |
| `juliana.ortiz12@marketplace.com` |
| `diana.perez13@marketplace.com` |
| `julian.munoz14@marketplace.com` |
| `alejandra.rojas15@marketplace.com` |

### 2.3 Tres cosas que conviene saber

1. **El administrador no se registra por API.** Se crea solo en el primer arranque con
   `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`. No existe endpoint público para
   crear administradores, y eso es intencionado.
2. **Un vendedor sin verificar no puede publicar** (la API responde `400 Your seller account is
   not verified yet`). Por eso la demo empieza verificando al vendedor desde el panel de admin.
   Las cuentas de arriba ya están verificadas.
3. **Si aparece un `429`** al hacer login repetido, es el *rate limit* (30/min por IP).
   Espera un minuto. Se puede usar otra de las cuentas de respaldo.

---

## 3. Arquitectura (para el diagrama)

```
   Navegador
       │  HTTPS
       ▼
┌──────────────────────────────┐
│  marketplace-web (Cloud Run) │   Next.js 16 · us-east1
│  Panel + BFF                 │   El navegador NUNCA ve el JWT:
│  /api/session/**             │   va en una cookie httpOnly y el
│  /api/v1/** → proxy          │   servidor la añade como Bearer.
└──────────────┬───────────────┘
               │ HTTPS
               ▼
┌──────────────────────────────┐
│  marketplace-api (Cloud Run) │   Spring Boot 3.3.4 · Java 21 · us-east1
│  Hexagonal · JWT · RBAC      │   44 operaciones · Flyway V1–V3
│  Bucket4j · Caffeine         │
└──────────────┬───────────────┘
               │ TLS 5432
               ▼
┌──────────────────────────────┐
│  Neon PostgreSQL 18.6        │   17 tablas · gratis · sin apagar nada
└──────────────────────────────┘
```

**Puntos que se pueden mencionar:**
- **Tres capas separadas** desplegadas de verdad, no en local.
- **Sin CORS**: el navegador solo habla con su propio dominio; el BFF hace de puente.
- **JWT en cookie httpOnly** → un XSS no puede robar el token (antes estaba en `localStorage`).
- **Flyway** versiona el esquema; Hibernate solo valida (`ddl-auto: validate`).

---

## 4. Guion de la demo (12–15 minutos)

### 4.1 Portada y catálogo — 2 min

1. Abre el panel → aterriza en la portada.
2. Entra en **Catálogo**.
3. **Qué destacar:**
   - Buscador, filtros por categoría, orden y paginación funcionando.
   - Cada tarjeta muestra **chip de stock** (Disponible / Últimas uds. / Agotado) con los colores del diseño.
   - Los datos vienen de Neon (base real, en la nube).

### 4.2 Los 4 estados obligatorios — 2 min ⭐

Esto es lo que pide la rúbrica. Se demuestran **en el catálogo**:

| Estado | Cómo enseñarlo |
|---|---|
| **Cargando** | Recarga con el navegador en *Slow 3G* (DevTools → Network → Throttling) |
| **Vacío** | Busca algo inexistente, p. ej. `zzzzz` → aparece el estado vacío |
| **Error** | DevTools → Network → *Offline*, y recarga |
| **Reintento** | En el estado de error, pulsa **Reintentar** con la red restaurada → se recupera |

> Los 4 estados están en **las 17 vistas** del panel, no solo en el catálogo.

### 4.3 Detalle, favoritos y reseñas — 2 min

1. Abre un producto → nombre, precio, chip de stock, estrellas y reseñas.
2. Pulsa el **corazón** → se guarda en **Favoritos** (menú lateral).
3. Añade al carrito.

### 4.4 Compra completa — 3 min ⭐

1. **Carrito** → cambia la cantidad, comprueba el total.
2. **Checkout** → se crea la orden con pago simulado.
3. **Mis compras** → la orden aparece con estado **PAID** y su número (`ORD-…`).
4. **Notificaciones** (campana) → hay un aviso de la compra.

### 4.5 Panel del vendedor — 2 min

1. Cierra sesión → entra como **vendedor**.
2. **Mis productos** → publica uno nuevo (nombre, precio, stock, categoría).
   - La imagen se **pega como URL** (ver limitación en el punto 7).
3. **Inventario** → ajusta el stock (delta o absoluto) y mira el histórico de movimientos.
4. **Despachos** → la orden del comprador aparece en `PROCESSING` → **Despachar** con guía → `SHIPPED`.

### 4.6 Panel del administrador — 2 min

1. Entra como **administrador**.
2. **Vendedores** → el nuevo vendedor está pendiente → **Verificar**.
3. **Productos** → el producto está `PENDING_APPROVAL` → **Aprobar** → pasa a `ACTIVE` y ya sale en el catálogo público.
4. **Órdenes** → visión global de todas las órdenes.
5. **Reseñas** → moderación (borrar una).

### 4.7 Seguridad, en vivo — 2 min ⭐

| Qué demostrar | Cómo |
|---|---|
| **RBAC** | Con sesión de comprador, intenta abrir `/admin` → redirige o bloquea |
| **BOLA** | Un comprador pide la orden de otro → **404** (no filtra si existe) |
| **Rate limiting** | 30 logins seguidos → **429** con `Retry-After` (curl o Postman) |
| **Swagger** | Abre Swagger y enseña las **44 operaciones** documentadas |

Comando para el 429:

```bash
curl -s -o /dev/null -w "%{http_code}\n" -X POST \
  https://marketplace-api-805790031718.us-east1.run.app/api/v1/auth/login \
  -H 'Content-Type: application/json' -d '{"email":"no@existe.com","password":"x"}'
```
(Repítelo 35 veces: las últimas devuelven **429**.)

### 4.8 Cierre — 1 min

- **39 aserciones** e2e pasando contra la nube (`backend/tools/e2e-test.ps1`).
- **137 tests** unitarios.
- **44 operaciones** de API · **17 vistas** · **17 tablas**.

---

## 5. Plan B si falla internet o Cloud Run

Todo está preparado para correr en local:

```powershell
# Terminal 1 — API
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1

# Terminal 2 — panel
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

- Panel local: <http://localhost:3000>
- Swagger local: <http://localhost:8080/swagger-ui/index.html>
- La base **sigue siendo Neon**, así que los datos son los mismos.

> Si tampoco hay internet, el panel no podrá llegar ni a Neon ni a la API.
> En ese caso, presenta con las capturas y este documento.

---

## 6. Antes de presentar — checklist de 15 minutos

- [ ] Abrir el panel y comprobar que carga (si tarda ~10 s, es el arranque en frío)
- [ ] Comprobar `…/actuator/health` → `{"status":"UP"}`
- [ ] Probar el login de las 3 cuentas
- [ ] Tener el catálogo abierto en una pestaña
- [ ] Tener Swagger abierto en otra
- [ ] Saber dónde están los botones de **Reintentar** (para el estado de error)
- [ ] Cerrar sesión antes de empezar (para que el login se vea en vivo)

**Comando de rescate** si el panel no responde:

```bash
gcloud run services describe marketplace-web --region us-east1 \
  --format="value(status.url,status.conditions[0].status)"
```

---

## 7. Limitaciones conocidas (dilas antes de que las pregunten)

| Limitación | Estado | Workaround |
|---|---|---|
| **Subir imagen desde archivo** | ⚠️ El bucket `marketplace-images` ya existe, pero faltan sus 4 credenciales en el servicio | **Pegar la URL de la imagen** en el formulario; funciona igual |
| **«Cambiar rol»** del diseño original | ❌ No implementado | Cada usuario tiene un rol fijo; el panel muestra «Estás operando como…» |
| **El panel duerme** | ⚠️ Con `min-instances 1` no duerme, pero cuesta ~$127/mes si está 24/7 | Para la demo, dejarlo encendido; después, `--min-instances 0` |
| `marcas`, `direcciones`, `cupones` | ❌ Fuera del alcance | No se mencionan |

---

## 8. Coste y apagado

Con `min-instances 1` en los dos servicios, **24/7 son ≈ $127/mes**.
Para una demo de 8 horas entra en la capa gratuita de Cloud Run.

**Apagar cuando termines** (deja de cobrar al instante):

```bash
gcloud run services update marketplace-api --min-instances 0 --region us-east1
gcloud run services update marketplace-web --min-instances 0 --region us-east1
```

Con `min-instances 0` el primer acceso tarda ~10 s y **no se paga nada**.

---

## 9. Problemas típicos durante la demo

| Síntoma | Causa | Qué hacer |
|---|---|---|
| El panel tarda 10 s en abrir | Arranque en frío (si `min-instances 0`) | Esperar; el resto va normal |
| «No se pudo contactar con la API» | La API está arrancando | Esperar 20 s y pulsar **Reintentar** |
| `401` al navegar | La sesión caducó (24 h) | Volver a iniciar sesión |
| `429` al hacer login | Rate limit (30/min por IP) | Esperar 1 minuto |
| La imagen no se sube | Faltan las credenciales del bucket | Pegar la URL de la imagen |

---

## 10. Comandos útiles (por si acaso)

```bash
# Estado de los servicios
gcloud run services list --region us-east1

# Ver logs de la API
gcloud run services logs read marketplace-api --region us-east1 --limit 30

# Ver logs del panel
gcloud run services logs read marketplace-web --region us-east1 --limit 30

# Redesplegar la API (si hiciera falta)
cd backend
gcloud run deploy marketplace-api --source . --region us-east1 --allow-unauthenticated --min-instances 1

# Redesplegar el panel
cd frontend
gcloud run deploy marketplace-web --source . --region us-east1 --allow-unauthenticated --min-instances 1 \
  --set-env-vars MARKETPLACE_API_URL=https://marketplace-api-805790031718.us-east1.run.app
```

> `gcloud` está instalado en `%LOCALAPPDATA%\gcloud-sdk\google-cloud-sdk\bin\gcloud.cmd`.
> Si no está en el PATH:
> ```powershell
> $env:Path += ";$env:LOCALAPPDATA\gcloud-sdk\google-cloud-sdk\bin"
> ```

---

## 11. Frase para abrir la presentación

> «El marketplace está desplegado en Google Cloud, con la API en Cloud Run, el panel en
> Cloud Run y la base de datos gestionada en Neon. Son tres capas separadas y reales, no
> una demo en local. Hay 44 operaciones de API, 17 vistas, 137 tests unitarios y 39
> aserciones end-to-end pasando contra el entorno desplegado. Empiezo por el catálogo.»
