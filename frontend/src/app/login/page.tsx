'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useEffect, useState } from 'react';
import { useAuth } from '@/lib/auth';
import { errorMessage } from '@/lib/api';
import { ErrorBox } from '@/components/feedback';
import { homeForRole } from '@/components/RequireAuth';

export default function LoginPage() {
  const { login, session, loading } = useAuth();
  const router = useRouter();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  // Si ya hay sesión, se va directo a su panel.
  useEffect(() => {
    if (!loading && session) {
      router.replace(homeForRole(session.role));
    }
  }, [loading, session, router]);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      const value = await login(email.trim(), password);
      router.push(homeForRole(value.role));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md py-8">
      <div className="card p-6">
        <h1 className="text-xl font-bold text-ink-900">Iniciar sesión</h1>
        <p className="mt-1 text-sm text-ink-500">
          El sistema te llevará al panel que corresponde a tu rol.
        </p>

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          <div>
            <label className="label" htmlFor="email">
              Correo electrónico
            </label>
            <input
              id="email"
              type="email"
              className="input"
              placeholder="admin@marketplace.com"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              autoComplete="username"
            />
          </div>

          <div>
            <label className="label" htmlFor="password">
              Contraseña
            </label>
            <input
              id="password"
              type="password"
              className="input"
              placeholder="••••••••"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              autoComplete="current-password"
            />
          </div>

          {error && <ErrorBox>{error}</ErrorBox>}

          <button type="submit" className="btn btn-primary w-full" disabled={submitting}>
            {submitting ? 'Entrando…' : 'Entrar'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-ink-500">
          ¿No tienes cuenta?{' '}
          <Link href="/registro" className="font-semibold text-brand-600 hover:underline">
            Regístrate
          </Link>
        </p>
      </div>

      <div className="mt-4 rounded-lg border border-amber-200 bg-amber-50 p-4 text-xs text-amber-900">
        <p className="font-semibold">Credenciales de administrador</p>
        <p className="mt-1">
          No existe registro público de administradores. Usa el correo y la contraseña definidos en{' '}
          <code>backend/.env</code> (<code>BOOTSTRAP_ADMIN_EMAIL</code> /{' '}
          <code>BOOTSTRAP_ADMIN_PASSWORD</code>).
        </p>
      </div>
    </div>
  );
}
