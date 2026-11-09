/**
 * Descarga las imagenes de producto desde Wikimedia Commons y las guarda en
 * frontend/public/images/productos/.
 *
 * Se guardan en el repositorio para que el panel las sirva el mismo: asi no
 * dependen de un tercero (ni de hotlinking) y funcionan igual en local y en
 * Cloud Run, porque van dentro de la imagen del contenedor.
 *
 * Uso: node design/descargar-imagenes-productos.mjs
 */
import { mkdirSync, writeFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';

const DESTINO = join(process.cwd(), '..', 'frontend', 'public', 'images', 'productos');

// slug -> termino de busqueda en Wikimedia Commons
const PRODUCTOS = [
  ['multimetro-digital-rms', 'digital multimeter'],
  ['camara-termica-mini', 'thermal imaging camera'],
  ['estacion-soldadura', 'soldering station'],
  ['atornillador-brushless', 'cordless drill'],
  ['medidor-laser-60m', 'laser rangefinder'],
  ['engrapadora-neumatica', 'pneumatic nail gun'],
  ['silla-ergonomica', 'office chair'],
  ['teclado-mecanico-rgb', 'mechanical keyboard'],
  ['audifonos-cancelacion', 'headphones'],
  ['cable-cobre-100m', 'copper wire cable'],
  ['cinta-aislante', 'electrical tape'],
  ['tuberia-pvc', 'pvc pipe'],
  ['laptop-14', 'laptop computer'],
  ['monitor-27-qhd', 'computer monitor'],
  ['mouse-inalambrico', 'computer mouse'],
];

mkdirSync(DESTINO, { recursive: true });

/** Busca en Commons y devuelve la URL de la miniatura de 800 px. */
async function buscarMiniatura(termino) {
  const url =
    'https://commons.wikimedia.org/w/api.php?action=query&generator=search' +
    `&gsrsearch=${encodeURIComponent('filetype:bitmap ' + termino)}` +
    '&gsrlimit=3&gsrnamespace=6&prop=imageinfo&iiprop=url|mime|size&iiurlwidth=800&format=json';

  const res = await fetch(url, { headers: { 'User-Agent': 'MarketplaceDemo/1.0 (proyecto academico)' } });
  if (!res.ok) return null;
  const json = await res.json();
  const pages = json?.query?.pages;
  if (!pages) return null;

  // Se prefiere una imagen apaisada y con tamano razonable.
  const candidatos = Object.values(pages)
    .map((p) => p?.imageinfo?.[0])
    .filter((i) => i && /^image\/(jpeg|png|webp)$/.test(i.mime))
    .sort((a, b) => (b.width || 0) * (b.height || 0) - (a.width || 0) * (a.height || 0));

  return candidatos[0]?.thumburl ?? null;
}

let ok = 0;
let fallos = 0;

for (const [slug, termino] of PRODUCTOS) {
  const destino = join(DESTINO, `${slug}.jpg`);
  if (existsSync(destino)) {
    console.log(`  [ya existe] ${slug}.jpg`);
    ok++;
    continue;
  }

  try {
    const miniatura = await buscarMiniatura(termino);
    if (!miniatura) {
      console.log(`  [sin resultado] ${slug} (${termino})`);
      fallos++;
      continue;
    }

    const img = await fetch(miniatura, { headers: { 'User-Agent': 'MarketplaceDemo/1.0 (proyecto academico)' } });
    if (!img.ok) {
      console.log(`  [error ${img.status}] ${slug}`);
      fallos++;
      continue;
    }

    const bytes = Buffer.from(await img.arrayBuffer());
    writeFileSync(destino, bytes);

    const esJpeg = bytes[0] === 0xff && bytes[1] === 0xd8;
    const esPng = bytes[0] === 0x89 && bytes[1] === 0x50;
    console.log(
      `  [ok] ${slug.padEnd(24)} ${(bytes.length / 1024).toFixed(1)} KB  ${esJpeg ? 'JPEG' : esPng ? 'PNG' : '?'}`,
    );
    ok++;
  } catch (error) {
    console.log(`  [error] ${slug}: ${error.message}`);
    fallos++;
  }

  // Cortesia con la API publica.
  await new Promise((r) => setTimeout(r, 300));
}

console.log(`\n  descargadas: ${ok}   fallos: ${fallos}`);
console.log(`  carpeta: ${DESTINO}`);
