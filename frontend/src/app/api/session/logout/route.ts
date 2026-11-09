import { NextResponse } from 'next/server';
import { SESSION_COOKIE, TOKEN_COOKIE, sessionCookieOptions } from '@/lib/server-session';

export const runtime = 'nodejs';

/** Borra las cookies de sesión. */
export async function POST() {
  const response = NextResponse.json({ ok: true });
  response.cookies.set(TOKEN_COOKIE, '', sessionCookieOptions(0));
  response.cookies.set(SESSION_COOKIE, '', sessionCookieOptions(0));
  return response;
}
