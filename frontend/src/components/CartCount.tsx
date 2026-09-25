'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth';
import type { Cart } from '@/lib/types';

/**
 * Contador de artículos del carrito en la barra de navegación.
 * Se refresca al cambiar de ruta y escucha el evento `cart:updated` que emiten
 * las páginas de carrito y checkout.
 */
export function CartCount() {
  const { session } = useAuth();
  const [count, setCount] = useState<number | null>(null);

  const load = useCallback(async () => {
    if (!session?.token) {
      setCount(null);
      return;
    }
    try {
      const cart = await apiFetch<Cart>('/api/v1/cart', { token: session.token });
      setCount(cart.totalItemCount);
    } catch {
      setCount(null);
    }
  }, [session?.token]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    const handler = () => void load();
    window.addEventListener('cart:updated', handler);
    return () => window.removeEventListener('cart:updated', handler);
  }, [load]);

  if (count === null || count === 0) {
    return null;
  }

  return (
    <Link
      href="/carrito"
      className="relative rounded-lg px-3 py-1.5 text-sm font-medium text-ink-600 hover:bg-ink-100"
    >
      🛒
      <span className="absolute -right-0.5 -top-0.5 grid h-5 min-w-5 place-items-center rounded-full bg-brand-600 px-1 text-[11px] font-bold text-white">
        {count}
      </span>
    </Link>
  );
}

/** Notifica a la barra de navegación que el carrito cambió. */
export function notifyCartUpdated() {
  window.dispatchEvent(new Event('cart:updated'));
}
