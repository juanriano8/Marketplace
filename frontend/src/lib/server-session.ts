import { NextResponse } from 'next/server';
import type { AuthResponse, UserRole } from './types';

/**
 * Utilidades de sesión del lado servidor (route handlers de Next).
 *
 * El JWT nunca llega al navegador: se guarda en una cookie httpOnly y las
 * llamadas a la API pasan por un proxy de Next que añade la cabecera
 * `Authorization`. Así un XSS no puede leer el token desde `localStorage`.
 */

export const API_URL = process.env.MARKETPLACE_API_URL ?? 'http://localhost:8080';

export const TOKEN_COOKIE = 'mp_token';
export const SESSION_COOKIE = 'mp_session';

export type SessionPayload = {
  userId: string;
  email: string;
  role: UserRole;
  sellerApproved: boolean | null;
};

/** Opciones comunes de las cookies de sesión. */
export function sessionCookieOptions(maxAge: number) {
  return {
    httpOnly: true,
    sameSite: 'lax' as const,
    secure: process.env.NODE_ENV === 'production',
    path: '/',
    maxAge,
  };
}

/**
 * Convierte la respuesta del backend en una respuesta del BFF:
 * guarda el JWT en cookie httpOnly y devuelve solo los datos públicos.
 */
export function sessionResponse(data: AuthResponse): NextResponse {
  const payload: SessionPayload = {
    userId: data.userId,
    email: data.email,
    role: data.role,
    sellerApproved: data.sellerApproved ?? null,
  };

  // El backend expresa `expiresIn` en milisegundos; las cookies usan segundos.
  const maxAge = Number.isFinite(data.expiresIn) && data.expiresIn > 0
    ? Math.floor(data.expiresIn / 1000)
    : 86_400;

  const response = NextResponse.json(payload);
  response.cookies.set(TOKEN_COOKIE, data.accessToken, sessionCookieOptions(maxAge));
  response.cookies.set(SESSION_COOKIE, JSON.stringify(payload), sessionCookieOptions(maxAge));
  return response;
}

/** Lee y valida el JSON mínimo que se guardó en la cookie de sesión. */
export function parseSessionCookie(raw: string | undefined): SessionPayload | null {
  if (!raw) return null;

  try {
    const value = JSON.parse(raw) as Partial<SessionPayload>;
    if (!value.userId || !value.email || !value.role) return null;
    return {
      userId: value.userId,
      email: value.email,
      role: value.role as UserRole,
      sellerApproved: value.sellerApproved ?? null,
    };
  } catch {
    return null;
  }
}
