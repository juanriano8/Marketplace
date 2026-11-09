# Marketplace: Backend API + Panel web

Marketplace multi-rol (Comprador / Vendedor / Administrador) con **backend Spring Boot 3.3.4 +
Java 21**, **panel web Next.js 16** y **PostgreSQL en Neon** (nube, plan gratuito). Arquitectura
hexagonal y seguridad siguiendo **OWASP API Security Top 10**.

## Documentación

**Si llegas nuevo o retomas el proyecto, empieza por [CONTEXTO.md](CONTEXTO.md).**

| Documento | Contenido |
|---|---|
| [**docs/README.md**](docs/README.md) | **Índice completo** de la documentación: operación, API, arquitectura, diseño, datos y referencia |
| [**CONTEXTO.md**](CONTEXTO.md) | **Traspaso completo**: estado real, credenciales (dónde viven), trampas conocidas, mapa de archivos y qué falta por hacer |
| [**ARRANQUE_RAPIDO.md**](docs/operacion/ARRANQUE_RAPIDO.md) | **Levantar todo en 3 comandos** y comprobar que está conectado |
| [**ACTIVACION.md**](docs/operacion/ACTIVACION.md) | **Qué queda activado**, variables, verificación y pendientes que dependen de Google Cloud |
| [**DESPLIEGUE_RENDER_VERCEL.md**](docs/operacion/DESPLIEGUE_RENDER_VERCEL.md) | Desplegar la API en **Render**, el panel en **Vercel** y las imágenes en **Neon Object Storage** |
| [**GUIA_ENTREGA.md**](docs/operacion/GUIA_ENTREGA.md) | **Cómo levantar el backend y el frontend**, requisitos, checklist antes de presentar y solución de problemas |
| [**POSTMAN_DEMO_PASO_A_PASO.md**](docs/api/POSTMAN_DEMO_PASO_A_PASO.md) | **Demostración paso a paso en Postman**, con el código a pegar y el resultado esperado |
| [**GUIA_OTRO_PC.md**](docs/operacion/GUIA_OTRO_PC.md) | **Qué instalar y hacer en otro computador con Windows** para ejecutarlo con otra conexión (guía histórica) |
| [**GUIA_OTRO_PC_LINUX.md**](docs/operacion/GUIA_OTRO_PC_LINUX.md) | **Lo mismo para Linux** (Ubuntu, Debian, Fedora, Arch, openSUSE), con los scripts `.sh` |
| [USUARIOS_PRUEBA.md](docs/api/USUARIOS_PRUEBA.md) | Las 16 cuentas de prueba con sus contraseñas y qué puede hacer cada rol |
| [ANALISIS_CSV_VS_BD.md](docs/datos/ANALISIS_CSV_VS_BD.md) | Qué datos de `marketplace_datos.csv` sirven para las tablas de Cloud SQL y cuáles no |
| [ESPECIFICACION_TECNICA.md](docs/arquitectura/ESPECIFICACION_TECNICA.md) | Arquitectura, modelo de datos, reglas de negocio, seguridad y estado de la conexión a Cloud SQL |
| [frontend/README.md](frontend/README.md) | Detalle del panel web |
| [GUIA_POSTMAN_MANUAL.md](docs/api/GUIA_POSTMAN_MANUAL.md) | Referencia de cada endpoint (consulta rápida) |
| [EJECUCION_Y_DEMO.md](docs/operacion/EJECUCION_Y_DEMO.md) | Detalle de configuración, Cloud SQL paso a paso y otros PC |
| [PENDIENTES.md](PENDIENTES.md) | Estado actual y qué queda por hacer |
| [task.md](docs/referencia/task.md) | Checklist de tareas de implementación |

> Los documentos marcados como **históricos** describen la infraestructura anterior (Google
> Cloud SQL). El estado actual, con **Neon**, está en [CONTEXTO.md](CONTEXTO.md) y
> [ARRANQUE_RAPIDO.md](docs/operacion/ARRANQUE_RAPIDO.md).

## Inicio rápido

> **Requisito previo:** tener `backend/.env` con las credenciales de **Neon**.
> Ver [ARRANQUE_RAPIDO.md, «Antes de empezar»](docs/operacion/ARRANQUE_RAPIDO.md#antes-de-empezar).

```powershell
# Terminal 1: backend
cd backend
.\run.ps1                       # perfil cloud (lee .env)
# o: .\run.ps1 -Profile local    # con PostgreSQL local por Docker

# Terminal 2: panel web
cd frontend
npm install                      # sólo la primera vez
npm run dev
```

| Recurso | URL |
|---|---|
| **Panel web** | <http://localhost:3000> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Health | <http://localhost:8080/actuator/health> |

### Probar los endpoints

- **A mano, paso a paso:** sigue [POSTMAN_DEMO_PASO_A_PASO.md](docs/api/POSTMAN_DEMO_PASO_A_PASO.md)
- **Automático:** importa
  [`backend/postman/Marketplace-API.postman_collection.json`](backend/postman/Marketplace-API.postman_collection.json)
  en Postman: **44 peticiones en 10 carpetas**, con captura automática de tokens y IDs

> Para llamar a los endpoints `/api/v1/admin/**` hace falta un administrador, y no existe
> registro público de administradores. Define `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`
> antes del primer arranque y la aplicación lo crea automáticamente.

## Estructura del proyecto

```
fullstack/
├── backend/          API REST Spring Boot (arquitectura hexagonal)
│   ├── src/main/java/com/marketplace/api/
│   │   ├── modules/  auth, user, admin, product, inventory, order, review, payment, notification, seller, favorite
│   │   ├── shared/   dominio base, excepciones RFC 7807, seguridad JWT
│   │   └── config/   SecurityConfig, OpenApiConfig
│   ├── tools/        scripts de verificación y pruebas end-to-end
│   └── postman/      colección lista para importar
├── docs/             documentación (índice en docs/README.md)
│   ├── operacion/    arranque, despliegue y troubleshooting
│   ├── api/          Postman, endpoints y usuarios de prueba
│   ├── arquitectura/ especificación técnica
│   ├── diseno/       DESIGN, UX_MAPPING y plan de rediseño
│   ├── datos/        dataset y análisis
│   └── referencia/   PDF y texto de la especificación original
└── frontend/         panel web Next.js con los tres paneles por rol
    └── src/
        ├── app/      páginas (catálogo, carrito, checkout, vendedor, admin)
        ├── components/
        └── lib/      cliente HTTP, sesión, tipos
```

## Módulos del backend

`auth` · `user` · `admin` · `product` · `inventory` · `order` · `review` · `payment` · `notification` · `seller` · `favorite`

Cada módulo sigue la misma estructura hexagonal:

```
domain/{model, port/{inbound, outbound}}   Reglas de negocio y contratos
application/{dto, mapper, service}         Casos de uso y DTOs inmutables
infrastructure/adapter/...                 Adaptadores REST y JPA
```

## Endpoints

**44 operaciones**: las 23 rutas de la especificación técnica más las de apoyo, notificaciones, ranking, perfil de vendedor y favoritos.
Ver el catálogo completo en
[ESPECIFICACION_TECNICA.md, sección 5](docs/arquitectura/ESPECIFICACION_TECNICA.md#5-catálogo-de-endpoints-implementados).

## Estado de verificación

| Comprobación | Resultado |
|---|---|
| Compilación del backend (`gradlew clean build`) | `BUILD SUCCESSFUL` |
| Suite de tests del backend | **137 tests, 0 fallos** |
| Arranque contra Neon | `HikariPool - Start completed` |
| Endpoints end-to-end contra Neon | **39 aserciones, 0 fallos** |
| Caché de catálogo (Caffeine) | 2 tests; invalidación al escribir producto o stock |
| Sesión del panel | JWT en cookie httpOnly; carrito 200 con sesión, 401 sin ella |
| Notificaciones | Flyway `V2`; campana, página `/notificaciones`, 5 tests |
| Perfil de vendedor y rating | Flyway `V3`; write-behind cada 5 s; E2E 0→5→0 |
| Ranking | `GET /api/v1/products/ranking`; la portada lo usa |
| Favoritos | Corazón en catálogo/portada/detalle y página `/favoritos` |
| Diseño Stitch | Shell con sidebar/topbar, catálogo con filtros, auth e Inter |
| Antisobreventa, split multi-vendedor, BOLA, RBAC | Verificados |
| Frontend: `tsc --noEmit` | 0 errores |
| Frontend: `next build` y servido en el puerto 3000 | Compila y responde `200` en las 17 páginas |

Comprobación rápida antes de presentar:

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

Detalle completo en
[ESPECIFICACION_TECNICA.md, sección 13](docs/arquitectura/ESPECIFICACION_TECNICA.md#13-limitaciones-conocidas-y-trabajo-pendiente).
