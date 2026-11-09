/**
 * Descarga la imagen y el HTML de una pantalla de Stitch.
 *
 * Los tool virtuales del CLI abortan, pero `get_screen` devuelve las URLs de
 * descarga directas; aqui se piden con la misma credencial.
 *
 * Uso: node stitch-download.mjs <url> <archivo-destino>
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';

const [, , url, destino] = process.argv;
if (!url || !destino) {
  console.error('Uso: node stitch-download.mjs <url> <destino>');
  process.exit(1);
}

// La clave puede estar en el archivo con el comando completo; se extrae el valor.
const crudo = readFileSync(join(homedir(), 'Desktop', 'stitch-key'), 'utf8');
const match = crudo.match(/-Value\s+"([^"]+)"/);
const key = (match ? match[1] : crudo).trim();

// Se prueban las dos formas de autenticacion habituales en las APIs de Google.
const cabeceras = [
  { 'X-Goog-Api-Key': key },
  { Authorization: `Bearer ${key}` },
];

for (const [indice, headers] of cabeceras.entries()) {
  const tipo = Object.keys(headers)[0];
  try {
    const res = await fetch(url, { headers, redirect: 'follow' });
    if (!res.ok) {
      console.log(`  [${indice}] ${tipo} -> HTTP ${res.status}`);
      continue;
    }
    const bytes = Buffer.from(await res.arrayBuffer());
    writeFileSync(destino, bytes);

    const esPng = bytes[0] === 0x89 && bytes[1] === 0x50;
    const esHtml = bytes.toString('utf8', 0, 15).toLowerCase().includes('<!doctype') ||
                   bytes.toString('utf8', 0, 15).toLowerCase().includes('<html');
    console.log(`  [OK] ${tipo} -> HTTP ${res.status}, ${(bytes.length / 1024).toFixed(1)} KB`);
    console.log(`       destino: ${destino}`);
    console.log(`       PNG=${esPng}  HTML=${esHtml}`);
    process.exit(0);
  } catch (error) {
    console.log(`  [${indice}] ${tipo} -> ERROR ${error.message}`);
  }
}

console.error('  No se pudo descargar con ninguna forma de autenticacion.');
process.exit(1);
