# CONTEXTO — traspaso para continuar el proyecto

> **Documento de relevo.** Léelo entero antes de tocar nada.
> Última actualización: **2026-11-09**, tras la reorganización de carpetas (`docs/`) y la
> incorporación de los módulos de favoritos, notificaciones y perfil de vendedor.
> Estado: **backend y frontend funcionando**, base de datos en Neon, todo en `main`.

---

## 1. Qué es el proyecto

Marketplace multi-rol (comprador / vendedor / administrador) para una entrega académica.

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot 3.3.4 · Java 21 · Gradle 8.10.2 · arquitectura hexagonal (11 módulos) |
| Persistencia | PostgreSQL 18.6 en **Neon** (nube, gratis) · **Flyway** + `ddl-auto: validate` |
| Seguridad | JWT HS256 (JJWT 0.12.6) · RBAC por URL + `@PreAuthorize` · comprobaciones BOLA |
| Extras | Bucket4j (rate limiting) · RFC 7807 (`ProblemDetail`) · Swagger/OpenAPI |
| Frontend | Next.js 16.3.6 (App Router) · React 19 · TypeScript · Tailwind 4 |
| Repo | <https://github.com/juanriano8/Marketplace> (público) |

---

## 2. Estado actual (verificado el 2026-11-09)

| Comprobación | Resultado |
|---|---|
| Backend `localhost:8080` | ✅ `{"status":"UP"}` |
| Frontend `localhost:3000` | ✅ 200 |
| Proxy panel → API | ✅ 200 |
| **Operaciones de la API** | ✅ **44** (32 históricas + favoritos, notificaciones, ranking y perfil) |
| Batería e2e | ⚠️ **39 OK / 0 fallos**, pero **no cubre** los módulos nuevos |
| Tests unitarios | ✅ **137, 0 fallos** · 14 clases |
| `tsc --noEmit` | ✅ 0 errores |
| Base de datos | ✅ **17 tablas** (+ `flyway_schema_history`) · **22 usuarios · 5 productos · 4 órdenes** |
| Migraciones | ✅ V1, V2 y V3 aplicadas |
| Git | ✅ `main` limpio y sincronizado |

### Datos sembrados en la base

- **1 administrador** (bootstrap)
- **9 vendedores**, todos verificados
- **12 compradores**
- **5 productos** (la mayoría creados por los tests e2e)

> Los 72 usuarios / 31 productos del dump original de Cloud SQL **no se restauraron**:
> el backup está en el otro PC (`~/backups/backup_marketplace_2026-11-03.sql`).
> Si se necesita ese estado exacto, hay que traerlo y restaurarlo.

---

## 3. Cómo arrancar (3 comandos)

```powershell
# Terminal 1 — backend (espera ~20 s)
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1

# Terminal 2 — panel
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

- Panel: <http://localhost:3000>
- Swagger: <http://localhost:8080/swagger-ui/index.html>
- Salud: <http://localhost:8080/actuator/health>

### Verificación rápida

```powershell
.\backend\tools\verify.ps1                    # comprobaciones básicas
.\backend\tools\e2e-test.ps1                  # 39 aserciones (CREA datos de prueba)
```

---

## 4. Credenciales: dónde viven (no están en el repo)

**Regla dura: ningún secreto va al repositorio.** Todo esto está en `.gitignore`.

| Secreto | Dónde |
|---|---|
| Contraseña de Neon, `JWT_SECRET`, contraseña del admin | `backend/.env` |
| Copia de la configuración anterior (Cloud SQL) | `backend/.env.cloudsql.bak` |
| URL del API para el panel | `frontend/.env.local` |
| API key de Stitch | `C:\Users\sebas\Desktop\stitch-key` (**fuera del repo**) |
| Token de Figma (ya no se usa) | `~/.figma-token` |

> ⚠️ El archivo `stitch-key` **no contiene solo la clave**: contiene la línea completa
> `Set-Content -Path ... -Value "<LA_CLAVE>"`. Hay que extraer el valor con
> `[regex]::Match($raw, '-Value\s+"([^"]+)"').Groups[1].Value`.

### Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Admin | `admin@marketplace.com` | la de `BOOTSTRAP_ADMIN_PASSWORD` en `.env` |
| Vendedor | `diego.herrera3@marketplace.com` | `Vendedor123!` |
| Comprador | `tomas.rodriguez1@marketplace.com` | `Comprador123!` |

Lista completa en [USUARIOS_PRUEBA.md](docs/api/USUARIOS_PRUEBA.md).

### Pendiente de seguridad

- 🔴 **Revocar la API key de Stitch** (se compartió en un chat).
- 🔴 **Revocar el token de Figma** (ya no se necesita).
- 🟡 Configurar `JWT_SECRET` y la contraseña de la base como **variables de entorno en Render**.

---

## 5. Infraestructura

### Base de datos: Neon

| Dato | Valor |
|---|---|
| Host (directo) | `ep-steep-dawn-bc86fdpf.c-16.us-east-1.aws.neon.tech` |
| Base | `neondb` |
| Usuario | `neondb_owner` |
| SSL | `require` |
| Esquema | Lo gobierna **Flyway** (`backend/src/main/resources/db/migration`) |

**Usa siempre la conexión directa, no la `-pooler`**: Flyway hace DDL y el pooler lo complica.

**Neon no necesita encender/apagar nada** y **no requiere autorizar IPs** (a diferencia de
Cloud SQL). Eso elimina el `0.0.0.0/0` que había antes.

### Render + Vercel (pendiente)

El despliegue objetivo es **Vercel → Render → Neon**, con las imágenes en **Neon Object Storage**.
Neon ya está listo; faltan las cuentas de Render/Vercel y conectar el repositorio. Guía:
`docs/operacion/DESPLIEGUE_RENDER_VERCEL.md`.

### Stitch (herramienta de diseño)

| Dato | Valor |
|---|---|
| Proyecto | **`Marketplace`** · `projects/16417096205034790396` |
| Tipo | `TEXT_TO_UI_PRO` · **MOBILE** · privado |
| Pantallas reales | **1**: «Catálogo Marketplace» (780×1768) |
| Pantalla ID | `3152c2d499da475387384c86c0d031cd` |
| Design system | `designTheme.designMd` — **10.845 caracteres** |
| Extraído a | `design/stitch-design-md.md`, `design/stitch-catalogo-real.png`, `.html` |

El proyecto trae **2 instancias más** de `DESIGN_SYSTEM_INSTANCE` (no son pantallas).

#### Cómo leer Stitch por CLI (la ruta que funciona)

```powershell
cd design
$raw = [System.IO.File]::ReadAllText('C:\Users\sebas\Desktop\stitch-key')
$env:STITCH_API_KEY = ([regex]::Match($raw, '-Value\s+"([^"]+)"')).Groups[1].Value.Trim()
$env:npm_config_cache = (Join-Path (Get-Location) '.npm-cache')

# Listar pantallas (tool NATIVO, funciona)
npx --yes '@_davideast/stitch-mcp' tool list_screens -d '{\"projectId\":\"16417096205034790396\"}'

# Detalle de una pantalla: devuelve las URLs de descarga
npx --yes '@_davideast/stitch-mcp' tool get_screen -d '{\"name\":\"projects/16417096205034790396/screens/3152c2d499da475387384c86c0d031cd\"}'
```

**Trampas comprobadas:**

1. PowerShell **elimina las comillas** del JSON de `-d` → hay que escaparlas (`\"`).
2. Los tools **virtuales** (`get_screen_image`, `get_screen_code`) **abortan**. Usa `get_screen`
   y descarga las URLs con `design/stitch-download.mjs <url> <destino>`.
3. La descarga devuelve **JSON con el contenido escapado** en `dataString`:
   - HTML: `dataString` es texto HTML directo.
   - Imagen: `dataString` es **base64** → decodificar a PNG.
4. La autenticación es la cabecera **`X-Goog-Api-Key`**.

---

## 6. Qué está hecho (con evidencia)

| Bloque | Evidencia |
|---|---|
| **44 operaciones REST** | e2e 39/39 (solo cubre los 32 originales) + 6 a mano; los módulos nuevos se probaron con sus 16 tests unitarios |
| **Máquina de estados de la orden** | `PROCESSING → SHIPPED → DELIVERED → COMPLETED` verificada |
| **Antisobreventa** | lock pesimista + ledger `INITIAL → RESERVATION → SALE` · 400 al exceder |
| **BOLA** | acceso a orden ajena → 404 |
| **Rate limiting (Bucket4j)** | 3 niveles; 40 logins → 13 × `429` con `Retry-After` |
| **Caché de catálogo (Caffeine)** | `productCatalog` 30 s y `productDetail` 60 s; invalidada al escribir producto o stock; 2 tests |
| **Sesión del panel** | JWT en cookie **httpOnly** + BFF (`/api/session/*`) y proxy `/api/v1/**`; saneamiento de entradas y `imageUrl` validada |
| **Notificaciones** | Tabla `notifications` (Flyway `V2`); avisos de vendedor/producto/pedido/reseña; campana y página `/notificaciones` |
| **Perfil de vendedor** | `seller_profiles` (`V3`) con tienda, descripción, logo y rating agregado |
| **Rating write-behind** | Deltas en memoria + volcado cada 5 s; E2E 0 → 5.00/1 → 0 |
| **Ranking** | `GET /api/v1/products/ranking` con rating y decaimiento exponencial de 30 días |
| **Favoritos** | Tabla `favoritos`; ids/listado/guardar/quitar y corazón en catálogo, portada, detalle y `/favoritos` |
| **Robustez de la API** | `sort` inválido → 400 · método no permitido → 405 · `size` tope 100 |
| **Flyway** | `V1` aplicada en Neon; arranque real verificado |
| **Diseño** | Paleta «Origen» + design system de Stitch en `globals.css` |
| **4 estados de UI** | 17/17 páginas con carga / vacío / error / **reintento** |
| **Documentación** | 3 documentos en la raíz y el resto agrupado en `docs/`, incluidos `DESIGN.md`, `UX_MAPPING.md`, `DECISION_UI.md`, `ACTIVACION.md` |

### Números clave

- 18 controladores · 44 operaciones · 14 clases de test · **137 tests**
- 17 páginas · 8 componentes · ~4.100 líneas de TSX
- Documentación: 3 documentos en la raíz y el resto en `docs/`

---

## 7. Qué falta (priorizado)

### 🟠 Alto

| # | Tarea | Esfuerzo | Nota |
|---|---|---|---|
| 1 | **Rediseñar el layout de las 17 vistas** | Días | La paleta y los componentes ya están; falta la **composición**. Solo hay 1 pantalla de referencia (catálogo) y es **móvil**; el panel es **escritorio** |
| 2 | **Desplegar el backend en Render** | 1-2 h | Blueprint `render.yaml` + variables de Neon |
| 3 | **Desplegar el panel en Vercel** | 1 h | `MARKETPLACE_API_URL` + Neon Object Storage |
| 4 | ~~**Seguridad del cliente (#7)**~~ ✅ **Hecho**: JWT en cookie httpOnly + saneamiento | — | BFF en `/api/session/*` y proxy `/api/v1/**` |

### 🟡 Medio

| # | Tarea | Esfuerzo |
|---|---|---|
| 5 | ~~Caché de catálogo (Redis o Caffeine)~~ ✅ **Hecho con Caffeine** (30/60 s, invalidación en escrituras) | — |
| 6 | **Resilience4j**: integrarlo de verdad o quitarlo de `build.gradle` (hoy está declarado y sin usar) | 2 h |
| 7 | ~~Reorganizar la base~~ ✅ **Hecho**: `V3` crea las 6 tablas; perfil y favoritos operativos. Quedan como features los endpoints de marcas, direcciones, cupones e imágenes | — |
| 8 | ~~*Write-behind* del rating de vendedor~~ ✅ **Hecho**: deltas + volcado cada 5 s | — |
| 9 | ~~Notificaciones~~ ✅ **Hecho**: tabla `notifications` + campana y página | — |
| 10 | ~~Ranking con decaimiento temporal~~ ✅ **Hecho**: `GET /api/v1/products/ranking` | — |
| 11 | ~~Adoptar Shadcn UI o justificar `ui.tsx`~~ ✅ **Justificado** en `docs/diseno/DECISION_UI.md` | — |
| 12 | Consolidar los documentos (hay solape; ya están agrupados en `docs/`) | 3 h |

### 🟢 Higiene

| # | Tarea |
|---|---|
| 13 | Tag `entrega-v1` + rama de respaldo (red de seguridad antes del rediseño) |
| 14 | Revocar API key de Stitch y token de Figma |
| 15 | Desplegar en Render + Vercel (recupera la URL pública sin GCP) |

---

## 8. Decisiones ya tomadas (no re-litigar)

1. **Neon, no Cloud SQL.** El crédito de GCP se acabó. Neon es gratis y sin límite de tiempo.
2. **Los commits los hace el usuario**, no el agente (excepto cuando lo autorice explícitamente).
3. **Figma ya no se usa.** Los tokens están extraídos y aplicados; no hace falta volver a la API.
4. **Stitch es la fuente del design system**; el layout de cada vista se decide con sus pantallas.
5. **Los límites del rate limiter son generosos a propósito**: la suite e2e lanza decenas de
   peticiones desde una IP y no debe chocar.
6. **En las pruebas de rebanada web el limitador se desactiva** (`@TestPropertySource`): esas
   clases verifican autorización, no tasa.
7. **No se editan migraciones aplicadas**: Flyway guarda el checksum. Se crea una `V2__...`.
8. **La conexión a Neon es la directa** (sin `-pooler`).

---

## 9. Trampas conocidas (esto ahorra horas)

| Síntoma | Causa real | Solución |
|---|---|---|
| La app usa `ddl-auto: update` aunque el YAML diga `validate` | **Las variables de entorno ganan al YAML**; `.env` tenía `JPA_DDL_AUTO=update` | Comentar esa línea del `.env` |
| `role "cloudsqlsuperuser" does not exist` | El dump de Cloud SQL arrastraba ese `GRANT` | Ya eliminado de `V1__initial_schema.sql`; **no volver a añadirlo** |
| `Flyway checksum mismatch` | Se editó una migración ya aplicada | Crear `V2__...` en vez de tocar `V1` |
| 46 tests fallan con `ApplicationContext failure` tras añadir un filtro | **`@WebMvcTest` carga los `Filter`** y el filtro necesita sus dependencias | Añadirlas al `@Import` del test |
| `curl.exe` falla con HTTPS (`SEC_E_NO_CREDENTIALS`) | El sandbox no tiene credenciales schannel | Usar **Node `fetch`** para HTTPS; `curl` solo para `localhost` |
| `spawn EPERM` al arrancar Next | El entorno no permite `fork` con pipes | Usar `node scripts/dev-server.mjs` |
| Página congelada sin hidratar desde otra IP | Next 16 bloquea `/_next/*` de orígenes no locales | `allowedDevOrigins` en `next.config.ts` |
| `Port 8080 already in use` | Hay un backend anterior vivo | Cerrar el proceso o cambiar `PORT` |

---

## 10. Mapa de archivos

### Raíz
**Solo tres documentos:** `README.md` (entrada) · `CONTEXTO.md` (este) · `PENDIENTES.md`
(estado y tareas).

### Documentación (`docs/`)
Índice completo en `docs/README.md`.

- `docs/operacion/` — `ARRANQUE_RAPIDO.md` (**el operativo, al día**), `ACTIVACION.md`
  (estado de activación y verificación), `TROUBLESHOOTING.md`, `GUIA_ENTREGA.md`,
  `EJECUCION_Y_DEMO.md`, `GUIA_OTRO_PC.md`, `GUIA_OTRO_PC_LINUX.md`.
- `docs/api/` — `POSTMAN_DEMO_PASO_A_PASO.md`, `GUIA_POSTMAN_MANUAL.md`, `USUARIOS_PRUEBA.md`.
- `docs/arquitectura/` — `ESPECIFICACION_TECNICA.md`.
- `docs/diseno/` — `DESIGN.md`, `UX_MAPPING.md`, `DECISION_UI.md`, `PLAN_REDISENO.md`.
- `docs/datos/` — `ANALISIS_CSV_VS_BD.md` y `marketplace_datos.csv`.
- `docs/referencia/` — PDF y `.txt` de la especificación original, `task.md`.

> Los documentos con aviso *«Nota de vigencia»* describen la infraestructura **anterior**
> (Cloud SQL). Son históricos; lo operativo está en `docs/operacion/ARRANQUE_RAPIDO.md`.

### Backend (`backend/`)
- `src/main/java/com/marketplace/api/modules/{auth,user,admin,product,inventory,order,review,payment,notification,seller,favorite}`
- `src/main/java/com/marketplace/api/shared/{security,exception,ratelimit}`
- `src/main/resources/db/migration/V1__initial_schema.sql` ← **no editar**
- `src/main/resources/db/migration/V2__notifications.sql` ← tabla de notificaciones
- `src/main/resources/db/migration/V3__catalog_and_profiles.sql` ← perfiles, marcas, imágenes, favoritos, direcciones y cupones
- `run.ps1` / `run.sh` · `setup.ps1` · `docker-compose.yml` (Postgres local, alternativa)
- `tools/`: `verify.ps1|sh`, `e2e-test.ps1`, `stock-test.ps1`, `split-test.ps1`,
  `seed-users.ps1`, `DbReport.java`, `SeedAdminUser.java`, `AdminPasswordSync.java`, `ResetPostmanUsers.java`
- `postman/Marketplace-API.postman_collection.json` (44 peticiones)

### Frontend (`frontend/`)
- `src/app/**/page.tsx` — 17 vistas (incluye `/notificaciones` y `/favoritos`)
- `src/app/api/session/{login,register,session,logout}` — BFF de sesión con cookies httpOnly
- `src/app/api/v1/[...path]/route.ts` — proxy que añade `Authorization` desde la cookie
- `src/components/` — `ui.tsx` (Badge, **StockChip**, Stars, formatMoney), `feedback.tsx`
  (Loading / EmptyState / ErrorState con reintento), `Navbar.tsx`, `RequireAuth.tsx`,
  `ProductForm.tsx`, `CartCount.tsx`, `NotificationBell.tsx`, `FavoriteButton.tsx`
- `src/lib/` — `api.ts`, `auth.tsx`, `server-session.ts`, `sanitize.ts`, `favorites.tsx`
- `src/app/globals.css` — tokens + componentes del design system
- `next.config.ts` · `.env.local` · `scripts/dev-server.mjs`

### Diseño (`design/`)
- `stitch-design-md.md` ← **el design system completo**
- `stitch-catalogo-real.png` / `.html` ← la pantalla de referencia
- `stitch-download.mjs` ← descarga pantallas de Stitch
- `tokens-origen.css` ← tokens de Figma (histórico)
- `MCP_SETUP.md` ← cómo configurar MCP en otro PC
- `notebooklm/owasp-frontend.md`

---

## 11. Primeros pasos sugeridos en la sesión nueva

1. **Comprobar el estado**: `git log --oneline -3` y abrir <http://localhost:3000>.
   Si los servidores no están corriendo: los 3 comandos del punto 3.
2. **Leer** `docs/operacion/ARRANQUE_RAPIDO.md` (operativo) y este `CONTEXTO.md`.
3. **Revisar** `PENDIENTES.md` → sección 7 de este documento.
4. **Decidir la siguiente tarea**:
   - Si hay **pantallas nuevas en Stitch** → implementarlas (leerlas con los comandos del punto 5).
   - Si no → la más rentable es **#2 Render** o **#3 Vercel** para dejar de depender de local.
5. **Verificar siempre** antes de commitear:
   ```powershell
   cd backend;  .\gradlew.bat test --no-daemon     # 137 tests
   cd ..\frontend; node node_modules\typescript\bin\tsc --noEmit
   ```

---

## 12. Frase para retomar

> «El proyecto está en `C:\Users\sebas\Desktop\marketplace\fullstack`, rama `main` al día (consulta `git log -1`).
> Backend y panel funcionando contra Neon. Lee `CONTEXTO.md` y
> `docs/operacion/ARRANQUE_RAPIDO.md` antes de tocar nada. Lo que falta está en la
> sección 7 de `CONTEXTO.md`.»
