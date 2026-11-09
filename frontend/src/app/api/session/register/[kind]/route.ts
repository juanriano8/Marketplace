import { NextResponse } from 'next/server';
import { API_URL, sessionResponse } from '@/lib/server-session';
import type { AuthResponse } from '@/lib/types';

export const runtime = 'nodejs';

/** BFF de registro: crea la cuenta y deja la sesión en cookies httpOnly. */
export async function POST(
  request: Request,
  { params }: { params: Promise<{ kind: string }> },
) {
  const { kind } = await params;

  if (kind !== 'buyer' && kind !== 'seller') {
    return NextResponse.json(
      { detail: 'Tipo de registro no válido' },
      { status: 400 },
    );
  }

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
    backend = await fetch(`${API_URL}/api/v1/auth/register/${kind}`, {
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
      data ?? { detail: 'No se pudo completar el registro' },
      { status: backend.status || 502 },
    );
  }

  return sessionResponse(data);
}
