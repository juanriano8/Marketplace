/**
 * Servidor de desarrollo de Next.js SIN crear procesos hijos.
 *
 * `next dev` lanza el servidor con `fork()`, y algunos entornos (sandbox de agentes,
 * antivirus corporativos, políticas de seguridad) bloquean esa operación con
 * "spawn EPERM". Este script usa la API programática de Next en el mismo proceso,
 * así que no necesita crear ningún proceso hijo.
 *
 * Uso:  node scripts/dev-server.mjs [puerto]
 */
import { createServer } from 'node:http';
import { parse } from 'node:url';
import { createRequire } from 'node:module';
import path from 'node:path';

const require = createRequire(import.meta.url);
const next = require('next');

const port = Number(process.argv[2] ?? process.env.PORT ?? 3000);
const dir = path.resolve(import.meta.dirname, '..');

const app = next({ dev: true, dir });
const handle = app.getRequestHandler();

await app.prepare();

createServer((req, res) => {
  handle(req, res, parse(req.url, true));
}).listen(port, () => {
  console.log(`▲ Next.js (modo sin fork) listo en http://localhost:${port}`);
});
