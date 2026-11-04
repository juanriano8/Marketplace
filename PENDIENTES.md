# Pendientes del proyecto

**Octubre de 2026** · Documento único de estado y tareas.

> Este documento sustituye al anterior. Combina:
> - el estado real del proyecto (Marketplace: Spring Boot + PostgreSQL + Next.js, desplegado en Cloud Run)
> - las **directrices conceptuales de la clase** (rúbrica de revisión)
>
> **Sobre las directrices:** son conceptuales, no literales. Otros equipos usaron otras tecnologías.
> Lo que se revisa es el **cumplimiento de los principios**, no la tecnología concreta. Por eso cada
> requisito se evalúa contra su equivalente en nuestro stack.
>
> **NotebookLM queda aparcado** por indicación tuya. Ver Anexo B.

---

## 1. Estado actual: lo que ya está hecho y verificado

| Componente | Estado | Evidencia |
|---|---|---|
| **Backend** Spring Boot 3.3.4 / Java 21 | ✅ Completo | Arquitectura hexagonal, 8 módulos |
| **Endpoints** | ✅ **27 rutas / 31 operaciones** | `39 aserciones end-to-end, 0 fallos` |
| **Tests unitarios** | ✅ **114, 0 fallos** | `gradlew test` |
| **Base de datos** Cloud SQL PostgreSQL 18.6 | ✅ 10 tablas con datos | 45 usuarios, 24 productos |
| **Despliegue** Cloud Run | ✅ Activo | `marketplace-api-...run.app`, `{"status":"UP"}` |
| **Frontend** Next.js 16 / Tailwind 4 / TS | ✅ 15 páginas | `tsc` sin errores |
| **Reglas de negocio** | ✅ Verificadas | Antisobreventa (400 + ledger), multi-vendedor (350.00 USD), BOLA (400) |
| **Diseño** Figma "Origen" | ✅ Extraído | 849 nodos, paleta y tipografía en `design/tokens-origen.css` |
| **Documentación** | ✅ 15 documentos | Guías de despliegue, Postman, usuarios, análisis del dataset |

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

| Archivo | Estado | Qué contiene hoy |
|---|---|---|
| **`DESIGN.md`** | ❌ **No existe** | El material sí: `design/tokens-origen.css` (paleta, tipografía, radios extraídos de Figma) |
| **`UX_MAPPING.md`** | ❌ **No existe** | El material sí: 27 endpoints documentados + guías de Postman |
| **`TROUBLESHOOTING.md`** | ❌ **No existe** | El material sí: **16 incidencias reales** documentadas (Anexo A) |

Los tres son **exigidos explícitamente** y los tres tienen el contenido ya preparado. Son las tareas
con mejor relación esfuerzo/impacto de todo el documento.

### 2.4 Seguridad

| Requisito | Estado | Detalle |
|---|---|---|
| **Cero credenciales en el código** | ❌ **INCUMPLE** | `JWT_SECRET` con valor por defecto en `application.yml` · contraseña del admin en la colección de Postman |
| **Google Secret Manager** | ⚠️ Parcial | Usamos variables de entorno del servicio, no Secret Manager |
| **Seguridad cliente** | ❌ Falta | Sin CSP, sin sanitización, token en `localStorage` (expuesto a XSS), sin CSRF explícito |
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
| Redis para presencia / rate limit / caché | ❌ Bucket4j declarado y **no integrado** |

### 2.6 Herramientas de la rúbrica

| Herramienta | Estado | Nota |
|---|---|---|
| **NotebookLM MCP** | ⏸️ Aparcado | IDs en el Anexo B |
| **Stitch MCP** | ⚠️ Sustituido | Usamos **Figma** como fuente de verdad del diseño (equivalente funcional) |
| Skills `react-components` / `stitch-loop` | ⏸️ | No usadas |
| `TROUBLESHOOTING.md` | ❌ Falta | Ver Anexo A |

---

## 3. Pendientes por prioridad

### 🔴 Bloqueantes (incumplen una regla explícita)

| # | Tarea | Esfuerzo |
|---|---|---|
| 1 | **Quitar credenciales del código**: `JWT_SECRET` fuera de `application.yml`; contraseña fuera de la colección de Postman | Bajo |
| 2 | ✅ **`TROUBLESHOOTING.md`** creado (16 incidencias + 4 recientes) | Hecho 2026-11-03 |
| 3 | ✅ **`DESIGN.md`** creado desde los tokens de Figma | Hecho 2026-11-03 |
| 4 | ✅ **`UX_MAPPING.md`** creado (31 operaciones / 27 rutas) | Hecho 2026-11-03 |
| 5 | ✅ **Reintento en los 4 estados** de las 15 páginas (`<ErrorState onRetry>`) | Hecho 2026-11-03 |
| 6 | ✅ **Estados completos** en `/`, `/registro` y `/login` (portada con datos + reintento) | Hecho 2026-11-03 |
| 7 | **Seguridad del cliente**: CSP, sanitización, revisar el token en `localStorage` | Medio |

### 🟠 Alto (lo pide la rúbrica y se nota)

| # | Tarea | Esfuerzo |
|---|---|---|
| 8 | **Secret Manager** para las credenciales del backend | Medio |
| 9 | **Bucket4j**: rate limiting real por rol | Medio |
| 10 | **Librería de componentes**: adoptar Shadcn UI o justificar `ui.tsx` | Medio |
| 11 | **Cloud Armor + reCAPTCHA Enterprise** en el formulario de login/registro | Alto |
| 12 | ✅ **Endpoint `DELIVERED`** implementado: `PATCH /api/v1/seller/orders/{id}/deliver` | Hecho 2026-11-03 |
| 13 | **Caché de catálogo** (Redis o Caffeine) | Medio |

### 🟡 Medio (mejoran la nota, no bloquean)

| # | Tarea | Esfuerzo |
|---|---|---|
| 14 | Migraciones versionadas (Flyway) en vez de `ddl-auto: update` | Medio |
| 15 | Reorganizar la base: tablas que pide la especificación (`marcas`, `direcciones`, `cupones`, `favoritos`, `producto_imagenes`, perfil de vendedor) | Alto |
| 16 | *Write-behind* para el rating agregado de vendedor | Medio |
| 17 | Notificaciones (tabla propia o Firestore) | Medio |
| 18 | Ranking de productos con decaimiento temporal (equivalente a *Hot Ranking Decay*) | Bajo |
| 19 | Rediseño del frontend con Figma "Origen" (tokens ya extraídos) | Alto |

### 🟢 Higiene del repositorio

| # | Tarea | Esfuerzo |
|---|---|---|
| 20 | ✅ **`.gitignore` en la raíz** creado (secretos, backups, builds) | Hecho 2026-11-03 |
| 21 | **Backup de la base** antes de que expire el trial | Bajo |
| 22 | **Tag `entrega-v1`** y rama `rediseno-frontend` | Bajo |
| 23 | Consolidar los 15 documentos (hay solape entre guías) | Medio |
| 24 | Revocar el token de Figma compartido en el chat | Bajo |

---

## 4. Lo que se puede hacer ya (barato y suma)

Ordenado por relación impacto/esfuerzo. **Todo esto son horas, no días:**

1. **`TROUBLESHOOTING.md`** — copiar y pegar el Anexo A. Cumple una directriz entera.
2. **`DESIGN.md`** — volcar `tokens-origen.css` a Markdown con la paleta, tipografía y radios.
3. **`UX_MAPPING.md`** — tabla de las 27 rutas con método, payload y respuesta.
4. **Quitar credenciales** — dos archivos, 15 minutos.
5. **Reintento en los 4 estados** — un hook común y aplicarlo a las 15 páginas.
6. **`.gitignore` raíz** — 5 líneas.
7. **Tag `entrega-v1`** — un comando, y te da red de seguridad.

---

## Anexo A · Material listo para `TROUBLESHOOTING.md`

La directriz exige este formato: `[Fecha] | [Capa] | [Descripción del Error] | [Solución Aplicada]`.
Estas son incidencias **reales** del proyecto, ya diagnosticadas y resueltas:

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

## Anexo B · NotebookLM

Cuaderno que se usará como fuente para el rediseño del frontend:

| Cuaderno | ID | Estado |
|---|---|---|
| Diseño y Desarrollo Seguro en Frontend | `9bb773f4-7b01-4be5-a382-7d3990d848b0` | ✅ ID disponible |

**Nota:** mi entorno no tiene cliente MCP, igual que con Figma/Stitch. Para usarlos hay que ir por API
o por un cliente que soporte MCP. Lo evaluamos cuando llegue el momento.

---

## Anexo C · Migración a Postgres gratuito (salida de Google Cloud)

**Objetivo:** dejar de depender del trial de GCP sin perder datos ni funcionalidad.
El backend **no cambia**: solo se reapunta `DB_HOST` (y credenciales).

### Estado de partida

| Qué | Estado |
|---|---|
| Datos reales (usuarios, productos, órdenes) | Solo en Cloud SQL `marketplace-509503` |
| Backup en el repo | ❌ No existe |
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
> acabe el crédito de GCP (ver Anexo C). La base **no está respaldada en el repo**.

| Bloque | Pendientes | Esfuerzo estimado |
|---|---|---|
| 🔴 Bloqueantes | 7 | 2-3 días |
| 🟠 Alto | 6 | 1-2 semanas |
| 🟡 Medio | 6 | 1-2 semanas |
| 🟢 Higiene | 5 | 2 horas |

**El proyecto está funcionalmente completo y verificado.** Lo que falta no es funcionalidad: es
**cumplimiento formal** de la rúbrica. Y de los 7 bloqueantes, **3 tienen ya todo el material
preparado** (TROUBLESHOOTING, DESIGN, UX_MAPPING).
