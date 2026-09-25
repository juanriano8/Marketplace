import type { ReactNode } from 'react';

/** Etiqueta de color para los estados (productos, órdenes, sub-órdenes). */
const TONE_CLASSES = {
  green: 'badge badge-green',
  yellow: 'badge badge-yellow',
  red: 'badge badge-red',
  blue: 'badge badge-blue',
  gray: 'badge badge-gray',
} as const;

export type Tone = keyof typeof TONE_CLASSES;

const STATUS_TONES: Record<string, Tone> = {
  // Productos
  ACTIVE: 'green',
  PENDING_APPROVAL: 'yellow',
  REJECTED: 'red',
  INACTIVE: 'gray',
  // Órdenes
  PAID: 'green',
  PENDING_PAYMENT: 'yellow',
  PAYMENT_FAILED: 'red',
  COMPLETED: 'blue',
  CANCELLED: 'gray',
  // Sub-órdenes
  PENDING: 'yellow',
  PROCESSING: 'blue',
  SHIPPED: 'green',
  DELIVERED: 'green',
  // Carrito
  ABANDONED: 'gray',
};

const STATUS_LABELS: Record<string, string> = {
  ACTIVE: 'Activo',
  PENDING_APPROVAL: 'Pendiente',
  REJECTED: 'Rechazado',
  INACTIVE: 'Inactivo',
  PAID: 'Pagada',
  PENDING_PAYMENT: 'Sin pagar',
  PAYMENT_FAILED: 'Pago fallido',
  COMPLETED: 'Completada',
  CANCELLED: 'Cancelada',
  PENDING: 'Pendiente',
  PROCESSING: 'En preparación',
  SHIPPED: 'Despachada',
  DELIVERED: 'Entregada',
  CHECKED_OUT: 'Finalizado',
  ABANDONED: 'Abandonado',
  INITIAL: 'Inicial',
  ADJUSTMENT: 'Ajuste',
  RESERVATION: 'Reserva',
  SALE: 'Venta',
};

export function StatusBadge({ status }: { status: string }) {
  const tone = STATUS_TONES[status] ?? 'gray';
  return <span className={TONE_CLASSES[tone]}>{STATUS_LABELS[status] ?? status}</span>;
}

export function Badge({ tone = 'gray', children }: { tone?: Tone; children: ReactNode }) {
  return <span className={TONE_CLASSES[tone]}>{children}</span>;
}

export function formatMoney(amount: number, currency = 'USD'): string {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
  }).format(amount ?? 0);
}

export function formatDate(value: string | null): string {
  if (!value) return '—';
  return new Intl.DateTimeFormat('es-CO', {
    dateStyle: 'medium',
    timeStyle: 'short',
  }).format(new Date(value));
}

export function Stars({ rating, size = 'md' }: { rating: number; size?: 'sm' | 'md' }) {
  const full = Math.round(rating);
  const className = size === 'sm' ? 'text-sm' : 'text-base';
  return (
    <span className={`${className} text-amber-500`} title={`${rating} de 5`}>
      {'★'.repeat(Math.max(0, Math.min(5, full)))}
      <span className="text-ink-300">{'★'.repeat(5 - Math.max(0, Math.min(5, full)))}</span>
    </span>
  );
}
