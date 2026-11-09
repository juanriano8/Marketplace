import { cookies } from 'next/headers';
import { NextResponse } from 'next/server';
import { SESSION_COOKIE, TOKEN_COOKIE, parseSessionCookie } from '@/lib/server-session';

export const runtime = 'nodejs';

/** Devuelve la sesión pública (sin el JWT) a partir de las cookies httpOnly. */
export async function GET() {
  const store = await cookies();
  const token = store.get(TOKEN_COOKIE)?.value;
  const session = parseSessionCookie(store.get(SESSION_COOKIE)?.value);

  if (!token || !session) {
    return NextResponse.json({ detail: 'No hay sesión activa' }, { status: 401 });
  }

  return NextResponse.json(session);
}
