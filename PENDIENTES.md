# Pendientes y cómo retomar el proyecto

Última actualización: sesión de verificación contra Google Cloud SQL.

---

## 1. Estado actual

| | |
|---|---|
| **Backend** | ✅ Arrancado y respondiendo en <http://localhost:8080> |
| **Frontend** | ✅ Implementado en `frontend/` (Next.js 16) — falta abrirlo en el navegador |
| **Base de datos** | ✅ Google Cloud SQL (`marketplace_db`), 10 tablas |
| **Compilación backend** | ✅ `BUILD SUCCESSFUL` |
| **Tests backend** | ✅ 114 tests, 0 fallos |
| **Endpoints backend** | ✅ 31 operaciones, verificadas contra Cloud SQL |
| **Tipos frontend** | ✅ `tsc --noEmit` sin errores |
| **Admin** | `admin@marketplace.com` (contraseña en `backend/.env`) |
| **Datos en la base** | 6 productos, 6 vendedores verificados, órdenes y reseñas de las pruebas |

---

## 2. Verlo ahora mismo

### Panel web (frontend)

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

Abre **<http://localhost:3000>** (el backend debe estar corriendo en el 8080).

### API directa (Swagger UI)

| Recurso | URL | Para qué |
|---|---|---|
| **Swagger UI** | <http://localhost:8080/swagger-ui.html> | **Probar los endpoints desde el navegador, sin Postman** |
| OpenAPI JSON | <http://localhost:8080/v3/api-docs> | Importar en Postman o Insomnia |
| Health | <http://localhost:8080/actuator/health> | Confirmar que está viva |

### Cómo probar en Swagger UI (sin instalar nada)

1. Abre <http://localhost:8080/swagger-ui.html>
2. Despliega **Authentication** → `POST /api/v1/auth/login` → **Try it out**
3. Pega las credenciales del admin (`backend/.env`) y ejecuta
4. Copia el `accessToken` de la respuesta (sin las comillas)
5. Arriba a la derecha pulsa **Authorize**, pega el token y acepta
6. Ya puedes ejecutar **cualquier** endpoint desde el navegador

---

## 3. Cómo volver a arrancarlo otro día

La aplicación se detiene al cerrar la terminal o al reiniciar el PC. Para volver:

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1
```

Espera a ver `Started MarketplaceApplication` (unos 12 segundos) y abre
<http://localhost:8080/swagger-ui.html>.

### Si algo no arranca

| Síntoma | Causa y solución |
|---|---|
| `App did not start` / error de conexión | La IP de este PC cambió: vuelve a autorizarla en Cloud SQL (ver §6) |
| Algo ya ocupa el puerto 8080 | `.\run.ps1 -Port 9090` |
| `JAVA_HOME no apunta a un JDK válido` | Reinstala JDK 21: `winget install EclipseAdoptium.Temurin.21.JDK` |
| Login del admin da 401 | La contraseña del `.env` no coincide con la de la base → ver §10.3.1 de la guía |
| La base no tiene las tablas | Debe apuntar a `marketplace_db`, **nunca** a `postgres` (tiene un esquema viejo incompatible) |

### Comprobación rápida de todo

```powershell
cd backend
powershell -ExecutionPolicy Bypass -File .\tools\verify.ps1
```

---

## 4. Pendientes

### ✅ 4.1 Guía manual de peticiones para Postman — **HECHA**

Está en **[GUIA_POSTMAN_MANUAL.md](GUIA_POSTMAN_MANUAL.md)**: los 30 endpoints listos para copiar y
pegar, con método, URL, cabeceras, cuerpo JSON, respuesta de ejemplo, códigos HTTP reales, errores
de validación, chuleta de roles y el orden recomendado para la demo.

### ✅ 4.2 Parte visual (frontend) — **IMPLEMENTADA, FALTA ABRIRLA EN EL NAVEGADOR**

Está en **`frontend/`**: **Next.js 16 + TypeScript + Tailwind CSS 4**, con **15 páginas** y tres
paneles según el rol. Documentación completa en [frontend/README.md](frontend/README.md).

```powershell
# Terminal 1 (ya está corriendo)
cd C:\Users\sebas\Desktop\marketplace\fullstack\backend
.\run.ps1

# Terminal 2
cd C:\Users\sebas\Desktop\marketplace\fullstack\frontend
npm run dev
```

Luego abre **<http://localhost:3000>**

| Panel | Pantallas |
|---|---|
| Comprador | Catálogo, detalle con reseñas, carrito, checkout, mis compras |
| Vendedor | Mi catálogo (crear/editar), inventario (ajustes), despachos (guías) |
| Administrador | Moderación de productos, verificación de vendedores, órdenes, auditoría de inventario |

**Verificado:** `npm install` (41 paquetes, 0 vulnerabilidades), `tsc --noEmit` con **0 errores** y
`next build` → `Compiled successfully` con las 12 rutas generadas.

**Pendiente:** abrirlo en el navegador. No pude hacerlo desde el entorno del agente porque su
sandbox bloquea la creación de procesos hijos con tuberías, que es justo lo que Next.js usa para
`next dev` (`spawn EPERM`) y para el comprobador de tipos del `next build`. En una terminal normal
no ocurre.

Al abrirlo por primera vez conviene este recorrido:

1. Entra como **admin** (credenciales del `backend/.env`) → verás la portada de moderación.
2. **Vendedores** → comprueba que aparecen los 6 vendedores verificados de las pruebas.
3. **Registro** → crea un vendedor nuevo, y verifícalo desde ese mismo panel.
4. Inicia sesión como ese vendedor → **Mi catálogo** → publica un producto.
5. Vuelve como admin → **Moderación** → apruébalo.
6. **Registro** → crea un comprador → catálogo → añade al carrito → **checkout** y paga.
7. Como vendedor → **Despachos** → registra la guía.
8. Como comprador → detalle del producto → **deja la reseña** (compra verificada).

### 🟡 4.3 Datos de ejemplo en la base de datos — **PENDIENTE**

La base ya tiene datos de las pruebas (6 productos, usuarios, órdenes y reseñas), así que la demo
funciona. Si quieres datos controlados y "bonitos", falta un `tools/seed.ps1` que cree por la API:
2-3 vendedores verificados, 6-8 productos aprobados (uno con **1 unidad** para lucir el control de
stock y otro con **stock 0**), 2 compradores, una orden **multi-vendedor** y 2-3 reseñas.

### 🟢 4.4 Subir los cambios a GitHub — **PENDIENTE**

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack
git add -A
git commit -m "Frontend Next.js con paneles por rol, endpoint de listado de vendedores y guia manual de Postman"
git push origin main
git status -sb        # no debe decir "ahead"
```

> Los commits anteriores **sí** están en el remoto. Lo que falta es este trabajo: el frontend
> completo, el endpoint nuevo del backend y los documentos.

### 🟢 4.5 Mejoras opcionales (no bloquean nada)

| Idea | Detalle |
|---|---|
| Endpoint para marcar `DELIVERED` | Hoy no existe, así que una orden nunca llega a `COMPLETED` |
| Rate limiting con Bucket4j | La dependencia ya está declarada, falta integrarla |
| Resilience4j en la pasarela de pago | Ídem |
| Memorystore Redis | Para caché de catálogo |
| Pasarela de pago real | Sustituir `StubPaymentGatewayAdapter` (el puerto ya está definido) |
| Subida de imágenes a Cloud Storage | Hoy el producto guarda una URL externa |

---

## 5. Datos para no perderlos

| Dato | Valor |
|---|---|
| Repositorio | `https://github.com/juanriano8/Marketplace` |
| Instancia Cloud SQL | `marketplace-509503:us-central1:marketplace` |
| IP pública de la BD | `136.112.91.42` (puerto 5432) |
| Base de datos | `marketplace_db` (PostgreSQL 18.6) |
| Usuario de BD | `postgres` |
| Contraseñas | En `backend/.env` (ignorado por Git) |
| Admin de la app | `admin@marketplace.com` |
| Documentación | `ESPECIFICACION_TECNICA.md`, `EJECUCION_Y_DEMO.md`, `GUIA_POSTMAN_MANUAL.md` |

⚠️ **No apuntes la aplicación a la base `postgres`** de la instancia: contiene 23 tablas de otro
proyecto con claves primarias `bigint`, incompatible con este modelo.

---

## 6. Autorizar la IP de un PC nuevo

La conexión es directa por IP pública, así que cada PC (y cada cambio de red) necesita
autorización:

1. Mira tu IP pública: <https://whatismyipaddress.com/>
2. <https://console.cloud.google.com/sql> → instancia **`marketplace`** →
   **Connections → Networking → Authorized networks → Add network**
3. Añade la IP y guarda (tarda ~1 minuto)

Si añades `0.0.0.0/0` funciona desde cualquier sitio, pero es menos seguro: quítalo cuando
termines la demo.

---

## 7. Herramientas incluidas

| Archivo | Para qué |
|---|---|
| `backend/run.ps1` | Arrancar la API (lee `.env`, localiza el JDK 21) |
| `backend/run.sh` | Lo mismo en Linux / macOS / WSL |
| `backend/setup.ps1` | Configuración inicial en un PC nuevo |
| `backend/tools/verify.ps1` | Comprobación completa antes de presentar |
| `backend/tools/e2e-test.ps1` | Recorrido de los 27 endpoints (39 aserciones) |
| `backend/tools/stock-test.ps1` | Prueba de antisobreventa |
| `backend/tools/split-test.ps1` | Prueba de orden multi-vendedor y BOLA |
| `backend/tools/AdminPasswordSync.java` | Sincronizar la contraseña del admin con la BD |
| `backend/postman/Marketplace-API.postman_collection.json` | 44 peticiones listas para Postman |
| `GUIA_POSTMAN_MANUAL.md` | Referencia para escribir las peticiones a mano |
| `frontend/` | Panel web Next.js con los tres roles |
| `frontend/README.md` | Cómo arrancar y usar el frontend |

> Ejecuta los `.ps1` con `powershell -ExecutionPolicy Bypass -File .\ruta\script.ps1` si tu
> política de ejecución los bloquea.
