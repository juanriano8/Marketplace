# Desplegar el backend en Cloud Run

**Fecha:** 28 de septiembre de 2026
**Objetivo:** que la API deje de correr en un PC y pase a tener una **URL pública** que funcione
desde cualquier computador, red o país, sin `.env`, sin túneles y sin abrir puertos.

---

## ✅ Estado: desplegado y verificado

| | |
|---|---|
| **URL pública** | `https://marketplace-api-805790031718.us-central1.run.app` |
| **Servicio** | `marketplace-api` · revisión `marketplace-api-00004-rs7` · `us-central1` |
| **Health** | `{"status":"UP"}` |
| **Endpoints** | **39 aserciones OK / 0 fallos** (las 27 rutas de la especificación) |
| **Antisobreventa** | ✅ Comprador 2 bloqueado con `400`; ledger `INITIAL → RESERVATION → SALE` |
| **Orden multi-vendedor** | ✅ 2 sub-órdenes, 350.00 USD |
| **BOLA** | ✅ Vendedor A no puede despachar la sub-orden de B (`400`) |
| **Base de datos** | La **misma** `marketplace_db`, con los 45 usuarios y todos los productos |

**Dos tropiezos en el camino, ambos documentados abajo con su solución:**

1. `403` al subir el código → faltaban permisos en la cuenta de servicio
   ([ver sección](#error-403-al-subir-el-código-does-not-have-storageobjectsget-access)).
2. `password authentication failed` → la contraseña llegó mal en la variable de entorno
   ([ver sección](#password-authentication-failed-for-user-postgres-sqlstate-28p01)).

---

## Por qué esto te resuelve el problema de fondo

Hasta ahora el backend vivía en un PC concreto, así que dependías de la IP del PC, del firewall, de
que la red no bloqueara el puerto 5432 y de tener Java instalado. Con Cloud Run:

| Problema que tenías | Con Cloud Run |
|---|---|
| "¿Cómo lo ejecuto en otro PC?" | Abres una URL. Nada que instalar |
| El PC Linux y el Windows veían cosas distintas | Una sola instancia para todos |
| La IP del cliente cambiaba → Cloud SQL bloqueaba | Irrelevante (la conexión sale de Google) |
| Había que transportar el `.env` a mano | Las variables viven en el servicio |
| El backend solo estaba arriba si el PC estaba encendido | Google lo mantiene y escala solo |

**Y lo mejor para tu demo:** el servicio se conecta a la **misma base `marketplace_db`**, así que
arranca con los 45 usuarios, los 17 productos y todas las órdenes que ya tienes. No hay que cargar
nada.

---

## Antes de empezar: dos requisitos

| Requisito | Cómo comprobarlo |
|---|---|
| **Facturación activa** en el proyecto `marketplace-509503` | Consola → Facturación → debe haber una cuenta vinculada. **Sin esto Cloud Run no despliega** |
| El repositorio en GitHub | `https://github.com/juanriano8/Marketplace` (ya lo tienes) |

> **Coste:** Cloud Run tiene capa gratuita (2 millones de peticiones al mes), Cloud Build da 120
> minutos de compilación al día gratis, y Artifact Registry 0,5 GB gratis. Para una demo **no
> deberías pagar nada extra**. Lo que ya te cuesta dinero es Cloud SQL, que seguirá igual.
>
> Si quieres que el servicio no consuma nada mientras no lo uses, deja **Instancias mínimas = 0**
> (es lo que hace el comando de abajo).

---

## Ruta A: Cloud Shell (recomendada)

Cloud Shell es la terminal que Google te da **dentro de la consola**, en una pestaña del navegador.
No hay que instalar nada: ya trae `gcloud`, `git` y `docker`, y **ya está autenticada con tu cuenta**.

**Ábrela así:** en <https://console.cloud.google.com> busca el icono **`>_`** arriba a la derecha
(«Activar Cloud Shell»). Se abre una terminal abajo.

### Paso 1: seleccionar el proyecto y habilitar las APIs

```bash
gcloud config set project marketplace-509503

gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com
```

Esto habilita los tres servicios que hacen falta (Cloud Run, el registro de imágenes y el
compilador). Tarda un par de minutos la primera vez.

### Paso 2: traer el código

```bash
cd ~
git clone https://github.com/juanriano8/Marketplace.git
cd Marketplace/backend
```

> **Importante:** hay que estar **dentro de `backend/`**. El `Dockerfile` está ahí y su contexto de
> compilación es esa carpeta.

### Paso 3: desplegar

Copia esto **tal cual**, cambiando solo `DB_PASSWORD` por la contraseña real de tu `.env`:

```bash
gcloud run deploy marketplace-api \
  --source . \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --cpu-boost \
  --min-instances 0 \
  --max-instances 2 \
  --timeout 60 \
  --port 8080 \
  --set-env-vars "SPRING_PROFILES_ACTIVE=cloud,DB_HOST=136.112.91.42,DB_PORT=5432,DB_NAME=marketplace_db,DB_USER=postgres,DB_SSLMODE=require,DB_POOL_SIZE=5,DB_POOL_MIN_IDLE=1,JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970,JWT_EXPIRATION_MS=86400000,BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com,BOOTSTRAP_ADMIN_PASSWORD=Admin123."
```

La primera vez te preguntará si quieres **crear un repositorio en Artifact Registry**: di que sí
(pulsa Enter). Después:

1. Sube el código a Cloud Build
2. Compila el `Dockerfile` (unos 3-5 minutos la primera vez)
3. Publica la imagen
4. Crea el servicio en Cloud Run

Al terminar imprime algo así:

```
Service [marketplace-api] revision [marketplace-api-00001-abc] has been deployed
and is serving 100 percent of traffic.
Service URL: https://marketplace-api-xxxxxxxxxx-uc.a.run.app
```

**Esa URL es tu API pública.** Cópiala.

### Paso 4: comprobar

```bash
# Ver la URL
gcloud run services describe marketplace-api --region us-central1 --format='value(status.url)'

# Que esté viva
curl -s "$(gcloud run services describe marketplace-api --region us-central1 --format='value(status.url)')/actuator/health"
```

Debe responder:

```json
{"status":"UP"}
```

Y en el navegador:

| Recurso | URL |
|---|---|
| Swagger UI | `https://TU-URL/swagger-ui.html` |
| Health | `https://TU-URL/actuator/health` |
| Catálogo (público) | `https://TU-URL/api/v1/products` |

---

## Ruta B: solo con clics en la consola

Si prefieres no escribir comandos, la consola puede compilar desde GitHub… **pero hay una trampa
importante** que te explico al final.

1. Consola → **Cloud Run** → **Crear servicio**
2. Elige **«Implementar desde código fuente»** (o «Implementar continuamente desde un repositorio»)
3. **Conectar repositorio** → autoriza GitHub → elige `juanriano8/Marketplace`, rama `main`
4. En **tipo de compilación** elige **Dockerfile**
5. **Aquí está la trampa:** el `Dockerfile` está en `backend/`, no en la raíz del repositorio. Si la
   consola te deja indicar la **carpeta de contexto de compilación** (o «ubicación del origen»),
   escribe `backend` y listo.
6. Configura:
   - Región: `us-central1`
   - Autenticación: **Permitir invocaciones no autenticadas**
   - Puerto del contenedor: `8080`
   - Memoria: `1 GiB` · CPU: `1`
   - Instancias mínimas: `0` · Máximas: `2`
7. **Variables y secretos** → añade las de la tabla de abajo
8. **Crear**

### Si la consola no te deja indicar la carpeta

Entonces la compilación desde la raíz fallará (`COPY gradlew` no encontrará el archivo). La
solución es añadir a la **raíz** del repositorio un archivo `cloudbuild.yaml`:

```yaml
# Compila el backend indicando que el contexto es la carpeta backend/
steps:
  - name: gcr.io/cloud-builders/docker
    dir: backend
    args:
      - build
      - -t
      - ${_IMAGE}
      - .
images:
  - ${_IMAGE}
```

Y en la consola, en lugar de «Dockerfile», elige **Cloud Build configuration file** y apunta a
`/cloudbuild.yaml`.

> Si prefieres, dime y te creo ese archivo en el repositorio. Ahora mismo no lo he tocado.

---

## Variables de entorno (las dos rutas)

Estas son **las que tu código realmente lee**, sacadas de `application.yml` y `application-cloud.yml`:

| Variable | Valor | ¿Obligatoria? |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `cloud` | **Sí** — activa el perfil de Cloud SQL |
| `DB_HOST` | `136.112.91.42` | **Sí** |
| `DB_PORT` | `5432` | Sí |
| `DB_NAME` | `marketplace_db` | **Sí** |
| `DB_USER` | `postgres` | **Sí** |
| `DB_PASSWORD` | *(la de tu `.env`)* | **Sí** |
| `DB_SSLMODE` | `require` | Sí |
| `JWT_SECRET` | `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970` | Sí |
| `JWT_EXPIRATION_MS` | `86400000` | Recomendada |
| `BOOTSTRAP_ADMIN_EMAIL` | `admin@marketplace.com` | Opcional |
| `BOOTSTRAP_ADMIN_PASSWORD` | *(la de tu `.env`)* | Opcional |
| `DB_POOL_SIZE` | `5` | Recomendada (Cloud Run usa menos conexiones) |
| `DB_POOL_MIN_IDLE` | `1` | Recomendada |
| `PAYMENT_STUB_CAPTURE` | `true` | Opcional |
| `PORT` | Cloud Run lo inyecta | **No la pongas**, la maneja Google |

**Sobre `BOOTSTRAP_ADMIN_*`:** el administrador **ya existe** en la base, y el bootstrap solo crea
usuarios que no existen. Así que esas dos variables no cambian nada: sirven para que un proyecto
nuevo arranque con admin.

> **Ojo con los caracteres especiales:** si tu contraseña tiene una **coma**, no puedes usar
> `--set-env-vars` con comas como separador. Usa otro delimitador:
> `--set-env-vars "^@^DB_PASSWORD=a,b,c@DB_HOST=..."`.

---

## Cómo se conecta a la base de datos

Tu perfil `cloud` arma esta cadena:

```
jdbc:postgresql://136.112.91.42:5432/marketplace_db?sslmode=require&connectTimeout=15
```

Es decir, **conexión por IP pública con SSL**. Y funciona desde Cloud Run porque:

1. La instancia tiene `0.0.0.0/0` autorizado (lo pusiste tú)
2. Cloud Run tiene salida a internet sin restricción de puertos
3. El perfil `cloud` ya pide `sslmode=require`, así que el tráfico va cifrado

**No hay que cambiar ni una línea de código.** ✅

### La forma "correcta" (para más adelante)

La conexión por IP pública funciona, pero mantiene la base abierta a internet. Lo propio sería el
**conector de Cloud SQL**, que va por un socket Unix sin exponer la base:

- En Cloud Run se activa con `--add-cloudsql-instances marketplace-509503:us-central1:marketplace`
- **Pero** requiere añadir a `build.gradle` la dependencia
  `com.google.cloud.sql:postgres-socket-factory` y cambiar la URL a
  `jdbc:postgresql:///marketplace_db?socketFactory=com.google.cloud.sql.postgres.SocketFactory&cloudSqlInstance=...`
- Una vez funcionando, se puede **quitar el `0.0.0.0/0`** de Authorized networks

Es un cambio de código, así que lo dejo apuntado y no lo hago. Ver el pendiente al final.

---

## Apuntar el frontend al backend en la nube

El panel web usa `rewrites()` para hacer de puente, así que solo hay que cambiar a dónde apunta.
En `frontend/`, antes de arrancar:

**Windows (PowerShell):**

```powershell
$env:MARKETPLACE_API_URL = "https://marketplace-api-xxxxxxxxxx-uc.a.run.app"
npm run dev
```

**Linux / macOS:**

```bash
export MARKETPLACE_API_URL="https://marketplace-api-xxxxxxxxxx-uc.a.run.app"
npm run dev
```

Así el panel local consume la API de la nube y **no hay problemas de CORS** (el navegador nunca
llama al puerto 8080 directamente).

Si dejas la variable sin definir, seguirá usando `http://localhost:8080`.

---

## Usar la colección de Postman contra Cloud Run

Solo hay que cambiar **una variable**. En Postman → tu colección → pestaña **Variables**:

| Variable | Valor nuevo |
|---|---|
| `baseUrl` | `https://marketplace-api-xxxxxxxxxx-uc.a.run.app` |

Todo lo demás funciona igual: los tokens se capturan solos y las 44 peticiones apuntan a la nube.
Cuando termines la demo, vuelve a poner `http://localhost:8080`.

---

## Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| `Container failed to start and listen on PORT` | La app no arrancó | Revisa los logs (ver abajo). Suele ser un error de conexión a la base |
| `Service Unavailable` (503) en la primera petición | Arranque en frío: Spring Boot tarda 15-30 s | Espera y recarga. Con `--cpu-boost` mejora |
| `Connection refused` / `SocketTimeoutException` | No llega a Cloud SQL | Comprueba que `0.0.0.0/0` siga autorizado y que `DB_HOST` sea la IP correcta |
| `password authentication failed` | Contraseña con caracteres mal escapados | Revisa `DB_PASSWORD`; cuidado con las comas |
| `The user-provided container failed to start` | Falta `SPRING_PROFILES_ACTIVE=cloud` | Sin el perfil `cloud` intenta conectarse a `localhost:5432` |
| Error de compilación `COPY gradlew: not found` | El contexto de compilación no es `backend/` | Usa la ruta A, o el `cloudbuild.yaml` de la ruta B |
| `Permission denied` al desplegar | La cuenta de servicio no tiene roles | Ver la sección siguiente ⬇ |
| La base se queda sin conexiones | Muchas instancias de Cloud Run | Baja `DB_POOL_SIZE=5` y `--max-instances 2` |
| `BILLING_DISABLED` | El proyecto no tiene facturación | Actívala en Consola → Facturación |

### Error 403 al subir el código: `does not have storage.objects.get access`

**Es el fallo más habitual y no es culpa del código.** El despliegue sube las fuentes a un bucket
temporal y luego la cuenta de servicio del proyecto las lee para compilar. Desde **mayo de 2024**,
los proyectos nuevos **ya no reciben el rol Editor automáticamente** en sus cuentas de servicio, así
que esa lectura falla:

```
Building and deploying new service...
  Validating configuration...done
  Creating Container Repository...done
  Uploading sources...failed
Deployment failed
ERROR: (gcloud.run.deploy) INVALID_ARGUMENT: Invalid build request. could not resolve source:
googleapi: Error 403: 805790031718-compute@developer.gserviceaccount.com does not have
storage.objects.get access to the Google Cloud Storage object.
```

**Solución:** conceder los permisos que necesita esa cuenta. Copia y pega en Cloud Shell. El número
de proyecto aparece en el propio mensaje de error (`805790031718-` es el de este proyecto):

```bash
PROJECT_ID=marketplace-509503
PROJECT_NUMBER=805790031718
SA="${PROJECT_NUMBER}-compute@developer.gserviceaccount.com"

for ROLE in \
  roles/storage.objectViewer \
  roles/cloudbuild.builds.builder \
  roles/artifactregistry.writer \
  roles/run.developer \
  roles/iam.serviceAccountUser \
  roles/logging.logWriter ; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member="serviceAccount:${SA}" --role="$ROLE" --condition=None --quiet
done
```

> Se conceden **los seis roles de una vez** a propósito. Si solo se arregla el del almacenamiento,
> el siguiente intento fallará al publicar la imagen, y el siguiente al crear el servicio. Así se
> resuelve en un solo paso.

Después, espera **un minuto** (los cambios de IAM tardan en propagarse) y **vuelve a lanzar el mismo
comando de despliegue**.

**Comprobar los roles concedidos:**

```bash
gcloud projects get-iam-policy marketplace-509503 \
  --flatten="bindings[].members" \
  --filter="bindings.members:${SA}" \
  --format="table(bindings.role)"
```

**Alternativa: un solo comando, pero con permisos más amplios.** Concede el rol Editor a esa cuenta.
Funciona para un proyecto personal, pero es menos limpio y deja la cuenta con permisos de más:

```bash
gcloud projects add-iam-policy-binding marketplace-509503 \
  --member="serviceAccount:805790031718-compute@developer.gserviceaccount.com" \
  --role="roles/editor" --condition=None
```

### `password authentication failed for user "postgres"` (SQLState 28P01)

**Es el segundo fallo más habitual** y significa que la contraseña llegó mal, no que la base esté mal
configurada. En los logs se ve así:

```
The following 1 profile is active: "cloud"        <- el perfil SÍ llegó bien
MarketplaceCloudSqlPool - Starting...
FATAL: password authentication failed for user "postgres"
SQL Error: 0, SQLState: 28P01
```

**Buena noticia:** que el error sea de *autenticación* y no de *timeout* demuestra que Cloud Run
**sí alcanza** Cloud SQL. La red, el firewall, la imagen y el perfil están correctos; solo falta la
contraseña.

Suele ocurrir al dejar el marcador `TU_PASSWORD` del comando, o al copiar con un espacio o una
comilla de más.

**Ver qué valor quedó configurado:**

```bash
gcloud run services describe marketplace-api --region us-central1 \
  --format='yaml(spec.template.spec.containers[0].env)'
```

**Corregirlo SIN volver a compilar** (reutiliza la imagen ya publicada, tarda segundos):

```bash
gcloud run services update marketplace-api \
  --region us-central1 \
  --update-env-vars DB_PASSWORD='LA_CONTRASENA_CORRECTA'
```

> Se usa `--update-env-vars` y no `--set-env-vars`, para **conservar** las demás variables y cambiar
> solo esa.

**Alternativa más segura para no equivocarse:** desplegar con `--env-vars-file env.yaml`, donde la
contraseña se escribe una sola vez y no hay comas ni comillas que escapar.

> Un `BOOTSTRAP_ADMIN_PASSWORD` equivocado **no** impide el arranque: el administrador ya existe en
> la base y el bootstrap solo crea usuarios que faltan.

### Ver los logs

Consola → **Cloud Run** → `marketplace-api` → pestaña **Registros**. Ahí ves los errores de arranque
de Spring Boot. O por comando:

```bash
gcloud run services logs read marketplace-api --region us-central1 --limit 50
```

---

## Coste y limpieza

| Recurso | Capa gratuita | Tu caso |
|---|---|---|
| Cloud Run | 2 M peticiones/mes | Muy por debajo |
| Cloud Build | 120 min/día | ~5 min por despliegue |
| Artifact Registry | 0,5 GB | Una imagen (~250 MB) |
| **Cloud SQL** | ❌ No tiene capa gratuita | **Ya lo pagas ahora** |

**Con `--min-instances 0` el servicio se apaga solo cuando nadie lo usa**, así que no hay coste fijo.

### Si quieres borrarlo todo al terminar

```bash
gcloud run services delete marketplace-api --region us-central1
gcloud artifacts repositories delete cloud-run-source-deploy --location us-central1
```

Cloud SQL déjalo: lo necesitas para la demo local.

---

## Checklist de verificación

- [ ] Facturación activa en `marketplace-509503`
- [ ] Las 3 APIs habilitadas (`run`, `artifactregistry`, `cloudbuild`)
- [ ] El despliegue terminó con `Service URL: https://...`
- [ ] `https://TU-URL/actuator/health` devuelve `{"status":"UP"}`
- [ ] `https://TU-URL/api/v1/products` devuelve los productos (¡la base compartida!)
- [ ] `https://TU-URL/swagger-ui.html` carga con las 31 operaciones
- [ ] Login del admin por la nube:
      ```bash
      curl -X POST https://TU-URL/api/v1/auth/login \
        -H 'Content-Type: application/json' \
        -d '{"email":"admin@marketplace.com","password":"Admin123."}'
      ```
      Debe devolver `accessToken`
- [ ] En Postman, `baseUrl` apunta a la URL de Cloud Run y las peticiones pasan
- [ ] `DB_POOL_SIZE=5` y `--max-instances 2` para no agotar las conexiones de Cloud SQL

---

## Lo que este despliegue NO resuelve

| Pendiente | Detalle |
|---|---|
| **Las imágenes de producto** | Hoy se guardan en el disco del frontend, no en la nube. Con el backend en Cloud Run siguen siendo locales. La solución correcta es **Cloud Storage** |
| **El panel web** | Sigue corriendo en un PC (con `MARKETPLACE_API_URL` apuntando a la nube). Desplegarlo es otro servicio aparte |
| **Los secretos** | Van como variables de entorno, visibles para quien tenga acceso al proyecto. Lo correcto es **Secret Manager** |
| **La base abierta** | `0.0.0.0/0` sigue puesto. Se puede quitar al usar el conector de Cloud SQL |
| **`ddl-auto: update`** | En un entorno real debería ser `validate` + migraciones (Flyway/Liquibase) |

---

## Resumen: los 4 comandos

```bash
# 1. Proyecto y APIs (una sola vez)
gcloud config set project marketplace-509503
gcloud services enable run.googleapis.com artifactregistry.googleapis.com cloudbuild.googleapis.com

# 2. Código
git clone https://github.com/juanriano8/Marketplace.git && cd Marketplace/backend

# 3. Desplegar (cambia DB_PASSWORD)
gcloud run deploy marketplace-api --source . --region us-central1 \
  --allow-unauthenticated --memory 1Gi --cpu 1 --cpu-boost \
  --min-instances 0 --max-instances 2 --port 8080 \
  --set-env-vars "SPRING_PROFILES_ACTIVE=cloud,DB_HOST=136.112.91.42,DB_PORT=5432,DB_NAME=marketplace_db,DB_USER=postgres,DB_SSLMODE=require,DB_POOL_SIZE=5,DB_POOL_MIN_IDLE=1,JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970,JWT_EXPIRATION_MS=86400000,BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com,BOOTSTRAP_ADMIN_PASSWORD=TU_PASSWORD"

# 4. Comprobar
curl -s "$(gcloud run services describe marketplace-api --region us-central1 --format='value(status.url)')/actuator/health"
```

**Todo esto se ejecuta en Cloud Shell, dentro del navegador.** No hay que instalar nada en ningún PC.

---

## Referencias

- [Implementar servicios desde el código fuente](https://docs.cloud.google.com/run/docs/deploying-source-code)
- [Conectar desde Cloud Run (Cloud SQL)](https://docs.cloud.google.com/sql/docs/postgres/connect-run)
- [Implementar en Cloud Run con Cloud Build](https://docs.cloud.google.com/build/docs/deploying-builds/deploy-cloud-run)
- [Variables de entorno en Cloud Run](https://docs.cloud.google.com/run/docs/configuring/services/environment-variables)
