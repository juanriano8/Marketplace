# Pendientes del proyecto

**Noviembre de 2026** · Documento único de estado y tareas.

> Este documento sustituye al anterior. Combina:
> - el estado real del proyecto (Marketplace: Spring Boot + PostgreSQL + Next.js)
> - las **directrices conceptuales de la clase** (rúbrica de revisión)
>
> **Sobre las directrices:** son conceptuales, no literales. Otros equipos usaron otras tecnologías.
> Lo que se revisa es el **cumplimiento de los principios**, no la tecnología concreta. Por eso cada
> requisito se evalúa contra su equivalente en nuestro stack.
>
> **NotebookLM y Stitch MCP ya están configurados y funcionando** (ver Anexo B).

---

## 1. Estado actual: lo que ya está hecho y verificado

| Componente | Estado | Evidencia |
|---|---|---|
| **Backend** Spring Boot 3.3.4 / Java 21 | ✅ Completo | Arquitectura hexagonal, 11 módulos |
| **Endpoints** | ✅ **39 rutas / 44 operaciones** | + notificaciones, ranking, perfil de vendedor y favoritos |
| **Tests unitarios** | ✅ **137, 0 fallos** | `BUILD SUCCESSFUL` (incluye 7 del limitador, 2 de caché, 5 de notificaciones, 5 de vendedor y 4 de favoritos) |
| **Base de datos** | ✅ **Neon** PostgreSQL 18.6 · 17 tablas | **22 usuarios · 5 productos · 4 órdenes** (sembrados; el dump de Cloud SQL no se restauró) |
| **Migraciones** | ✅ **Flyway V1 aplicada y verificada** | Arranque real contra Neon con `ddl-auto: validate` |
| **Despliegue** Render + Vercel | ⏸️ Pendiente de crear cuentas/conectar repo | Guía en [DESPLIEGUE_RENDER_VERCEL.md](docs/operacion/DESPLIEGUE_RENDER_VERCEL.md) |
| **Frontend** Next.js 16 / Tailwind 4 / TS | ✅ 17 páginas con **4 estados + reintento** | `tsc --noEmit` sin errores |
| **Reglas de negocio** | ✅ Verificadas | Antisobreventa (400 + ledger), multi-vendedor (350.00 USD), BOLA (400) |
| **Notificaciones** | ✅ Implementadas | Tabla `notifications` (`V2`), 4 endpoints, campana y página `/notificaciones` |
| **Perfil de vendedor (#15/#16)** | ✅ Implementado | `seller_profiles` (`V3`), rating write-behind y endpoints público/propio |
| **Favoritos (#15)** | ✅ Implementado | Tabla `favoritos`, 4 endpoints, corazón y página `/favoritos` |
| **Ranking (#18)** | ✅ Implementado | `GET /api/v1/products/ranking` con rating y decaimiento de 30 días |
| **Diseño** | ✅ Paleta «Origen» **aplicada** + design system de Stitch | `globals.css` con tokens y componentes |
| **Stitch** | ✅ **Conectado** (CLI) y design system extraído | 1 pantalla móvil + `designMd` de 10,8 KB en `design/stitch-design-md.md` |
| **NotebookLM** | ✅ Cuaderno OWASP configurado | MCP conectado (Anexo B) |
| **Documentación** | ✅ Documentos organizados en `docs/` (3 en la raíz) | + `ACTIVACION.md`, `TROUBLESHOOTING.md`, `DESIGN.md`, `DECISION_UI.md` |
| **Backup de la base** | ⚠️ Solo existe en el **otro PC** | `backup_marketplace_2026-11-03.sql` (96 KB), no restaurado en Neon |

**Este es el activo.** Lo que sigue es lo que falta para la rúbrica.

---

## 2. Cumplimiento de las directrices

Cada fila traduce el requisito conceptual de la clase a nuestro stack.

### 2.1 Stack y arquitectura

| Requisito | Qué pide | Nuestro equivalente | Estado |
|---|---|---|---|
| Stack frontend | Next.js + React + Tailwind + TypeScript | Next 16 + Tailwind 4 + TS 5 | ✅ |
| **Librería de componentes** | Shadcn UI | `ui.tsx` propio, sin librería | ⚠️ **Parcial** |
| Backend | Microservicios | **Monolito modular** hexagonal (11 módulos con fronteras y puertos) | ✅ Conceptual |
| Persistencia políglota | PostgreSQL + Firestore + Redis | Solo **PostgreSQL** | ⚠️ **Parcial** |
| IA (FastAPI) | Servicio de IA aparte | No aplica al dominio | N/A |
| WebSockets (GKE) | Tiempo real | No aplica al dominio | N/A |
| `ltree` + GiST | Hilos anidados | No aplica (no hay foros) | N/A |

### 2.2 Frontend: los 4 estados obligatorios

**Requisito explícito: toda vista debe tener Loading (skeletons), Success, Empty State y Error con
reintento.** Verificado página por página:

| Página | Carga | Vacío | Error | Reintento |
|---|---|---|---|---|
| `/` portada | ✅ | ✅ | ✅ | ✅ |
| `/productos` | ✅ | ✅ | ✅ | ✅ |
| `/productos/[slug]` | ✅ | — | ✅ | ✅ |
| `/login` | ✅ | — | ✅ | ✅ |
| `/registro` | ✅ | — | ✅ | ✅ |
| `/carrito` | ✅ | ✅ | ✅ | ✅ |
| `/checkout` | ✅ | ✅ | ✅ | ✅ |
| `/mis-compras` | ✅ | ✅ | ✅ | ✅ |
| `/vendedor` | ✅ | ✅ | ✅ | ✅ |
| `/vendedor/inventario` | ✅ | ✅ | ✅ | ✅ |
| `/vendedor/despachos` | ✅ | ✅ | ✅ | ✅ |
| `/admin` | ✅ | ✅ | ✅ | ✅ |
| `/admin/vendedores` | ✅ | ✅ | ✅ | ✅ |
| `/admin/ordenes` | ✅ | ✅ | ✅ | ✅ |
| `/admin/inventario` | ✅ | ✅ | ✅ | ✅ |
| `/notificaciones` | ✅ | ✅ | ✅ | ✅ |
| `/favoritos` | ✅ | ✅ | ✅ | ✅ |
| **Total** | **17/17** | **14/17 + 3 N/A** | **17/17** | **17/17** ✅ |

**Actualización 2026-11-09:** se añadieron `/notificaciones` y `/favoritos` con los 4 estados; el total pasa a **17 vistas**.

**Actualización 2026-11-03:** el reintento se añadió a las **15 vistas** con el componente
`<ErrorState message onRetry={load}>`; `/` incorpora la sección «Productos destacados» con los 4
estados y un `Skeleton` reutilizable. En formularios, «Reintentar» reenvía el formulario
(`requestSubmit`). `—` = estado vacío no aplica (formularios y detalle).

### 2.3 Archivos de contexto exigidos

| Archivo | Estado | Qué contiene |
|---|---|---|
| **`DESIGN.md`** | ✅ **Creado** | Tokens de Figma, tipografía, componentes, 4 estados, accesibilidad y seguridad de UI |
| **`UX_MAPPING.md`** | ✅ **Creado** | 44 operaciones / 39 rutas con payloads, roles y pantalla asociada |
| **`TROUBLESHOOTING.md`** | ✅ **Creado** | 16 incidencias reales + 4 recientes, con el formato exigido |

### 2.4 Seguridad

| Requisito | Estado | Detalle |
|---|---|---|
| **Cero credenciales en el código** | ✅ **Cumple** | `JWT_SECRET` obligatorio (sin valor por defecto) · contraseña del admin vacía en la colección de Postman |
| **Secretos de despliegue** | ⚠️ Pendiente | Variables de entorno de Render/Vercel; no se versionan |
| **Seguridad cliente** | ✅ **Cumple** | CSP activa · JWT en cookie **httpOnly** (inaccesible a JS) · saneamiento de entradas · `imageUrl` validada · CSRF con `SameSite=Lax` + comprobación de `Origin` |
| Autorización por rol | ✅ Cumple | RBAC por URL + `@PreAuthorize` + BOLA en servicios |
| Contraseñas | ✅ Cumple | BCrypt |
| Errores sin filtrar datos | ✅ Cumple | RFC 7807 `ProblemDetail` |

### 2.5 Backend: normas verificables

| Requisito | Estado |
|---|---|
| Métodos HTTP y códigos correctos (RFC 9110) | ✅ `200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`, `422` |
| Segregación `/api/v1/` (cliente) vs `/api/v1/admin/` | ✅ Cumplida |
| Validación de entrada con Bean Validation | ✅ Cumplida |
| *Write-behind* para reputación por lotes | ✅ Implementado: deltas en memoria + volcado cada 5 s a `seller_profiles` |
| Ranking con decaimiento temporal | ✅ Implementado: `GET /api/v1/products/ranking` |
| Notificaciones en Firestore | ✅ Equivalente implementado: tabla `notifications` + endpoints + campana |
| Redis para presencia / rate limit / caché | ⚠️ Rate limit con Bucket4j y caché con Caffeine, ambos en memoria; Redis sigue pendiente |

### 2.6 Herramientas de la rúbrica

| Herramienta | Estado | Nota |
|---|---|---|
| **NotebookLM MCP** | ✅ **Configurado** | `notebooklm-mcp` autenticado con cookies de Firefox; cuaderno OWASP en Anexo B |
| **Stitch MCP** | ✅ **Configurado** | `serverUrl` + API key; proyecto «Marketplace» visible (Anexo B) |
| Skills `react-components` / `stitch-loop` | ⏸️ | Aún no usadas (Stitch tiene `upload_design_md` / `build_site`) |
| `TROUBLESHOOTING.md` | ✅ **Creado** | Ver [TROUBLESHOOTING.md](docs/operacion/TROUBLESHOOTING.md) |

---

## 3. Pendientes por prioridad

### 🔴 Bloqueantes (incumplen una regla explícita)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 1 | Credenciales fuera del código (`JWT_SECRET` obligatorio, Postman sin contraseña) | ✅ Hecho | — |
| 2 | `TROUBLESHOOTING.md` (16 incidencias + 4 recientes) | ✅ Hecho | — |
| 3 | `DESIGN.md` desde los tokens de Figma | ✅ Hecho | — |
| 4 | `UX_MAPPING.md` (44 operaciones / 39 rutas) | ✅ Hecho | — |
| 5 | Reintento en los 4 estados de las 15 vistas (`<ErrorState>`) | ✅ Hecho | — |
| 6 | Estados completos en `/`, `/registro` y `/login` | ✅ Hecho | — |
| 7 | Seguridad del cliente: sanitización + sacar el token de `localStorage` | ✅ Hecho | — |

### 🟠 Alto (lo pide la rúbrica y se nota)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 8 | Desplegar el backend en Render con Neon | ⏸️ Pendiente: crear cuenta y conectar repo | Medio |
| 9 | Bucket4j: rate limiting real por rol | ✅ Hecho | — |
| 10 | Librería de componentes: adoptar Shadcn UI o justificar `ui.tsx` | ✅ Hecho · [DECISION_UI.md](docs/diseno/DECISION_UI.md) | — |
| 11 | Desplegar el panel en Vercel apuntando a Render | ⏸️ Pendiente: crear cuenta y configurar env | Medio |
| 12 | Endpoint `DELIVERED` (`PATCH /seller/orders/{id}/deliver`) | ✅ Hecho | — |
| 13 | Caché de catálogo (Redis o Caffeine) | ✅ Hecho · **Caffeine** 30/60 s, invalidada en escrituras | — |

### 🟡 Medio (mejoran la nota, no bloquean)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 14 | Migraciones versionadas con Flyway | ✅ Hecho | Medio |
| 15 | Reorganizar la base (`marcas`, `direcciones`, `cupones`, `favoritos`, `producto_imagenes`, perfil de vendedor) | ✅ Esquema `V3` con las 6 tablas; perfil y favoritos operativos. Endpoints de marcas/direcciones/cupones/imágenes quedan como features posteriores | — |
| 16 | *Write-behind* para el rating agregado de vendedor | ✅ Hecho: deltas en memoria + volcado cada 5 s | — |
| 17 | Notificaciones (tabla propia o Firestore) | ✅ Hecho · tabla `notifications` + campana | — |
| 18 | Ranking de productos con decaimiento temporal | ✅ Hecho: `GET /api/v1/products/ranking` | — |
| 19 | Rediseño del frontend con Stitch / Figma «Origen» | ⚠️ Base aplicada: shell, portada, catálogo, auth, Inter y skeletons. Faltan role-switcher, búsqueda global, filtros avanzados, hoja móvil, quick-view/batch selection e imágenes reales | Medio |

### 🟢 Higiene del repositorio

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 20 | `.gitignore` en la raíz | ✅ Hecho | — |
| 21 | Backup de la base antes de que expire el trial | ✅ Hecho | — |
| 22 | Tag `entrega-v1` y rama `rediseno-frontend` (los commits los hace el usuario) | ⏸️ Bloqueado: requiere commit del usuario | Bajo |
| 23 | Consolidar los documentos (hay solape entre guías; ya están agrupados en `docs/`) | ❌ Pendiente | Medio |
| 24 | Revocar el token de Figma **y la API key de Stitch** expuestos en el chat | ❌ Pendiente | Bajo |

---

## 4. Registro de la sesión (2026-11-03)

| Qué | Resultado |
|---|---|
| Cuenta correcta de GCP | `jsebas8tian@gmail.com` (no `sebas00hola@…`) |
| Cloud SQL | Arrancada y verificada (`RUNNABLE`) |
| Backup | `~/backups/backup_marketplace_2026-11-03.sql` · 96 KB · 10 tablas · SHA-256 |
| Stitch | API habilitada, MCP conectado, proyecto «Marketplace» |
| NotebookLM | Login con cookies de Firefox; cuaderno OWASP accedido |
| Documentos | `DESIGN.md`, `TROUBLESHOOTING.md`, `UX_MAPPING.md`, `.gitignore` |
| Backend | `DELIVERED` · Flyway V1 · `JWT_SECRET` obligatorio |
| Frontend | 4 estados + reintento en las 15 vistas · `tsc` OK |
| Commits | Los hace el usuario (no se hacen más desde el agente) |

### Sesión 2026-11-08 — migración a Neon y design system

| Qué | Resultado |
|---|---|
| **Base de datos** | Migrada a **Neon** (PostgreSQL 18.6, gratis). Flyway creó las 10 tablas |
| **Incidencia resuelta** | `V1` fallaba con `role "cloudsqlsuperuser" does not exist` (rol exclusivo de Cloud SQL). Se eliminó la sentencia |
| **Incidencia resuelta** | `JPA_DDL_AUTO=update` en `.env` anulaba el `validate`; ahora manda Flyway |
| **Datos** | Sembrados de cero: 1 admin + 5 vendedores + 10 compradores + productos y órdenes de prueba |
| **Rate limiting** | **Bucket4j implementado** (3 niveles: anon 60, usuario 600, login 30 por minuto) · 7 tests · verificado con 429 real |
| **Robustez API** | `sort` inválido → **400** (antes 500) · método no permitido → **405** (antes 500) · tope de página **100** |
| **Diseño** | Paleta «Origen» aplicada y **design system de Stitch** volcado a `globals.css` (botones, chips de stock, inputs, tarjetas, badges) |
| **Stitch** | Conectado por CLI; proyecto «Marketplace» leído, pantalla y `designMd` descargados a `design/` |
| **Documentación** | `.env.example` reescrito para Neon · `ARRANQUE_RAPIDO.md` reescrito · aviso de vigencia en los 7 documentos que describen Cloud SQL |
| **Verificación** | **32/32 operaciones** de la API probadas · 39 aserciones e2e · 121 tests unitarios |
| **Limpieza** | Cachés borradas: **1 GB** (`frontend/.next`, `frontend/.npm-cache`, `design/.npm-cache`, `backend/build`) |

### Sesión 2026-11-09 — caché de catálogo, decisión UI y activación

| Qué | Resultado |
|---|---|
| **Caché de catálogo** | **Caffeine** en memoria: `productCatalog` (30 s) y `productDetail` (60 s), máximo 1000 entradas por caché |
| **Invalidación** | `@CacheEvict` en crear/editar/aprobar producto y en ajustar/reservar/liberar/confirmar stock |
| **Configuración** | `CACHE_CATALOG_TTL_SECONDS`, `CACHE_DETAIL_TTL_SECONDS`, `CACHE_MAX_ENTRIES` en `application.yml` |
| **Pruebas** | 2 de caché, 5 de notificaciones, 5 de vendedor y 4 de favoritos · **137 tests, 0 fallos** |
| **Decisión UI** | `docs/diseno/DECISION_UI.md`: se mantiene `ui.tsx` y se justifica frente a Shadcn UI |
| **Documentación** | `docs/operacion/ACTIVACION.md` con el estado de activación, variables y verificación |
| **Organización** | `docs/` con 6 carpetas; en la raíz solo `README.md`, `CONTEXTO.md` y `PENDIENTES.md` |
| **Arranque** | Corregido `run.ps1`: `JAVA_HOME` con barra final rompía Gradle; backend verificado en `localhost:8080` contra Neon |
| **Seguridad del cliente (#7)** | JWT movido a cookie **httpOnly**; login/registro/sesión/logout pasan por el BFF de Next y `/api/v1/**` por un proxy que añade `Authorization` |
| **Saneamiento** | `sanitizeText`, `sanitizeMultiline` y `sanitizeImageUrl` en el formulario de producto y reseñas; `imageUrl` validada también en el backend |
| **Notificaciones (#17)** | Flyway `V2` crea `notifications`; endpoints de listado/contador/marcar leída; campana en la navbar y página `/notificaciones` |
| **#22 Tag/rama** | ⏸️ No se creó: el árbol tiene cambios sin commitear; se hará después de que el usuario commitee |
| **#15 Esquema V3** | `seller_profiles`, `marcas`, `producto_imagenes`, `favoritos`, `direcciones` y `cupones`; 17 tablas en total |
| **#16 Rating write-behind** | Deltas en memoria + volcado cada 5 s; E2E: 0 → 5.00/1 al crear reseña y 5.00/1 → 0 al borrarla |
| **#18 Ranking** | `GET /api/v1/products/ranking` con rating y decaimiento exponencial de 30 días; la portada lo usa |
| **#15 Favoritos** | Tabla `favoritos` operativa: ids, listado, guardar/quitar; corazón en catálogo, portada, detalle y página `/favoritos` |
| **#19 Rediseño Stitch** | Shell con sidebar/topbar, portada, catálogo con filtros, login/registro e Inter; tablas y paneles heredan el design system |
| **Render/Vercel** | ⏸️ Pendientes de crear cuenta y conectar el repo; guía en [DESPLIEGUE_RENDER_VERCEL.md](docs/operacion/DESPLIEGUE_RENDER_VERCEL.md) |
| **Config de despliegue** | `render.yaml` listo; Vercel con `frontend/.env.example`; `/api/upload` usa Neon Object Storage cuando hay credenciales |
| **Verificación en vivo** | Health `UP` · ranking/perfil/favoritos OK · login BFF/carrito/notificaciones OK · write-behind 0→5→0 · 137 tests |

### Orden sugerido para continuar

| Orden | Tarea | Estado |
|---|---|---|
| 1 | #9 Bucket4j (rate limiting) | ✅ Hecho |
| 2 | #14 Flyway: probar arranque y corregir si falla `validate` | ✅ Hecho |
| 3 | #13 Caché de catálogo | ✅ Hecho (Caffeine) |
| 4 | #7 Sanitización + token fuera de `localStorage` | ✅ Hecho (cookie httpOnly + BFF) |
| 5 | #22 Tag/branch + push | ❌ |
| 6 | #23 Consolidar documentos | ❌ |
| 7 | #8 / #11 Render + Vercel | ⏸️ Pendiente de cuentas |
| 8 | #15 Reorganizar la base | ✅ Esquema V3 completo; perfil y favoritos operativos |
| 9 | #19 Rediseño (Stitch → código) | ⚠️ Base aplicada; pulido pendiente |
| 10 | #16 Write-behind del rating | ✅ Hecho |
| 11 | #18 Ranking con decaimiento | ✅ Hecho |

---

## Anexo A · Incidencias (ya volcadas a `TROUBLESHOOTING.md`)

> El listado completo, con fecha y formato exigido, está en [TROUBLESHOOTING.md](docs/operacion/TROUBLESHOOTING.md).
> Se mantiene aquí como referencia histórica.

Formato: `[Fecha] | [Capa] | [Descripción del Error] | [Solución Aplicada]`.

| # | Capa | Error | Solución |
|---|---|---|---|
| 1 | Backend | `SocketTimeoutException: Connect timed out` al conectar con Cloud SQL | IP del cliente no autorizada. Se autorizó `0.0.0.0/0` en Authorized Networks |
| 2 | Backend | `database "marketplace_db" does not exist` (SQLState 3D000) | Se creó la base por JDBC con el usuario `postgres` |
| 3 | Backend | La base `postgres` tenía 23 tablas de otro proyecto con PK `bigint` | Se decidió usar siempre `marketplace_db`; nunca la base por defecto |
| 4 | Backend | 5 tests de `AuthControllerTest` fallaban con `NoSuchBeanDefinitionException` | Faltaba `JwtAccessDeniedHandler` en el `@Import` del test |
| 5 | Backend | Login del admin devolvía `401` | El bootstrap **solo crea**, nunca actualiza contraseñas. Se sincronizó con `AdminPasswordSync.java` |
| 6 | Backend | `gradlew` fallaba con `FileNotFoundException ... .lck (Acceso denegado)` | El proceso no tenía permiso de escritura en la caché de Gradle; se ejecutó con permisos ampliados |
| 7 | Backend | Cloud Run: `403 storage.objects.get access` al subir el código | Desde mayo de 2024 los proyectos nuevos no reciben Editor automático. Se concedieron 6 roles a la cuenta de servicio |
| 8 | Backend | Cloud Run: `password authentication failed for user "postgres"` | La contraseña llegó mal en `--set-env-vars`; se corrigió con `--update-env-vars` |
| 9 | Backend | Cloud Run: `503 Service Unavailable` en todos los endpoints | La instancia de Cloud SQL estaba **STOPPED**; se arrancó a `RUNNABLE` |
| 10 | Backend | Cloud Run: la primera petición tarda ~25 s | Arranque en frío con `min-instances 0`; se mitiga con `min-instances 1` |
| 11 | Frontend | `spawn EPERM` al arrancar `next dev` | Entorno sin permisos de `fork`; se creó `scripts/dev-server.mjs` con la API programática de Next |
| 12 | Frontend | `npm EPERM` al instalar dependencias | Caché de npm en una ruta sin permisos; se fijó una caché local con `.npmrc` |
| 13 | Frontend | Next 15.5.4 reportaba una CVE | Se actualizó a Next 16.3.6 (`npm audit` → 0 vulnerabilidades) |
| 14 | Frontend | `403` en `/_next/**` y la página se quedaba "cargando" sin hidratar | Next 16 bloquea orígenes no locales en desarrollo; se añadió `allowedDevOrigins` con las IPs de red |
| 15 | Frontend | El proxy `/api/v1/**` devolvía `500` | `frontend/.env.local` no existía (está en `.gitignore`); se fijó Cloud Run como destino por defecto en `next.config.ts` |
| 16 | Postman | La colección daba `401` en el primer paso | `adminPassword` desactualizada; y los registros no eran idempotentes (el token quedaba `undefined` al repetir) |

---

## Anexo B · Herramientas MCP configuradas

### NotebookLM

| Cuaderno | ID | Estado |
|---|---|---|
| Diseño y Desarrollo Seguro en Frontend | `9bb773f4-7b01-4be5-a382-7d3990d848b0` | ✅ Accedido |

- CLI: `notebooklm` 0.8.6 + `notebooklm-mcp` (instalados con `uv tool`).
- Login con **cookies de Firefox** (`notebooklm login --browser-cookies firefox`); no requiere Chromium.
- Si caduca la sesión: repetir ese comando (perfil en `~/.notebooklm/`).

### Stitch

| Dato | Valor |
|---|---|
| Endpoint MCP | `https://stitch.googleapis.com/mcp` |
| API | `stitch.googleapis.com` habilitada en `marketplace-509503` |
| Proyecto | «Marketplace» (`projects/16417096205034790396`) |
| Pantallas | 1 (`Catálogo Marketplace`, móvil 780×1768) |
| Config | `~/.gemini/config/mcp_config.json` (`serverUrl` + `X-Goog-Api-Key`) |

> ⚠️ **Rotar la API key de Stitch**: la actual se pegó en el chat.
> Para escritorio, generar la pantalla con `deviceType: "DESKTOP"`.

---


---

## Resumen ejecutivo

> La base ya está en **Neon** y el backend funciona contra ella. Para que deje de correr en local
> falta desplegar la API en **Render**, el panel en **Vercel** y las imágenes en **Neon Object
> Storage**; la guía está en [DESPLIEGUE_RENDER_VERCEL.md](docs/operacion/DESPLIEGUE_RENDER_VERCEL.md).

| Bloque | Hechos | Pendientes |
|---|---|---|
| 🔴 Bloqueantes | **7/7** | 0 |
| 🟠 Alto | **4/6** | 2 (Render, Vercel) |
| 🟡 Medio | **5/6** | 1 parcial (#19) |
| 🟢 Higiene | **2/5** | 3 (#22, #23, #24) |

**El proyecto está funcionalmente completo y verificado en local.** Quedan:
- #15: esquema V3 completo, perfil de vendedor y favoritos operativos. Los endpoints/UI de
  marcas, direcciones, cupones e imágenes quedan como features posteriores.
- #19: base visual Stitch aplicada; el pulido fino de cada vista puede seguir iterándose.
- #8/#11: desplegar API en Render y panel en Vercel (requiere crear cuentas y conectar el repo).
- #22/#24: tag/rama y revocación de claves, que dependen de ti.
