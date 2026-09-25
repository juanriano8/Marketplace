# Guía: ejecutar el proyecto en otro PC

Objetivo: que el proyecto **arranque y se conecte a Google Cloud SQL** desde otro computador con
otra conexión a internet, sin tocar nada en Google Cloud.

> **Ya no hay que autorizar IPs.** La instancia tiene `0.0.0.0/0` en Authorized networks, así que
> **cualquier PC y cualquier red** se conecta directamente. Ese problema quedó resuelto.

---

## Índice

1. [Qué hay que descargar](#1-qué-hay-que-descargar)
2. [Qué hay que llevar del PC actual](#2-qué-hay-que-llevar-del-pc-actual)
3. [Lo que NO hay que hacer](#3-lo-que-no-hay-que-hacer)
4. [Pasos, uno por uno](#4-pasos-uno-por-uno)
5. [El archivo .env](#5-el-archivo-env)
6. [Comprobar que funciona](#6-comprobar-que-funciona)
7. [Problemas típicos en un PC nuevo](#7-problemas-típicos-en-un-pc-nuevo)
8. [Checklist final](#8-checklist-final)

---

## 1. Qué hay que descargar

Sólo dos cosas. Ambas son gratuitas y se instalan una sola vez.

| Programa | Para qué | Cómo se instala | Obligatorio |
|---|---|---|---|
| **Java 21 (JDK)** | Ejecutar el backend | `winget install EclipseAdoptium.Temurin.21.JDK` | **Sí** |
| **Node.js 20 o superior** | Ejecutar el panel web | <https://nodejs.org> (botón LTS) | Sólo si quieres el panel |
| Git | Traer el código desde GitHub | `winget install Git.Git` | Sólo si no vas a copiar la carpeta |

### Comprobar que quedaron bien

Cierra y reabre la terminal después de instalar, y ejecuta:

```powershell
# Java 21: debe existir esta carpeta
Get-ChildItem 'C:\Program Files\Eclipse Adoptium' -Directory

# Node: debe imprimir v20 o superior
node -v
```

> **Java 8 no sirve.** Spring Boot 3 no arranca con esa versión. Si en el PC hay una versión vieja
> de Java no pasa nada, siempre que exista también la 21: el proyecto la busca solo.

---

## 2. Qué hay que llevar del PC actual

Hay **dos** cosas y la segunda es la que se olvida.

### a) El código

**Opción 1, desde GitHub** (recomendado):

```powershell
cd $env:USERPROFILE\Desktop
git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
```

**Opción 2, copiar la carpeta entera** del PC actual (por USB o red).

> Mira el aviso de la sección 4, paso 2: hay archivos recientes que puede que todavía no estén en
> GitHub.

### b) El archivo `backend\.env` — **esto no está en GitHub**

El `.env` contiene la contraseña de la base de datos, así que **está excluido de Git a propósito**.
Sin él, el proyecto no arranca.

Tienes dos formas de llevarlo:

- **Copiar el archivo** `backend\.env` del PC actual a la misma ruta en el PC nuevo (USB, o
  abrirlo y pegar el contenido)
- **Recrearlo** en el PC nuevo con `setup.ps1` y escribir la contraseña a mano

En la [sección 5](#5-el-archivo-env) tienes el contenido exacto que debe tener.

---

## 3. Lo que NO hay que hacer

Marca mentalmente estas para no perder tiempo:

| ❌ No hay que... | Por qué |
|---|---|
| Autorizar ninguna IP en Google Cloud | Ya está `0.0.0.0/0`: funciona desde cualquier red |
| Crear la base de datos | `marketplace_db` ya existe con sus 10 tablas |
| Crear el administrador | Ya existe; usa la misma contraseña del PC actual |
| Crear usuarios ni insertar datos | La base es **la misma** y ya tiene datos de las pruebas |
| Instalar PostgreSQL ni Docker | Se conecta a Cloud SQL directamente |
| Instalar Gradle | El proyecto trae el wrapper y ya se descarga solo |
| Configurar CORS en el backend | El frontend hace de puente; el navegador nunca llama al puerto 8080 |

---

## 4. Pasos, uno por uno

### Paso 0: antes de irte del PC actual

Asegúrate de que en GitHub está todo. En el PC actual:

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack
git status --porcelain
```

Si aparece algo listado, súbelo:

```powershell
git add -A
git commit -m "Guia de entrega, guia de Postman y ajustes de scripts"
git push origin main
```

> **Importante:** si no lo subes, el PC nuevo no tendrá esos archivos al clonar. Tendrás que
> copiarlos aparte o llevar la carpeta completa.

### Paso 1: instalar Java 21

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
```

Cierra y reabre la terminal.

### Paso 2: traer el código

```powershell
cd $env:USERPROFILE\Desktop
git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
```

Si prefieres copiar la carpeta del PC actual, sáltate este paso y ponla en el escritorio.

### Paso 3: instalar Node.js (sólo si quieres el panel web)

Descárgalo de <https://nodejs.org> (versión **LTS**), instálalo con las opciones por defecto, y
cierra y reabre la terminal.

### Paso 4: colocar el `.env`

**Si copiaste la carpeta completa**, el `.env` ya viene: comprueba que existe.

```powershell
cd $env:USERPROFILE\Desktop\marketplace-fullstack\backend
Test-Path .env      # debe decir True
```

**Si clonaste de GitHub**, el `.env` no existe. Créalo así:

```powershell
cd $env:USERPROFILE\Desktop\marketplace-fullstack\backend
powershell -ExecutionPolicy Bypass -File .\setup.ps1
notepad .env        # escribe la contraseña en DB_PASSWORD
```

El script comprueba Java, crea el `.env` con el host y la base correctos, y prueba la conexión.

### Paso 5: arrancar el backend

```powershell
cd $env:USERPROFILE\Desktop\marketplace-fullstack\backend
.\run.ps1
```

La primera vez tarda **más** (2 a 5 minutos): Gradle descarga dependencias por internet. Verás
progreso en pantalla. Los siguientes arranques tardan unos 15 segundos.

Debe terminar con estas líneas:

```
The following 1 profile is active: "cloud"
MarketplaceCloudSqlPool - Added connection org.postgresql.jdbc.PgConnection@...
MarketplaceCloudSqlPool - Start completed.              <-- CONECTÓ A CLOUD SQL
Tomcat started on port 8080 (http)
Started MarketplaceApplication in 12.25 seconds
```

**Deja esta terminal abierta.**

> Si PowerShell se queja del script, usa:
> `powershell -ExecutionPolicy Bypass -File .\run.ps1`

### Paso 6: arrancar el panel web (otra terminal)

```powershell
cd $env:USERPROFILE\Desktop\marketplace-fullstack\frontend
npm install      # la primera vez: 1 a 3 minutos, descarga unos 350 MB
npm run dev
```

Debe aparecer:

```
▲ Next.js 16.3.6
- Local:  http://localhost:3000
✓ Ready in 3.4s
```

Abre **<http://localhost:3000>**

---

## 5. El archivo `.env`

Debe estar en `backend\.env` con **exactamente** este contenido (sólo cambia la contraseña):

```ini
DB_HOST=136.112.91.42
DB_PORT=5432
DB_NAME=marketplace_db
DB_USER=postgres
DB_PASSWORD=<la contraseña de la base>
DB_SSLMODE=require

SPRING_PROFILES_ACTIVE=cloud
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION_MS=86400000

BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com
BOOTSTRAP_ADMIN_PASSWORD=<la contraseña del admin>
```

| Campo | De dónde sale |
|---|---|
| `DB_PASSWORD` | Es la que ya usas en el PC actual: ábrela en `backend\.env` y cópiala |
| `BOOTSTRAP_ADMIN_PASSWORD` | La misma del PC actual. **Da igual lo que pongas aquí**: el admin ya existe en la base y no se recrea |

> **No cambies `BOOTSTRAP_ADMIN_PASSWORD` pensando que cambia la contraseña del admin.** No lo
> hace: el administrador se creó una sola vez. Para entrar usa la contraseña original.

---

## 6. Comprobar que funciona

En una tercera terminal:

```powershell
cd $env:USERPROFILE\Desktop\marketplace-fullstack\backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

Debe terminar con:

```
 TODO CORRECTO - el proyecto está listo para la demostración
```

Ese script comprueba Java, la API, los endpoints públicos, la seguridad, la conexión a la base y
el login del administrador. Es la forma más rápida de saber si algo falta.

### Las tres URLs

| Qué | URL | Debe mostrar |
|---|---|---|
| Panel web | <http://localhost:3000> | Portada con los tres roles |
| Swagger UI | <http://localhost:8080/swagger-ui.html> | 31 operaciones |
| Health | <http://localhost:8080/actuator/health> | `{"status":"UP"}` |

### Comprobar sólo la base de datos

```powershell
Test-NetConnection -ComputerName 136.112.91.42 -Port 5432 -InformationLevel Quiet
# True
```

---

## 7. Problemas típicos en un PC nuevo

| Síntoma | Causa | Solución |
|---|---|---|
| `JAVA_HOME no apunta a un JDK válido` | Falta Java 21 | Paso 1, y reabre la terminal |
| `SocketTimeoutException: Connect timed out` | **La red de ese PC bloquea el puerto 5432** (típico en redes corporativas o universitarias) | Usa el Cloud SQL Auth Proxy, ver abajo |
| Tarda muchísimo la primera vez | Gradle y npm descargan dependencias | Es normal, sólo pasa la primera vez |
| `Cannot find module 'next'` | Faltan dependencias del frontend | `npm install` |
| `Port 8080 was already in use` | Otro proceso | `.\run.ps1 -Port 9090` |
| `Error: spawn EPERM` en el frontend | Antivirus o política que bloquea procesos hijos | `npm run dev:nofork` |
| El login del admin da `401` | Estás usando una contraseña distinta a la original | Pídele la contraseña al PC donde se creó el admin |
| Las páginas del panel salen vacías | El backend está apagado | Arráncalo (paso 5) |
| El catálogo no muestra productos | Los productos están en `PENDING_APPROVAL` | Entra como admin y apruébalos |

### Si la red de ese PC bloquea el puerto 5432

Algunas redes (oficinas, universidades, hoteles) sólo permiten salir por los puertos 80 y 443. Como
la conexión a Cloud SQL usa el **5432**, no funcionará. La solución es el **Cloud SQL Auth Proxy**,
que va por el 443:

```powershell
# 1. Instala Google Cloud CLI: https://cloud.google.com/sdk/docs/install
# 2. Autentícate (abre el navegador)
gcloud auth application-default login
# 3. Abre el túnel y deja esta ventana abierta
cloud-sql-proxy --port 5432 marketplace-509503:us-central1:marketplace
# 4. En backend\.env cambia sólo esta línea:
#    DB_HOST=127.0.0.1
```

---

## 8. Checklist final

- [ ] Java 21 instalado (`Get-ChildItem 'C:\Program Files\Eclipse Adoptium'` muestra `jdk-21...`)
- [ ] Código traído (clonado o copiado)
- [ ] `backend\.env` existe y tiene `DB_PASSWORD` correcta
- [ ] `Test-NetConnection 136.112.91.42 -Port 5432` responde `True`
- [ ] Backend arrancado y el log dice `Start completed` y `Started MarketplaceApplication`
- [ ] `curl.exe http://localhost:8080/actuator/health` da `{"status":"UP"}`
- [ ] (Opcional) `npm install` y `npm run dev` funcionan y <http://localhost:3000> carga
- [ ] `tools\verify.ps1` termina con `TODO CORRECTO`
- [ ] Puedo entrar como admin con `admin@marketplace.com` y la contraseña original

---

## Resumen en 4 líneas

```powershell
# 1. Java 21 (una sola vez)
winget install EclipseAdoptium.Temurin.21.JDK

# 2. Traer el código
git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
cd marketplace-fullstack\backend

# 3. Colocar el .env (copiarlo del PC actual o crearlo con .\setup.ps1)

# 4. Arrancar
.\run.ps1
```

En Google Cloud **no hay que hacer nada**: la red `0.0.0.0/0` ya permite conectarse desde
cualquier PC y cualquier conexión.
