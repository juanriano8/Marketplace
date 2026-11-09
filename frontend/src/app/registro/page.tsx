'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { useRef, useState } from 'react';
import { ShoppingBag, Store } from 'lucide-react';
import { useAuth } from '@/lib/auth';
import { errorMessage } from '@/lib/api';
import { ErrorState, Info } from '@/components/feedback';
import { AuthShell } from '@/components/AuthShell';
import { homeForRole } from '@/components/RequireAuth';

type Kind = 'buyer' | 'seller';

export default function RegisterPage() {
  const { register } = useAuth();
  const formRef = useRef<HTMLFormElement>(null);
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
    <AuthShell
      title="Crear cuenta"
      subtitle="Elige el tipo de cuenta que necesitas para empezar."
    >
      <div className="grid grid-cols-2 gap-2">
        {(
          [
            { value: 'buyer', label: 'Comprador', hint: 'Comprar productos', icon: ShoppingBag },
            { value: 'seller', label: 'Vendedor', hint: 'Vender productos', icon: Store },
          ] as const
        ).map((option) => {
          const Icon = option.icon;
          const active = kind === option.value;
          return (
            <button
              key={option.value}
              type="button"
              onClick={() => setKind(option.value)}
              className={`rounded-lg border p-3 text-left transition ${
                active
                  ? 'border-brand-500 bg-brand-50 ring-1 ring-brand-500'
                  : 'border-ink-200 bg-white hover:bg-ink-50'
              }`}
            >
              <Icon className={`h-4 w-4 ${active ? 'text-brand-700' : 'text-ink-400'}`} />
              <span className="mt-2 block text-sm font-semibold text-ink-900">{option.label}</span>
              <span className="block text-[11px] text-ink-500">{option.hint}</span>
            </button>
          );
        })}
      </div>

      <form ref={formRef} onSubmit={handleSubmit} className="mt-5 space-y-4">
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

        {error && <ErrorState message={error} onRetry={() => formRef.current?.requestSubmit()} />}

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
    </AuthShell>
  );
}
