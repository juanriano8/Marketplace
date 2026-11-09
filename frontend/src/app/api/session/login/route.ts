import { NextResponse } from 'next/server';
import { API_URL, sessionResponse } from '@/lib/server-session';
import type { AuthResponse } from '@/lib/types';

export const runtime = 'nodejs';

/** BFF de login: el backend devuelve el JWT y aquí se convierte en cookie httpOnly. */
export async function POST(request: Request) {
  let body: { email?: string; password?: string };

  try {
    body = await request.json();
  } catch {
    return NextResponse.json({ detail: 'El cuerpo debe ser JSON válido' }, { status: 400 });
  }

  if (!body?.email || !body?.password) {
    return NextResponse.json(
      { detail: 'Correo y contraseña son obligatorios' },
      { status: 400 },
    );
  }

  let backend: Response;
  try {
    backend = await fetch(`${API_URL}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Accept: 'application/json' },
      body: JSON.stringify({ email: body.email, password: body.password }),
      cache: 'no-store',
    });
  } catch {
    return NextResponse.json(
      { detail: 'No se pudo contactar con la API. ¿Está arrancado el backend?' },
      { status: 502 },
    );
  }

  const data = (await backend.json().catch(() => null)) as AuthResponse | null;

  if (!backend.ok || !data?.accessToken) {
    return NextResponse.json(
      data ?? { detail: 'No se pudo iniciar sesión' },
      { status: backend.status || 502 },
    );
  }

  return sessionResponse(data);
}
