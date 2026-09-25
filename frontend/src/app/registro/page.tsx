'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { errorMessage } from '@/lib/api';
import { ErrorBox, Info } from '@/components/feedback';
import { homeForRole } from '@/components/RequireAuth';

type Kind = 'buyer' | 'seller';

export default function RegisterPage() {
  const { register } = useAuth();
  const router = useRouter();
  const [kind, setKind] = useState<Kind>('buyer');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setError(null);

    if (password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres.');
      return;
    }
    if (password !== confirm) {
      setError('Las dos contraseñas no coinciden.');
      return;
    }

    setSubmitting(true);
    try {
      const session = await register(kind, email.trim(), password);
      router.push(homeForRole(session.role));
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="mx-auto max-w-md py-8">
      <div className="card p-6">
        <h1 className="text-xl font-bold text-ink-900">Crear cuenta</h1>
        <p className="mt-1 text-sm text-ink-500">Elige el tipo de cuenta que necesitas.</p>

        <div className="mt-4 grid grid-cols-2 gap-2">
          {(
            [
              { value: 'buyer', label: 'Comprador', hint: 'Comprar productos' },
              { value: 'seller', label: 'Vendedor', hint: 'Vender productos' },
            ] as const
          ).map((option) => (
            <button
              key={option.value}
              type="button"
              onClick={() => setKind(option.value)}
              className={`rounded-lg border p-3 text-left transition ${
                kind === option.value
                  ? 'border-brand-500 bg-brand-50 ring-1 ring-brand-500'
                  : 'border-ink-200 bg-white hover:bg-ink-50'
              }`}
            >
              <span className="block text-sm font-semibold text-ink-900">{option.label}</span>
              <span className="block text-xs text-ink-500">{option.hint}</span>
            </button>
          ))}
        </div>

        <form onSubmit={handleSubmit} className="mt-5 space-y-4">
          <div>
            <label className="label" htmlFor="email">
              Correo electrónico
            </label>
            <input
              id="email"
              type="email"
              className="input"
              placeholder="tu@correo.com"
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
              placeholder="Mínimo 8 caracteres"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              minLength={8}
              autoComplete="new-password"
            />
          </div>

          <div>
            <label className="label" htmlFor="confirm">
              Repite la contraseña
            </label>
            <input
              id="confirm"
              type="password"
              className="input"
              value={confirm}
              onChange={(event) => setConfirm(event.target.value)}
              required
              autoComplete="new-password"
            />
          </div>

          {error && <ErrorBox>{error}</ErrorBox>}

          {kind === 'seller' && (
            <Info>
              Las cuentas de vendedor nacen <strong>sin verificar</strong>. Un administrador debe
              aprobarlas antes de que puedas publicar productos.
            </Info>
          )}

          <button type="submit" className="btn btn-primary w-full" disabled={submitting}>
            {submitting ? 'Creando cuenta…' : 'Crear cuenta'}
          </button>
        </form>

        <p className="mt-4 text-center text-sm text-ink-500">
          ¿Ya tienes cuenta?{' '}
          <Link href="/login" className="font-semibold text-brand-600 hover:underline">
            Inicia sesión
          </Link>
        </p>
      </div>
    </div>
  );
}
