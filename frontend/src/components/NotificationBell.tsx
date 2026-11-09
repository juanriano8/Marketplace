'use client';

import Link from 'next/link';
import { useCallback, useEffect, useState } from 'react';
import { apiFetch } from '@/lib/api';
import { useAuth } from '@/lib/auth';

/** Campana de notificaciones con el contador de no leídas. */
export function NotificationBell() {
  const { session } = useAuth();
  const [count, setCount] = useState(0);

  const load = useCallback(async () => {
    if (!session) {
      setCount(0);
      return;
    }

    try {
      const result = await apiFetch<{ count: number }>('/api/v1/notifications/unread-count');
      setCount(result.count);
    } catch {
      setCount(0);
    }
  }, [session]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    const handler = () => void load();
    window.addEventListener('notifications:updated', handler);
    const interval = window.setInterval(() => void load(), 60_000);
    return () => {
      window.removeEventListener('notifications:updated', handler);
      window.clearInterval(interval);
    };
  }, [load]);

  if (!session) return null;

  return (
    <Link
      href="/notificaciones"
      className="relative rounded-lg px-3 py-1.5 text-sm font-medium text-ink-600 hover:bg-ink-100"
      aria-label={`Notificaciones${count > 0 ? `: ${count} sin leer` : ''}`}
    >
      🔔
      {count > 0 && (
        <span className="absolute -right-0.5 -top-0.5 grid h-5 min-w-5 place-items-center rounded-full bg-brand-600 px-1 text-[11px] font-bold text-white">
          {count > 9 ? '9+' : count}
        </span>
      )}
    </Link>
  );
}

/** Avisa a la campana de que el contador cambió. */
export function notifyNotificationsUpdated() {
  window.dispatchEvent(new Event('notifications:updated'));
}
