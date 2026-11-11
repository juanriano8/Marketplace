# Ejecutar el proyecto en local, en otro PC

> **Guía autocontenida.** El proyecto funciona **sin Google Cloud y sin Neon**: la base de
> datos es un PostgreSQL local que se rellena con un volcado incluido en el repositorio.
> Última verificación: **2026-11-11**, commit `bbf0155`.

---

## 1. Qué hace falta instalar

| Herramienta | Versión | Para qué |
|---|---|---|
| **JDK 21** | 21 (no 17, no 8) | La API es Spring Boot 3.3.4 |
| **Node.js** | 20 o superior | El panel es Next.js 16 |
| **PostgreSQL** | **16 o superior** | La base de datos |
| Git | cualquiera | Clonar el repositorio |

> **No** hace falta Docker, ni cuenta de Google, ni Neon.

### Windows

```powershell
winget install EclipseAdoptium.Temurin.21.JDK
winget install OpenJS.NodeJS.LTS
# PostgreSQL: instala el .exe de https://www.enterprisedb.com/downloads/postgres-postgresql-downloads
# (o usa el portable, ver el apartado 2.2)
```

### Linux (Ubuntu / Debian)

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk postgresql-16 git curl
curl -fsSL https://deb.nodesource.com/setup_22.x | sudo -E bash -
sudo apt install -y nodejs
```

### Linux (Fedora / Arch)

```bash
# Fedora
sudo dnf install -y java-21-openjdk-devel postgresql-server nodejs npm git
sudo postgresql-setup --initdb && sudo systemctl enable --now postgresql
# Arch
sudo pacman -S --needed jdk21-openjdk postgresql nodejs npm git
sudo -u postgres initdb -D /var/lib/postgres/data && sudo systemctl enable --now postgresql
```

---

## 2. Preparar la base de datos

Elige **una** de las tres formas. La 2.1 es la más rápida si ya instalaste PostgreSQL.

### 2.1 Con PostgreSQL instalado (Windows o Linux)

Crea la base y el usuario:

```bash
# Linux
sudo -u postgres psql -c "ALTER USER postgres PASSWORD 'postgres';"
sudo -u postgres createdb marketplace_db
```

```powershell
# Windows (psql está en C:\Program Files\PostgreSQL\16\bin)
$env:PGPASSWORD = 'postgres'
& "C:\Program Files\PostgreSQL\16\bin\createdb.exe" -U postgres -h localhost marketplace_db
```

### 2.2 Con PostgreSQL portable (sin administrador, sin servicio)

Descarga los binarios y descomprímelos:

```powershell
# Windows
$destino = "$env:LOCALAPPDATA\pg16"
New-Item -ItemType Directory -Force -Path $destino | Out-Null
curl.exe -L -o "$destino\pg.zip" https://get.enterprisedb.com/postgresql/postgresql-16.9-1-windows-x64-binaries.zip
Expand-Archive "$destino\pg.zip" -DestinationPath $destino
```

```bash
# Linux
wget -O /tmp/pg.zip https://get.enterprisedb.com/postgresql/postgresql-16.9-1-linux-x64-binaries.tar.gz
tar -xzf /tmp/pg.zip -C "$HOME"
```

Inicializa y arranca:

```powershell
# Windows
[System.IO.File]::WriteAllText("$env:LOCALAPPDATA\pg16\pw.txt", 'postgres')
& "$env:LOCALAPPDATA\pg16\pgsql\bin\initdb.exe" -D "$env:LOCALAPPDATA\pg16\data" -U postgres -A md5 "--pwfile=$env:LOCALAPPDATA\pg16\pw.txt" -E UTF8 --locale=C
& "$env:LOCALAPPDATA\pg16\pgsql\bin\pg_ctl.exe" -D "$env:LOCALAPPDATA\pg16\data" -l "$env:LOCALAPPDATA\pg16\server.log" -o '"-p 5432"' start
```

### 2.3 Con Docker (si lo tienes funcionando)

```bash
cd backend
docker compose up -d
```

Ya crea `marketplace_db` con usuario `postgres` / `postgres` en el puerto 5432.

---

## 3. Clonar y configurar

```bash
git clone https://github.com/juanriano8/Marketplace.git
cd Marketplace
```

### 3.1 `backend/.env`

Este archivo **no está en el repositorio** (contiene credenciales). Créalo copiando la plantilla:

```powershell
# Windows
cd backend
Copy-Item .env.example .env
notepad .env
```

```bash
# Linux
cd backend
cp .env.example .env
nano .env
```

Y deja exactamente estos valores (el resto puede quedarse como viene):

```ini
DB_HOST=localhost
DB_PORT=5432
DB_NAME=marketplace_db
DB_USER=postgres
DB_PASSWORD=postgres
DB_SSLMODE=disable
DB_POOL_SIZE=10
DB_POOL_MIN_IDLE=2

SPRING_PROFILES_ACTIVE=local

# Obligatorio: sin esto la app no arranca (falla a propósito)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970

BOOTSTRAP_ADMIN_EMAIL=admin@marketplace.com
BOOTSTRAP_ADMIN_PASSWORD=Admin123.
```

### 3.2 `frontend/.env.local`

```ini
MARKETPLACE_API_URL=http://localhost:8080
```

---

## 4. Cargar la base de datos (un solo comando)

El repositorio incluye **`backend/db/marketplace_local.sql`**: el esquema completo más los
datos de demostración ya cargados (16 usuarios, 15 productos con imagen, una orden y una reseña).

```bash
# Linux
PGPASSWORD=postgres psql -U postgres -h localhost -d marketplace_db -v ON_ERROR_STOP=1 -f backend/db/marketplace_local.sql
```

```powershell
# Windows
$env:PGPASSWORD = 'postgres'
psql -U postgres -h localhost -d marketplace_db -v ON_ERROR_STOP=1 -f backend\db\marketplace_local.sql
```

> Con el PostgreSQL portable, `psql` está en `%LOCALAPPDATA%\pg16\pgsql\bin\psql.exe`.

Debe terminar sin errores. Comprueba que quedó bien:

```bash
psql -U postgres -h localhost -d marketplace_db -c "SELECT role, COUNT(*) FROM users GROUP BY role;"
# ROLE_ADMIN 1 · ROLE_BUYER 10 · ROLE_SELLER 5
```

> **¿Prefieres empezar de cero?** No cargues el volcado. Al arrancar la API, **Flyway** crea
> las 17 tablas y el administrador se crea solo. Después puedes sembrar datos con
> `backend/tools/seed-users.ps1` y `backend/tools/seed-catalogo.ps1`.

---

## 5. Arrancar

### Terminal 1 — API

```powershell
# Windows
cd backend
.\run.ps1
```

```bash
# Linux
cd backend
chmod +x run.sh gradlew
./run.sh
```

Espera a `Started MarketplaceApplication` (~20 s). Queda en <http://localhost:8080>.

### Terminal 2 — Panel

```bash
cd frontend
npm install      # solo la primera vez
npm run dev
```

Queda en <http://localhost:3000>.

### Atajo en Windows

```powershell
cd backend
.\start-local.ps1 -ConPanel     # arranca PostgreSQL + API + panel
```

---

## 6. Comprobar que funciona

| Qué | Cómo |
|---|---|
| Salud de la API | <http://localhost:8080/actuator/health> → `{"status":"UP"}` |
| Swagger | <http://localhost:8080/swagger-ui/index.html> |
| Catálogo | <http://localhost:3000/productos> → **15 productos con foto** |
| Batería e2e | `backend/tools/e2e-test.ps1` (crea datos de prueba) |
| Tests unitarios | `cd backend && .\gradlew.bat test` → **137 tests** |

### Cuentas

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin@marketplace.com` | `Admin123.` |
| Vendedor | `diego.herrera3@marketplace.com` | `Vendedor123!` |
| Comprador | `tomas.rodriguez1@marketplace.com` | `Comprador123!` |

---

## 7. Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| `database "marketplace_db" does not exist` | No creaste la base | Paso 2 |
| `password authentication failed` | `DB_PASSWORD` no coincide | Revisa `backend/.env` |
| `Port 8080 already in use` | Ya hay una API corriendo | Ciérrala o cambia `PORT` |
| `Port 3000 already in use` | Ya hay un panel corriendo | Ciérralo |
| `connection refused` en 5432 | PostgreSQL no está arrancado | `pg_ctl start` o `systemctl start postgresql` |
| `unrecognized configuration parameter "transaction_timeout"` | Base con **PostgreSQL 16** y una migración antigua | Ya corregido en `V1__initial_schema.sql`; si tienes una copia vieja, actualiza el repo |
| El panel muestra «No se pudo contactar con la API» | La API está arrancando o caída | Espera 20 s y pulsa **Reintentar** |
| Las imágenes no aparecen | Falta `frontend/public/images/productos/` | Revisa que el clon esté completo |

Más incidencias en [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

---

## 8. Resumen en 5 comandos

```powershell
# Windows
cd backend; .\start-local.ps1 -ConPanel
```

```bash
# Linux
sudo systemctl start postgresql
PGPASSWORD=postgres psql -U postgres -h localhost -d marketplace_db -f backend/db/marketplace_local.sql
cd backend && ./run.sh &
cd ../frontend && npm install && npm run dev
```

**Panel:** <http://localhost:3000> · **API:** <http://localhost:8080>
