# TROUBLESHOOTING — Incidencias reales del proyecto

Registro de los errores que **aparecieron de verdad** durante el desarrollo, con su causa y la
solución aplicada. Formato exigido por la rúbrica:

> `[Fecha] | [Capa] | [Descripción del Error] | [Solución Aplicada]`

**Proyecto:** Marketplace multi-rol · Spring Boot 3.3.4 + Next.js 16 + PostgreSQL 18
**Periodo:** septiembre–noviembre de 2026

---

## Tabla de incidencias

| Fecha | Capa | Descripción del error | Solución aplicada |
|---|---|---|---|
| 2026-09-20 | Backend | `database "marketplace_db" does not exist` (SQLState 3D000) al arrancar | Se creó la base por JDBC con el usuario `postgres` antes del primer arranque |
| 2026-09-20 | Backend | La base `postgres` tenía 23 tablas de otro proyecto con PK `bigint`, incompatibles con el modelo | Se fijó `DB_NAME=marketplace_db` en todos los entornos; nunca se usa la base por defecto |
| 2026-09-22 | Backend | 5 tests de `AuthControllerTest` fallaban con `NoSuchBeanDefinitionException` | Faltaba `JwtAccessDeniedHandler` en el `@Import` del test |
| 2026-09-24 | Backend | `SocketTimeoutException: Connect timed out` al conectar con Cloud SQL | La IP del cliente no estaba autorizada. Se autorizó `0.0.0.0/0` en *Authorized Networks* |
| 2026-09-24 | Backend | Login del administrador devolvía `401` | El bootstrap **solo crea**, nunca actualiza contraseñas. Se sincronizó el hash con `tools/AdminPasswordSync.java` |
| 2026-09-25 | Backend | `gradlew` fallaba con `FileNotFoundException ... .lck (Acceso denegado)` | El proceso no podía escribir en la caché de Gradle; se ejecutó con permisos ampliados |
| 2026-09-28 | Backend | Cloud Run: `403 storage.objects.get access` al subir el código | Desde mayo de 2024 los proyectos nuevos no reciben Editor automático. Se concedieron 6 roles a la cuenta de servicio |
| 2026-09-29 | Backend | Cloud Run: `password authentication failed for user "postgres"` | La contraseña llegó mal escapada en `--set-env-vars`; se corrigió con `--update-env-vars` |
| 2026-09-29 | Backend | Cloud Run: la primera petición tarda ~25 s | Arranque en frío con `min-instances 0`; se mitiga con `min-instances 1` durante la demo |
| 2026-09-30 | Frontend | `spawn EPERM` al ejecutar `next dev` | Entorno sin permisos de `fork`; se creó `scripts/dev-server.mjs` usando la API programática de Next |
| 2026-09-30 | Frontend | `npm EPERM` al instalar dependencias | Caché de npm en una ruta sin permisos; se fijó una caché local con `.npmrc` |
| 2026-10-01 | Frontend | Next 15.5.4 reportaba una CVE | Se actualizó a Next 16.3.6 (`npm audit` → 0 vulnerabilidades) |
| 2026-10-01 | Frontend | `403` en `/_next/**` y la página se quedaba «cargando» sin hidratar | Next 16 bloquea orígenes no locales en desarrollo; se añadió `allowedDevOrigins` con las IPs de red |
| 2026-10-02 | Postman | La colección daba `401` en el primer paso | `adminPassword` desactualizada y registros no idempotentes (el token quedaba `undefined` al repetir) |
| 2026-11-03 | Frontend | El proxy `/api/v1/**` devolvía `500` | `frontend/.env.local` no existía (está en `.gitignore`); se fijó Cloud Run como destino en `next.config.ts` y se creó `.env.local` |
| 2026-11-03 | Backend | Cloud Run: `503 Service Unavailable` en todos los endpoints | La instancia de Cloud SQL estaba **STOPPED**; se arrancó a `RUNNABLE` con `--activation-policy ALWAYS` |
| 2026-11-08 | Backend | Migración a Neon: Flyway abortaba con `ERROR: role "cloudsqlsuperuser" does not exist` (SQLState 42704, línea 449 de `V1__initial_schema.sql`) | El `V1` se generó con un `pg_dump` de Cloud SQL y arrastraba `GRANT ALL ON SCHEMA public TO cloudsqlsuperuser`. Ese rol es exclusivo de Cloud SQL. Se eliminó la sentencia (el motivo queda documentado dentro del propio `.sql`) y la migración se aplicó completa |
| 2026-11-08 | Backend | Flyway no creaba el esquema: la app usaba `ddl-auto: update` pese a estar `validate` en el YAML | `backend/.env` definía `JPA_DDL_AUTO=update`, y **las variables de entorno tienen prioridad** sobre `application.yml`. Se comentó esa línea para que Flyway gobierne el esquema y Hibernate solo valide |
| 2026-11-08 | Backend | Tras integrar el limitador, 46 tests de `RbacSecurityTest` y `AuthControllerTest` fallaban por `ApplicationContext failure` | `@WebMvcTest` carga los `Filter`, así que `RateLimitFilter` se instanciaba sin `RateLimitProperties`. Se añadieron ambas clases al `@Import` y se desactivó el limitador en esas pruebas con `@TestPropertySource`; el limitador tiene su propia prueba unitaria |
| 2026-11-09 | Backend | `run.ps1` fallaba con `Value 'C:\...\jdk-21...\ " bootRun' given for org.gradle.java.home Gradle property is invalid` | `JAVA_HOME` terminaba en `\`; al pasarlo entre comillas a `gradlew.bat`, la barra escapaba la comilla y Gradle incluía `bootRun` en la ruta. Se recorta la barra final antes de construir `-Dorg.gradle.java.home` |
| 2026-11-09 | Backend | `.\run.ps1` no se podía cargar: «no está firmado digitalmente» | La política de ejecución de PowerShell bloquea scripts sin firmar. Ejecutar con `powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1` |
| 2026-11-09 | Frontend | Con la cookie de sesión, `GET /api/v1/cart` seguía devolviendo `401`; el proxy de Next no se ejecutaba | `next.config.ts` declaraba un rewrite de `/api/v1/**` que se aplicaba antes del route handler y reenviaba al backend sin `Authorization`. Se eliminó ese rewrite (solo queda `/actuator`) y el route handler `app/api/v1/[...path]` pasó a ser el único proxy |
| 2026-11-09 | Backend | Los mensajes con tildes se veían como `verificÃ³` al revisarlos con PowerShell 5.1 | El JSON de la API era UTF-8 correcto; PowerShell 5.1 lo decodificaba como Latin-1. Se comprobó con `curl` y se fijó `options.encoding = 'UTF-8'` en Gradle para que el fuente también se compile igual |

---

## Incidencias recientes (2026-11-03)

Estas se detectaron al verificar el despliegue y configurar las herramientas de diseño.

| Fecha | Capa | Descripción del error | Solución aplicada |
|---|---|---|---|
| 2026-11-03 | Backend | `https://…/swagger-ui.html` cargaba en blanco (el `curl` daba 302→200) | La CSP incluía `sandbox` sin `allow-scripts`, lo que **bloquea todo el JavaScript**. Se quitó `sandbox` y se mantuvo `default-src 'self'; frame-ancestors 'none'` |
| 2026-11-03 | Google Cloud | `doctor` de Stitch: `403 — Stitch API has not been used in project` | La API `stitch.googleapis.com` estaba deshabilitada; se habilitó con `gcloud services enable` |
| 2026-11-03 | NotebookLM | `notebooklm login` fallaba con `Executable doesn't exist … chrome-linux64/chrome` | La descarga de Chromium (191 MB) se cortaba por un pre-flight de 300 s. Se usó `notebooklm login --browser-cookies firefox` (sin navegador propio) |
| 2026-11-03 | Google Cloud | `gcloud sql export sql` devolvía `409 The instance or operation is not in an appropriate state` | La operación de arranque seguía `RUNNING`; se esperó a que terminara antes de exportar |

---

## Cómo usar este documento

1. **Busca por el síntoma** en la columna «Descripción del error».
2. Si el error no está, revisa:
   - [GUIA_ENTREGA.md](GUIA_ENTREGA.md) → problemas de arranque local.
   - [DESPLIEGUE_RENDER_VERCEL.md](DESPLIEGUE_RENDER_VERCEL.md) → problemas de despliegue actual.
   - [GUIA_OTRO_PC_LINUX.md](GUIA_OTRO_PC_LINUX.md) → problemas en Linux.
3. Cuando aparezca uno nuevo, **añádelo aquí** con fecha, capa, error y solución.

## Prevención (lo aprendido)

| Regla | Motivo |
|---|---|
| Verificar `state=RUNNABLE` **y** que no haya operaciones `RUNNING` antes de exportar | El export falla con 409 |
| No usar `sandbox` en la CSP de una página con JavaScript | Bloquea todo el script |
| Autorizar `0.0.0.0/0` o la IP correcta en Cloud SQL | El `Connect timed out` es siempre red, no código |
| Fijar `DB_NAME=marketplace_db` | La base `postgres` tiene tablas ajenas |
| Calentar Cloud Run antes de presentar | Arranque en frío de ~25 s |
| Guardar `.env` fuera de Git y respaldarlo | Contiene la contraseña de la BD y el `JWT_SECRET` |
