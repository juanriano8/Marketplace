# Guía de Ejecución y Demostración

**Marketplace API — Spring Boot 3.3.4 + Java 21 + PostgreSQL (Google Cloud SQL)**
Proyecto: `backend/` · Documento técnico: [ESPECIFICACION_TECNICA.md](ESPECIFICACION_TECNICA.md)

> **✅ ESTADO ACTUAL: el proyecto está funcionando contra Cloud SQL.**
> Compila, los **114 tests pasan** y los **30 endpoints fueron probados end-to-end** contra la
> instancia real (`39 aserciones, 0 fallos`). Para volver a comprobarlo en cualquier momento:
>
> ```powershell
> cd backend
> powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
> ```
>
> Si ya está arrancado, salta directo a [Probar los endpoints en Postman](#7-probar-los-endpoints-en-postman).

---

## Índice

1. [Antes de empezar: el JDK](#1-antes-de-empezar-el-jdk)
2. [Elección de base de datos](#2-elección-de-base-de-datos)
3. [Camino A — PostgreSQL local con Docker](#3-camino-a--postgresql-local-con-docker)
4. [Camino B — Conectar a la base de datos de Google Cloud](#4-camino-b--conectar-a-la-base-de-datos-de-google-cloud)
5. [Arrancar la aplicación](#5-arrancar-la-aplicación)
6. [Comprobar que funciona](#6-comprobar-que-funciona)
7. [Probar los endpoints en Postman](#7-probar-los-endpoints-en-postman)
8. [Orden de ejecución de la demo](#8-orden-de-ejecución-de-la-demo)
9. [Resumen de endpoints](#9-resumen-de-endpoints)
10. [Solución de problemas](#10-solución-de-problemas)
11. [Comandos de referencia](#11-comandos-de-referencia)

---

## 1. Antes de empezar: el JDK

El proyecto **exige Java 21** (`build.gradle` → `JavaLanguageVersion.of(21)`). En este equipo
actualmente sólo hay **JRE 1.8**, que **no sirve** (Spring Boot 3 no arranca con Java 8 y Gradle
tampoco).

### Instalar Java 21

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Si `winget` no funciona, descarga el instalador MSI de Adoptium:
<https://adoptium.net/temurin/releases/?version=21&os=windows&arch=x64&package=jdk>

### Verificar la instalación

```powershell
# Busca el JDK instalado
Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory

# Comprueba la versión
& 'C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot\bin\java.exe' -version
```

Debe imprimir `openjdk version "21.x.x"`. Después cierra y reabre la terminal.

> **Nota:** si prefieres no tocar la instalación global, no hace falta. El script
> `run.ps1` que viene incluido busca el JDK 21 automáticamente y se lo pasa a Gradle
> (`-Dorg.gradle.java.home=...`), así que basta con que esté instalado.

---

## 2. Elección de base de datos

El proyecto tiene **dos perfiles** y puedes usar el que te convenga. Los endpoints funcionan
igual en ambos.

| | Camino A — Docker local | Camino B — Cloud SQL |
|---|---|---|
| Perfil Spring | por defecto (`local`) | `cloud` |
| Preparación | `docker compose up -d` | autorizar tu IP + crear usuario/BD |
| Requiere internet | sólo la primera vez | siempre |
| Latencia | instantánea | depende de la red |
| Riesgo en la demo | ninguno | depende de IP/credenciales |
| Datos | locales, se borran con el contenedor | persistidos en la nube |

> **Consejo para la presentación:** monta el Camino A como plan principal (es instantáneo y no
> depende de la red del aula) y ten el Camino B preparado como respaldo. Con Cloud SQL se
> necesita que la red del lugar permita salir al puerto **5432**.

---

## 3. Camino A — PostgreSQL local con Docker (recomendado para la demo)

### 3.1 Arrancar PostgreSQL

En este equipo **Docker ya está instalado** (`C:\Program Files\Docker\Docker\...`), pero el
servicio está detenido: **abre Docker Desktop una vez** y espera a que el icono deje de
animarse.

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend

# Levanta PostgreSQL 16 en el puerto 5432 con la base marketplace_db ya creada
docker compose up -d

# Comprueba que está sano (debe decir "healthy")
docker compose ps
```

Si el puerto 5432 ya estuviera ocupado por un PostgreSQL instalado en Windows, cambia el mapeo
en `docker-compose.yml` a `"5433:5432"` y arranca con `-Port`/variables apuntando a 5433.

### 3.2 Alternativa sin Docker

Si tienes PostgreSQL instalado nativamente, basta con crear la base:

```sql
CREATE DATABASE marketplace_db;
```

No hace falta crear tablas: Hibernate las crea solo al arrancar (`ddl-auto: update`). Se crean
**15 tablas** (`users`, `products`, `stock_items`, `stock_movements`, `carts`, `cart_items`,
`orders`, `sub_orders`, `order_items`, `reviews` y las de auditoría/versionado de Hibernate).

### 3.3 No hay que configurar nada más

El perfil por defecto ya apunta a `localhost:5432/marketplace_db` con usuario `postgres` y
contraseña `postgres`, que es exactamente lo que levanta `docker-compose.yml`.

---

## 4. Camino B — Conectar a la base de datos de Google Cloud

### 4.1 Estado actual: ✅ **CONECTADA Y VERIFICADA**

La conexión está funcionando. Esta es la configuración real que quedó en `backend/.env`:

```ini
DB_HOST=136.112.91.42          # IP pública de la instancia marketplace
DB_PORT=5432
DB_NAME=marketplace_db         # base creada y vacía, con las 10 tablas de la app
DB_USER=postgres
DB_PASSWORD=<tu contraseña>
DB_SSLMODE=require
SPRING_PROFILES_ACTIVE=cloud
```

Instancia: `marketplace-509503:us-central1:marketplace` · PostgreSQL 18.6

**Evidencia de que funciona** (log de arranque):

```
MarketplaceCloudSqlPool - Added connection org.postgresql.jdbc.PgConnection@2a2815cc
MarketplaceCloudSqlPool - Start completed.
Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 8080 (http)
Started MarketplaceApplication in 12.25 seconds
Bootstrap administrator created for admin@marketplace.com
```

> ⚠️ **Importante — la instancia ya tenía datos de otro proyecto.** La base `postgres` de esta
> instancia contiene 23 tablas de una aplicación anterior (`brands`, `buyer_profiles`, `coupons`,
> `user_roles`, …) con un esquema **incompatible**: todas sus claves primarias son `bigint` y las
> columnas tienen otros nombres (`users.password_hash`, `products.title`, `products.category_id`).
>
> Hibernate con `ddl-auto: update` **nunca cambia el tipo de una PK existente**, así que se creó
> una base **nueva y vacía** (`marketplace_db`) para este proyecto. **No apuntes la aplicación a
> la base `postgres`** o fallará al arrancar.

Si alguna vez necesitas rehacer la conexión desde cero, los pasos son los de §4.2 a §4.6.

### 4.2 Paso 1 — Averiguar los datos de la instancia

En **Google Cloud Console → SQL → tu instancia**:

- **Nombre de conexión**: `proyecto:region:instancia` (lo usarás sólo con el proxy)
- **IP pública** (ej. `136.112.91.42`) **o** **IP privada** (ej. `10.xxx.xxx.xxx`)
- **Usuario y contraseña** de la base

### 4.3 Paso 2 — Autorizar el acceso

Elige **una** de estas tres formas:

**Opción 1 — IP pública + autorizar tu IP (la más rápida para la demo)**

1. `Cloud SQL → tu instancia → Connections → Networking → Authorized networks → Add network`
2. Añade tu IP actual. Para verla: <https://whatismyipaddress.com/>
3. Guarda y espera ~1 minuto.

⚠️ Si tu operador te da IP dinámica, la autorización puede dejar de funcionar a mitad de la
demo. Compruébala justo antes de presentar.

**Opción 2 — Cloud SQL Auth Proxy (recomendada: no expone tu IP y va cifrada)**

```powershell
# Descarga el proxy
mkdir C:\tools -Force
Invoke-WebRequest -Uri "https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.14.1/cloud-sql-proxy.x64.exe" -OutFile C:\tools\cloud-sql-proxy.exe

# Autentícate (abre el navegador)
gcloud auth application-default login   # requiere Google Cloud CLI

# Abre el túnel: deja esta ventana abierta
C:\tools\cloud-sql-proxy.exe --port 5432 PROYECTO:REGION:INSTANCIA
```

Con esto, `DB_HOST=127.0.0.1` y el resto igual.

**Opción 3 — IP privada**: requiere que la app corra dentro de la VPC (Cloud Run con VPC
Connector, o VPN desde tu equipo). Para una demo local es la opción más compleja.

### 4.4 Paso 3 — Crear la base de datos y el usuario

En **Cloud SQL → tu instancia → Databases → Create database**:

- Nombre: `marketplace_db`

En **Users → Add user**: crea un usuario dedicado (no uses `postgres` en producción).

### 4.5 Paso 4 — Configurar el proyecto en local

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend

# Crea tu .env a partir de la plantilla
Copy-Item .env.example .env
notepad .env
```

Rellena con tus datos reales:

```ini
DB_HOST=34.xxx.xxx.xxx          # IP pública de Cloud SQL, 127.0.0.1 si usas el proxy, o IP privada
DB_PORT=5432
DB_NAME=marketplace_db
DB_USER=marketplace_app
DB_PASSWORD=tu-password-real
DB_SSLMODE=require
SPRING_PROFILES_ACTIVE=cloud
BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com
BOOTSTRAP_ADMIN_PASSWORD=Admin123!
```

### 4.6 Paso 5 — Arrancar con el perfil cloud

```powershell
.\run.ps1 -Profile cloud
```

El script lee el `.env`, localiza el JDK 21 y arranca. Si la conexión es correcta verás en el
log:

```
HikariPool-1 - Start completed.
Tomcat started on port 8080 (http)
```

y en la consola de Cloud SQL (pestaña **Monitoring**) aparecerán conexiones activas.

### 4.7 Si la conexión falla

El error más habitual es éste:

```
org.postgresql.util.PSQLException: FATAL: no pg_hba.conf entry for host "x.x.x.x"
```

Significa que **Cloud SQL rechazó tu IP**: vuelve al Paso 2 y autorízala. Ver la tabla
completa de errores en [Solución de problemas](#10-solución-de-problemas).

---

## 5. Arrancar la aplicación

### Opción 1 — Script incluido (recomendado)

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend

.\run.ps1                 # perfil cloud (lee .env) en el puerto 8080
.\run.ps1 -Profile local  # PostgreSQL local
.\run.ps1 -Port 9090      # otro puerto
.\run.ps1 -Test           # sólo compila y ejecuta los tests
```

Si PowerShell bloquea el script:

```powershell
Unblock-File .\run.ps1
# o, sólo para esta sesión:
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
```

### Opción 2 — Gradle directamente

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.x.x-hotspot"

# Local
.\gradlew.bat bootRun

# Cloud (con el .env cargado en la sesión)
$env:SPRING_PROFILES_ACTIVE = "cloud"
$env:DB_HOST = "34.xxx.xxx.xxx"
$env:DB_NAME = "marketplace_db"
$env:DB_USER = "marketplace_app"
$env:DB_PASSWORD = "tu-password"
.\gradlew.bat bootRun
```

### Opción 3 — JAR ejecutable

```powershell
.\gradlew.bat clean bootJar
java -jar build\libs\marketplace-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

### Opción 4 — Docker (igual que se desplegaría en Cloud Run)

```powershell
docker build -t marketplace-api:local .
docker run --rm -p 8080:8080 `
  -e SPRING_PROFILES_ACTIVE=cloud `
  -e DB_HOST=host.docker.internal `
  -e DB_NAME=marketplace_db -e DB_USER=postgres -e DB_PASSWORD=postgres `
  -e BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com -e BOOTSTRAP_ADMIN_PASSWORD=Admin123! `
  marketplace-api:local
```

---

## 6. Comprobar que funciona

### 6.1 URLs disponibles

| Recurso | URL |
|---|---|
| Swagger UI (probar todo desde el navegador) | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON (importable en Postman) | <http://localhost:8080/v3/api-docs> |
| Health check | <http://localhost:8080/actuator/health> |

### 6.2 Prueba rápida en la terminal

```powershell
# 1. La app responde
curl.exe http://localhost:8080/actuator/health
# -> {"status":"UP"}

# 2. Un endpoint público funciona (no necesita token)
curl.exe "http://localhost:8080/api/v1/products?page=0&size=5"
# -> {"content":[],"pageable":{...}}   (vacío al principio, es correcto)

# 3. La seguridad está activa (debe dar 401)
curl.exe -i http://localhost:8080/api/v1/admin/orders
# -> HTTP/1.1 401 Unauthorized
```

### 6.3 El administrador inicial

**No existe endpoint público para registrar administradores.** Para poder llamar a
`/api/v1/admin/**` la aplicación crea el administrador en el arranque si defines:

```ini
BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com
BOOTSTRAP_ADMIN_PASSWORD=Admin123!
```

Verás en el log:

```
WARN  Bootstrap administrator created for admin@marketplace.com — change this password before going live
```

Ya puedes hacer login con esas credenciales. Si las dejas vacías, el bootstrap no hace nada (es
lo correcto en producción).

---

## 7. Probar los endpoints en Postman

### 7.1 Importar la colección

1. Abre Postman → **Import**
2. Selecciona el archivo:
   [`backend/postman/Marketplace-API.postman_collection.json`](backend/postman/Marketplace-API.postman_collection.json)
3. Aparecerán **10 carpetas con 44 peticiones**.

### 7.2 Cómo funciona la colección

- La variable `{{baseUrl}}` apunta a `http://localhost:8080`. **Si cambias el puerto, cámbiala**
  (clic en la colección → pestaña *Variables*).
- **Los tokens y los IDs se guardan solos**: cada petición tiene un script que lee la respuesta y
  rellena `{{sellerToken}}`, `{{productId}}`, `{{subOrderId}}`, etc. No tienes que copiar nada.
- Hay una pestaña **Authorization** configurada por petición (tipo *Bearer Token*), así que no
  tienes que pegar el token a mano en cada llamada.

### 7.3 Alternativa: importar desde OpenAPI

Con la app arrancada: Postman → **Import** → pega `http://localhost:8080/v3/api-docs` →
*Import*. Esto genera también todas las rutas automáticamente.

---

## 8. Orden de ejecución de la demo

Sigue este orden: cada paso habilita el siguiente. Duración aproximada: 3 minutos.

| # | Carpeta | Petición | Qué demuestra |
|---|---|---|---|
| 1 | `00 - Health` | Health check | La app está viva |
| 2 | `02 - Admin` | **Login admin** | JWT de administrador guardado |
| 3 | `01 - Auth` | Registrar vendedor | El vendedor nace sin verificar |
| 4 | `09 - Seguridad` | Vendedor no verificado → 400 | **No puede publicar sin aprobación** |
| 5 | `02 - Admin` | Verificar vendedor | El admin lo aprueba |
| 6 | `01 - Auth` | Login vendedor | Token de vendedor |
| 7 | `04 - Seller` | Crear producto | Nace en `PENDING_APPROVAL` |
| 8 | `03 - Product` | Listar productos | **Aún no aparece** (no está aprobado) |
| 9 | `02 - Admin` | Aprobar producto | Pasa a `ACTIVE` |
| 10 | `03 - Product` | Listar productos | **Ahora sí aparece** |
| 11 | `01 - Auth` | Registrar + login comprador | Token de comprador |
| 12 | `06 - Cart` | Agregar item | Valida stock disponible |
| 13 | `09 - Seguridad` | Cantidad 9999 → 400 | **Control de stock** |
| 14 | `07 - Orders` | **Checkout** | Orden multi-vendedor + pago capturado |
| 15 | `07 - Orders` | Despachos pendientes | El vendedor ve su sub-orden |
| 16 | `07 - Orders` | Registrar despacho | Sub-orden → `SHIPPED` |
| 17 | `08 - Reviews` | Crear reseña | `verifiedPurchase: true` |
| 18 | `08 - Reviews` | Opiniones del producto | Nota media agregada |
| 19 | `05 - Inventory` | Ajustar stock | Recuento físico + auditoría |
| 20 | `05 - Inventory` | Auditoría global | Ledger completo de movimientos |
| 21 | `07 - Orders` | Todas las transacciones | Vista global de admin |
| 22 | `09 - Seguridad` | Sin token → 401 | Endpoint protegido |
| 23 | `09 - Seguridad` | Comprador → 403 | RBAC por rol |

### Pruebas concretas que conviene mencionar en la presentación

**RBAC (los 3 rechazos):**
- Sin `Authorization` en `/api/v1/admin/orders` → **401** (autenticación)
- Con token de comprador en `/api/v1/admin/orders` → **403** (autorización por rol)
- Con token de vendedor en `/api/v1/cart` → **403**

**BOLA (un usuario no puede tocar datos de otro):**
- Modificar un producto de otro vendedor con `PUT /api/v1/seller/products/{id}` → **400**
- Despachar una sub-orden de otro vendedor → **400**
- Leer una orden de otro comprador → **404**

**Validación y errores RFC 7807:**
- `POST /api/v1/auth/register/buyer` con email inválido → **400** con `invalidParams`
- Todas las respuestas de error incluyen `type`, `title`, `status`, `detail`, `timestamp`

**Reserva de stock con bloqueo pesimista:**
- Deja un producto con 3 unidades, agrega 3 al carrito de un comprador y haz checkout.
- Un segundo checkout de esas 3 unidades falla con **400** (stock insuficiente) en lugar de
  vender dos veces el mismo stock.

---

## 9. Resumen de endpoints

**23 rutas de la especificación** + **7 de apoyo** = **30 endpoints**.

| Módulo | Método | Endpoint | Rol | Códigos reales |
|---|---|---|---|---|
| Auth | POST | `/api/v1/auth/register/buyer` | Público | 201, 400 |
| Auth | POST | `/api/v1/auth/register/seller` | Público | 201, 400 |
| Auth | POST | `/api/v1/auth/login` | Público | 200, 400, 401 |
| Auth | PATCH | `/api/v1/admin/sellers/{sellerId}/verify` | ADMIN | 200, 400, 403, 404 |
| Product | GET | `/api/v1/products` | Público | 200, 400 |
| Product | GET | `/api/v1/products/{slug}` | Público | 200, 404 |
| Product | GET | `/api/v1/seller/products` | SELLER | 200, 401, 403 |
| Product | POST | `/api/v1/seller/products` | SELLER | 201, 400, 403 |
| Product | PUT | `/api/v1/seller/products/{id}` | SELLER | 200, 400, 403, 404 |
| Product | PATCH | `/api/v1/admin/products/{id}/approval` | ADMIN | 200, 400, 403, 404 |
| Inventory | GET | `/api/v1/inventory/check/{productId}` | Público | 200, 404 |
| Inventory | POST | `/api/v1/seller/inventory/adjust` | SELLER | 200, 400, 403, 404 |
| Inventory | GET | `/api/v1/admin/inventory/audit` | ADMIN | 200, 403 |
| Order | GET | `/api/v1/cart` | BUYER | 200, 403 |
| Order | POST | `/api/v1/cart/items` | BUYER | 200, 400, 403, 404 |
| Order | POST | `/api/v1/orders/checkout` | BUYER | **201**, 400, 403, 404, 409 |
| Order | GET | `/api/v1/buyer/orders` | BUYER | 200, 401, 403 |
| Order | GET | `/api/v1/seller/orders` | SELLER | 200, 403 |
| Order | PATCH | `/api/v1/seller/orders/{subOrderId}/ship` | SELLER | 200, 400, 403, 404 |
| Order | GET | `/api/v1/admin/orders` | ADMIN | 200, 403 |
| Review | POST | `/api/v1/buyer/reviews` | BUYER | 201, 400, 403 |
| Review | GET | `/api/v1/reviews/product/{productId}` | Público | 200, 404 |
| Review | DELETE | `/api/v1/admin/reviews/{reviewId}` | ADMIN | 204, 403, 404 |
| *(apoyo)* | PUT | `/api/v1/cart/items/{productId}` | BUYER | 200, 400 |
| *(apoyo)* | DELETE | `/api/v1/cart/items/{productId}` | BUYER | 200 |
| *(apoyo)* | DELETE | `/api/v1/cart` | BUYER | 200 |
| *(apoyo)* | GET | `/api/v1/buyer/orders/{orderId}` | BUYER | 200, 404 |
| *(apoyo)* | GET | `/api/v1/seller/inventory` | SELLER | 200, 403 |
| *(apoyo)* | GET | `/api/v1/admin/products` | ADMIN | 200, 403 |
| *(apoyo)* | GET | `/api/v1/admin/reviews` | ADMIN | 200, 403 |

---

## 10. Solución de problemas

### 10.1 El puerto 8080 está ocupado

```
Web server failed to start. Port 8080 was already in use.
```

```powershell
# Ver quién lo ocupa
Get-NetTCPConnection -LocalPort 8080 -State Listen | Select-Object OwningProcess
Get-Process -Id (Get-NetTCPConnection -LocalPort 8080 -State Listen).OwningProcess

# O simplemente usa otro puerto
.\run.ps1 -Port 9090
```

### 10.2 Errores de conexión a la base de datos

| Mensaje | Causa | Solución |
|---|---|---|
| `no pg_hba.conf entry for host "x.x.x.x"` | Cloud SQL rechaza tu IP | Autorízala en *Cloud SQL → Connections → Authorized networks* |
| `The connection attempt failed` / `Connection refused` | Host o puerto mal, o la app no llega a la instancia | Revisa `DB_HOST`/`DB_PORT`; si es **IP privada**, necesitas VPC Connector o el proxy |
| `FATAL: password authentication failed` | Usuario/contraseña incorrectos | Revísalos en *Cloud SQL → Users*; ojo con caracteres especiales en el `.env` |
| `FATAL: database "marketplace_db" does not exist` | Falta crear la base | *Cloud SQL → Databases → Create database* |
| `The server does not support SSL` | Instancia sin SSL y perfil `cloud` | Usa `DB_SSLMODE=disable` (sólo en pruebas) |
| `Connection is not available, request timed out` | Firewall corporativo bloquea el 5432 | Usa el Cloud SQL Auth Proxy (túnel por 443) o el Camino A |
| `relation "users" does not exist` | `ddl-auto` no es `update` | Pon `JPA_DDL_AUTO=update` y reinicia |

**Comprobar la conectividad al puerto:**

```powershell
Test-NetConnection -ComputerName 34.xxx.xxx.xxx -Port 5432 -InformationLevel Detailed
# TcpTestSucceeded : True   <- si es False, el problema es de red/firewall, no del código
```

### 10.3 `401 Unauthorized` cuando ya hiciste login

- El token caduca a las **24 h** (`JWT_EXPIRATION_MS`). Vuelve a hacer login.
- En Postman, comprueba que la petición tiene la pestaña **Authorization → Bearer Token** con la
  variable correcta (`{{buyerToken}}`, `{{sellerToken}}` o `{{adminToken}}`).
- Asegúrate de que el header empieza por `Bearer ` (con espacio).
- **Si el login del admin falla con `Invalid email or password`**, mira el punto 10.3.1.

### 10.3.1 El login del admin falla aunque la contraseña del `.env` sea correcta

**Es el error más fácil de cometer en este proyecto.** `BootstrapAdminRunner` solo crea el
administrador **si no existe**: si cambias `BOOTSTRAP_ADMIN_PASSWORD` en el `.env` después del
primer arranque, la base conserva el hash antiguo y el login devuelve `401`.

Dos soluciones:

**a) Sincronizar el hash con la contraseña del `.env`** (sin borrar datos):

```powershell
cd backend
$base = "$env:USERPROFILE\.gradle\caches\modules-2\files-2.1"
$jars = @()
$jars += (Get-ChildItem "$base\org.postgresql\postgresql" -Recurse -Filter 'postgresql-4*.jar' | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1).FullName
$jars += (Get-ChildItem "$base\org.springframework.security\spring-security-crypto" -Recurse -Filter '*.jar' | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1).FullName
$jars += (Get-ChildItem "$base\org.springframework\spring-jcl" -Recurse -Filter '*.jar' | Where-Object { $_.Name -notmatch 'sources|javadoc' } | Select-Object -First 1).FullName
$cp = ($jars | Where-Object { $_ }) -join ';'

$v = @{}
Get-Content .env | ForEach-Object { $l=$_.Trim(); if ($l -and -not $l.StartsWith('#') -and $l.Contains('=')) { $i=$l.IndexOf('='); $v[$l.Substring(0,$i).Trim()]=$l.Substring($i+1).Trim() } }

& "$env:JAVA_HOME\bin\java.exe" -cp $cp tools\AdminPasswordSync.java `
    $v['DB_HOST'] $v['DB_PORT'] $v['DB_USER'] $v['DB_PASSWORD'] marketplace_db `
    $v['BOOTSTRAP_ADMIN_EMAIL'] $v['BOOTSTRAP_ADMIN_PASSWORD']
```

Debe responder `VERIFICACION ... matches : true`. Después reinicia la app.

**b) Borrar el administrador y dejar que se recree.** En Cloud Shell:

```bash
gcloud sql connect marketplace --user=postgres --database=marketplace_db
# dentro de psql:
DELETE FROM users WHERE email = 'admin@marketplace.com';
\q
```

Al reiniciar, el bootstrap lo vuelve a crear con la contraseña actual del `.env`.

> **Evitar el problema de raíz:** define `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`
> **con el valor definitivo antes del primer arranque**, y no los cambies después.

### 10.4 `403 Forbidden`

Es lo correcto si el rol del token no corresponde: **ADMIN** para `/api/v1/admin/**`,
**SELLER** para `/api/v1/seller/**`, **BUYER** para `/api/v1/cart`, `/api/v1/buyer/**` y
`POST /api/v1/orders/checkout`. Vuelve a hacer login con el usuario del rol adecuado.

### 10.5 `400` al crear un producto siendo vendedor

Mensaje: *"Your seller account is not verified yet"*. El vendedor debe estar aprobado antes con
`PATCH /api/v1/admin/sellers/{sellerId}/verify` usando el token de admin.

### 10.6 `400` al crear una reseña

Mensaje: *"Only buyers with a dispatched or delivered order for this product can review it"*.
Necesitas: comprar el producto → que el vendedor registre el despacho
(`PATCH /api/v1/seller/orders/{subOrderId}/ship`) → y entonces ya puedes reseñar.

### 10.7 Error al compilar con Gradle

```
Could not determine the dependencies of task ':compileJava'. ... toolchain ... Java 21
```

No hay JDK 21 o `JAVA_HOME` apunta a Java 8. Vuelve al [punto 1](#1-antes-de-empezar-el-jdk).
El script `run.ps1` ya se encarga de esto si el JDK 21 está instalado en una ruta estándar.

---

## 11. Comandos de referencia

```powershell
# --- Ubicación ---
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend

# --- Base de datos local (Docker) ---
docker compose up -d            # arrancar PostgreSQL
docker compose ps               # estado
docker compose logs -f postgres # ver logs
docker compose down             # parar (conserva los datos)
docker compose down -v          # parar y borrar los datos

# --- Aplicación ---
.\run.ps1                       # arrancar (perfil cloud / .env)
.\run.ps1 -Profile local        # arrancar con PostgreSQL local
.\run.ps1 -Test                 # compilar y ejecutar tests

# --- Gradle directo ---
.\gradlew.bat clean build       # compilar todo + tests
.\gradlew.bat bootRun           # arrancar
.\gradlew.bat test              # sólo tests
.\gradlew.bat clean bootJar     # generar el JAR

# --- Comprobaciones rápidas ---
curl.exe http://localhost:8080/actuator/health
curl.exe "http://localhost:8080/api/v1/products?page=0&size=5"
curl.exe -i http://localhost:8080/api/v1/admin/orders   # debe dar 401
Start-Process http://localhost:8080/swagger-ui.html     # abrir Swagger
```

---

## Siguiente paso recomendado

1. Instala el JDK 21 (§1).
2. Levanta PostgreSQL con Docker (§3) y ejecuta `.\run.ps1 -Profile local`.
3. Importa la colección de Postman (§7) y recorre los 23 pasos de la demo (§8).
4. Sólo cuando la demo funcione en local, cambia al camino de Cloud SQL (§4).

Así separas los dos riesgos: si algo falla en la demo, sabrás si es del código (no funcionaría
tampoco en local) o de la red/credenciales de GCP (funciona en local pero no en cloud).
