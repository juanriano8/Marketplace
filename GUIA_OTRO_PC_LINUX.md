# Guía: ejecutar el proyecto en un PC con Linux

**Fecha:** 26 de septiembre de 2026

Objetivo: que el proyecto **arranque y se conecte a Google Cloud SQL** desde un computador con
Linux, sin tocar nada en Google Cloud.

> **Ya no hay que autorizar IPs.** La instancia tiene `0.0.0.0/0` en Authorized networks, así que
> **cualquier PC y cualquier red** se conecta directamente.
>
> **Para Windows existe otra guía:** [GUIA_OTRO_PC.md](GUIA_OTRO_PC.md).

---

## Índice

1. [Qué hay que instalar](#1-qué-hay-que-instalar)
2. [Qué hay que llevar del PC actual](#2-qué-hay-que-llevar-del-pc-actual)
3. [Lo que NO hay que hacer](#3-lo-que-no-hay-que-hacer)
4. [Pasos, uno por uno](#4-pasos-uno-por-uno)
5. [El archivo .env](#5-el-archivo-env)
6. [Comprobar que funciona](#6-comprobar-que-funciona)
7. [Diferencias con Windows](#7-diferencias-con-windows)
8. [Problemas típicos en Linux](#8-problemas-típicos-en-linux)
9. [Checklist final](#9-checklist-final)

---

## 1. Qué hay que instalar

Sólo dos cosas obligatorias. Elige los comandos según tu distribución.

### Java 21 (JDK) — obligatorio

| Distribución | Comando |
|---|---|
| **Ubuntu 24.04+ / Debian 13+** | `sudo apt install openjdk-21-jdk` |
| **Ubuntu 22.04 / Debian 12** | `sudo apt install temurin-21-jdk` (requiere el repositorio de Adoptium, ver abajo) |
| **Fedora / RHEL 9+** | `sudo dnf install java-21-openjdk-devel` |
| **Arch / Manjaro** | `sudo pacman -S jdk21-openjdk` |
| **openSUSE** | `sudo zypper install java-21-openjdk-devel` |
| **Cualquiera (recomendado)** | SDKMAN: `curl -s "https://get.sdkman.io" \| bash` y luego `sdk install java 21.0.5-tem` |

**Si tu Ubuntu es 22.04 o anterior**, el paquete `openjdk-21-jdk` no existe. Añade el repositorio
de Adoptium:

```bash
sudo apt install -y wget apt-transport-https gpg
wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | \
  sudo gpg --dearmor -o /etc/apt/keyrings/adoptium.gpg
echo "deb [signed-by=/etc/apt/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb $(lsb_release -cs) main" | \
  sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update && sudo apt install -y temurin-21-jdk
```

**Comprueba:**

```bash
java -version          # debe decir 21.x
echo $JAVA_HOME        # puede estar vacío; run.sh lo detecta solo
```

> **Java 17 o anterior no sirve.** Spring Boot 3 no arranca con esas versiones. Si el sistema tiene
> varias, `run.sh` busca la 21 automáticamente; si te empeñas en fijarla:
> `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`

### Node.js 20 o superior — sólo si quieres el panel web

| Método | Comando |
|---|---|
| **nvm (recomendado)** | `curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh \| bash` y luego `nvm install 22` |
| **NodeSource** | `curl -fsSL https://deb.nodesource.com/setup_22.x \| sudo -E bash - && sudo apt install -y nodejs` |
| **Fedora** | `sudo dnf install nodejs` |
| **Arch** | `sudo pacman -S nodejs npm` |

> **No instales Node con `sudo apt install nodejs` en Ubuntu 22.04**: da la versión 12, demasiado
> antigua. Usa nvm o NodeSource.
>
> **Nunca uses `sudo npm install`.** Si tienes errores de permisos, es que instalaste Node como root;
> la solución es nvm.

**Comprueba:**

```bash
node -v     # v20.x o superior
npm -v
```

### Git — sólo si vas a clonar el repositorio

```bash
sudo apt install git        # Debian/Ubuntu
sudo dnf install git        # Fedora
```

### Opcional: PowerShell 7 (para los scripts de verificación de Windows)

Los scripts `verify.ps1`, `e2e-test.ps1`, `stock-test.ps1` y `split-test.ps1` son de Windows y
**usan `curl.exe`, que no existe en Linux**, así que no funcionan ni con PowerShell 7.

**Para Linux ya existe `backend/tools/verify.sh`**, que hace lo mismo con curl y grep. No necesitas
instalar nada más.

---

## 2. Qué hay que llevar del PC actual

### a) El código

**Opción 1, desde GitHub** (recomendado, ya trae los permisos de ejecución correctos):

```bash
cd ~
git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
cd marketplace-fullstack
```

**Opción 2, copiar la carpeta** del PC actual (USB, `scp` o red compartida).

> Si copias desde Windows, los archivos `.sh` pueden venir con finales de línea de Windows (CRLF).
> Eso rompe los scripts: el error típico es `bash: ./run.sh: /bin/bash^M: bad interpreter`.
> **Arréglalo así:**
> ```bash
> sudo apt install dos2unix
> dos2unix backend/run.sh backend/tools/verify.sh backend/gradlew
> ```
> Si clonas de Git no hace falta: el archivo `.gitattributes` fuerza LF en los `.sh`.

### b) El archivo `backend/.env` — **esto no está en GitHub**

Contiene la contraseña de la base de datos, así que **está excluido de Git a propósito**. Sin él el
proyecto no arranca.

- **Copiarlo** del PC actual (USB, `scp`, o abrirlo y pegar el contenido)
- **O recrearlo** con `setup.ps1`... ojo: ese script es de Windows. En Linux créalo a mano con el
  contenido de la [sección 5](#5-el-archivo-env)

> **Importante si copias el `.env` desde Windows:** tendrá finales de línea CRLF, y entonces la
> contraseña quedaría con un `\r` al final → **login fallido y perfil `cloud\r` no reconocido**.
> `run.sh` ya limpia los CR automáticamente, pero si lo vas a leer con otras herramientas, pásale:
> ```bash
> sed -i 's/\r$//' backend/.env
> ```

---

## 3. Lo que NO hay que hacer

| ❌ No hay que... | Por qué |
|---|---|
| Autorizar ninguna IP en Google Cloud | Ya está `0.0.0.0/0`: funciona desde cualquier red |
| Crear la base de datos | `marketplace_db` ya existe con sus 10 tablas |
| Crear el administrador | Ya existe; usa la misma contraseña del PC actual |
| Crear usuarios ni insertar datos | La base es **la misma** y ya tiene los 34 usuarios |
| Instalar PostgreSQL ni Docker | Se conecta a Cloud SQL directamente |
| Instalar Gradle | El proyecto trae el wrapper (`gradlew`) |
| Configurar CORS | El frontend hace de puente; el navegador nunca llama al puerto 8080 |
| Ejecutar los scripts `.ps1` | Son de Windows; en Linux usa `verify.sh` |

---

## 4. Pasos, uno por uno

### Paso 0: antes de irte del PC actual

Sube a GitHub lo que falte. En el PC actual (Windows):

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack
git status --porcelain
```

Si sale algo, súbelo:

```powershell
git add -A
git commit -m "Guia para Linux y scripts de ejecucion"
git push origin main
```

> **Si no lo subes**, al clonar en Linux no tendrás `run.sh` con los arreglos ni `tools/verify.sh`.

### Paso 1: instalar Java 21

Ver [sección 1](#1-qué-hay-que-instalar). Comprueba con `java -version`.

### Paso 2: traer el código

```bash
cd ~
git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
cd marketplace-fullstack
```

### Paso 3: dar permisos de ejecución

Si clonaste de Git los permisos ya vienen, pero si copiaste la carpeta por USB hay que darlos:

```bash
chmod +x backend/gradlew backend/run.sh backend/tools/verify.sh
```

> `run.sh` también se auto-repara: si detecta que `gradlew` no es ejecutable, le aplica `chmod +x`
> o lo lanza con `bash`. Aun así, hazlo a mano la primera vez.

### Paso 4: colocar el `.env`

```bash
cd ~/marketplace-fullstack/backend
ls -la .env          # si existe, comprueba que la contraseña es correcta
nano .env            # si no existe, créalo con el contenido de la sección 5
```

### Paso 5: arrancar el backend

```bash
cd ~/marketplace-fullstack/backend
./run.sh
```

La primera vez tarda **2 a 5 minutos** (Gradle descarga dependencias). Después, unos 15 segundos.

Debe terminar con:

```
JAVA_HOME = /usr/lib/jvm/java-21-openjdk-amd64
Cargando variables desde /home/usuario/marketplace-fullstack/backend/.env
Arrancando Marketplace API  |  perfil=cloud  puerto=8080
...
The following 1 profile is active: "cloud"
HikariPool-1 - Start completed.          <-- CONECTÓ A CLOUD SQL
Tomcat started on port 8080 (http)
Started MarketplaceApplication in 12.25 seconds
```

**Deja esta terminal abierta.**

### Paso 6: arrancar el panel web (otra terminal)

```bash
cd ~/marketplace-fullstack/frontend
npm install          # la primera vez: 1 a 3 minutos
npm run dev
```

Debe aparecer:

```
▲ Next.js 16.3.6
- Local:  http://localhost:3000
✓ Ready in 3.4s
```

Abre **<http://localhost:3000>**

> ⚠️ **Usa `localhost`, NO la dirección "- Network: http://192.168.x.x:3000".** Next.js 16 bloquea
> por defecto las peticiones a `/_next/*` cuando el origen no es `localhost` (devuelve 403), y
> entonces el JavaScript no carga, no hay hidratación y la página se queda "cargando" para siempre.
> Si necesitas entrar por IP (por ejemplo desde el móvil), añade `allowedDevOrigins` en
> `frontend/next.config.ts`.

---

## 5. El archivo `.env`

Debe estar en `backend/.env` con **exactamente** este contenido (sólo cambia la contraseña):

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
| `DB_PASSWORD` | Ábrela en el `backend/.env` del PC actual y cópiala |
| `BOOTSTRAP_ADMIN_PASSWORD` | La misma del PC actual. **Da igual lo que pongas**: el admin ya existe y no se recrea |

**Buenas prácticas en Linux** (el archivo tiene una contraseña):

```bash
chmod 600 backend/.env          # sólo tu usuario puede leerlo

# Si lo creas con nano, no dejes espacios alrededor del '=' ni uses comillas
```

> **No uses comillas** en los valores (`DB_PASSWORD="abc"` guardaría las comillas como parte de la
> contraseña). Tampoco dejes espacios: `DB_HOST = 1.2.3.4` sería inválido.
>
> **Cuidado con `!` en la contraseña** si la escribes a mano en una terminal interactiva: bash podría
> interpretarlo como historial. Entre comillas simples no hay problema: `nano` no las interpreta.

---

## 6. Comprobar que funciona

### Verificación automática (recomendado)

```bash
cd ~/marketplace-fullstack/backend
./tools/verify.sh
```

Comprueba Java, la API, los endpoints públicos, la seguridad, la conexión a la base y el login del
administrador. Debe terminar con:

```
==================================================================
 TODO CORRECTO - el proyecto esta listo para la demostracion
==================================================================
```

### Comprobaciones manuales

```bash
# La API está viva
curl -s http://localhost:8080/actuator/health
# {"status":"UP"}

# El catálogo es público (sin token)
curl -s "http://localhost:8080/api/v1/products?size=2" | head -c 300

# La base de datos es alcanzable
(exec 3<>/dev/tcp/136.112.91.42/5432) && echo "Cloud SQL OK"

# Login del administrador
curl -s -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@marketplace.com","password":"<la del .env>"}'
```

### Las cuatro URLs

| Qué | URL | Debe mostrar |
|---|---|---|
| Panel web | <http://localhost:3000> | Portada con los tres roles |
| Swagger UI | <http://localhost:8080/swagger-ui.html> | 31 operaciones |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> | El contrato completo |
| Health | <http://localhost:8080/actuator/health> | `{"status":"UP"}` |

### Usuarios de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin.prueba@marketplace.com` | `Admin123!` |
| Vendedor | `diego.herrera3@marketplace.com` | `Vendedor123!` |
| Comprador | `tomas.rodriguez1@marketplace.com` | `Comprador123!` |

Lista completa en [USUARIOS_PRUEBA.md](USUARIOS_PRUEBA.md).

---

## 7. Diferencias con Windows

| Tarea | Windows | Linux |
|---|---|---|
| Arrancar el backend | `.\run.ps1` | `./run.sh` |
| Verificación | `tools\verify.ps1` | `tools/verify.sh` |
| Scripts de prueba | `e2e-test.ps1`, `stock-test.ps1`, `split-test.ps1` | ❌ No disponibles (usan `curl.exe`) |
| Permisos | No aplica | `chmod +x` en `gradlew`, `run.sh`, `verify.sh` |
| Finales de línea | `.ps1` en CRLF (automático) | `.sh` en LF (automático por `.gitattributes`) |
| Puerto ocupado | `netstat -ano \| findstr :8080` | `ss -ltnp \| grep :8080` |
| Abrir un archivo | `notepad .env` | `nano .env` |
| Variables de entorno | `$env:JAVA_HOME` | `$JAVA_HOME` o `export JAVA_HOME=…` |

### Docker

Si además quieres el PostgreSQL local (perfil `local`), en Linux:

```bash
sudo apt install docker.io docker-compose-plugin
sudo usermod -aG docker $USER      # y vuelve a iniciar sesión
docker compose up -d               # ojo: "docker compose", con espacio
./run.sh local
```

> En Linux **no existe Docker Desktop**: se instala `docker.io` y el plugin `docker-compose-plugin`.
> El comando moderno es `docker compose` (v2), no `docker-compose` (v1).

---

## 8. Problemas típicos en Linux

| Error | Causa | Solución |
|---|---|---|
| `bash: ./run.sh: Permission denied` | Falta el bit de ejecución | `chmod +x backend/run.sh backend/gradlew` |
| `bash: ./run.sh: /bin/bash^M: bad interpreter` | El archivo tiene finales de línea de Windows (CRLF) | `dos2unix backend/run.sh backend/gradlew` |
| `ERROR: no se encontró un JDK` | No hay Java 21 | Paso 1. Comprueba con `java -version` |
| `UnsupportedClassVersionError` | Java 17 o anterior | Instala la 21 y borra `JAVA_HOME` para que lo detecte |
| Login del administrador da `401` | El `.env` tiene CRLF y la contraseña acaba en `\r` | `sed -i 's/\r$//' backend/.env` |
| `The following 1 profile is active: "default"` | `SPRING_PROFILES_ACTIVE` mal escrito o con `\r` | Revisa esa línea del `.env` |
| `SocketTimeoutException: Connect timed out` | Tu red bloquea el puerto 5432 | Usa el Cloud SQL Auth Proxy (abajo) |
| `Port 8080 was already in use` | Otro proceso | `ss -ltnp \| grep 8080` y mátalo, o `PORT=9090 ./run.sh` |
| `EADDRINUSE` en el 3000 | Otro Next corriendo | `ss -ltnp \| grep 3000`, o `npm run dev -- -p 3001` |
| `npm: command not found` | Node sin instalar | Paso 1 (usa nvm) |
| `EACCES` al hacer `npm install` | Node instalado como root | **No uses `sudo`**: instala nvm y reinstala |
| La página se queda cargando y los botones no responden | Estás entrando por la IP de red en vez de `localhost` | Abre <http://localhost:3000> |
| `Cannot find module 'next'` | Faltan dependencias | `npm install` dentro de `frontend/` |
| `Error: spawn EPERM` en el frontend | Antivirus o política que bloquea procesos hijos | `npm run dev:nofork` |
| No puedo subir imágenes de producto | Permisos en la carpeta | `chmod -R u+w frontend/public/images` |
| Tarda muchísimo la primera vez | Gradle y npm descargan dependencias | Normal, sólo la primera vez |
| `run.sh` falla por cualquier otro motivo | — | Arranca Gradle a mano (ver abajo) |

### Salida de emergencia: arrancar sin `run.sh`

Si `run.sh` da algún problema, equivale exactamente a esto:

```bash
cd ~/marketplace-fullstack/backend

# 1. Cargar las variables del .env en la sesion
set -a; . ./.env; set +a

# 2. Localizar el JDK 21
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64    # ajusta segun tu distro

# 3. Arrancar
./gradlew -Dorg.gradle.java.home="$JAVA_HOME" bootRun
```

Para comprobar la sintaxis del script antes de ejecutarlo:

```bash
bash -n run.sh && echo "sintaxis correcta"
```

### Si tu red bloquea el puerto 5432

Algunas redes (oficinas, universidades, hoteles) sólo permiten salir por los puertos 80 y 443. Como
Cloud SQL usa el **5432**, no funcionará. Se resuelve con el **Cloud SQL Auth Proxy**, que va por el
443:

```bash
# 1. Instalar Google Cloud CLI
curl https://sdk.cloud.google.com | bash
exec -l $SHELL

# 2. Autenticarse (abre el navegador)
gcloud auth application-default login

# 3. Descargar el proxy
curl -o cloud-sql-proxy \
  https://storage.googleapis.com/cloud-sql-connectors/cloud-sql-proxy/v2.14.1/cloud-sql-proxy.linux.amd64
chmod +x cloud-sql-proxy

# 4. Abrir el tunel y dejar esta terminal abierta
./cloud-sql-proxy --port 5432 marketplace-509503:us-central1:marketplace

# 5. En backend/.env cambia SOLO esta linea:
#    DB_HOST=127.0.0.1
```

---

## 9. Checklist final

- [ ] Java 21 instalado (`java -version` dice 21.x)
- [ ] Código en `~/marketplace-fullstack`
- [ ] `chmod +x backend/gradlew backend/run.sh backend/tools/verify.sh`
- [ ] `backend/.env` existe, con permisos `600` y la contraseña correcta
- [ ] `(exec 3<>/dev/tcp/136.112.91.42/5432) && echo OK` responde `OK`
- [ ] `./run.sh` arranca y el log dice `Start completed` y `Started MarketplaceApplication`
- [ ] `curl -s http://localhost:8080/actuator/health` da `{"status":"UP"}`
- [ ] (Opcional) `npm install && npm run dev` y <http://localhost:3000> carga **por localhost**
- [ ] `./tools/verify.sh` termina con `TODO CORRECTO`
- [ ] Puedo entrar como admin con `admin@marketplace.com`

---

## Resumen en 6 líneas

```bash
# 1. Java 21 (una sola vez)
sudo apt install openjdk-21-jdk          # Ubuntu 24.04+; ver alternativas arriba

# 2. Traer el codigo
cd ~ && git clone https://github.com/juanriano8/Marketplace.git marketplace-fullstack
cd marketplace-fullstack

# 3. Permisos
chmod +x backend/gradlew backend/run.sh backend/tools/verify.sh

# 4. Colocar el .env en backend/ (copiarlo del PC actual)

# 5. Arrancar
cd backend && ./run.sh

# 6. Panel web (otra terminal)
cd ../frontend && npm install && npm run dev     # abrir http://localhost:3000
```

En Google Cloud **no hay que hacer nada**: la red `0.0.0.0/0` ya permite conectarse desde cualquier
PC y cualquier conexión.

> **Recuerda:** persiste en Git las imágenes que subas
> (`git add frontend/public/images/productos`) o no se verán en este PC.
