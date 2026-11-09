'use client';

import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { apiFetch } from './api';
import { useAuth } from './auth';

type FavoritesContextValue = {
  ids: Set<string>;
  loading: boolean;
  isFavorite: (productId: string) => boolean;
  toggle: (productId: string) => Promise<void>;
  refresh: () => Promise<void>;
};

const FavoritesContext = createContext<FavoritesContextValue | null>(null);

/** Estado compartido de favoritos del comprador. */
export function FavoritesProvider({ children }: { children: React.ReactNode }) {
  const { session } = useAuth();
  const [ids, setIds] = useState<Set<string>>(new Set());
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (session?.role !== 'ROLE_BUYER') {
      setIds(new Set());
      setLoading(false);
      return;
    }

    setLoading(true);
    try {
      const list = await apiFetch<string[]>('/api/v1/favorites/ids');
      setIds(new Set(list));
    } catch {
      setIds(new Set());
    } finally {
      setLoading(false);
    }
  }, [session]);

  useEffect(() => {
    void refresh();
  }, [refresh]);

  const toggle = useCallback(
    async (productId: string) => {
      const wasFavorite = ids.has(productId);
      const next = new Set(ids);

      if (wasFavorite) {
        next.delete(productId);
      } else {
        next.add(productId);
      }
      setIds(next);

      try {
        await apiFetch(`/api/v1/favorites/${productId}`, {
          method: wasFavorite ? 'DELETE' : 'POST',
        });
      } catch (error) {
        setIds(ids);
        throw error;
      }
    },
    [ids],
  );

  const value = useMemo<FavoritesContextValue>(
    () => ({
      ids,
      loading,
      isFavorite: (productId) => ids.has(productId),
      toggle,
      refresh,
    }),
    [ids, loading, toggle, refresh],
  );

  return <FavoritesContext.Provider value={value}>{children}</FavoritesContext.Provider>;
}

export function useFavorites(): FavoritesContextValue {
  const context = useContext(FavoritesContext);
  if (!context) {
    throw new Error('useFavorites debe usarse dentro de <FavoritesProvider>');
  }
  return context;
}
