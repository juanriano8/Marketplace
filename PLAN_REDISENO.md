# Plan: rediseño del frontend, documentación y migración

**Septiembre de 2026**

Este documento organiza las cuatro cosas que vienen: **Figma → Stitch → NotebookLM → base de datos
y servicios**. Ordena el trabajo y dice qué necesita cada fase.

---

## ⚠️ 0. Lo urgente primero: el trial se acaba

**Ahora mismo la base de datos está DETENIDA** (verificado). Y el periodo de prueba tiene fecha
límite. Antes de tocar el diseño hay que **proteger lo que ya funciona**: vas a rehacer el frontend y
a migrar servicios, así que necesitas un punto de retorno.

### 0.1 Respaldo de la base (hazlo esta semana)

En tu PC tienes **Docker**, pero no `pg_dump`, `psql` ni `gcloud`. Con Docker basta:

```powershell
# 1. Arranca la base en Cloud SQL (consola → Iniciar) y espera a RUNNABLE
# 2. En la carpeta del proyecto:
docker run --rm -v "${PWD}:/out" `
  -e PGPASSWORD="TU_PASSWORD" `
  -e PGSSLMODE=require `
  postgres:18 sh -c 'pg_dump -h 136.112.91.42 -U postgres -d marketplace_db --no-owner > /out/backup_marketplace.sql'
```

> **Usa la imagen `postgres:18`**, no la 16: el servidor es PostgreSQL **18.6** y `pg_dump` no puede
> volcar desde una versión mayor que la suya.
>
> El `-v "${PWD}:/out"` es importante: escribe el `.sql` desde dentro del contenedor, así te saltas
> el problema de codificación de `>` en PowerShell.

**Alternativa sin Docker** (en Cloud Shell, necesitas un bucket):

```bash
gcloud sql export sql marketplace gs://TU-BUCKET/backup_marketplace.sql --database=marketplace_db
gcloud storage cp gs://TU-BUCKET/backup_marketplace.sql .
```

### 0.2 Congelar la versión que funciona

**No rediseñes sobre `main`.** Si el rediseño sale mal, tiene que existir una versión verificada a la
que volver:

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack
git tag -a entrega-v1 -m "Version verificada: 39 aserciones OK contra Cloud Run"
git push origin entrega-v1

# y trabaja el rediseño en una rama aparte
git checkout -b rediseno-frontend
```

Ese tag es el que presentas si algo falla.

---

## 🎨 1. Conectarse a Figma

### Qué necesito

**1. Un token de acceso personal** (30 segundos):

1. En Figma, desde el *file browser*: menú de cuenta (arriba a la izquierda) → **Settings**
2. Pestaña **Security**
3. Sección **Personal access tokens** → **Generate new token**
4. Nombre: `DSH` · **Scope: `file_read`** (solo lectura) → Enter
5. **Copia el token en ese momento**: solo se muestra una vez

**2. El file key** del archivo. Está en la URL:

```
https://www.figma.com/design/<ESTE_ES_EL_FILE_KEY>/Marketplace
```

### Cómo pasármelo sin exponerlo

Guárdalo en un archivo **fuera del repositorio**:

```
C:\Users\sebas\.figma-token
```

Y dime: *"el token está en `C:\Users\sebas\.figma-token`"*. Lo cargo en una variable sin imprimirlo.
**No lo pegues en el chat** — todo lo que se pega queda en el historial.

> ⚠️ **Un token personal da acceso a TODOS tus archivos de Figma y no se puede restringir por
> archivo.** Es como dar la llave del cajón completo. Revócalo al terminar la fase:
> *Settings → Security → Revoke access*.

### Qué puedo hacer con eso

| Acción | Para qué sirve |
|---|---|
| Leer el árbol del archivo | Páginas, frames, capas y nombres |
| Extraer **variables y estilos** | Colores, tipografías, radios, sombras → el design system |
| Listar componentes y variantes | Saber qué hay que implementar |
| **Exportar imágenes** de cualquier frame | Revisar el diseño visualmente |
| Generar el código | `@theme` de Tailwind + componentes |

### Alternativa inmediata: capturas

Puedes **pegar capturas de pantalla directamente aquí** y las veo. Para iterar sobre una pantalla
concreta es más rápido que la API, y sirve desde el primer minuto.

### Nota sobre el MCP de Figma

Figma tiene un **Dev Mode MCP server** local (`http://127.0.0.1:3845/mcp`) que expone el frame
seleccionado. **Mi entorno no tiene cliente MCP**, así que no puedo usarlo directamente — pero a ti
sí te sirve si trabajas con otro cliente que lo soporte (VS Code, Antigravity, Claude Code).

---

## 🧩 2. Qué cambios hacer en el frontend

### La regla de oro: separar el diseño de la lógica

La tentación es reescribir todo. **No.** El frontend actual tiene una capa funcional que está
verificada y no depende del aspecto.

**NO tocar** (el núcleo que ya funciona):

| Archivo | Por qué |
|---|---|
| `frontend/src/lib/api.ts` | El cliente HTTP y el manejo de errores |
| `frontend/src/lib/auth.tsx` | Sesión, `localStorage`, roles |
| `frontend/src/lib/types.ts` | Los contratos de la API |
| `frontend/src/components/RequireAuth.tsx` | Protección de rutas por rol |
| `frontend/next.config.ts` | Rewrites y `allowedDevOrigins` |

**Rehacer** (la capa visual):

| Archivo | Qué se hace |
|---|---|
| `frontend/src/app/globals.css` | **Los tokens** `@theme` salen de Figma |
| `frontend/src/components/ui.tsx` | Primitivas: botón, tarjeta, badge, input, tabla, estados |
| `frontend/src/components/Navbar.tsx` | Cabecera y navegación |
| `frontend/src/components/ProductForm.tsx` | Formulario de producto |
| `frontend/src/components/feedback.tsx` | Avisos y errores |
| Las 15 páginas | El **JSX**, conservando sus `useEffect` y fetch |

### El puente entre Figma y el código: los *design tokens*

Esto es lo que hace el rediseño repetible en vez de artesanal. En Figma se definen como
**variables** o **estilos**:

```
color/brand/600      #2563eb
color/ink/900        #0f172a
font/display         Inter, 700
radius/card          12px
space/4              16px
```

Y se convierten en un solo sitio del CSS:

```css
@theme {
  --color-brand-600: #2563eb;
  --color-ink-900: #0f172a;
  --radius-card: 12px;
}
```

**Todo lo demás deriva de ahí.** Si mañana cambia un color, se cambia en un token y no en 40
componentes. Por eso el primer paso del rediseño **no es diseñar pantallas, es definir los tokens**.

### Orden de trabajo (de dentro hacia fuera)

1. **Tokens** → `globals.css` (bloquea todo lo demás)
2. **Primitivas** → `ui.tsx` (botón, tarjeta, badge, input…)
3. **Layout** → `Navbar` y el contenedor
4. **Páginas públicas** → portada, catálogo, detalle, login, registro
5. **Paneles por rol** → comprador (carrito, checkout, mis compras), vendedor, admin
6. **Estados** → *loading*, vacío, error, sin permiso

Cada paso se verifica en el navegador antes de pasar al siguiente. Si algo se rompe, sabes en qué
paso fue.

---

## 🗂️ 3. El orden de todo

| Fase | Qué | Depende de | Estado |
|---|---|---|---|
| **0. Proteger** | Backup de la base + tag `entrega-v1` | — | 🔴 **Urgente** |
| **1. Diseño** | Tokens → Stitch → Figma → código | Figma conectado | ⏳ |
| **2. Documentación** | NotebookLM (guion y material de la expo) | Fase 0 hecha | ⏳ |
| **3. Datos y despliegue** | Esquema nuevo + re-desplegar | Fase 0 + decisión de hosting | ⏳ |

**Por qué NotebookLM va después del backup y antes de la migración:** no toca el código, así que
puede hacerse en paralelo al diseño. Y conviene tenerlo listo **antes** de que el trial expire,
porque parte del material sale de lo que hoy está desplegado.

---

## 📚 4. NotebookLM: para qué sirve aquí

**No modifica código.** Es una herramienta de estudio y síntesis. Encaja en dos cosas:

| Entrada que le das | Salida que te da |
|---|---|
| Los documentos del proyecto (`README`, `ESPECIFICACION_TECNICA`, `ANALISIS_CSV_VS_BD`, `USUARIOS_PRUEBA`…) | Un **guion de presentación** ordenado |
| La especificación + el código | **FAQ** con las preguntas que te pueden hacer y sus respuestas |
| El dataset (`marketplace_datos.csv`) | Explicación del modelo de datos |

**Lo que hay que preparar antes:** hoy tienes **14 documentos `.md`** con solape entre ellos
(`EJECUCION_Y_DEMO` vs `GUIA_ENTREGA`, `GUIA_POSTMAN_MANUAL` vs `POSTMAN_DEMO_PASO_A_PASO`).
Para NotebookLM conviene **un documento maestro consolidado**, no 14 archivos sueltos. Eso lo puedo
generar.

---

## 🗄️ 5. Base de datos y servicios: la reorganización

### 5.1 Qué le falta al esquema actual

Hoy hay **10 tablas**. Según el análisis del dataset ([ANALISIS_CSV_VS_BD.md](ANALISIS_CSV_VS_BD.md)),
la especificación pide cosas que **no existen**:

| Falta | Impacto |
|---|---|
| `marcas` | Los productos no tienen marca |
| `direcciones` | **Las órdenes no guardan dirección de envío** |
| `cupones` | No hay descuentos |
| `favoritos` | No hay lista de deseos |
| `producto_imagenes` | Solo **una** imagen por producto |
| Perfil de vendedor | No hay nombre de tienda ni descripción |
| `pagos` como tabla | El pago vive en columnas de `orders` |

Y hay un hueco funcional: **no existe el endpoint para marcar `DELIVERED`**, así que una orden nunca
llega a `COMPLETED`.

### 5.2 Dónde alojar cuando expire el trial

| Opción | Coste | Esfuerzo | Cuándo conviene |
|---|---|---|---|
| **Seguir en Cloud SQL** | Pago (~10-50 USD/mes) | Ninguno | Si tienes crédito o presupuesto |
| **Postgres gratuito** (Neon, Supabase, Aiven) | 0 | Bajo: cambia `DB_HOST` y el `.env` | **Recomendado para la entrega** |
| **Docker local** | 0 | Bajo, pero solo para desarrollo | Día a día, no para presentar |

La buena noticia: el backend **no cambia**. Solo se reapunta `DB_HOST` (y el SSL, según el
proveedor). Todo lo demás sigue igual.

### 5.3 El orden correcto de la migración

1. **Backup** (fase 0) — sin esto no hay red
2. Crear la base nueva en el destino
3. Aplicar el **esquema nuevo**, con las tablas que faltan, mediante **migraciones versionadas**
   (Flyway/Liquibase) en vez de `ddl-auto: update`
4. Importar los datos del backup
5. Reapuntar el `.env` de cada PC y las variables de Cloud Run
6. **Verificar con las 39 aserciones** (`e2e-test.ps1 -BaseUrl ...`)

### 5.4 Si hay que re-desplegar

Cloud Run + Artifact Registry se recrean con lo que ya está documentado en
[DESPLIEGUE_CLOUD_RUN.md](DESPLIEGUE_CLOUD_RUN.md). Los dos tropiezos que ya conoces (permisos de la
cuenta de servicio y la contraseña) están documentados con su solución.

---

## ✅ 6. Lo que necesito de ti para arrancar

- [ ] **Arrancar la base** en Cloud SQL y esperar `RUNNABLE` (ahora está detenida)
- [ ] **Hacer el backup** (comando de la sección 0.1) y guardarlo fuera del repositorio
- [ ] **Crear el tag `entrega-v1`** y empujarlo
- [ ] **Decidir el orden**: ¿empezamos por Figma ya, o primero aseguras el backup?
- [ ] **Token + file key de Figma** (en `C:\Users\sebas\.figma-token`), o capturas para empezar
- [ ] **Decidir el alcance del rediseño**: ¿solo el aspecto visual, o también cambia la navegación
      y los flujos? (cambia mucho el esfuerzo)

Con la respuesta a esas seis cosas puedo empezar la fase que elijas.
