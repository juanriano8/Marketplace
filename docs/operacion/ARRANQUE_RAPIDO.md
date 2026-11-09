# Arranque rápido: levantar todo

> **Última actualización:** 2026-11-08
> **Base de datos:** Neon (PostgreSQL en la nube, plan gratuito).
> Ya **no hay nada que encender ni apagar**: la base está siempre disponible.

---

## Antes de empezar

| Requisito | Cómo comprobarlo |
|---|---|
| JDK 21 | `java -version` |
| Node 20+ | `node -v` |
| `backend/.env` con las credenciales de Neon | `Get-Content backend\.env` → debe tener `DB_HOST=ep-...neon.tech` |

Si no tienes `.env`, copia la plantilla y rellena la contraseña:

```powershell
cd backend
Copy-Item .env.example .env
notepad .env      # pega DB_PASSWORD y JWT_SECRET
```

> La cadena de conexión se saca de <https://console.neon.tech> → tu proyecto →
> **Connect** → conexión **directa** (la que **no** lleva `-pooler`).

---

## 1. Arrancar el backend

```powershell
cd backend
.\run.ps1
```

En Linux:

```bash
cd backend
./run.sh
```

Qué pasa al arrancar:

1. **Flyway** aplica las migraciones (`db/migration`) y crea el esquema si la base está vacía
2. **Hibernate** valida que el esquema coincida (no lo modifica)
3. **BootstrapAdminRunner** crea el administrador si no existe
4. Tomcat escucha en el **puerto 8080**

Espera hasta ver:

```
Started MarketplaceApplication in X seconds
```

## 2. Arrancar el panel web

En **otra terminal**:

```powershell
cd frontend
npm run dev
```

Abre <http://localhost:3000>.

> El panel hace de proxy hacia `http://localhost:8080` (configurado en
> `frontend/.env.local`). Si el backend no está arriba, verás el estado de error
> con su botón de reintento.

---

## 3. Comprobar que todo está conectado

```powershell
# Salud del backend
Invoke-RestMethod http://localhost:8080/actuator/health
# -> status = UP

# Catálogo público (no necesita token)
Invoke-RestMethod 'http://localhost:8080/api/v1/products?size=3' | Select-Object totalElements
```

Batería completa de verificación (27 endpoints, 39 comprobaciones):

```powershell
.\backend\tools\verify.ps1
# o, contra otra URL:
.\backend\tools\e2e-test.ps1 -BaseUrl http://localhost:8080
```

---

## Páginas útiles

| Qué | URL |
|---|---|
| Salud del backend | <http://localhost:8080/actuator/health> |
| **Swagger UI** (probar endpoints) | <http://localhost:8080/swagger-ui/index.html> |
| OpenAPI en JSON | <http://localhost:8080/v3/api-docs> |
| Panel web | <http://localhost:3000> |
| Catálogo | <http://localhost:3000/productos> |
| Panel del vendedor | <http://localhost:3000/vendedor> |
| Panel del administrador | <http://localhost:3000/admin> |

---

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| **Administrador** | `admin@marketplace.com` | la de `BOOTSTRAP_ADMIN_PASSWORD` en `.env` |
| **Vendedores** (5) | `diego.herrera3@`, `jorge.ramirez4@`, `santiago.flores9@`, `daniel.herrera17@`, `andres.medina21@marketplace.com` | `Vendedor123!` |
| **Compradores** (10) | `tomas.rodriguez1@`, `veronica.medina2@`, … `alejandra.rojas15@marketplace.com` | `Comprador123!` |

Detalle completo en [USUARIOS_PRUEBA.md](../api/USUARIOS_PRUEBA.md).

> **Los vendedores deben estar verificados** por el administrador antes de publicar
> productos. Los que siembra `tools/seed-users.ps1` ya salen verificados.

---

## Problemas típicos

| Síntoma | Causa | Solución |
|---|---|---|
| `Port 8080 was already in use` | Ya hay un backend corriendo | Cierra el otro proceso o cambia `PORT` |
| `JWT_SECRET` no definido al arrancar | Falta en `.env` | Añádelo (`openssl rand -hex 32`) |
| `password authentication failed` | Contraseña de Neon incorrecta en `.env` | Cópiala otra vez desde la consola de Neon |
| `role "cloudsqlsuperuser" does not exist` | Migración antigua | Ya corregido en `V1__initial_schema.sql` |
| El panel muestra error de red | Backend caído o `.env.local` mal | Arranca el backend; revisa `frontend/.env.local` |
| `Flyway ... checksum mismatch` | Se editó una migración ya aplicada | No se editan: se crea una `V2__...` nueva |

Más incidencias resueltas en [TROUBLESHOOTING.md](TROUBLESHOOTING.md).

---

## Datos de referencia

| Dato | Valor |
|---|---|
| Base de datos | **Neon**, PostgreSQL 18.6, base `neondb` |
| Host | `ep-steep-dawn-bc86fdpf.c-16.us-east-1.aws.neon.tech` (directo) |
| Esquema | 10 tablas, gestionadas por **Flyway** |
| Backend local | `http://localhost:8080` |
| Panel local | `http://localhost:3000` |
| Cloud Run (histórico) | `marketplace-api` en `us-central1` — apunta a la base antigua, responde 500 |

> **Sobre Cloud SQL:** la instancia `marketplace` de Google Cloud se usó hasta el
> 2026-11-08. Su periodo de prueba terminó y los datos se migraron a Neon. Los
> documentos que aún la mencionan describen la infraestructura **anterior**.

---

## Resumen en 3 líneas

```powershell
cd backend;  .\run.ps1        # 1. backend  -> http://localhost:8080
cd frontend; npm run dev      # 2. panel    -> http://localhost:3000
# No hay que encender ninguna base: Neon está siempre disponible.
```
