/**
 * Analisis profundo de un archivo de Figma: recorre todo el arbol y extrae
 * los tokens que de verdad se usan (colores, tipografias, radios, imagenes).
 *
 * Uso: node figma-deep.mjs <file-key>
 */
import { readFileSync, writeFileSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';

const API = 'https://api.figma.com/v1';
const key = process.argv[2];
if (!key) {
  console.error('Uso: node figma-deep.mjs <file-key>');
  process.exit(1);
}

const token = readFileSync(join(homedir(), '.figma-token'), 'utf8').replace(/^\uFEFF/, '').trim();
const res = await fetch(`${API}/files/${key}?depth=12`, { headers: { 'X-Figma-Token': token } });
if (!res.ok) {
  console.error(`HTTP ${res.status}`);
  process.exit(1);
}
const doc = await res.json();
writeFileSync(join(import.meta.dirname, 'figma-data', 'documento-completo.json'), JSON.stringify(doc, null, 2));

console.log(`Tamano del arbol: ${JSON.stringify(doc).length} bytes\n`);

function hex(c) {
  const n = (v) => Math.round((v ?? 0) * 255).toString(16).padStart(2, '0');
  return `#${n(c.r)}${n(c.g)}${n(c.b)}`.toUpperCase();
}

const tipos = new Map();
const colores = new Map();
const fuentes = new Map();
const tamanos = new Map();
const radios = new Map();
const imagenes = [];
let textos = 0;
let nodos = 0;

function recorrer(n, profundidad = 0) {
  nodos++;
  tipos.set(n.type, (tipos.get(n.type) ?? 0) + 1);

  // Colores de relleno y borde
  for (const relleno of [...(n.fills ?? []), ...(n.strokes ?? [])]) {
    if (relleno.type === 'SOLID' && relleno.visible !== false) {
      const h = hex(relleno.color);
      colores.set(h, (colores.get(h) ?? 0) + 1);
    }
    if (relleno.type === 'IMAGE') {
      imagenes.push({ nombre: n.name, tipo: n.type, ref: relleno.imageRef });
    }
  }

  // Tipografias de las capas de texto
  if (n.type === 'TEXT') {
    textos++;
    const f = n.style?.fontFamily ?? '(sin familia)';
    fuentes.set(f, (fuentes.get(f) ?? 0) + 1);
    const t = `${n.style?.fontWeight ?? '?'} / ${Math.round(n.style?.fontSize ?? 0)}px`;
    tamanos.set(t, (tamanos.get(t) ?? 0) + 1);
  }

  // Radios de esquina
  const r = n.cornerRadius;
  if (typeof r === 'number' && r > 0) radios.set(r, (radios.get(r) ?? 0) + 1);

  for (const c of n.children ?? []) recorrer(c, profundidad + 1);
}

for (const p of doc.document.children ?? []) recorrer(p);

console.log(`=== RESUMEN: ${nodos} nodos ===`);
console.log('');
console.log('=== TIPOS DE CAPA ===');
[...tipos.entries()].sort((a, b) => b[1] - a[1]).forEach(([t, c]) => console.log(`  ${t.padEnd(18)} ${c}`));

console.log(`\n=== TEXTO ===`);
console.log(`  capas de texto: ${textos}`);
if (fuentes.size) {
  console.log('  tipografias:');
  [...fuentes.entries()].forEach(([f, c]) => console.log(`     ${f.padEnd(24)} ${c} usos`));
  console.log('  tamanos (peso / px):');
  [...tamanos.entries()].sort((a, b) => b[1] - a[1]).slice(0, 15).forEach(([t, c]) => console.log(`     ${t.padEnd(24)} ${c} usos`));
}

console.log(`\n=== COLORES (${colores.size} distintos) ===`);
[...colores.entries()].sort((a, b) => b[1] - a[1]).slice(0, 25).forEach(([h, c]) => console.log(`  ${h}   ${String(c).padStart(4)} usos`));

console.log(`\n=== RADIOS DE ESQUINA ===`);
if (radios.size === 0) console.log('  ninguno');
else [...radios.entries()].sort((a, b) => b[1] - a[1]).forEach(([r, c]) => console.log(`  ${String(r).padStart(3)}px   ${c} usos`));

console.log(`\n=== IMAGENES ===`);
if (imagenes.length === 0) console.log('  ninguna');
else {
  console.log(`  ${imagenes.length} rellenos de imagen`);
  imagenes.slice(0, 10).forEach((i) => console.log(`     [${i.tipo}] "${i.nombre}"`));
}
