# Guía de Entrega: cómo levantar el proyecto

**Marketplace Multi-Rol** · Backend Spring Boot 3.3.4 (Java 21) + Frontend Next.js 16 + PostgreSQL
en Google Cloud SQL.

Esta es la guía definitiva para poner en marcha el proyecto. Si sólo vas a leer un documento,
es este.

| Documento | Para qué |
|---|---|
| **GUIA_ENTREGA.md** (este) | Levantar backend y frontend |
| [POSTMAN_DEMO_PASO_A_PASO.md](POSTMAN_DEMO_PASO_A_PASO.md) | Demostrar los endpoints uno por uno |
| [ESPECIFICACION_TECNICA.md](ESPECIFICACION_TECNICA.md) | Arquitectura, modelo de datos, seguridad |
| [frontend/README.md](frontend/README.md) | Detalle del panel web |
| [PENDIENTES.md](PENDIENTES.md) | Qué queda por hacer |

---

## Índice

1. [Antes que nada: autoriza tu IP](#1-antes-que-nada-autoriza-tu-ip)
2. [Requisitos](#2-requisitos)
3. [Paso 1: configurar las credenciales](#paso-1-configurar-las-credenciales)
4. [Paso 2: levantar el backend](#paso-2-levantar-el-backend)
5. [Paso 3: levantar el frontend](#paso-3-levantar-el-frontend)
6. [Paso 4: verificar que todo funciona](#paso-4-verificar-que-todo-funciona)
7. [Checklist antes de presentar](#checklist-antes-de-presentar)
8. [Solución de problemas](#solución-de-problemas)
9. [Comandos de referencia](#comandos-de-referencia)

---

## 1. Antes que nada: autoriza tu IP

> **Este es el paso que se olvida y el que rompe la demo.**

La conexión a la base de datos es directa por **IP pública**, así que hay que autorizar la IP del
equipo en Cloud SQL. **Si tu IP cambia, el backend no arranca.**

Esto ya pasó una vez durante el desarrollo:

| Fecha | IP pública | Resultado |
|---|---|---|
| 23-sep | `186.28.26.68` | Conexión correcta |
| 24-sep | `186.31.165.104` | **Falló** |

Y el error que aparece en el log es:

```
Caused by: java.net.SocketTimeoutException: Connect timed out
Caused by: org.postgresql.util.PSQLException: El intento de conexión falló.
```

**Hazlo siempre antes de presentar.** Son 2 minutos:

1. Mira tu IP pública en <https://whatismyipaddress.com/>
2. Entra a <https://console.cloud.google.com/sql>
3. Abre la instancia **`marketplace`**
4. Menú lateral: **Connections**, luego **Networking**, luego **Authorized networks**, botón
   **Add network**
5. Pega tu IP (o `0.0.0.0/0` para permitir cualquiera: más cómodo, menos seguro)
6. **Save** y espera un minuto

Comprueba que funcionó. Debe responder `True`:

```powershell
Test-NetConnection -ComputerName 136.112.91.42 -Port 5432 -InformationLevel Quiet
```

> **Si no tienes acceso a Google Cloud** el día de la presentación, usa PostgreSQL local con Docker:
> `docker compose up -d` dentro de `backend/` y arranca con `.\run.ps1 -Profile local`. Todo
> funciona igual, sólo cambia la base de datos.

---

## 2. Requisitos

| Requisito | Versión | Cómo comprobarlo | Si falta |
|---|---|---|---|
| **JDK 21** | 21.x | Que exista `C:\Program Files\Eclipse Adoptium\jdk-21*` | `winget install EclipseAdoptium.Temurin.21.JDK` |
| **Node.js** | 20 o superior | `node -v` | <https://nodejs.org> |
| **Acceso a la base** | — | `Test-NetConnection 136.112.91.42 -Port 5432` | Ver [sección 1](#1-antes-que-nada-autoriza-tu-ip) |

> **Java 8 no sirve.** Spring Boot 3 no arranca con esa versión. El proyecto exige la 21
> (`build.gradle`, bloque `toolchain`). Gradle 8.10.2 y las dependencias ya están en la caché
> local, así que no hay que descargarlas.

---

## Paso 1: configurar las credenciales

Las credenciales viven en `backend/.env`, que **no está en Git** porque contiene la contraseña de
la base de datos.

### Si el archivo ya existe (tu PC actual)

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
Test-Path .env      # debe decir True
```

Si dice `True`, salta al paso 2.

### Si es un PC nuevo

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
powershell -ExecutionPolicy Bypass -File .\setup.ps1
notepad .env        # rellena DB_PASSWORD con la contraseña de la base
```

El script comprueba el JDK, crea el `.env` con el host y la base correctos, y prueba la conexión.

### Contenido esperado del `.env`

```ini
DB_HOST=136.112.91.42
DB_PORT=5432
DB_NAME=marketplace_db
DB_USER=postgres
DB_PASSWORD=<la contraseña de la base>
DB_SSLMODE=require
SPRING_PROFILES_ACTIVE=cloud
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com
BOOTSTRAP_ADMIN_PASSWORD=<la contraseña del admin>
```

> **No apuntes `DB_NAME` a la base `postgres`**: esa contiene 23 tablas de otro proyecto con claves
> primarias `bigint`, incompatibles con este modelo. Usa siempre `marketplace_db`.

---

## Paso 2: levantar el backend

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1
```

Si PowerShell bloquea el script:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

### Qué debe aparecer

Tarda entre **12 y 20 segundos**. Estas son las líneas que confirman que está bien:

```
The following 1 profile is active: "cloud"
MarketplaceCloudSqlPool - Added connection org.postgresql.jdbc.PgConnection@...
MarketplaceCloudSqlPool - Start completed.              <-- CONEXIÓN A CLOUD SQL OK
Initialized JPA EntityManagerFactory for persistence unit 'default'
Tomcat started on port 8080 (http)
Started MarketplaceApplication in 12.25 seconds         <-- ARRANCÓ CORRECTAMENTE
```

La primera vez que arranca, además, crea el administrador:

```
Bootstrap administrator created for admin@marketplace.com
```

En arranques posteriores dirá `Bootstrap admin already present`, que también es correcto.

### Comprobar que responde

En **otra** terminal:

```powershell
curl.exe http://localhost:8080/actuator/health
# {"status":"UP"}
```

| Recurso | URL |
|---|---|
| **Swagger UI** | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |

> **Deja esta terminal abierta.** El backend se detiene al cerrarla o al pulsar `Ctrl+C`.

---

## Paso 3: levantar el frontend

En una **segunda terminal**:

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm install      # sólo la primera vez, tarda 1 o 2 minutos
npm run dev
```

### Qué debe aparecer

```
▲ Next.js 16.3.6
- Local:  http://localhost:3000
✓ Ready in 3.4s
```

Abre **<http://localhost:3000>**

### Si `npm run dev` falla con `Error: spawn EPERM`

Ocurre en equipos con antivirus o políticas que bloquean la creación de procesos hijos. Usa el
modo sin `fork` que viene incluido:

```powershell
npm run dev:nofork
```

### Qué verás en el navegador

1. **Portada** con las tarjetas de los tres roles
2. Botón **Entrar** (arriba a la derecha), que lleva al login
3. Al iniciar sesión te lleva al panel de tu rol:

| Rol | Panel |
|---|---|
| Comprador | Catálogo, carrito, checkout, mis compras |
| Vendedor | Mi catálogo, inventario, despachos |
| Administrador | Moderación, vendedores, órdenes, auditoría |

> El frontend **no habla directamente con el puerto 8080**: Next reenvía `/api/v1/**` al backend
> desde el servidor, así que no hay problemas de CORS. Si el backend está apagado, las páginas
> cargan pero no muestran datos.

---

## Paso 4: verificar que todo funciona

### Comprobación rápida (recomendada)

Con el backend arrancado:

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

Debe terminar con:

```
 TODO CORRECTO - el proyecto está listo para la demostración
```

Comprueba el JDK, los 114 tests, que la API responde, los endpoints públicos, la seguridad
(401, 403, 400), la conexión a la base y el login del administrador.

### Comprobación completa de los endpoints

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\e2e-test.ps1
```

Recorre los 27 endpoints en orden funcional y muestra **39 OK / 0 FALLOS**.

### Las tres URLs

| Qué | URL | Esperado |
|---|---|---|
| Panel web | <http://localhost:3000> | Portada con los tres roles |
| Swagger UI | <http://localhost:8080/swagger-ui.html> | Lista de 31 operaciones |
| Health | <http://localhost:8080/actuator/health> | `{"status":"UP"}` |

---

## Checklist antes de presentar

Ejecútalo en orden el mismo día:

- [ ] **IP autorizada** en Cloud SQL ([sección 1](#1-antes-que-nada-autoriza-tu-ip)):
      `Test-NetConnection 136.112.91.42 -Port 5432` responde `True`
- [ ] **Backend arrancado** con `.\run.ps1` y aparece `Started MarketplaceApplication`
- [ ] **Health responde**: `curl.exe http://localhost:8080/actuator/health` da `{"status":"UP"}`
- [ ] **Frontend arrancado** con `npm run dev` y aparece `Ready`
- [ ] **Login del admin funciona** en <http://localhost:3000/login> con las credenciales del `.env`
- [ ] **Hay productos en el catálogo**: abre <http://localhost:3000/productos>
- [ ] **Postman preparado**: colección importada y `baseUrl` correcta
      (ver [POSTMAN_DEMO_PASO_A_PASO.md](POSTMAN_DEMO_PASO_A_PASO.md))
- [ ] **Puerto 8080 libre** antes de arrancar
- [ ] **PostgreSQL local como respaldo** (opcional): `docker compose up -d` y probar
      `.\run.ps1 -Profile local`

### Los tres datos que debes tener a mano

| Dato | Valor |
|---|---|
| Admin | `admin@marketplace.com` más la contraseña del `.env` |
| Backend | <http://localhost:8080> |
| Frontend | <http://localhost:3000> |

---

## Solución de problemas

### El backend no arranca

| Mensaje en el log | Causa | Solución |
|---|---|---|
| `SocketTimeoutException: Connect timed out` | **Tu IP cambió** y Cloud SQL no la autoriza | [Autoriza la IP](#1-antes-que-nada-autoriza-tu-ip) |
| `no pg_hba.conf entry for host "x.x.x.x"` | El mismo caso | Igual |
| `FATAL: password authentication failed` | Contraseña incorrecta en el `.env` | Revísala en Cloud SQL, sección Users |
| `FATAL: database "marketplace_db" does not exist` | Falta la base | Cloud SQL, sección Databases, Create database |
| `Port 8080 was already in use` | Otro proceso ocupa el puerto | `.\run.ps1 -Port 9090` o cerrar el otro proceso |
| `JAVA_HOME no apunta a un JDK válido` | No hay JDK 21 | `winget install EclipseAdoptium.Temurin.21.JDK` |
| Todo el log es un `stacktrace` de Hibernate | No hay conexión a la base | Mira la primera línea `Caused by:` |

**Ver quién ocupa el puerto 8080:**

```powershell
Get-NetTCPConnection -LocalPort 8080 -State Listen |
  Select-Object OwningProcess |
  ForEach-Object { Get-Process -Id $_.OwningProcess }
```

### El frontend no arranca o no muestra datos

| Síntoma | Causa | Solución |
|---|---|---|
| `Error: spawn EPERM` | El sistema bloquea procesos hijos | `npm run dev:nofork` |
| Las páginas cargan vacías | El backend está apagado | Arráncalo (paso 2) y recarga |
| `401 Unauthorized` en todo | Sesión caducada (24 h) | Cierra sesión y vuelve a entrar |
| El catálogo no muestra nada | No hay productos **aprobados** | Admin, Moderación, aprueba alguno |
| El puerto 3000 está ocupado | Otro proceso | `npm run dev -- -p 3001` |
| `Cannot find module 'next'` | Faltan dependencias | `npm install` |

### El login del administrador da `401`

Cambiaste `BOOTSTRAP_ADMIN_PASSWORD` en el `.env` **después** del primer arranque. El
administrador se crea una sola vez, así que la base conserva el hash anterior.

Solución rápida: borra la fila y reinicia, para que se recree con la contraseña actual.

```sql
DELETE FROM users WHERE email = 'admin@marketplace.com';
```

La alternativa (sincronizar el hash sin borrar nada) está explicada en
[EJECUCION_Y_DEMO.md](EJECUCION_Y_DEMO.md), apartado 10.3.1.

---

## Comandos de referencia

```powershell
# ---------- Ubicación ----------
cd C:\Users\sebas\Desktop\marketplace\fullstack

# ---------- BACKEND ----------
cd backend
.\run.ps1                       # arrancar (perfil cloud, lee .env)
.\run.ps1 -Profile local        # arrancar con PostgreSQL local (Docker)
.\run.ps1 -Port 9090            # otro puerto
.\run.ps1 -Test                 # compilar y ejecutar los 114 tests
.\gradlew.bat clean build       # build completo y JAR

# ---------- FRONTEND ----------
cd ..\frontend
npm install                     # primera vez
npm run dev                     # desarrollo, puerto 3000
npm run dev:nofork              # si aparece spawn EPERM
npm run build; npm start        # producción
npm run typecheck               # sólo comprobar tipos

# ---------- BASE DE DATOS LOCAL (respaldo) ----------
cd ..\backend
docker compose up -d            # PostgreSQL en el puerto 5432
docker compose ps               # estado
docker compose down             # parar

# ---------- VERIFICACIÓN ----------
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1      # comprobación rápida
powershell -ExecutionPolicy Bypass -File .\tools\e2e-test.ps1    # 27 endpoints, 39 aserciones
powershell -ExecutionPolicy Bypass -File .\tools\stock-test.ps1  # antisobreventa
powershell -ExecutionPolicy Bypass -File .\tools\split-test.ps1  # orden multi-vendedor y BOLA

# ---------- COMPROBACIONES RÁPIDAS ----------
curl.exe http://localhost:8080/actuator/health                   # {"status":"UP"}
curl.exe "http://localhost:8080/api/v1/products?page=0&size=5"   # catálogo público
curl.exe -i http://localhost:8080/api/v1/admin/orders            # debe dar 401
Test-NetConnection -ComputerName 136.112.91.42 -Port 5432        # conectividad a Cloud SQL
Start-Process http://localhost:3000                              # abrir el panel web
Start-Process http://localhost:8080/swagger-ui.html              # abrir Swagger
```

---

## Resumen en 4 pasos

```powershell
# 1. Autoriza tu IP en Cloud SQL
#    console.cloud.google.com/sql -> marketplace -> Connections -> Authorized networks

# 2. Backend (terminal 1)
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1

# 3. Frontend (terminal 2)
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev

# 4. Abre http://localhost:3000 y entra con admin@marketplace.com
```

Ya puedes seguir con **[POSTMAN_DEMO_PASO_A_PASO.md](POSTMAN_DEMO_PASO_A_PASO.md)** para demostrar
los endpoints uno por uno.
