# Pendientes del proyecto

**Noviembre de 2026** · Documento único de estado y tareas.

> Este documento sustituye al anterior. Combina:
> - el estado real del proyecto (Marketplace: Spring Boot + PostgreSQL + Next.js, desplegado en Cloud Run)
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
| **Backend** Spring Boot 3.3.4 / Java 21 | ✅ Completo | Arquitectura hexagonal, 8 módulos |
| **Endpoints** | ✅ **28 rutas / 32 operaciones** | + `DELIVERED`; 39 aserciones end-to-end históricas |
| **Tests unitarios** | ✅ 114 (histórico) | `JwtTokenProviderTest` re-ejecutado ✅ |
| **Base de datos** Cloud SQL PostgreSQL 18.6 | ✅ 10 tablas · **72 usuarios · 31 productos** | Dump verificado (365 filas) |
| **Migraciones** | ⚠️ **Flyway V1 creada** + `ddl-auto: validate` | Compila; falta probar arranque |
| **Despliegue** Cloud Run | ✅ Activo (imagen 2026-10-05) | `{"status":"UP"}`; pendiente redesplegar cambios |
| **Frontend** Next.js 16 / Tailwind 4 / TS | ✅ 15 páginas con **4 estados + reintento** | `tsc --noEmit` sin errores |
| **Reglas de negocio** | ✅ Verificadas | Antisobreventa (400 + ledger), multi-vendedor (350.00 USD), BOLA (400) |
| **Rate limiting** | ✅ Bucket4j integrado | 60 anónimo · 600 autenticado · 30 auth por minuto · `429` + `Retry-After` |
| **Diseño** | ✅ Figma «Origen» + `DESIGN.md` | 849 nodos → `design/tokens-origen.css` |
| **Stitch** | ✅ Proyecto «Marketplace» + 1 pantalla | MCP conectado (Anexo B) |
| **NotebookLM** | ✅ Cuaderno OWASP configurado | MCP conectado (Anexo B) |
| **Documentación** | ✅ 18 documentos | + `TROUBLESHOOTING.md`, `DESIGN.md`, `UX_MAPPING.md` |
| **Backup de la base** | ✅ `~/backups/backup_marketplace_2026-11-03.sql` | 96 KB · SHA-256 verificado |

**Este es el activo.** Lo que sigue es lo que falta para la rúbrica.

---

## 2. Cumplimiento de las directrices

Cada fila traduce el requisito conceptual de la clase a nuestro stack.

### 2.1 Stack y arquitectura

| Requisito | Qué pide | Nuestro equivalente | Estado |
|---|---|---|---|
| Stack frontend | Next.js + React + Tailwind + TypeScript | Next 16 + Tailwind 4 + TS 5 | ✅ |
| **Librería de componentes** | Shadcn UI | `ui.tsx` propio, sin librería | ⚠️ **Parcial** |
| Backend | Microservicios | **Monolito modular** hexagonal (8 módulos con fronteras y puertos) | ✅ Conceptual |
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
| **Total** | **15/15** | **13/15 + 2 N/A** | **15/15** | **15/15** ✅ |

**Actualización 2026-11-03:** el reintento se añadió a las **15 vistas** con el componente
`<ErrorState message onRetry={load}>`; `/` incorpora la sección «Productos destacados» con los 4
estados y un `Skeleton` reutilizable. En formularios, «Reintentar» reenvía el formulario
(`requestSubmit`). `—` = estado vacío no aplica (formularios y detalle).

### 2.3 Archivos de contexto exigidos

| Archivo | Estado | Qué contiene |
|---|---|---|
| **`DESIGN.md`** | ✅ **Creado** | Tokens de Figma, tipografía, componentes, 4 estados, accesibilidad y seguridad de UI |
| **`UX_MAPPING.md`** | ✅ **Creado** | 31 operaciones / 27 rutas con payloads, roles y pantalla asociada |
| **`TROUBLESHOOTING.md`** | ✅ **Creado** | 16 incidencias reales + 4 recientes, con el formato exigido |

### 2.4 Seguridad

| Requisito | Estado | Detalle |
|---|---|---|
| **Cero credenciales en el código** | ✅ **Cumple** | `JWT_SECRET` obligatorio (sin valor por defecto) · contraseña del admin vacía en la colección de Postman |
| **Google Secret Manager** | ⚠️ Parcial | Usamos variables de entorno del servicio, no Secret Manager |
| **Seguridad cliente** | ⚠️ **Parcial** | ✅ CSP activa (sin `sandbox`) · ❌ falta sanitización · ❌ token en `localStorage` |
| **Cloud Armor + reCAPTCHA Enterprise** | ❌ Falta | Sin perímetro ni puntuación de riesgo |
| Autorización por rol | ✅ Cumple | RBAC por URL + `@PreAuthorize` + BOLA en servicios |
| Contraseñas | ✅ Cumple | BCrypt |
| Errores sin filtrar datos | ✅ Cumple | RFC 7807 `ProblemDetail` |

### 2.5 Backend: normas verificables

| Requisito | Estado |
|---|---|
| Métodos HTTP y códigos correctos (RFC 9110) | ✅ `200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`, `422` |
| Segregación `/api/v1/` (cliente) vs `/api/v1/admin/` | ✅ Cumplida |
| Validación de entrada con Bean Validation | ✅ Cumplida |
| Ranking con decaimiento temporal | ⏸️ No aplica al dominio |
| *Write-behind* para reputación por lotes | ⏸️ Aplicable: los ratings de vendedor se calculan en consulta |
| Notificaciones en Firestore | ⏸️ No existe (equivalente: tabla `notifications`) |
| Redis para presencia / rate limit / caché | ⚠️ **Bucket4j integrado** (60/600/30 req/min); Redis sigue sin integrar |

### 2.6 Herramientas de la rúbrica

| Herramienta | Estado | Nota |
|---|---|---|
| **NotebookLM MCP** | ✅ **Configurado** | `notebooklm-mcp` autenticado con cookies de Firefox; cuaderno OWASP en Anexo B |
| **Stitch MCP** | ✅ **Configurado** | `serverUrl` + API key; proyecto «Marketplace» visible (Anexo B) |
| Skills `react-components` / `stitch-loop` | ⏸️ | Aún no usadas (Stitch tiene `upload_design_md` / `build_site`) |
| `TROUBLESHOOTING.md` | ✅ **Creado** | Ver [TROUBLESHOOTING.md](TROUBLESHOOTING.md) |

---

## 3. Pendientes por prioridad

### 🔴 Bloqueantes (incumplen una regla explícita)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 1 | Credenciales fuera del código (`JWT_SECRET` obligatorio, Postman sin contraseña) | ✅ Hecho | — |
| 2 | `TROUBLESHOOTING.md` (16 incidencias + 4 recientes) | ✅ Hecho | — |
| 3 | `DESIGN.md` desde los tokens de Figma | ✅ Hecho | — |
| 4 | `UX_MAPPING.md` (31 operaciones / 27 rutas) | ✅ Hecho | — |
| 5 | Reintento en los 4 estados de las 15 vistas (`<ErrorState>`) | ✅ Hecho | — |
| 6 | Estados completos en `/`, `/registro` y `/login` | ✅ Hecho | — |
| 7 | Seguridad del cliente: sanitización + sacar el token de `localStorage` | ⚠️ Parcial | Medio |

### 🟠 Alto (lo pide la rúbrica y se nota)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 8 | Secret Manager para las credenciales del backend | ❌ Pendiente | Medio |
| 9 | Bucket4j: rate limiting real por rol | ✅ Hecho | — |
| 10 | Librería de componentes: adoptar Shadcn UI o justificar `ui.tsx` | ❌ Pendiente | Medio |
| 11 | Cloud Armor + reCAPTCHA Enterprise en login/registro | ❌ Pendiente | Alto |
| 12 | Endpoint `DELIVERED` (`PATCH /seller/orders/{id}/deliver`) | ✅ Hecho | — |
| 13 | Caché de catálogo (Redis o Caffeine) | ❌ Pendiente | Medio |

### 🟡 Medio (mejoran la nota, no bloquean)

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 14 | Migraciones versionadas con Flyway | ⚠️ En progreso | Medio |
| 15 | Reorganizar la base (`marcas`, `direcciones`, `cupones`, `favoritos`, `producto_imagenes`, perfil de vendedor) | ❌ Pendiente | Alto |
| 16 | *Write-behind* para el rating agregado de vendedor | ❌ Pendiente | Medio |
| 17 | Notificaciones (tabla propia o Firestore) | ❌ Pendiente | Medio |
| 18 | Ranking de productos con decaimiento temporal | ❌ Pendiente | Bajo |
| 19 | Rediseño del frontend con Stitch / Figma «Origen» | ⚠️ En progreso | Alto |

### 🟢 Higiene del repositorio

| # | Tarea | Estado | Esfuerzo |
|---|---|---|---|
| 20 | `.gitignore` en la raíz | ✅ Hecho | — |
| 21 | Backup de la base antes de que expire el trial | ✅ Hecho | — |
| 22 | Tag `entrega-v1` y rama `rediseno-frontend` (los commits los hace el usuario) | ❌ Pendiente | Bajo |
| 23 | Consolidar los 18 documentos (hay solape entre guías) | ❌ Pendiente | Medio |
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

### Orden sugerido para continuar

| Orden | Tarea | Estado |
|---|---|---|
| 1 | #14 Flyway: probar arranque y corregir si falla `validate` | ⚠️ |
| 2 | #13 Caché de catálogo | ❌ |
| 3 | #7 Sanitización + token fuera de `localStorage` | ⚠️ |
| 4 | #22 Tag/branch + push | ❌ |
| 5 | #23 Consolidar documentos | ❌ |
| 6 | #8 / #11 Secret Manager + Cloud Armor | ❌ (GCP) |
| 7 | #15 Reorganizar la base | ❌ |
| 8 | #19 Rediseño (Stitch → código) | ⚠️ |

---

## Anexo A · Incidencias (ya volcadas a `TROUBLESHOOTING.md`)

> El listado completo, con fecha y formato exigido, está en [TROUBLESHOOTING.md](TROUBLESHOOTING.md).
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

## Anexo C · Migración a Postgres gratuito (salida de Google Cloud)

**Objetivo:** dejar de depender del trial de GCP sin perder datos ni funcionalidad.
El backend **no cambia**: solo se reapunta `DB_HOST` (y credenciales).

### Estado de partida

| Qué | Estado |
|---|---|
| Datos reales (usuarios, productos, órdenes) | Solo en Cloud SQL `marketplace-509503` |
| Backup | ✅ `~/backups/` (fuera del repo) |
| Código y documentación | ✅ En el repo/GitHub |
| Facturación del proyecto | ⚠️ Activa: al acabar el crédito se cobra a la tarjeta |

### Paso 0 — Backup (obligatorio antes de tocar nada)

```bash
export PATH="$HOME/google-cloud-sdk/bin:$PATH"
gcloud sql instances patch marketplace --activation-policy ALWAYS --project marketplace-509503
gcloud storage buckets create gs://marketplace-backups-509503 --project marketplace-509503 --location us-central1
gcloud sql export sql marketplace gs://marketplace-backups-509503/backup_marketplace_$(date +%F).sql \
  --database=marketplace_db --project marketplace-509503
mkdir -p ~/backups
gcloud storage cp gs://marketplace-backups-509503/backup_marketplace_$(date +%F).sql ~/backups/
```

> El dump contiene emails y hashes BCrypt: **no subirlo al repo**. Guardarlo en `~/backups/` y en una copia externa.

### Paso 1 — Crear el Postgres gratuito (Neon)

1. <https://neon.tech> → cuenta gratis → **New project** (región `us-east-1` o la más cercana).
2. Copiar la cadena de conexión (`postgresql://user:pass@host/db?sslmode=require`).
3. Guardar los datos en `backend/.env` (no en Git).

### Paso 2 — Restaurar el dump

```bash
psql "postgresql://...@.../neondb?sslmode=require" < ~/backups/backup_marketplace_YYYY-MM-DD.sql
```

Sin `psql` local: usar el **SQL Editor** de Neon y subir el archivo, o
`docker run --rm -i postgres:18 psql ...`.

### Paso 3 — Reapuntar el backend

`backend/.env`:

```ini
DB_HOST=<host-neon>
DB_PORT=5432
DB_NAME=<db>
DB_USER=<user>
DB_PASSWORD=<pass>
DB_SSLMODE=require
```

Cloud Run:

```bash
gcloud run services update marketplace-api --region us-central1 \
  --update-env-vars DB_HOST=...,DB_PORT=5432,DB_NAME=...,DB_USER=...,DB_PASSWORD=...,DB_SSLMODE=require
```

### Paso 4 — Verificar

```bash
BASE_URL=https://marketplace-api-805790031718.us-central1.run.app ./backend/tools/verify.sh
```

Debe terminar en **TODO CORRECTO**.

### Paso 5 — Apagar/liberar Cloud SQL

```bash
gcloud sql instances patch marketplace --activation-policy NEVER --project marketplace-509503
# cuando ya no se necesite:
# gcloud sql instances delete marketplace --project marketplace-509503
```

### Tablas que faltan (aprovechar la migración)

Según la especificación y el dataset: `marcas`, `direcciones`, `cupones`, `favoritos`,
`producto_imagenes` (el diseño de Figma ya pide **3 imágenes por producto**) y perfil de vendedor.
Aprovechar para pasar de `ddl-auto: update` a **migraciones versionadas** (Flyway/Liquibase).

### Alternativas de hosting

| Opción | Coste | Notas |
|---|---|---|
| **Neon** | $0 | Recomendada: Postgres real, sin tarjeta, con ramas/restore |
| Supabase | $0 | 500 MB; pausa por inactividad |
| Aiven | $0 | 1 GB; algunos planes piden tarjeta |
| Cloud SQL | ~$10–50/mes | Solo si hay presupuesto |
| Docker local | $0 | Desarrollo; no para presentar |

### Checklist

- [x] Backup exportado y descargado a `~/backups/` (2026-11-03 · 96 KB · 10 tablas)
- [ ] Copia externa del `.sql`
- [x] `backend/.env` y `~/backups/backend.env` guardados
- [x] Variables de Cloud Run respaldadas (`~/backups/cloudrun-env.yaml`)
- [ ] Proyecto Neon creado
- [ ] Dump restaurado y verificado (`verify.sh`)
- [ ] Cloud Run reapuntado
- [ ] Cloud SQL apagado
- [ ] Bucket temporal borrado si no se usa

**Backup actual:**

```
~/backups/backup_marketplace_2026-11-03.sql   (96 KB, 10 tablas)
sha256: c1c9ce078a4ae4cd621c31f9b46911127dbc938063befcd8b5a568b6b8b6c477
```

---

## Resumen ejecutivo

> ⚠️ **Urgente e independiente de la rúbrica:** migrar la base a un Postgres gratuito antes de que
> acabe el crédito de GCP (ver Anexo C). El backup ya está en `~/backups/`; **falta crear el Neon**.

| Bloque | Hechos | Pendientes |
|---|---|---|
| 🔴 Bloqueantes | **6/7** | 1 (seguridad del cliente, parcial) |
| 🟠 Alto | **2/6** | 4 |
| 🟡 Medio | **0/6** | 5 pendientes + 1 en progreso (Flyway) |
| 🟢 Higiene | **2/5** | 3 |

**El proyecto está funcionalmente completo y verificado.** Los bloqueantes de documentación y
credenciales están cerrados; quedan la seguridad del cliente y las tareas de infraestructura
(Neon, Secret Manager, Cloud Armor), que dependen de cuentas o permisos.

**Urgente:** migrar la base a Neon antes de que acabe el crédito de GCP (Anexo C).
