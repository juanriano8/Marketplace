# Activación y estado operativo

> **Foto de la sesión del 2026-11-09.** Resume qué queda activado, cómo se enciende desde cero
> y qué falta por activar porque depende de credenciales o cuentas externas.
>
> Para el día a día basta [ARRANQUE_RAPIDO.md](ARRANQUE_RAPIDO.md); este documento explica
> **qué hay detrás de cada pieza** y cómo comprobarla.

---

## 1. Resumen en una línea

**Todo lo que se puede activar sin credenciales de Google Cloud queda activado y verificado en
local:** backend, panel, Neon, Flyway, JWT/RBAC/BOLA, rate limiting, caché de catálogo, Swagger,
subida de imágenes, notificaciones, perfil de vendedor, favoritos y 137 tests. **Render/Vercel
siguen pendientes de desplegar**; Neon ya está listo como base de datos.

Verificación real de esta sesión (2026-11-09):

| Comprobación | Resultado |
|---|---|
| `GET /actuator/health` | `{"status":"UP"}` |
| `GET /api/v1/products?size=3` | 5 productos en el catálogo |
| Panel `http://localhost:3000` | HTTP 200 |
| Proxy panel → API | `totalElements=5` |
| Login de comprador (BFF) | Cookie httpOnly, carrito autenticado 200; tras logout, 401 |
| `./gradlew test` | **137 tests, 0 fallos** |
| `npm run typecheck` | 0 errores |

---

## 2. Matriz de activación

| Pieza | Estado | Cómo se activa | Evidencia |
|---|---|---|---|
| **PostgreSQL (Neon)** | ✅ Activa | Ya está en la nube; no hay que encender nada | `backend/.env` apunta a `ep-...neon.tech` |
| **Flyway** | ✅ Activo | Arranca con el backend y aplica `db/migration` | Log `Successfully applied` / `Current version` |
| **Backend Spring Boot** | ✅ Activo | `cd backend; .\run.ps1` | <http://localhost:8080/actuator/health> → `UP` |
| **Panel Next.js** | ✅ Activo | `cd frontend; npm run dev` | <http://localhost:3000> |
| **Proxy panel → API** | ✅ Activo | Route handler `app/api/v1/[...path]` | El catálogo carga datos sin CORS |
| **Sesión del panel (cookie httpOnly)** | ✅ Activa | BFF `/api/session/*` + proxy `/api/v1/**` | El JWT no es accesible desde JavaScript |
| **JWT + RBAC + BOLA** | ✅ Activo | `SecurityConfig` + `@PreAuthorize` + servicios | 401/403/404 en las pruebas de seguridad |
| **Saneamiento de entradas** | ✅ Activo | `lib/sanitize.ts` + validación `imageUrl` en el backend | El formulario rechaza URLs no http(s)/relativas |
| **Contraseñas BCrypt** | ✅ Activo | `PasswordEncoder` de Spring Security | Login real de los tres roles |
| **Errores RFC 7807** | ✅ Activo | `GlobalExceptionHandler` | Respuestas `application/problem+json` |
| **Rate limiting (Bucket4j)** | ✅ Activo | `RateLimitFilter` | 429 + `Retry-After` al superar el límite |
| **Caché de catálogo (Caffeine)** | ✅ Activa | `CacheConfig` + `@Cacheable` | 2.ª lectura sin consultar la BD |
| **Swagger / OpenAPI** | ✅ Activo | Springdoc | <http://localhost:8080/swagger-ui/index.html> |
| **Subida de imágenes** | ✅ Activa | `frontend/src/app/api/upload/route.ts` | Local: disco; cloud: Neon Object Storage |
| **Perfil de vendedor** | ✅ Activo | Flyway `V3` + `/api/v1/seller/profile` | Tienda, descripción, logo y rating |
| **Rating write-behind** | ✅ Activo | `SellerRatingWriter` + job cada 5 s | E2E 0 → 5.00/1 → 0 |
| **Ranking de productos** | ✅ Activo | `GET /api/v1/products/ranking` | La portada usa el ranking |
| **Favoritos** | ✅ Activo | Flyway `V3` + `/api/v1/favorites/**` | Corazón y página `/favoritos` |
| **Tests del backend** | ✅ **137, 0 fallos** | `.\gradlew.bat test` | `BUILD SUCCESSFUL` |
| **Typecheck del frontend** | ✅ 0 errores | `npm run typecheck` | `tsc --noEmit` |
| **Render (API)** | ⏸️ Pendiente | Crear cuenta y Blueprint | [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) |
| **Vercel (panel)** | ⏸️ Pendiente | Importar repo y configurar env | [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) |
| **Neon Object Storage** | ⏸️ Pendiente | Bucket `public_read` + credenciales | [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) |
| **Redis / caché distribuida** | ❌ No activo | Requiere Memorystore o similar | Se usa Caffeine en memoria |
| **Notificaciones** | ✅ Activas | Flyway `V2` + `/api/v1/notifications/**` | Campana en la navbar y página `/notificaciones` |
| **Reorganización de la BD** | ❌ No activa | Migración `V2+` | Pendiente |
| **Rediseño de las 17 vistas** | ⚠️ Parcial | Tokens y componentes listos | Falta composición visual |

---

## 3. Requisitos

| Requisito | Versión / comprobación |
|---|---|
| JDK | **21** (`java -version`) |
| Node.js | **20 o superior** (`node -v`) |
| `backend/.env` | credenciales de Neon + `JWT_SECRET` |
| `frontend/.env.local` | `MARKETPLACE_API_URL=http://localhost:8080` |
| Puertos libres | 8080 (API) y 3000 (panel) |

> `backend/.env` y `frontend/.env.local` están en `.gitignore`: **no viajan en el repositorio**.
> En otro PC hay que recrearlos.

---

## 4. Variables de entorno

### 4.1 Backend (`backend/.env`)

| Variable | Obligatoria | Valor actual / defecto | Para qué |
|---|---|---|---|
| `DB_HOST` | ✅ | host directo de Neon (sin `-pooler`) | Conexión a PostgreSQL |
| `DB_PORT` | ✅ | `5432` | Puerto |
| `DB_NAME` | ✅ | `neondb` | Base de datos |
| `DB_USER` | ✅ | `neondb_owner` | Usuario |
| `DB_PASSWORD` | ✅ | la de Neon | Contraseña |
| `DB_SSLMODE` | ✅ | `require` | Neon exige TLS |
| `JWT_SECRET` | ✅ | secreto propio | Firma HS256; sin él la app no arranca |
| `BOOTSTRAP_ADMIN_EMAIL` | recomendada | `admin@marketplace.com` | Crea el admin en el primer arranque |
| `BOOTSTRAP_ADMIN_PASSWORD` | recomendada | la del admin | Solo se usa al crear la cuenta |
| `JPA_DDL_AUTO` | opcional | `validate` | Hibernate valida; **no** debe ser `update` |
| `RATE_LIMIT_ENABLED` | opcional | `true` | Enciende/apaga Bucket4j |
| `RATE_LIMIT_ANON` | opcional | `60` | Peticiones/minuto anónimas por IP |
| `RATE_LIMIT_USER` | opcional | `600` | Peticiones/minuto autenticadas |
| `RATE_LIMIT_AUTH` | opcional | `30` | Logins/registros por minuto |
| `CACHE_CATALOG_TTL_SECONDS` | opcional | `30` | TTL del listado de catálogo |
| `CACHE_DETAIL_TTL_SECONDS` | opcional | `60` | TTL del detalle por slug |
| `CACHE_MAX_ENTRIES` | opcional | `1000` | Entradas máximas por caché |
| `PAYMENT_STUB_CAPTURE` | opcional | `true` | `false` deja pagos `PENDING` para probar el fallo |
| `SPRING_PROFILES_ACTIVE` | opcional | `cloud` | Perfil de conexión |

### 4.2 Frontend (`frontend/.env.local`)

| Variable | Obligatoria | Valor actual | Para qué |
|---|---|---|---|
| `MARKETPLACE_API_URL` | ✅ | `http://localhost:8080` | Destino del proxy `/api/v1/**` |
| `AWS_ENDPOINT_URL_S3` | Solo cloud | — | Endpoint de Neon Object Storage |
| `AWS_REGION` | Solo cloud | `us-east-1` | Región del branch |
| `AWS_ACCESS_KEY_ID` | Solo cloud | — | Credencial S3 de Neon |
| `AWS_SECRET_ACCESS_KEY` | Solo cloud | — | Secreto S3 de Neon |
| `NEON_STORAGE_BUCKET` | Solo cloud | `marketplace-images` | Bucket público de imágenes |

> Sin las variables de storage, la subida usa el disco local (solo desarrollo).
> En Vercel/Render deben estar configuradas o `/api/upload` responde `503`.

---

## 5. Arranque desde cero

### Terminal 1 — backend

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1
```

> Si PowerShell bloquea el script por la política de ejecución:
> `powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1`.

Qué ocurre, en orden:

1. `run.ps1` carga `backend/.env` en el proceso.
2. **Flyway** aplica las migraciones pendientes.
3. **Hibernate** valida que el esquema coincida (`ddl-auto: validate`).
4. `BootstrapAdminRunner` crea el administrador si no existe.
5. Tomcat escucha en **8080**.

Espera el log:

```
Started MarketplaceApplication in X seconds
```

### Terminal 2 — panel

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

> Si `npm run dev` falla con `spawn EPERM` (entornos sin permiso de `fork`), usa
> `npm run dev:nofork`, que arranca Next con su API programática.

Abre <http://localhost:3000>.

### En Linux

```bash
cd backend && ./run.sh
cd frontend && npm run dev
```

---

## 6. Verificación

### 6.1 Salud y catálogo

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
# -> status = UP

Invoke-RestMethod 'http://localhost:8080/api/v1/products?size=3'
# -> content, totalElements, pageable...
```

### 6.2 Seguridad activa

```powershell
# Sin token debe devolver 401
try {
  Invoke-WebRequest http://localhost:8080/api/v1/cart -UseBasicParsing | Out-Null
} catch {
  "HTTP $([int]$_.Exception.Response.StatusCode)"   # -> HTTP 401
}

# Login directo contra la API (para Postman/Swagger)
$body = @{ email = 'tomas.rodriguez1@marketplace.com'; password = 'Comprador123!' } |
  ConvertTo-Json
Invoke-RestMethod http://localhost:8080/api/v1/auth/login `
  -Method Post -ContentType 'application/json' -Body $body
# -> accessToken, tokenType, expiresIn, userId, role
```

### 6.3 Sesión del panel (BFF con cookies httpOnly)

El panel **no** guarda el JWT en `localStorage`. El flujo es:

```
Navegador → /api/session/login (Next) → backend /api/v1/auth/login
          ← cookie httpOnly mp_token + mp_session
Navegador → /api/v1/** (Next) → proxy añade Authorization → backend
```

Comprobación con PowerShell:

```powershell
$body = @{ email = 'tomas.rodriguez1@marketplace.com'; password = 'Comprador123!' } |
  ConvertTo-Json

# Login a través del BFF: guarda las cookies en $web
$login = Invoke-RestMethod http://localhost:3000/api/session/login `
  -Method Post -ContentType 'application/json' -Body $body -SessionVariable web
$login.email                      # -> correo del comprador
$login.role                       # -> ROLE_BUYER

# La cookie httpOnly no se puede leer desde JS; el BFF devuelve solo los datos públicos
Invoke-RestMethod http://localhost:3000/api/session -WebSession $web

# El proxy añade el Authorization y el carrito responde 200
Invoke-WebRequest http://localhost:3000/api/v1/cart -WebSession $web -UseBasicParsing

# Tras logout, la cookie se borra y el carrito vuelve a 401
Invoke-RestMethod http://localhost:3000/api/session/logout -Method Post -WebSession $web
try {
  Invoke-WebRequest http://localhost:3000/api/v1/cart -WebSession $web -UseBasicParsing | Out-Null
} catch {
  "HTTP $([int]$_.Exception.Response.StatusCode)"   # -> HTTP 401
}
```

> `next.config.ts` **no** declara rewrite para `/api/v1`: si lo hiciera, la petición
> saltaría el route handler y llegaría al backend sin `Authorization` (fue un fallo real
> detectado en esta sesión).

### 6.4 Tests

```powershell
cd backend
.\gradlew.bat test --no-daemon
# -> BUILD SUCCESSFUL, 137 tests
```

```powershell
cd frontend
npm run typecheck
# -> 0 errores
```

### 6.5 Batería end-to-end (opcional, crea datos)

```powershell
.\backend\tools\verify.ps1
.\backend\tools\e2e-test.ps1 -BaseUrl http://localhost:8080
# -> 39 aserciones
```

> `e2e-test.ps1` **crea** usuarios, productos y órdenes de prueba. Es para una base de demo,
> no para producción.

### 6.6 Caché de catálogo

La caché es transparente: la primera petición consulta PostgreSQL y las siguientes (dentro del
TTL) salen de Caffeine. Para comprobarlo sin instrumentación:

1. Pide dos veces `GET /api/v1/products?size=20` y observa que la segunda responde antes.
2. Cambia un producto o su stock desde el panel: la siguiente lectura ya trae el dato nuevo,
   porque las operaciones de escritura ejecutan `@CacheEvict`.
3. Si quieres verla desactivada, reinicia el backend: la caché es en memoria y arranca vacía.

### 6.7 Notificaciones

La tabla `notifications` se crea con Flyway `V2`. El usuario autenticado puede:

| Método | Ruta | Para qué |
|---|---|---|
| `GET` | `/api/v1/notifications?page=0&size=20` | Listar sus notificaciones |
| `GET` | `/api/v1/notifications/unread-count` | Contador para la campana |
| `PATCH` | `/api/v1/notifications/{id}/read` | Marcar una como leída |
| `PATCH` | `/api/v1/notifications/read-all` | Marcar todas |

Se generan al verificar un vendedor, aprobar/rechazar un producto, despachar/entregar un pedido
y recibir una reseña. En el panel aparecen en la campana de la navbar y en `/notificaciones`.

```powershell
# Con la sesión del BFF (ver 6.3)
Invoke-RestMethod http://localhost:3000/api/v1/notifications/unread-count -WebSession $web
Invoke-RestMethod 'http://localhost:3000/api/v1/notifications?page=0&size=10' -WebSession $web
```

> La creación es asíncrona (`@TransactionalEventListener` + `@Async`): si la notificación falla,
> la operación de negocio ya quedó confirmada. El evento se escribe después del commit.

### 6.8 Perfil de vendedor, rating y ranking

| Método | Ruta | Para qué |
|---|---|---|
| `GET` | `/api/v1/sellers/{sellerId}/profile` | Perfil público (tienda, logo, rating) |
| `GET` | `/api/v1/seller/profile` | Perfil del vendedor autenticado |
| `PUT` | `/api/v1/seller/profile` | Editar tienda, descripción y logo |
| `GET` | `/api/v1/products/ranking?limit=10` | Ranking con rating + decaimiento de 30 días |

El rating se actualiza con **write-behind**: al crear una reseña se encola `+rating/+1`; al
borrarla, `-rating/-1`. Un job cada 5 s vuelca los deltas a `seller_profiles`. Se puede ajustar con
`SELLER_RATING_FLUSH_MS`.

```powershell
# Perfil público de un vendedor del catálogo
$sellerId = (Invoke-RestMethod 'http://localhost:8080/api/v1/products?size=1').content[0].sellerId
Invoke-RestMethod "http://localhost:8080/api/v1/sellers/$sellerId/profile"

# Ranking
Invoke-RestMethod 'http://localhost:8080/api/v1/products/ranking?limit=5'
```

> Si el proceso cae antes del volcado, se pierde el delta pendiente. Es el compromiso de un
> write-behind en memoria; para durabilidad total haría falta una cola persistente.

### 6.9 Favoritos

| Método | Ruta | Para qué |
|---|---|---|
| `GET` | `/api/v1/favorites` | Productos guardados (con stock actual) |
| `GET` | `/api/v1/favorites/ids` | IDs para pintar el corazón |
| `POST` | `/api/v1/favorites/{productId}` | Guardar |
| `DELETE` | `/api/v1/favorites/{productId}` | Quitar |

Solo accesible con rol `BUYER`. El corazón aparece en el catálogo, la portada y el detalle; el
listado completo está en `/favoritos`.

```powershell
# Con la sesión de comprador del BFF
Invoke-RestMethod http://localhost:3000/api/v1/favorites/ids -WebSession $web
```

---

## 7. Render + Vercel: qué falta para la URL pública

El despliegue objetivo es:

```
Vercel (Next.js) → Render (Spring Boot) → Neon (PostgreSQL + Object Storage)
```

Pasos resumidos:

1. Crear el bucket `marketplace-images` (`public_read`) en Neon Object Storage y una credencial.
2. Render → Blueprint → conectar el repo; `render.yaml` crea `marketplace-api` con Neon.
3. Rellenar `DB_HOST`, `DB_PASSWORD`, `JWT_SECRET` y `BOOTSTRAP_ADMIN_PASSWORD`.
4. Verificar `https://marketplace-api.onrender.com/actuator/health`.
5. Vercel → importar repo, root `frontend`, `MARKETPLACE_API_URL` + variables de Neon Storage.
6. Abrir la URL de Vercel y probar login/catálogo/carrito.

La guía completa está en [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md).

---

## 8. Lo que no se puede activar sin credenciales externas

| Tarea | Qué falta | Dónde queda documentada |
|---|---|---|
| **Render / Vercel** | Crear cuentas gratuitas y conectar el repositorio | [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) |
| **Neon Object Storage** | Bucket y credenciales del branch | [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) |
| **Redis / Memorystore** | Instancia y VPC Connector | [PENDIENTES.md](../../PENDIENTES.md) |
| **Revocar API key de Stitch / token de Figma** | Acceso a las cuentas | [CONTEXTO.md](../../CONTEXTO.md) |
| **Backup restaurado en Neon** | El `.sql` está en el otro PC | [CONTEXTO.md](../../CONTEXTO.md) |

Estas no bloquean la demo local: el proyecto funciona sin ellas y así queda declarado en la
matriz de la sección 2.

---

## 9. Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro backend vivo | Cierra el proceso o usa `.\run.ps1 -Port 8081` |
| `JWT_SECRET` no definido | Falta en `.env` | Añádelo (`openssl rand -hex 32`) |
| `password authentication failed` | Contraseña de Neon incorrecta | Cópiala de nuevo desde la consola de Neon |
| `Flyway ... checksum mismatch` | Se editó una migración aplicada | Crea una `V2__...`, no toques `V1` |
| El panel muestra error de red | Backend caído o `.env.local` mal | Arranca el backend y revisa `MARKETPLACE_API_URL` |
| La caché sirve un dato viejo | TTL aún no venció | Espera 30–60 s o reinicia; las escrituras ya invalidan |
| `429 Too Many Requests` | Rate limit | Espera al `Retry-After` o sube `RATE_LIMIT_*` |

---

## 10. Checklist antes de presentar

- [ ] `backend/.env` existe y apunta a Neon.
- [ ] `frontend/.env.local` existe y apunta a `http://localhost:8080`.
- [ ] Backend arriba: `/actuator/health` → `UP`.
- [ ] Panel arriba: <http://localhost:3000>.
- [ ] Login de admin, vendedor y comprador.
- [ ] Catálogo con productos activos.
- [ ] `.\gradlew.bat test` → **137 tests, 0 fallos**.
- [ ] `npm run typecheck` → 0 errores.
- [ ] `.env` y `.env.local` **no** aparecen en `git status`.
- [ ] Swagger abre en <http://localhost:8080/swagger-ui/index.html>.

---

## 11. Referencias

- [ARRANQUE_RAPIDO.md](ARRANQUE_RAPIDO.md) — los 3 comandos del día a día.
- [TROUBLESHOOTING.md](TROUBLESHOOTING.md) — incidencias reales con causa y solución.
- [CONTEXTO.md](../../CONTEXTO.md) — estado del proyecto, credenciales y decisiones.
- [PENDIENTES.md](../../PENDIENTES.md) — lo que falta y su prioridad.
- [ESPECIFICACION_TECNICA.md](../arquitectura/ESPECIFICACION_TECNICA.md) — arquitectura y API.
