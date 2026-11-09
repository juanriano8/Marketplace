import { PutObjectCommand, S3Client } from '@aws-sdk/client-s3';
import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { TOKEN_COOKIE } from '@/lib/server-session';

/**
 * Subida de imágenes de producto.
 *
 * Guarda el archivo en `frontend/public/images/productos/` y devuelve la ruta
 * pública (`/images/productos/...`), que es la que se guarda en el campo
 * `imageUrl` del producto. Next.js sirve automáticamente todo lo que hay en
 * `public/`, así que la imagen queda visible sin configurar nada más.
 *
 * IMPORTANTE: el archivo se escribe en el DISCO LOCAL del PC donde corre el
 * frontend. La fila del producto sí va a Neon (compartida), pero la imagen
 * no. Si vas a ejecutar el proyecto en otro PC, sube la carpeta
 * `frontend/public/images/productos/` a Git para que las imágenes viajen.
 */

// Necesita el runtime de Node para poder escribir en disco.
export const runtime = 'nodejs';

const TAMANO_MAXIMO = 3 * 1024 * 1024; // 3 MB

// ---------------------------------------------------------------------------
// Almacenamiento de imágenes
// ---------------------------------------------------------------------------
// En local, sin variables de Neon Object Storage, se guarda en
// `frontend/public/images/productos` (como hasta ahora).
// En Vercel/cloud se usa Neon Object Storage (S3 compatible, forcePathStyle).
const STORAGE_ENDPOINT = process.env.AWS_ENDPOINT_URL_S3?.replace(/\/+$/, '');
const STORAGE_BUCKET = process.env.NEON_STORAGE_BUCKET;
const STORAGE_REGION = process.env.AWS_REGION;
const STORAGE_ACCESS_KEY = process.env.AWS_ACCESS_KEY_ID;
const STORAGE_SECRET_KEY = process.env.AWS_SECRET_ACCESS_KEY;

const storageConfigured = Boolean(
  STORAGE_ENDPOINT &&
    STORAGE_BUCKET &&
    STORAGE_REGION &&
    STORAGE_ACCESS_KEY &&
    STORAGE_SECRET_KEY,
);

const s3 = storageConfigured
  ? new S3Client({
      region: STORAGE_REGION,
      endpoint: STORAGE_ENDPOINT,
      forcePathStyle: true,
      credentials: {
        accessKeyId: STORAGE_ACCESS_KEY!,
        secretAccessKey: STORAGE_SECRET_KEY!,
      },
      // Neon Object Storage no implementa todos los checksums opcionales del SDK.
      requestChecksumCalculation: 'WHEN_REQUIRED',
      responseChecksumValidation: 'WHEN_REQUIRED',
    })
  : null;

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
    // La sesión viaja en una cookie httpOnly; el navegador ya no envía el JWT
    // en la cabecera Authorization.
    const token = (await cookies()).get(TOKEN_COOKIE)?.value;
    if (!token) {
      return NextResponse.json(
        { detail: 'Se requiere una sesión para subir imágenes' },
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

    if (s3 && STORAGE_BUCKET && STORAGE_ENDPOINT) {
      const key = `productos/${nombre}`;
      await s3.send(
        new PutObjectCommand({
          Bucket: STORAGE_BUCKET,
          Key: key,
          Body: bytes,
          ContentType: archivo.type,
          CacheControl: 'public, max-age=31536000, immutable',
        }),
      );

      return NextResponse.json({
        url: `${STORAGE_ENDPOINT}/${STORAGE_BUCKET}/${key}`,
        nombre,
        bytes: archivo.size,
        storage: 'neon',
      });
    }

    if (process.env.VERCEL) {
      return NextResponse.json(
        {
          detail:
            'Falta configurar Neon Object Storage en Vercel: AWS_ENDPOINT_URL_S3, AWS_REGION, AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY y NEON_STORAGE_BUCKET.',
        },
        { status: 503 },
      );
    }

    // Fallback local para desarrollo.
    // La ruta se escribe de forma estática (sin variables) para que el empaquetador
    // sepa exactamente qué carpeta se usa y no arrastre todo el proyecto al build.
    const carpeta = path.join(process.cwd(), 'public', 'images', 'productos');

    await mkdir(carpeta, { recursive: true });
    await writeFile(path.join(carpeta, nombre), bytes);

    return NextResponse.json({
      url: `/images/productos/${nombre}`,
      nombre,
      bytes: archivo.size,
      storage: 'local',
    });
  } catch (error) {
    const detalle = error instanceof Error ? error.message : 'error desconocido';
    return NextResponse.json(
      { detail: `No se pudo guardar la imagen: ${detalle}` },
      { status: 500 },
    );
  }
}
