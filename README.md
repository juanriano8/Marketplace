# Marketplace — Backend API

Backend del marketplace multi-rol (Comprador / Vendedor / Administrador) construido con
**Spring Boot 3.3.4 + Java 21 + PostgreSQL**, siguiendo arquitectura **hexagonal** y las
directrices de seguridad **OWASP API Security Top 10**.

## Documentación

| Documento | Contenido |
|---|---|
| [**ESPECIFICACION_TECNICA.md**](ESPECIFICACION_TECNICA.md) | Arquitectura, catálogo de endpoints, modelo de datos, reglas de negocio, seguridad, estado de conexión a Google Cloud SQL y limitaciones conocidas |
| [**EJECUCION_Y_DEMO.md**](EJECUCION_Y_DEMO.md) | Cómo instalar el JDK, configurar la base de datos, arrancar el proyecto y probar los endpoints en Postman paso a paso |
| [task.md](task.md) | Checklist de tareas de implementación |

## Inicio rápido

```powershell
cd backend

# 1. Requisito: Java 21 (el proyecto no funciona con Java 8)
winget install EclipseAdoptium.Temurin.21.JDK

# 2. Base de datos local (alternativa a Cloud SQL)
docker compose up -d

# 3. Arrancar
.\run.ps1 -Profile local
```

| Recurso | URL |
|---|---|
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> |
| Health | <http://localhost:8080/actuator/health> |

### Probar los endpoints

Importa [`backend/postman/Marketplace-API.postman_collection.json`](backend/postman/Marketplace-API.postman_collection.json)
en Postman: **44 peticiones en 10 carpetas**, con captura automática de tokens y IDs.

> Para llamar a los endpoints `/api/v1/admin/**` hace falta un administrador, y no existe
> registro público de administradores. Define `BOOTSTRAP_ADMIN_EMAIL` y `BOOTSTRAP_ADMIN_PASSWORD`
> antes del primer arranque y la aplicación lo crea automáticamente.

## Módulos

`auth` · `user` · `admin` · `product` · `inventory` · `order` · `review` · `payment`

Cada módulo sigue la misma estructura hexagonal:

```
domain/{model, port/{inbound, outbound}}   Reglas de negocio y contratos
application/{dto, mapper, service}         Casos de uso y DTOs inmutables
infrastructure/adapter/...                 Adaptadores REST y JPA
```

## Endpoints

**30 rutas**: las 23 de la especificación técnica más 7 de apoyo.
Ver el catálogo completo en [ESPECIFICACION_TECNICA.md §5](ESPECIFICACION_TECNICA.md#5-catálogo-de-endpoints-implementados).

## Estado de verificación

✅ **Verificado en ejecución contra Google Cloud SQL:**

| Comprobación | Resultado |
|---|---|
| Compilación (`./gradlew clean build`) | `BUILD SUCCESSFUL` |
| Suite de tests | **114 tests, 0 fallos** |
| Arranque contra Cloud SQL | `HikariPool - Start completed` |
| Endpoints end-to-end | **39 aserciones, 0 fallos** |
| Antisobreventa, split multi-vendedor, BOLA, RBAC | Verificados |

Comprobación rápida antes de presentar:

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

Detalle completo en [ESPECIFICACION_TECNICA.md §13](ESPECIFICACION_TECNICA.md#13-limitaciones-conocidas-y-trabajo-pendiente).
