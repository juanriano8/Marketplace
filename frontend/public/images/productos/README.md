# Imágenes de productos

Carpeta donde se guardan las imágenes de los productos del marketplace.

> **Las imágenes se suben solas desde el panel.** Entra como vendedor, abre **Nuevo producto** (o
> edita uno) y en la sección **Imagen del producto** elige un archivo. Se guarda aquí con un nombre
> seguro y su ruta (`/images/productos/...`) queda en el campo `imageUrl` del producto.
>
> El resto de este documento explica por qué funciona así y qué tener en cuenta.

## ⚠️ Importante: las imágenes viven en el disco local

| | |
|---|---|
| Los **datos** del producto (nombre, precio, categoría, ruta de la imagen) | ✅ van a **Cloud SQL**, compartida |
| Los **archivos** de imagen | ❌ quedan en `frontend/public/images/productos/` de **este PC** |

Es decir: si publicas un producto con imagen aquí y luego abres el proyecto en **otro PC**, la fila
del producto existirá (porque está en la base compartida) pero **la imagen saldrá rota**, porque el
archivo no viaja con la base de datos.

**Cómo solucionarlo:** sube la carpeta a Git.

```powershell
cd C:\Users\sebas\Desktop\marketplace\fullstack
git add frontend/public/images/productos
git commit -m "Imagenes de productos"
git push origin main
```

En el otro PC, un `git pull` las trae y las imágenes vuelven a verse. (La carpeta **no** está en
`.gitignore`, así que Git sí las versiona.)

> Para un despliegue real lo correcto sería **Cloud Storage**: subir el archivo a un bucket y guardar
> la URL pública. Requiere credenciales de GCP y no está implementado.

## Dónde está y por qué aquí

```
frontend/public/images/productos/
```

Next.js sirve automáticamente todo lo que esté en `frontend/public/` en la raíz del sitio. Por eso
una imagen guardada aquí como:

```
frontend/public/images/productos/audifonos-pro.jpg
```

queda accesible en el navegador en:

```
http://localhost:3000/images/productos/audifonos-pro.jpg
```

**No hay que configurar nada** ni tocar el backend: basta con poner esa ruta en el campo
`imageUrl` del producto y el panel web la mostrará.

## Cómo usarla

### Desde el panel web (recomendado)

1. Entra como vendedor → **Mi catálogo** → **Nuevo producto**
2. En **Imagen del producto** pulsa el selector de archivos y elige la imagen
3. Se sube al instante y verás la vista previa y la ruta generada
4. Rellena el resto y pulsa **Publicar producto**

Se aceptan **JPG, PNG, WebP y GIF** de hasta **3 MB**. El endpoint valida el tipo real del archivo
(no solo la extensión) y limpia el nombre para evitar rutas maliciosas.

### Desde la API

En `POST /api/v1/seller/products`, el campo `imageUrl` con la ruta que devuelve la subida:

```json
{
  "name": "Audifonos Inalambricos Pro",
  "price": 149.99,
  "stockQuantity": 25,
  "category": "Audio",
  "imageUrl": "/images/productos/audifonos-pro.jpg"
}
```

Para subir el archivo manualmente:

```powershell
curl.exe -X POST http://localhost:3000/api/upload `
  -H "Authorization: Bearer <token del vendedor>" `
  -F "file=@C:\ruta\a\mi-imagen.jpg"
# -> {"url":"/images/productos/mi-imagen-xxxxx.jpg","nombre":"...","bytes":12345}
```

### También acepta URLs externas

En el formulario hay un desplegable **"O pega una URL externa"**, y el campo sigue siendo libre:

```json
{ "imageUrl": "https://cdn.marketplace.com/productos/producto_1.jpg" }
```

## Convenciones recomendadas

| Aspecto | Recomendación |
|---|---|
| Formato | `.jpg` para fotos, `.png` si necesitas transparencia, `.webp` para menor peso |
| Tamaño | 800×800 px es suficiente para las tarjetas y el detalle |
| Peso | Menos de 300 KB por imagen (el máximo permitido es 3 MB) |
| Nombre | Da igual cómo se llame el archivo: el sistema lo normaliza a minúsculas y guiones |

> **Nota:** el modelo de datos actual soporta **una sola imagen por producto** (`products.image_url`).
> El dataset de referencia (`marketplace_datos.csv`, hoja `producto_imagenes`) contempla varias
> imágenes por producto, pero eso requeriría una tabla nueva. Ver
> [ANALISIS_CSV_VS_BD.md](../../../ANALISIS_CSV_VS_BD.md).

## Si prefieres que las sirva el backend

Hoy las sirve el frontend. Si necesitas servirlas desde el puerto 8080 (por ejemplo, para que las
consuma otro cliente), habría que añadir un manejador de recursos estáticos en Spring Boot y mover
la carpeta al backend. No está implementado.

## Carpeta relacionada

El archivo `.gitkeep` existe solo para que Git conserve esta carpeta aunque esté vacía. Puedes
borrarlo cuando agregues imágenes.

