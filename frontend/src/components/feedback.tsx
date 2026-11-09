import type { ReactNode } from 'react';
import { AlertTriangle, Inbox } from 'lucide-react';

/** Aviso neutro (información). */
export function Info({ children }: { children: ReactNode }) {
  return (
    <div className="rounded-lg border border-brand-200 bg-brand-50 px-3 py-2 text-sm text-brand-800">
      {children}
    </div>
  );
}

/** Aviso de éxito. */
export function Success({ children }: { children: ReactNode }) {
  return (
    <div className="rounded-lg border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-800">
      {children}
    </div>
  );
}

/** Aviso de error. */
export function ErrorBox({ children }: { children: ReactNode }) {
  return (
    <div className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-800">
      {children}
    </div>
  );
}

/**
 * Estado de error de una vista con acción de reintento.
 * Se usa cuando falla la carga inicial de datos (no para errores de formulario).
 */
export function ErrorState({
  message,
  onRetry,
  retryLabel = 'Reintentar',
}: {
  message: string;
  onRetry?: () => void;
  retryLabel?: string;
}) {
  return (
    <div className="card border border-red-200 bg-red-50 p-6 text-center">
      <span className="mx-auto mb-2 grid h-9 w-9 place-items-center rounded-full bg-white text-red-600">
        <AlertTriangle className="h-4 w-4" />
      </span>
      <p className="font-semibold text-red-800">No se pudo cargar la información</p>
      <p className="mt-1 text-sm text-red-700">{message}</p>
      {onRetry && (
        <button type="button" className="btn btn-primary mt-4" onClick={onRetry}>
          {retryLabel}
        </button>
      )}
    </div>
  );
}

/** Bloque de esqueleto para estados de carga. */
export function Skeleton({ className = '' }: { className?: string }) {
  return <div className={`animate-pulse rounded-lg bg-ink-100 ${className}`} />;
}

/** Grid de tarjetas con la forma real de un catálogo. */
export function CardGridSkeleton({
  count = 6,
  cardClassName = 'h-72',
}: {
  count?: number;
  cardClassName?: string;
}) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      {Array.from({ length: count }).map((_, index) => (
        <Skeleton key={index} className={cardClassName} />
      ))}
    </div>
  );
}

/** Tabla con cabecera y filas esqueleto. */
export function TableSkeleton({ rows = 6 }: { rows?: number }) {
  return (
    <div className="card overflow-hidden">
      <div className="flex items-center gap-4 border-b border-ink-100 bg-ink-50 px-4 py-3">
        <Skeleton className="h-3 w-1/3" />
        <Skeleton className="h-3 w-16" />
        <Skeleton className="h-3 w-16" />
        <Skeleton className="ml-auto h-3 w-20" />
      </div>
      <div className="divide-y divide-ink-100">
        {Array.from({ length: rows }).map((_, index) => (
          <div key={index} className="flex items-center gap-4 px-4 py-4">
            <Skeleton className="h-4 flex-1" />
            <Skeleton className="h-4 w-20" />
            <Skeleton className="h-4 w-14" />
            <Skeleton className="h-7 w-24 rounded-md" />
          </div>
        ))}
      </div>
    </div>
  );
}

/** Lista de tarjetas apiladas (órdenes, notificaciones, carrito). */
export function ListSkeleton({
  rows = 4,
  itemClassName = 'h-24',
}: {
  rows?: number;
  itemClassName?: string;
}) {
  return (
    <div className="space-y-3">
      {Array.from({ length: rows }).map((_, index) => (
        <Skeleton key={index} className={itemClassName} />
      ))}
    </div>
  );
}

/** Detalle de producto: media + ficha lateral. */
export function DetailSkeleton() {
  return (
    <div className="space-y-4">
      <Skeleton className="h-4 w-48" />
      <div className="grid gap-6 lg:grid-cols-3">
        <Skeleton className="h-80 rounded-xl lg:col-span-2" />
        <Skeleton className="h-80 rounded-xl" />
      </div>
    </div>
  );
}

/** Aviso de advertencia. */
export function Warning({ children }: { children: ReactNode }) {
  return (
    <div className="rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-900">
      {children}
    </div>
  );
}

/** Estado vacío con mensaje y acción opcional. */
export function EmptyState({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="card p-10 text-center">
      <span className="mx-auto mb-3 grid h-10 w-10 place-items-center rounded-full bg-ink-50 text-ink-400">
        <Inbox className="h-5 w-5" />
      </span>
      <p className="font-semibold text-ink-800">{title}</p>
      {description && <p className="mt-1 text-sm text-ink-500">{description}</p>}
      {action && <div className="mt-4 flex justify-center">{action}</div>}
    </div>
  );
}

/** Indicador de carga. */
export function Loading({ label = 'Cargando…' }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-2 py-10 text-sm text-ink-500">
      <span className="h-4 w-4 animate-spin rounded-full border-2 border-ink-300 border-t-brand-600" />
      {label}
    </div>
  );
}

/** Encabezado de página con título y acción opcional. */
export function PageHeader({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: ReactNode;
}) {
  return (
    <div className="mb-6 flex flex-wrap items-end justify-between gap-3 border-b border-ink-100 pb-4">
      <div>
        <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
          Marketplace
        </p>
        <h1 className="mt-0.5 text-2xl font-semibold tracking-tight text-ink-900">{title}</h1>
        {description && <p className="mt-1 text-sm text-ink-500">{description}</p>}
      </div>
      {action}
    </div>
  );
}
