/**
 * Comprueba la conexion con Figma y muestra la estructura del archivo.
 *
 * Uso:
 *   node figma-check.mjs <url-o-file-key> [ruta-al-token]
 *
 * Ejemplos:
 *   node figma-check.mjs https://www.figma.com/design/ABC123xyz/Marketplace
 *   node figma-check.mjs ABC123xyz
 *   node figma-check.mjs ABC123xyz C:\Users\sebas\.figma-token
 *
 * El token se busca en este orden:
 *   1. Variable de entorno FIGMA_TOKEN
 *   2. La ruta indicada como 2o argumento
 *   3. ~/.figma-token
 */
import { readFileSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';

const API = 'https://api.figma.com/v1';

/** Saca el file key de una URL de Figma o devuelve el valor tal cual. */
function extraerKey(entrada) {
  if (!entrada) return null;
  const m = entrada.match(/\/(?:design|file|board|proto)\/([A-Za-z0-9]+)/);
  return m ? m[1] : entrada.trim();
}

/** Carga el token desde entorno, archivo indicado o ~/.figma-token. */
function cargarToken(rutaIndicada) {
  /** Quita BOM y espacios: el Bloc de notas suele añadir un BOM al principio. */
  const limpiar = (v) => v.replace(/^\uFEFF/, '').trim();

  if (process.env.FIGMA_TOKEN) {
    return { token: limpiar(process.env.FIGMA_TOKEN), origen: 'variable FIGMA_TOKEN' };
  }
  const candidatas = [rutaIndicada, join(homedir(), '.figma-token')].filter(Boolean);
  for (const ruta of candidatas) {
    try {
      const valor = limpiar(readFileSync(ruta, 'utf8'));
      if (valor) return { token: valor, origen: ruta };
    } catch {
      // se prueba la siguiente
    }
  }
  return null;
}

/** Explica un fallo de la API en lenguaje util. */
function explicar(status) {
  switch (status) {
    case 403:
      return 'El token no tiene el scope "file_read", o no tienes acceso a ese archivo.';
    case 404:
      return 'El file key no existe o es incorrecto.';
    case 429:
      return 'Demasiadas peticiones (limite de Figma). Espera un minuto.';
    default:
      return `Figma respondio HTTP ${status}.`;
  }
}

const [, , entrada, rutaToken] = process.argv;

async function main() {
  if (!entrada) {
    console.error('Uso: node figma-check.mjs <url-o-file-key> [ruta-al-token]');
    console.error('');
    console.error('El file key es la parte de la URL:');
    console.error('  https://www.figma.com/design/AQUI_VA_EL_KEY/Marketplace');
    process.exitCode = 1;
    return;
  }

  const cargado = cargarToken(rutaToken);
  if (!cargado) {
    console.error('No encontre el token.');
    console.error('Crealo en Figma: menu de cuenta -> Settings -> Security -> Personal access tokens');
    console.error(`Guardalo en: ${join(homedir(), '.figma-token')}`);
    process.exitCode = 1;
    return;
  }

  const key = extraerKey(entrada);
  const cabeceras = { 'X-Figma-Token': cargado.token };

  console.log('=== CONEXION CON FIGMA ===');
  console.log(`  token : cargado desde ${cargado.origen} (${cargado.token.length} caracteres, oculto)`);
  console.log(`  clave : ${key}`);
  console.log('');

  try {
    // 1. Quien soy: valida el token
    const yo = await fetch(`${API}/me`, { headers: cabeceras });
    if (!yo.ok) {
      console.error(`  FALLO al validar el token: ${explicar(yo.status)}`);
      process.exitCode = 1;
      return;
    }
    const usuario = await yo.json();
    console.log('  --- Usuario ---');
    console.log(`  email : ${usuario.email ?? '(no visible)'}`);
    console.log(`  handle: ${usuario.handle ?? '(sin handle)'}`);
    console.log(`  id    : ${usuario.id}`);
    console.log('');

    // 2. El archivo: valida el acceso y muestra las paginas
    const fichero = await fetch(`${API}/files/${key}?depth=2`, { headers: cabeceras });
    if (!fichero.ok) {
      console.error(`  FALLO al abrir el archivo: ${explicar(fichero.status)}`);
      process.exitCode = 1;
      return;
    }
    const doc = await fichero.json();
    const paginas = (doc.document?.children ?? []).map((p) => p.name);

    console.log('  --- Archivo ---');
    console.log(`  nombre      : ${doc.name}`);
    console.log(`  modificado  : ${doc.lastModified}`);
    console.log(`  version     : ${doc.version}`);
    console.log(`  paginas (${paginas.length}) :`);
    for (const p of paginas) console.log(`     - ${p}`);
    console.log('');
    console.log('  CONEXION CORRECTA');
  } catch (error) {
    console.error(`  No se pudo conectar: ${error.message}`);
    console.error('  (en este entorno el HTTPS funciona desde Node, no desde curl)');
    process.exitCode = 1;
  }
}

await main();
