# Desplegar en Render + Vercel + Neon

> **Stack objetivo:** Vercel (panel Next.js) → Render (API Spring Boot) → Neon (PostgreSQL)
> y Neon Object Storage (imágenes). Sin Google Cloud.

---

## 1. Por qué esta combinación

| Pieza | Servicio | Plan gratuito |
|---|---|---|
| Base de datos | **Neon PostgreSQL** | 1 GB por proyecto, 100 CU-horas/mes, scale-to-zero |
| Imágenes | **Neon Object Storage** | 5 GB por proyecto, API S3 |
| API Spring Boot | **Render Web Service** | 512 MB RAM, 0.1 CPU, 750 h/mes, duerme a los 15 min |
| Panel Next.js | **Vercel Hobby** | 100 GB de banda/mes, HTTPS |

**Neon Functions no sirve para este backend:** solo ejecuta JavaScript/TypeScript sobre Node.js 24.
El backend es Spring Boot/Java 21 y necesita un contenedor o una VM; Render lo ejecuta desde el
`Dockerfile` existente.

### Límites que hay que asumir

- **Render free** duerme el servicio tras 15 minutos sin tráfico. La primera petición puede tardar
  ~1 minuto (contenedor + Neon despertando). Para una demo es aceptable; para uso continuo,
  Render Starter (~7 USD/mes) o Railway Hobby (~5 USD/mes).
- **512 MB de RAM** es justo para Spring Boot. Si hay `OutOfMemoryError`, subir a un plan pago o
  bajar `DB_POOL_SIZE`, `CACHE_MAX_ENTRIES` y `JAVA_TOOL_OPTIONS`.
- **Neon free** tiene 1 GB de datos y 5 GB de objetos; suficiente para la demo.
- **Vercel Hobby** es gratis para uso personal/no comercial.

---

## 2. Neon: base de datos y Object Storage

### 2.1 PostgreSQL

1. Entra en <https://console.neon.tech> → proyecto existente.
2. **Connect** → copia la cadena **directa** (sin `-pooler`).
3. Esos datos van a Render como `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`,
   `DB_SSLMODE=require`.

Las migraciones Flyway (`V1`–`V3`) se aplican solas al arrancar el backend.

### 2.2 Bucket de imágenes

1. Neon Console → tu proyecto → rama `main` → **Object storage**.
2. **New bucket**:
   - Nombre: `marketplace-images`
   - Acceso: **`public_read`** (las imágenes se leen sin credencial).
3. Crea una credencial con permisos `storage:read` y `storage:write`.
4. Anota:
   - `AWS_ENDPOINT_URL_S3` (endpoint del branch, termina en `.neon.tech`)
   - `AWS_REGION`
   - `AWS_ACCESS_KEY_ID`
   - `AWS_SECRET_ACCESS_KEY`
   - `NEON_STORAGE_BUCKET=marketplace-images`

> El endpoint y las credenciales son **por rama**. Si creas una rama de preview, usa las de esa rama.

---

## 3. Render: API Spring Boot

### 3.1 Crear el servicio

1. Sube el repo a GitHub (incluido `render.yaml`).
2. Render Dashboard → **New → Blueprint** → conecta el repositorio.
3. Render detecta [render.yaml](../../render.yaml) y crea `marketplace-api`.
4. Rellena las variables marcadas `sync: false`:

| Variable | Valor |
|---|---|
| `DB_HOST` | host directo de Neon |
| `DB_PASSWORD` | contraseña de Neon |
| `JWT_SECRET` | `openssl rand -hex 32` |
| `BOOTSTRAP_ADMIN_PASSWORD` | contraseña del admin |

`DB_NAME=neondb`, `DB_USER=neondb_owner`, `DB_SSLMODE=require`, `SPRING_PROFILES_ACTIVE=cloud` y
`DB_POOL_SIZE=5` ya vienen en el blueprint.

### 3.2 Verificar

Cuando Render termine el build:

```text
https://marketplace-api.onrender.com/actuator/health
```

Debe devolver `{"status":"UP"}`. Luego:

```text
https://marketplace-api.onrender.com/api/v1/products?size=3
https://marketplace-api.onrender.com/swagger-ui/index.html
```

> La primera petición tras 15 minutos de inactividad puede tardar ~1 minuto. Para evitarlo,
> configura un ping HTTP cada 10 minutos a `/actuator/health` (por ejemplo con cron-job.org o
> UptimeRobot). Eso mantiene Render despierto y cabe en las 750 h/mes. **No** hagas ping a un
> endpoint que consulte la base: Neon tiene 100 CU-horas/mes y se suspende a los 5 minutos.

---

## 4. Panel Next.js: Vercel **o** Render

El panel necesita un runtime de Node, no una base de datos. Hay dos caminos y **el `render.yaml`
ya trae el segundo preparado**, así que puedes elegir sin tocar código.

### 4.A Vercel (recomendado para presentar)

Vercel es el mejor host para Next.js: CDN, builds optimizados y **el panel no duerme nunca**.

1. Vercel → **Add New → Project** → importa el repo.
2. **Root Directory:** `frontend`.
3. Framework: Next.js (detectado).
4. Environment Variables:

| Variable | Valor |
|---|---|
| `MARKETPLACE_API_URL` | `https://marketplace-api.onrender.com` |
| `AWS_ENDPOINT_URL_S3` | endpoint de Neon Object Storage |
| `AWS_REGION` | región del branch |
| `AWS_ACCESS_KEY_ID` | credencial de storage |
| `AWS_SECRET_ACCESS_KEY` | secreto de storage |
| `NEON_STORAGE_BUCKET` | `marketplace-images` |

5. Deploy. Abre la URL `https://<proyecto>.vercel.app`.

> Si eliges Vercel, **borra el servicio `marketplace-web`** de `render.yaml` para no crear un
> servicio que no vas a usar.

### 4.B Todo en Render (una sola cuenta)

Si prefieres no crear cuenta en Vercel, el `render.yaml` ya declara `marketplace-web`:

| Ajuste | Valor |
|---|---|
| Runtime | `node` |
| Root directory | `frontend` |
| Build | `npm ci && npm run build` |
| Start | `npm run start:prod` |
| Health check | `/` |

> **`start:prod` y no `start`.** El script local fija el puerto 3000 (`next start -p 3000`), pero
> Render inyecta el suyo en `PORT`. Si se fijara el puerto, el health check fallaría y Render
> reiniciaría el servicio en bucle.

Solo hay que rellenar `MARKETPLACE_API_URL` (ya viene con el valor correcto) y las 4 variables de
Neon Object Storage.

**Comparación:**

| | Vercel (4.A) | Render (4.B) |
|---|---|---|
| Cuentas nuevas | 2 (Render + Vercel) | **1** (Render) |
| El panel duerme | ❌ Nunca | ✅ A los 15 min |
| Primer arranque | Instantáneo | ~30-60 s |
| Cuota | 100 GB de banda | 750 h/mes compartidas |

---

## 5. El BFF: por qué no hace falta CORS

El panel llama a `/api/v1/**` **en su propio dominio**; el route handler de Next lee la cookie
httpOnly y reenvía a Render con `Authorization`. Por eso **no hace falta CORS** y la cookie
`secure` funciona bajo HTTPS.

Comprobado en modo producción (`next start`):

```
POST /api/session/login          -> 200
set-cookie: mp_token=...; Secure; HttpOnly; SameSite=lax
cuerpo de la respuesta           -> sin accessToken (el JWT no llega al navegador)
GET /api/v1/cart con cookie      -> 200
GET /api/v1/cart sin cookie      -> 401
```

---

## 6. Probar el flujo completo

1. Abre la URL de Vercel.
2. Login con un usuario de prueba.
3. Catálogo, carrito, checkout.
4. Panel de vendedor: publicar producto y subir imagen (debe devolver una URL de Neon Object Storage).
5. Administrador: aprobar producto/vendedor.
6. Notificaciones, favoritos y ranking.

---

## 7. Desarrollo local apuntando a la nube

Si quieres seguir usando el panel en local pero con la API de Render:

`frontend/.env.local`:

```env
MARKETPLACE_API_URL=https://marketplace-api.onrender.com
```

y reinicia `npm run dev`. Para las imágenes, añade también las variables de Neon Object Storage.

---

## 8. Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| Render tarda ~1 min | Free duerme tras 15 min | Esperar o usar plan pago |
| `502` en Render | Contenedor arrancando o OOM | Ver logs; subir RAM o bajar pool/caché |
| `401` en el panel | Cookie no llega o API sin sesión | Revisar HTTPS y `MARKETPLACE_API_URL` |
| Imágenes `503` | Faltan variables de Neon Storage | Configurarlas en Vercel |
| Imágenes 404 | Bucket privado | Crear/usar bucket `public_read` |
| Flyway falla | Esquema inesperado | Revisar logs; no editar migraciones aplicadas |
| `OutOfMemoryError` | 512 MB insuficiente | Render Starter o Railway Hobby; bajar pool |

---

## 9. Referencias

- Neon Free: <https://neon.com/faqs/free-plan-limits-and-quotas>
- Neon Object Storage: <https://neon.com/docs/storage/overview>
- Neon Functions (solo JS/TS): <https://neon.com/docs/compute/functions/overview>
- Render free: <https://render.com/docs/free>
- Render Blueprint: <https://render.com/docs/blueprint-spec>
- Vercel Hobby: <https://vercel.com/docs/plans/hobby>
- Railway (alternativa de pago): <https://railway.com/pricing>
