'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch, errorMessage, type Page } from '@/lib/api';
import type { NotificationItem } from '@/lib/types';
import { RequireAuth } from '@/components/RequireAuth';
import { EmptyState, ErrorState, ListSkeleton, PageHeader } from '@/components/feedback';
import { formatDate } from '@/components/ui';
import { notifyNotificationsUpdated } from '@/components/NotificationBell';

export default function NotificationsPage() {
  return (
    <RequireAuth>
      <NotificationsContent />
    </RequireAuth>
  );
}

function NotificationsContent() {
  const [items, setItems] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const page = await apiFetch<Page<NotificationItem>>(
        '/api/v1/notifications?page=0&size=50&sort=createdAt,desc',
      );
      setItems(page.content);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  async function markRead(id: string) {
    setBusy(id);
    try {
      await apiFetch(`/api/v1/notifications/${id}/read`, { method: 'PATCH' });
      setItems((current) => current.map((item) => (item.id === id ? { ...item, read: true } : item)));
      notifyNotificationsUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  async function markAllRead() {
    setBusy('all');
    try {
      await apiFetch('/api/v1/notifications/read-all', { method: 'PATCH' });
      setItems((current) => current.map((item) => ({ ...item, read: true })));
      notifyNotificationsUpdated();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(null);
    }
  }

  if (loading) return <ListSkeleton rows={5} itemClassName="h-24" />;

  const hasUnread = items.some((item) => !item.read);

  return (
    <div>
      <PageHeader
        title="Notificaciones"
        description="Avisos de tus pedidos, productos y reseñas."
        action={
          hasUnread ? (
            <button
              type="button"
              className="btn btn-secondary btn-sm"
              disabled={busy === 'all'}
              onClick={() => void markAllRead()}
            >
              {busy === 'all' ? 'Marcando…' : 'Marcar todas como leídas'}
            </button>
          ) : undefined
        }
      />

      {error && <ErrorState message={error} onRetry={load} />}

      {items.length === 0 ? (
        <EmptyState
          title="No tienes notificaciones"
          description="Aquí aparecerán los avisos de tus pedidos, productos y reseñas."
          action={
            <Link href="/productos" className="btn btn-primary">
              Ir al catálogo
            </Link>
          }
        />
      ) : (
        <div className="space-y-2">
          {items.map((item) => (
            <article
              key={item.id}
              className={`card p-4 ${item.read ? 'opacity-70' : 'border-brand-200'}`}
            >
              <div className="flex flex-wrap items-start justify-between gap-3">
                <div className="min-w-[220px] flex-1">
                  <p className="text-sm font-semibold text-ink-900">{item.title}</p>
                  <p className="mt-1 text-sm text-ink-600">{item.message}</p>
                  <p className="mt-1 text-[11px] text-ink-400">{formatDate(item.createdAt)}</p>
                </div>

                <div className="flex items-center gap-2">
                  {item.link && (
                    <Link href={item.link} className="btn btn-secondary btn-sm">
                      Abrir
                    </Link>
                  )}
                  {!item.read && (
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      disabled={busy === item.id}
                      onClick={() => void markRead(item.id)}
                    >
                      {busy === item.id ? 'Marcando…' : 'Marcar leída'}
                    </button>
                  )}
                </div>
              </div>
            </article>
          ))}
        </div>
      )}
    </div>
  );
}
