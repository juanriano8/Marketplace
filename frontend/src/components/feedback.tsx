import type { ReactNode } from 'react';

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
    <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 className="text-xl font-bold text-ink-900">{title}</h1>
        {description && <p className="mt-0.5 text-sm text-ink-500">{description}</p>}
      </div>
      {action}
    </div>
  );
}
