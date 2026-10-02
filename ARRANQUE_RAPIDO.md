# Arranque rápido: encender y apagar todo

**Fecha:** 28 de septiembre de 2026
Guía de una página, para Windows y Linux.

---

## ⚠️ Lo que cuesta dinero (léelo antes de apagar nada)

| Recurso | ¿Cuesta créditos? | Detalle |
|---|---|---|
| **Cloud SQL** (la base de datos) | 🔴 **SÍ, siempre** | Es el **único coste real**. Corre 24 h y **no tiene capa gratuita** |
| Cloud Run (el backend) | 🟢 **No, en reposo** | Con `min-instances 0` solo pagas por petición. Sin tráfico = **0** |
| Artifact Registry (la imagen) | 🟢 No | ~250 MB, dentro de los 0,5 GB gratuitos |
| Cloud Build | 🟢 No | 120 min/día gratis; cada despliegue usa ~5 |

> **Conclusión: borrar el servicio de Cloud Run NO te ahorra dinero. Lo que hay que apagar es
> Cloud SQL.**

---

## ✅ ENCENDER (3 pasos)

### Paso 1 — Encender la base de datos

**Solo si la apagaste antes.** Si nunca la has apagado, sáltate este paso.

Por comando:

```bash
gcloud sql instances patch marketplace --activation-policy ALWAYS
```

Por consola: **Cloud SQL** → instancia `marketplace` → botón **Iniciar** (*Start*).

⏱️ Tarda **2-3 minutos**. Espera a que el estado sea **RUNNABLE**.

### Paso 2 — Calentar el backend

No hay que arrancar nada: el backend vive en Google. Pero si lleva un rato sin uso, la primera
petición tarda **~25 segundos** (arranque en frío).

Abre esto en el navegador y espera a que responda:

```
https://marketplace-api-805790031718.us-central1.run.app/actuator/health
```

Cuando veas `{"status":"UP"}`, ya está caliente y las siguientes van en milisegundos.

> **Para que no haya espera nunca**, durante la presentación:
> ```bash
> gcloud run services update marketplace-api --region us-central1 --min-instances 1
> ```
> Y al terminar: `--min-instances 0`

### Paso 3 — Arrancar el panel web

Una sola terminal. **El backend no se arranca.**

**Windows (PowerShell):**

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

**Linux / macOS:**

```bash
cd ~/marketplace-fullstack/frontend
npm run dev
```

Luego abre **<http://localhost:3000>**

> El panel ya está configurado para hablar con Cloud Run mediante `frontend/.env.local`.
> No hay que definir ninguna variable ni arrancar el backend local.

---

## 🌐 Páginas para verificar los endpoints

### Con el backend en Google Cloud (recomendado)

Funcionan desde **cualquier PC y cualquier red**, incluido el PC con Linux. No dependen de tu red
local ni de que tengas algo instalado.

| Página | URL | Qué debe mostrar |
|---|---|---|
| **Health** | `https://marketplace-api-805790031718.us-central1.run.app/actuator/health` | `{"status":"UP"}` |
| **Swagger UI** | `https://marketplace-api-805790031718.us-central1.run.app/swagger-ui.html` | Las **31 operaciones**, con el botón *Authorize* |
| **OpenAPI JSON** | `https://marketplace-api-805790031718.us-central1.run.app/v3/api-docs` | El contrato completo de la API |
| **Catálogo (público)** | `https://marketplace-api-805790031718.us-central1.run.app/api/v1/products` | La lista de productos en JSON |

> **Desde el PC Linux son exactamente las mismas URLs.** No hay que cambiar nada: son públicas y no
> dependen de la red. Para probar endpoints protegidos desde Swagger: **Authorize** → pega el token
> que devuelve el login.

### Comprobar desde la terminal

**Windows (PowerShell):**

```powershell
curl.exe -s https://marketplace-api-805790031718.us-central1.run.app/actuator/health
```

**Linux / macOS:**

```bash
curl -s https://marketplace-api-805790031718.us-central1.run.app/actuator/health
```

**Login del administrador** (igual en las dos plataformas; solo cambia `curl.exe` por `curl`):

```bash
curl -s -X POST https://marketplace-api-805790031718.us-central1.run.app/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@marketplace.com","password":"Admin123."}'
```

### Con el backend LOCAL (solo si lo arrancaste en tu PC)

| Página | URL |
|---|---|
| Health | <http://localhost:8080/actuator/health> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |

### Páginas del panel web

| Página | URL | Para qué |
|---|---|---|
| Portada | <http://localhost:3000> | Los tres roles |
| Catálogo | <http://localhost:3000/productos> | Ver los productos (es público) |
| Login | <http://localhost:3000/login> | Entrar con cualquier cuenta |
| Carrito | <http://localhost:3000/carrito> | Requiere sesión de comprador |
| Panel vendedor | <http://localhost:3000/vendedor> | Publicar y gestionar productos |
| Panel admin | <http://localhost:3000/admin> | Aprobar productos y verificar vendedores |

> **Usa `localhost`, no la IP de red.** Si abres el panel con la URL que Next muestra como
> `- Network:` verás la página "cargando" para siempre, porque Next bloquea el JavaScript desde
> otros orígenes. Si necesitas entrar por la IP, añádela a `allowedDevOrigins` en
> `frontend/next.config.ts`.

### ⚠️ Si el backend devuelve 503 y la base no responde

Un `503 Service Unavailable` en Cloud Run casi siempre significa que **el contenedor no pudo
arrancar porque no alcanza la base de datos**. Comprueba en este orden:

1. **¿Está encendida?** Cloud SQL → instancia `marketplace` → el estado debe ser **RUNNABLE**.
   Justo después de encenderla tarda **2-3 minutos**; hasta entonces no acepta conexiones.
2. **¿Cambió la IP?** Cloud SQL → `marketplace` → **Conexiones** → *IP pública*. Al detener y volver
   a iniciar una instancia, la IP pública **puede cambiar**. Si ya no es `136.112.91.42`, hay que
   actualizarla en **tres sitios**:
   - En Cloud Run:
     ```bash
     gcloud run services update marketplace-api --region us-central1 \
       --update-env-vars DB_HOST=LA_NUEVA_IP
     ```
   - En el `backend/.env` de este PC (`DB_HOST=`)
   - En el `backend/.env` del PC con Linux
3. **🔴 ¿Sigue autorizada la red `0.0.0.0/0`?** Este es el fallo silencioso más frecuente. Si
   quitaste esa red de **Authorized networks** (era la recomendación de seguridad al terminar),
   **Cloud Run ya no puede conectar a la base** y verás exactamente este 503.
   - Cloud SQL → `marketplace` → **Conexiones** → **Redes autorizadas** → debe estar `0.0.0.0/0`
   - **Mientras uses Cloud Run hay que dejarla**: la IP de salida de Cloud Run es dinámica, así que
     no se puede autorizar una IP fija. La alternativa correcta es el conector de Cloud SQL
     (ver [DESPLIEGUE_CLOUD_RUN.md](DESPLIEGUE_CLOUD_RUN.md)), que no necesita IP pública.
4. **Comprueba la conexión** desde Cloud Shell: `nc -zv LA_IP 5432`. Si no conecta, el problema es
   la base o el firewall, no el backend.

> Para diagnosticar sin adivinar: `gcloud run services logs read marketplace-api --region
> us-central1 --limit 30`. Si ves `SocketTimeoutException` o `Connection refused`, es lo de arriba.
> Si ves `password authentication failed`, es la contraseña.

---

## 🛑 APAGAR (para no gastar créditos)

### Opción 1 — Apagar solo la base de datos (recomendado)

Esto es lo único que te ahorra dinero de verdad.

```bash
gcloud sql instances patch marketplace --activation-policy NEVER
```

Por consola: **Cloud SQL** → instancia `marketplace` → botón **Detener** (*Stop*).

✅ Los datos **se conservan**. Puedes volver a encenderla cuando quieras.
💰 Ahorras el cómputo (la mayor parte). Queda un coste mínimo de **almacenamiento** (~1-2 USD/mes).

> Cloud Run y el panel dejarán de funcionar mientras la base esté apagada. Es lo esperado.

### Opción 2 — Apagado total (si terminas el proyecto)

```bash
# 1. Borrar el servicio de Cloud Run
gcloud run services delete marketplace-api --region us-central1

# 2. Borrar la imagen del registro (opcional)
gcloud artifacts repositories delete cloud-run-source-deploy --location us-central1

# 3. Apagar la base de datos (o borrarla, pero eso SÍ pierde los datos)
gcloud sql instances patch marketplace --activation-policy NEVER
```

Por consola: **Cloud Run** → `marketplace-api` → **Eliminar**.

⚠️ Si borras el servicio, para volver a tenerlo hay que **redesplegar** (3-5 min). El proceso está en
[DESPLIEGUE_CLOUD_RUN.md](DESPLIEGUE_CLOUD_RUN.md).

> **Recomendación:** no borres el servicio. No cuesta nada en reposo, y así encenderlo mañana es
> instantáneo. Apaga solo la base de datos.

---

## 📋 Comandos por sistema operativo

| Tarea | Windows (PowerShell) | Linux / macOS |
|---|---|---|
| Ir al panel | `cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend` | `cd ~/marketplace-fullstack/frontend` |
| Arrancar el panel | `npm run dev` | `npm run dev` |
| Backend **local** (opcional) | `cd ..\backend; .\run.ps1` | `cd ../backend && ./run.sh` |
| Verificación del backend local | `.\tools\verify.ps1` | `./tools/verify.sh` |
| Ver si el puerto está ocupado | `netstat -ano \| findstr :3000` | `ss -ltnp \| grep :3000` |
| Abrir un archivo de config | `notepad .env.local` | `nano .env.local` |
| **Encender la base** | `gcloud sql instances patch marketplace --activation-policy ALWAYS` | *(igual)* |
| **Apagar la base** | `gcloud sql instances patch marketplace --activation-policy NEVER` | *(igual)* |

> Los comandos de `gcloud` se ejecutan en **Cloud Shell** (el icono `>_` de la consola de Google) o
> en tu terminal si tienes el SDK instalado. Son idénticos en los dos sistemas.

---

## 🔧 Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| El catálogo se queda "cargando" y ningún botón responde | Abriste el panel por la **IP de red** en vez de `localhost` | Usa <http://localhost:3000>, o añade tu IP a `allowedDevOrigins` en `frontend/next.config.ts` |
| El catálogo sale vacío o con error 500 | `frontend/.env.local` apunta a un backend apagado | Revisa que tenga la URL de Cloud Run |
| Todo tarda ~25 s la primera vez | **Arranque en frío** | Espera, o usa `--min-instances 1` |
| `{"status":"DOWN"}` o error 500 en el panel | **La base de datos está apagada** | Paso 1: `--activation-policy ALWAYS` |
| Postman da `ECONNREFUSED` | `baseUrl` sigue en `http://localhost:8080` | Pon la URL de Cloud Run (sin `:8080`) |
| Postman da `401` en el login | Contraseña del admin mal | Debe ser `Admin123.` (con punto) |
| El panel no arranca: "Another next dev server is already running" | Ya tienes un `npm run dev` abierto | Ciérralo con Ctrl+C, o usa el puerto que te indique |

---

## 📌 Datos de referencia

| Dato | Valor |
|---|---|
| **URL del backend en la nube** | `https://marketplace-api-805790031718.us-central1.run.app` |
| **Swagger UI** | `https://marketplace-api-805790031718.us-central1.run.app/swagger-ui.html` |
| **Panel web local** | <http://localhost:3000> |
| Proyecto de GCP | `marketplace-509503` |
| Instancia Cloud SQL | `marketplace` (región `us-central1`) |
| IP de la base | `136.112.91.42` (puerto 5432) |
| Base de datos | `marketplace_db` |
| Repositorio | `https://github.com/juanriano8/Marketplace` |

### Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin.prueba@marketplace.com` | `Admin123!` |
| Vendedor | `diego.herrera3@marketplace.com` | `Vendedor123!` |
| Comprador | `tomas.rodriguez1@marketplace.com` | `Comprador123!` |

Lista completa de las 16 cuentas en [USUARIOS_PRUEBA.md](USUARIOS_PRUEBA.md).

---

## Resumen en 3 líneas

```bash
# ENCENDER
gcloud sql instances patch marketplace --activation-policy ALWAYS   # base de datos
curl https://marketplace-api-805790031718.us-central1.run.app/actuator/health   # calentar
cd frontend && npm run dev                                          # panel

# APAGAR (lo único que ahorra créditos)
gcloud sql instances patch marketplace --activation-policy NEVER
```
