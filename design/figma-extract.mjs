/**
 * Extrae la estructura y los design tokens de un archivo de Figma.
 *
 * Guarda las respuestas crudas en figma-data/ y muestra un resumen legible.
 *
 * Uso: node figma-extract.mjs <file-key>
 */
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { homedir } from 'node:os';
import { join } from 'node:path';

const API = 'https://api.figma.com/v1';
const key = process.argv[2];
if (!key) {
  console.error('Uso: node figma-extract.mjs <file-key>');
  process.exit(1);
}

const token = readFileSync(join(homedir(), '.figma-token'), 'utf8')
  .replace(/^\uFEFF/, '')
  .trim();
const cabeceras = { 'X-Figma-Token': token };

const datos = join(import.meta.dirname, 'figma-data');
mkdirSync(datos, { recursive: true });

/** Pide un endpoint y guarda la respuesta cruda. */
async function pedir(ruta, nombreArchivo) {
  const res = await fetch(`${API}${ruta}`, { headers: cabeceras });
  if (!res.ok) {
    console.log(`  ${nombreArchivo}: HTTP ${res.status}`);
    return null;
  }
  const json = await res.json();
  writeFileSync(join(datos, nombreArchivo), JSON.stringify(json, null, 2));
  return json;
}

/** Color de Figma (0..1) a #RRGGBB. */
function hex(c) {
  const n = (v) => Math.round((v ?? 0) * 255).toString(16).padStart(2, '0');
  return `#${n(c.r)}${n(c.g)}${n(c.b)}`.toUpperCase();
}

console.log('=== DESCARGANDO DATOS DE FIGMA ===\n');

// 1. Arbol del documento
const doc = await pedir(`/files/${key}?depth=3`, 'documento.json');
// 2. Estilos (colores, textos, efectos)
const estilos = await pedir(`/files/${key}/styles`, 'estilos.json');
// 3. Componentes
const componentes = await pedir(`/files/${key}/components`, 'componentes.json');
// 4. Variables locales (design tokens nativos) - puede requerir plan Enterprise
const variables = await pedir(`/files/${key}/variables/local`, 'variables.json');

// ---------------------------------------------------------------- estructura
if (doc) {
  console.log('=== PANTALLAS (frames de nivel superior) ===');
  for (const pagina of doc.document.children ?? []) {
    console.log(`\n  Pagina: ${pagina.name}`);
    for (const frame of pagina.children ?? []) {
      const caja = frame.absoluteBoundingBox ?? {};
      const w = Math.round(caja.width ?? 0);
      const h = Math.round(caja.height ?? 0);
      const hijos = frame.children?.length ?? 0;
      console.log(`    [${frame.type}] "${frame.name}"  ${w}x${h}  (${hijos} capas)`);
    }
  }

  // Recuento de tipos de capa, para saber que hay que implementar
  const tipos = new Map();
  const recorrer = (n) => {
    tipos.set(n.type, (tipos.get(n.type) ?? 0) + 1);
    for (const c of n.children ?? []) recorrer(c);
  };
  for (const p of doc.document.children ?? []) recorrer(p);
  console.log('\n=== TIPOS DE CAPA EN TODO EL ARCHIVO ===');
  [...tipos.entries()]
    .sort((a, b) => b[1] - a[1])
    .forEach(([t, n]) => console.log(`  ${t.padEnd(16)} ${n}`));
}

// ---------------------------------------------------------------- estilos
if (estilos) {
  const porTipo = (tipo) => Object.values(estilos.meta?.styles ?? {}).filter((s) => s.style_type === tipo);

  const rellenos = porTipo('FILL');
  if (rellenos.length) {
    console.log(`\n=== COLORES (${rellenos.length} estilos de relleno) ===`);
    for (const s of rellenos) {
      const def = estilos.meta.styles[s.node_id];
      console.log(`  ${(s.name ?? '').padEnd(28)} ${def?.description?.slice(0, 40) ?? ''}`);
    }
  }

  const textos = porTipo('TEXT');
  if (textos.length) {
    console.log(`\n=== ESTILOS DE TEXTO (${textos.length}) ===`);
    for (const s of textos) console.log(`  ${s.name}`);
  }

  const efectos = porTipo('EFFECT');
  if (efectos.length) {
    console.log(`\n=== EFECTOS / SOMBRAS (${efectos.length}) ===`);
    for (const s of efectos) console.log(`  ${s.name}`);
  }

  if (!rellenos.length && !textos.length) {
    console.log('\n  (el archivo no tiene estilos publicados; los colores van sueltos en las capas)');
  }
}

// ---------------------------------------------------------------- componentes
if (componentes) {
  const lista = Object.values(componentes.meta?.components ?? {});
  console.log(`\n=== COMPONENTES (${lista.length}) ===`);
  if (lista.length === 0) {
    console.log('  (ninguno: el diseno esta hecho con capas sueltas, no con componentes)');
  } else {
    const porNombre = new Map();
    for (const c of lista) porNombre.set(c.name, (porNombre.get(c.name) ?? 0) + 1);
    [...porNombre.entries()].forEach(([n, q]) => console.log(`  ${n}${q > 1 ? ` (x${q})` : ''}`));
  }
}

// ---------------------------------------------------------------- variables
if (variables && variables.meta) {
  const cols = Object.values(variables.meta.variables ?? {});
  console.log(`\n=== VARIABLES DE FIGMA (${cols.length}) ===`);
  for (const v of cols.slice(0, 40)) {
    const valor = v.valuesByMode ? Object.values(v.valuesByMode)[0] : '';
    const txt = typeof valor === 'object' && valor?.r !== undefined ? hex(valor) : String(valor);
    console.log(`  ${(v.name ?? '').padEnd(34)} ${txt}`);
  }
}

// ---------------------------------------------------------------- colores usados
if (doc) {
  const colores = new Map();
  const buscarColores = (n) => {
    for (const relleno of [...(n.fills ?? []), ...(n.strokes ?? [])]) {
      if (relleno.type === 'SOLID' && relleno.visible !== false) {
        const h = hex(relleno.color);
        const op = relleno.opacity ?? relleno.color?.a ?? 1;
        colores.set(h, (colores.get(h) ?? 0) + 1);
      }
    }
    for (const c of n.children ?? []) buscarColores(c);
  };
  for (const p of doc.document.children ?? []) buscarColores(p);

  console.log(`\n=== COLORES MAS USADOS (${colores.size} distintos) ===`);
  [...colores.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 20)
    .forEach(([h, n]) => console.log(`  ${h}   usado ${n} ${n === 1 ? 'vez' : 'veces'}`));
}

console.log(`\nDatos crudos guardados en: ${datos}`);
