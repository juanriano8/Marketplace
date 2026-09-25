'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';
import type { AuthResponse, UserRole } from './types';

const STORAGE_KEY = 'marketplace.session';

export type Session = {
  token: string;
  userId: string;
  email: string;
  role: UserRole;
  sellerApproved: boolean | null;
};

type AuthContextValue = {
  session: Session | null;
  /** true mientras se lee la sesión guardada (evita parpadeos al recargar). */
  loading: boolean;
  login: (email: string, password: string) => Promise<Session>;
  register: (kind: 'buyer' | 'seller', email: string, password: string) => Promise<Session>;
  logout: () => void;
  isAdmin: boolean;
  isSeller: boolean;
  isBuyer: boolean;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function toSession(response: AuthResponse): Session {
  return {
    token: response.accessToken,
    userId: response.userId,
    email: response.email,
    role: response.role,
    sellerApproved: response.sellerApproved,
  };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [session, setSession] = useState<Session | null>(null);
  const [loading, setLoading] = useState(true);

  // La sesión se guarda en el navegador para sobrevivir a recargas.
  useEffect(() => {
    try {
      const stored = window.localStorage.getItem(STORAGE_KEY);
      if (stored) {
        setSession(JSON.parse(stored) as Session);
      }
    } catch {
      window.localStorage.removeItem(STORAGE_KEY);
    } finally {
      setLoading(false);
    }
  }, []);

  const persist = useCallback((value: Session | null) => {
    setSession(value);
    if (value) {
      window.localStorage.setItem(STORAGE_KEY, JSON.stringify(value));
    } else {
      window.localStorage.removeItem(STORAGE_KEY);
    }
  }, []);

  const login = useCallback(
    async (email: string, password: string) => {
      const response = await apiFetch<AuthResponse>('/api/v1/auth/login', {
        method: 'POST',
        body: { email, password },
      });
      const value = toSession(response);
      persist(value);
      return value;
    },
    [persist],
  );

  const register = useCallback(
    async (kind: 'buyer' | 'seller', email: string, password: string) => {
      const response = await apiFetch<AuthResponse>(`/api/v1/auth/register/${kind}`, {
        method: 'POST',
        body: { email, password },
      });
      const value = toSession(response);
      persist(value);
      return value;
    },
    [persist],
  );

  const logout = useCallback(() => persist(null), [persist]);

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
