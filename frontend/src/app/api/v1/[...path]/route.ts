import { cookies } from 'next/headers';
import { NextRequest, NextResponse } from 'next/server';
import { API_URL, TOKEN_COOKIE } from '@/lib/server-session';

export const runtime = 'nodejs';

/**
 * Proxy del panel hacia la API.
 *
 * El navegador nunca ve el JWT: llama a `/api/v1/**` en el mismo origen, este
 * route handler lee la cookie httpOnly y añade `Authorization` al reenviar.
 * Los endpoints públicos siguen funcionando sin cookie; los protegidos
 * responden 401 desde el backend.
 */

const REQUEST_HEADERS = ['accept', 'content-type', 'if-none-match', 'if-modified-since'];
const RESPONSE_HEADERS = [
  'content-type',
  'cache-control',
  'etag',
  'last-modified',
  'retry-after',
  'www-authenticate',
];

async function proxy(
  request: NextRequest,
  context: { params: Promise<{ path: string[] }> },
) {
  const { path } = await context.params;

  // Defensa CSRF básica: si el navegador envía Origin, debe ser el mismo host.
  const origin = request.headers.get('origin');
  if (origin) {
    try {
      if (new URL(origin).host !== new URL(request.url).host) {
        return NextResponse.json({ detail: 'Origen no permitido' }, { status: 403 });
      }
    } catch {
      return NextResponse.json({ detail: 'Origen no válido' }, { status: 400 });
    }
  }

  const token = (await cookies()).get(TOKEN_COOKIE)?.value;

  const target = new URL(`${API_URL}/api/v1/${path.join('/')}`);
  target.search = new URL(request.url).search;

  const headers = new Headers();
  for (const name of REQUEST_HEADERS) {
    const value = request.headers.get(name);
    if (value) headers.set(name, value);
  }
  if (token) {
    headers.set('authorization', `Bearer ${token}`);
  }

  const hasBody = request.method !== 'GET' && request.method !== 'HEAD';
  const body = hasBody ? await request.arrayBuffer() : undefined;

  let backend: Response;
  try {
    backend = await fetch(target, {
      method: request.method,
      headers,
      body,
      cache: 'no-store',
      redirect: 'manual',
    });
  } catch {
    return NextResponse.json(
      { detail: 'No se pudo contactar con la API. ¿Está arrancado el backend?' },
      { status: 502 },
    );
  }

  const responseHeaders = new Headers();
  for (const name of RESPONSE_HEADERS) {
    const value = backend.headers.get(name);
    if (value) responseHeaders.set(name, value);
  }


  return new NextResponse(backend.body, {
    status: backend.status,
    headers: responseHeaders,
  });
}

export const GET = proxy;
export const POST = proxy;
export const PUT = proxy;
export const PATCH = proxy;
export const DELETE = proxy;
