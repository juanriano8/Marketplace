# Documentación del proyecto

> En la **raíz** del repositorio solo quedan los tres documentos de entrada:
> [README](../README.md), [CONTEXTO](../CONTEXTO.md) y [PENDIENTES](../PENDIENTES.md).
> El resto de la documentación está agrupada aquí por tema.

## Ruta de lectura recomendada

1. [CONTEXTO](../CONTEXTO.md) — estado real, credenciales, trampas y mapa del proyecto.
2. [PENDIENTES](../PENDIENTES.md) — qué está hecho y qué falta, por prioridad.
3. [ARRANQUE_RAPIDO](operacion/ARRANQUE_RAPIDO.md) — levantar backend y panel en 3 comandos.
   [ACTIVACION](operacion/ACTIVACION.md) — qué queda activado y cómo verificarlo.
4. [ESPECIFICACION_TECNICA](arquitectura/ESPECIFICACION_TECNICA.md) — arquitectura, modelo de datos, reglas y seguridad.
5. [POSTMAN_DEMO_PASO_A_PASO](api/POSTMAN_DEMO_PASO_A_PASO.md) — demostrar los endpoints uno por uno.
6. [DESIGN](diseno/DESIGN.md) y [UX_MAPPING](diseno/UX_MAPPING.md) — contrato visual y correspondencia API ↔ pantallas.

> **Vigencia:** los documentos marcados como *histórico* describen la infraestructura anterior
> (Google Cloud SQL). Desde el **2026-11-08** el proyecto usa **Neon**; para el estado actual
> consulta [CONTEXTO](../CONTEXTO.md) y [ARRANQUE_RAPIDO](operacion/ARRANQUE_RAPIDO.md).
>
> Los comandos de los documentos asumen que se ejecutan **desde la raíz del repositorio**
> (`fullstack/`), salvo que el propio documento indique otra cosa.

## `operacion/` — arranque, despliegue y problemas

| Documento | Contenido | Vigencia |
|---|---|---|
| [ARRANQUE_RAPIDO.md](operacion/ARRANQUE_RAPIDO.md) | Levantar todo en 3 comandos y comprobar la conexión | ✅ Actual (Neon) |
| [ACTIVACION.md](operacion/ACTIVACION.md) | Qué queda activado, variables y verificación | ✅ Actual |
| [DESPLIEGUE_RENDER_VERCEL.md](operacion/DESPLIEGUE_RENDER_VERCEL.md) | Desplegar API en Render, panel en Vercel y objetos en Neon | ✅ Guía nueva |
| [TROUBLESHOOTING.md](operacion/TROUBLESHOOTING.md) | Incidencias reales con causa y solución | ✅ Actual |
| [GUIA_ENTREGA.md](operacion/GUIA_ENTREGA.md) | Requisitos, arranque, checklist antes de presentar | ⚠️ Histórico (Cloud SQL) |
| [EJECUCION_Y_DEMO.md](operacion/EJECUCION_Y_DEMO.md) | Configuración detallada, demo y solución de problemas | ⚠️ Histórico (Cloud SQL) |
| [GUIA_OTRO_PC.md](operacion/GUIA_OTRO_PC.md) | Ejecutar el proyecto en otro PC con Windows | ⚠️ Histórico (Cloud SQL) |
| [GUIA_OTRO_PC_LINUX.md](operacion/GUIA_OTRO_PC_LINUX.md) | Lo mismo para Linux | ⚠️ Histórico (Cloud SQL) |

## `api/` — endpoints, Postman y usuarios de prueba

| Documento | Contenido | Vigencia |
|---|---|---|
| [POSTMAN_DEMO_PASO_A_PASO.md](api/POSTMAN_DEMO_PASO_A_PASO.md) | Demostración de los endpoints paso a paso | ✅ |
| [GUIA_POSTMAN_MANUAL.md](api/GUIA_POSTMAN_MANUAL.md) | Referencia rápida de cada endpoint | ✅ |
| [USUARIOS_PRUEBA.md](api/USUARIOS_PRUEBA.md) | Cuentas de prueba y permisos por rol | ⚠️ Datos de la base anterior |

## `arquitectura/` — diseño técnico

| Documento | Contenido | Vigencia |
|---|---|---|
| [ESPECIFICACION_TECNICA.md](arquitectura/ESPECIFICACION_TECNICA.md) | Stack, arquitectura hexagonal, modelo de datos, reglas, seguridad y pruebas | ⚠️ Infraestructura histórica; arquitectura y API vigentes |

## `diseno/` — sistema de diseño y UX

| Documento | Contenido | Vigencia |
|---|---|---|
| [DESIGN.md](diseno/DESIGN.md) | Tokens, tipografía, componentes, 4 estados y accesibilidad | ✅ |
| [UX_MAPPING.md](diseno/UX_MAPPING.md) | Rutas de la API ↔ pantallas y estados | ✅ |
| [DECISION_UI.md](diseno/DECISION_UI.md) | Por qué se mantiene `ui.tsx` y no se adopta Shadcn UI | ✅ Decisión aceptada |
| [PLAN_REDISENO.md](diseno/PLAN_REDISENO.md) | Plan de rediseño, Figma/Stitch y migración | ⚠️ Plan histórico |

## `datos/` — dataset y análisis

| Documento | Contenido | Vigencia |
|---|---|---|
| [ANALISIS_CSV_VS_BD.md](datos/ANALISIS_CSV_VS_BD.md) | Qué datos del CSV encajan en el modelo relacional | ✅ |
| [marketplace_datos.csv](datos/marketplace_datos.csv) | Dataset de referencia (16 hojas) | — |

## `referencia/` — material original

| Documento | Contenido |
|---|---|
| [Especificacion Tecnica Marketplace Spring Boot GCP V2.pdf](referencia/Especificacion%20Tecnica%20Marketplace%20Spring%20Boot%20GCP%20V2.pdf) | Enunciado original de la especificación técnica |
| [especificacion_tecnica_marketplace_spring_boot_gcp_v2.txt](referencia/especificacion_tecnica_marketplace_spring_boot_gcp_v2.txt) | Texto extraído del PDF |
| [task.md](referencia/task.md) | Checklist histórico de implementación del backend |

## Documentación fuera de `docs/`

| Documento | Contenido |
|---|---|
| [frontend/README.md](../frontend/README.md) | Detalle del panel web Next.js |
| [design/stitch-design-md.md](../design/stitch-design-md.md) | Design system completo extraído de Stitch |
| [design/MCP_SETUP.md](../design/MCP_SETUP.md) | Configuración de MCP (Stitch, Figma, NotebookLM) |
| [design/notebooklm/owasp-frontend.md](../design/notebooklm/owasp-frontend.md) | Requisitos OWASP para el frontend |
| [frontend/public/images/productos/README.md](../frontend/public/images/productos/README.md) | Convenciones de las imágenes de producto |
