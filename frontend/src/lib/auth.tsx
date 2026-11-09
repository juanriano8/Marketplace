'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';
import type { UserRole } from './types';

export type Session = {
  userId: string;
  email: string;
  role: UserRole;
  sellerApproved: boolean | null;
};

type AuthContextValue = {
  session: Session | null;
  /** true mientras se consulta la sesión al servidor (evita parpadeos al recargar). */
  loading: boolean;
  login: (email: string, password: string) => Promise<Session>;
  register: (kind: 'buyer' | 'seller', email: string, password: string) => Promise<Session>;
  logout: () => void;
  isAdmin: boolean;
  isSeller: boolean;
  isBuyer: boolean;
};

const AuthContext = createContext<AuthContextValue | null>(null);

/** Clave antigua: se borra para no dejar JWT en localStorage de versiones previas. */
const LEGACY_STORAGE_KEY = 'marketplace.session';

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;

    try {
      window.localStorage.removeItem(LEGACY_STORAGE_KEY);
    } catch {
      // localStorage puede estar bloqueado; no es crítico.
    }

    // La sesión se reconstruye desde las cookies httpOnly, nunca desde localStorage.
    apiFetch<Session>('/api/session')
      .then((value) => {
        if (active) setSession(value);
      })
      .catch(() => {
        if (active) setSession(null);
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const value = await apiFetch<Session>('/api/session/login', {
      method: 'POST',
      body: { email, password },
    });
    setSession(value);
    return value;
  }, []);

  const register = useCallback(
    async (kind: 'buyer' | 'seller', email: string, password: string) => {
      const value = await apiFetch<Session>(`/api/session/register/${kind}`, {
        method: 'POST',
        body: { email, password },
      });
      setSession(value);
      return value;
    },
    [],
  );

  const logout = useCallback(() => {
    setSession(null);
    void fetch('/api/session/logout', {
      method: 'POST',
      credentials: 'same-origin',
    }).catch(() => undefined);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      session,
      loading,
      login,
      register,
      logout,
      isAdmin: session?.role === 'ROLE_ADMIN',
      isSeller: session?.role === 'ROLE_SELLER',
      isBuyer: session?.role === 'ROLE_BUYER',
    }),
    [session, loading, login, register, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth debe usarse dentro de <AuthProvider>');
  }
  return context;
}
