import { NextResponse } from 'next/server';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';

/**
 * Subida de imágenes de producto.
 *
 * Guarda el archivo en `frontend/public/images/productos/` y devuelve la ruta
 * pública (`/images/productos/...`), que es la que se guarda en el campo
 * `imageUrl` del producto. Next.js sirve automáticamente todo lo que hay en
 * `public/`, así que la imagen queda visible sin configurar nada más.
 *
 * IMPORTANTE: el archivo se escribe en el DISCO LOCAL del PC donde corre el
 * frontend. La fila del producto sí va a Cloud SQL (compartida), pero la imagen
 * no. Si vas a ejecutar el proyecto en otro PC, sube la carpeta
 * `frontend/public/images/productos/` a Git para que las imágenes viajen.
 */

// Necesita el runtime de Node para poder escribir en disco.
export const runtime = 'nodejs';

const TAMANO_MAXIMO = 3 * 1024 * 1024; // 3 MB

/** Tipos aceptados y su extensión. */
const TIPOS: Record<string, string> = {
  'image/jpeg': 'jpg',
  'image/png': 'png',
  'image/webp': 'webp',
  'image/gif': 'gif',
};

/**
 * Comprueba la firma real del archivo (magic bytes), no solo el tipo que
 * declara el navegador, que se puede falsificar.
 */
function firmaValida(bytes: Uint8Array): boolean {
  if (bytes.length < 12) return false;

  const esJpeg = bytes[0] === 0xff && bytes[1] === 0xd8 && bytes[2] === 0xff;
  const esPng =
    bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4e && bytes[3] === 0x47;
  const esGif = bytes[0] === 0x47 && bytes[1] === 0x49 && bytes[2] === 0x46;
  // WebP: "RIFF" .... "WEBP"
  const esWebp =
    bytes[0] === 0x52 &&
    bytes[1] === 0x49 &&
    bytes[2] === 0x46 &&
    bytes[3] === 0x46 &&
    bytes[8] === 0x57 &&
    bytes[9] === 0x45 &&
    bytes[10] === 0x42 &&
    bytes[11] === 0x50;

  return esJpeg || esPng || esGif || esWebp;
}

/** Deja solo letras, números y guiones, para evitar rutas maliciosas. */
function nombreSeguro(nombreOriginal: string): string {
  const sinExtension = nombreOriginal.replace(/\.[^.]+$/, '');
  const limpio = sinExtension
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '') // quita acentos
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .slice(0, 60);

  return limpio || 'producto';
}

export async function POST(request: Request) {
  try {
    // El panel siempre envía el token del vendedor. Se exige su presencia para
    // que el endpoint no quede completamente abierto.
    const autorizacion = request.headers.get('authorization');
    if (!autorizacion || !autorizacion.toLowerCase().startsWith('bearer ')) {
      return NextResponse.json(
        { detail: 'Se requiere un token de sesión para subir imágenes' },
        { status: 401 },
      );
    }

    const formulario = await request.formData();
    const archivo = formulario.get('file');

    if (!(archivo instanceof File)) {
      return NextResponse.json({ detail: 'No se recibió ningún archivo' }, { status: 400 });
    }

    if (archivo.size === 0) {
      return NextResponse.json({ detail: 'El archivo está vacío' }, { status: 400 });
    }

    if (archivo.size > TAMANO_MAXIMO) {
      return NextResponse.json(
        {
          detail: `La imagen pesa ${(archivo.size / 1024 / 1024).toFixed(1)} MB y el máximo es 3 MB`,
        },
        { status: 413 },
      );
    }

    const extension = TIPOS[archivo.type];
    if (!extension) {
      return NextResponse.json(
        { detail: 'Formato no permitido. Usa JPG, PNG, WebP o GIF' },
        { status: 415 },
      );
    }

    const bytes = new Uint8Array(await archivo.arrayBuffer());

    if (!firmaValida(bytes)) {
      return NextResponse.json(
        { detail: 'El archivo no parece una imagen válida' },
        { status: 415 },
      );
    }

    const nombre = `${nombreSeguro(archivo.name)}-${Date.now().toString(36)}.${extension}`;
    // La ruta se escribe de forma estática (sin variables) para que el empaquetador
    // sepa exactamente qué carpeta se usa y no arrastre todo el proyecto al build.
    const carpeta = path.join(process.cwd(), 'public', 'images', 'productos');

    await mkdir(carpeta, { recursive: true });
    await writeFile(path.join(carpeta, nombre), bytes);

    return NextResponse.json({
      url: `/images/productos/${nombre}`,
      nombre,
      bytes: archivo.size,
    });
  } catch (error) {
    const detalle = error instanceof Error ? error.message : 'error desconocido';
    return NextResponse.json(
      { detail: `No se pudo guardar la imagen: ${detalle}` },
      { status: 500 },
    );
  }
}
