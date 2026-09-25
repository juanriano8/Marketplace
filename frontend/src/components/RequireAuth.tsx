'use client';

import { useRouter } from 'next/navigation';
import { useEffect } from 'react';
import { useAuth } from '@/lib/auth';
import { Loading } from './feedback';
import type { UserRole } from '@/lib/types';

const HOME_BY_ROLE: Record<UserRole, string> = {
  ROLE_ADMIN: '/admin',
  ROLE_SELLER: '/vendedor',
  ROLE_BUYER: '/productos',
};

export function homeForRole(role: UserRole): string {
  return HOME_BY_ROLE[role] ?? '/';
}

/**
 * Protege una página: exige sesión y, opcionalmente, un rol concreto.
 *
 * Aunque el backend ya valida los permisos (una llamada sin token devuelve 401),
 * esto evita que el usuario vea una pantalla vacía o un error crudo.
 */
export function RequireAuth({
  role,
  children,
}: {
  role?: UserRole;
  children: React.ReactNode;
}) {
  const { session, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (loading) return;

    if (!session) {
      router.replace('/login');
      return;
    }

    // Un rol distinto se manda a su propio panel en lugar de mostrar un 403.
    if (role && session.role !== role) {
      router.replace(homeForRole(session.role));
    }
  }, [loading, session, role, router]);

  if (loading) {
    return <Loading label="Comprobando sesión…" />;
  }

  if (!session) {
    return <Loading label="Redirigiendo al inicio de sesión…" />;
  }

  if (role && session.role !== role) {
    return <Loading label="Redirigiendo a tu panel…" />;
  }

  return <>{children}</>;
}
