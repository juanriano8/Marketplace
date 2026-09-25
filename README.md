# Marketplace: Backend API + Panel web

Marketplace multi-rol (Comprador / Vendedor / Administrador) con **backend Spring Boot 3.3.4 +
Java 21**, **panel web Next.js 16** y **PostgreSQL en Google Cloud SQL**. Arquitectura hexagonal y
seguridad siguiendo **OWASP API Security Top 10**.

## Documentación

Empieza por la guía de entrega.

| Documento | Contenido |
|---|---|
| [**GUIA_ENTREGA.md**](GUIA_ENTREGA.md) | **Cómo levantar el backend y el frontend**, requisitos, checklist antes de presentar y solución de problemas |
| [**POSTMAN_DEMO_PASO_A_PASO.md**](POSTMAN_DEMO_PASO_A_PASO.md) | **Demostración de los 27 endpoints en Postman**, paso a paso con el código a pegar y el resultado esperado |
| [**GUIA_OTRO_PC.md**](GUIA_OTRO_PC.md) | **Qué instalar y hacer en otro computador** para ejecutarlo con otra conexión, conectando a Cloud SQL |
| [ESPECIFICACION_TECNICA.md](ESPECIFICACION_TECNICA.md) | Arquitectura, modelo de datos, reglas de negocio, seguridad y estado de la conexión a Cloud SQL |
| [frontend/README.md](frontend/README.md) | Detalle del panel web |
| [GUIA_POSTMAN_MANUAL.md](GUIA_POSTMAN_MANUAL.md) | Referencia de cada endpoint (consulta rápida) |
| [EJECUCION_Y_DEMO.md](EJECUCION_Y_DEMO.md) | Detalle de configuración, Cloud SQL paso a paso y otros PC |
| [PENDIENTES.md](PENDIENTES.md) | Estado actual y qué queda por hacer |
| [task.md](task.md) | Checklist de tareas de implementación |

> **La base de datos ya acepta conexiones desde cualquier red** (`0.0.0.0/0` en Authorized
> networks), así que no hay que autorizar IPs ni cambiar nada en Google Cloud.

## Inicio rápido

> **Requisito previo:** tu IP debe estar autorizada en Cloud SQL o el backend no arranca.
> Ver [GUIA_ENTREGA.md, sección 1](GUIA_ENTREGA.md#1-antes-que-nada-autoriza-tu-ip).

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

- **A mano, paso a paso:** sigue [POSTMAN_DEMO_PASO_A_PASO.md](POSTMAN_DEMO_PASO_A_PASO.md)
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
│   │   ├── modules/  auth, user, admin, product, inventory, order, review, payment
│   │   ├── shared/   dominio base, excepciones RFC 7807, seguridad JWT
│   │   └── config/   SecurityConfig, OpenApiConfig
│   ├── tools/        scripts de verificación y pruebas end-to-end
│   └── postman/      colección lista para importar
└── frontend/         panel web Next.js con los tres paneles por rol
    └── src/
        ├── app/      páginas (catálogo, carrito, checkout, vendedor, admin)
        ├── components/
        └── lib/      cliente HTTP, sesión, tipos
```

## Módulos del backend

`auth` · `user` · `admin` · `product` · `inventory` · `order` · `review` · `payment`

Cada módulo sigue la misma estructura hexagonal:

```
domain/{model, port/{inbound, outbound}}   Reglas de negocio y contratos
application/{dto, mapper, service}         Casos de uso y DTOs inmutables
infrastructure/adapter/...                 Adaptadores REST y JPA
```

## Endpoints

**31 operaciones**: las 23 rutas de la especificación técnica más las de apoyo.
Ver el catálogo completo en
[ESPECIFICACION_TECNICA.md, sección 5](ESPECIFICACION_TECNICA.md#5-catálogo-de-endpoints-implementados).

## Estado de verificación

| Comprobación | Resultado |
|---|---|
| Compilación del backend (`gradlew clean build`) | `BUILD SUCCESSFUL` |
| Suite de tests del backend | **114 tests, 0 fallos** |
| Arranque contra Cloud SQL | `HikariPool - Start completed` |
| Endpoints end-to-end contra Cloud SQL | **39 aserciones, 0 fallos** |
| Antisobreventa, split multi-vendedor, BOLA, RBAC | Verificados |
| Frontend: `tsc --noEmit` | 0 errores |
| Frontend: `next build` y servido en el puerto 3000 | Compila y responde `200` en las 15 páginas |

Comprobación rápida antes de presentar:

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

Detalle completo en
[ESPECIFICACION_TECNICA.md, sección 13](ESPECIFICACION_TECNICA.md#13-limitaciones-conocidas-y-trabajo-pendiente).
