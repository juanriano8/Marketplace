'use client';

import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { useFavorites } from '@/lib/favorites';

/** Corazón para guardar/quitar un producto de favoritos. */
export function FavoriteButton({
  productId,
  className = '',
}: {
  productId: string;
  className?: string;
}) {
  const { session } = useAuth();
  const { isFavorite, toggle } = useFavorites();
  const [busy, setBusy] = useState(false);

  if (session?.role !== 'ROLE_BUYER') {
    return null;
  }

  const active = isFavorite(productId);

  return (
    <button
      type="button"
      aria-label={active ? 'Quitar de favoritos' : 'Guardar en favoritos'}
      aria-pressed={active}
      className={`btn btn-secondary btn-sm ${active ? 'text-red-600' : ''} ${className}`}
      disabled={busy}
      onClick={(event) => {
        // Si el botón vive dentro de un <Link>, evita navegar.
        event.preventDefault();
        event.stopPropagation();
        setBusy(true);
        void toggle(productId)
          .catch(() => undefined)
          .finally(() => setBusy(false));
      }}
    >
      {active ? '♥' : '♡'}
    </button>
  );
}
