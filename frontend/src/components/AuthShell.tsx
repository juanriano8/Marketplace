import Link from 'next/link';
import { CheckCircle2, ShieldCheck, ShoppingBag, Store } from 'lucide-react';

/** Layout de dos columnas para login y registro. */
export function AuthShell({
  title,
  subtitle,
  children,
}: {
  title: string;
  subtitle: string;
  children: React.ReactNode;
}) {
  return (
    <div className="mx-auto grid max-w-4xl gap-5 py-6 lg:grid-cols-[1.05fr_0.95fr]">
      <section className="card p-6 sm:p-8">
        <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
          Marketplace
        </p>
        <h1 className="mt-1 text-2xl font-semibold tracking-tight text-ink-900">{title}</h1>
        <p className="mt-1 text-sm text-ink-500">{subtitle}</p>
        <div className="mt-6">{children}</div>
      </section>

      <aside className="hidden flex-col justify-between rounded-xl border border-brand-200 bg-brand-50 p-6 lg:flex">
        <div>
          <p className="text-[10px] font-semibold uppercase tracking-[0.16em] text-brand-700">
            Un solo panel
          </p>
          <h2 className="mt-2 text-xl font-semibold tracking-tight text-ink-900">
            Compra, vende y opera con el mismo sistema.
          </h2>
          <ul className="mt-5 space-y-3">
            {[
              { icon: ShoppingBag, text: 'Catálogo, carrito y compras' },
              { icon: Store, text: 'Productos, inventario y despachos' },
              { icon: ShieldCheck, text: 'Moderación y auditoría' },
            ].map((item) => {
              const Icon = item.icon;
              return (
                <li key={item.text} className="flex items-center gap-2.5 text-sm text-ink-700">
                  <span className="grid h-8 w-8 place-items-center rounded-lg bg-white text-brand-700">
                    <Icon className="h-4 w-4" />
                  </span>
                  {item.text}
                </li>
              );
            })}
          </ul>
        </div>

        <div className="rounded-lg border border-brand-200 bg-white/70 p-4">
          <p className="flex items-center gap-1.5 text-xs font-semibold text-brand-800">
            <CheckCircle2 className="h-4 w-4" />
            Sesión segura
          </p>
          <p className="mt-1 text-[11px] leading-5 text-ink-500">
            JWT en cookie httpOnly, RBAC por rol y sin tokens en localStorage.
          </p>
          <Link href="/productos" className="mt-3 inline-block text-[11px] font-semibold text-brand-700 hover:underline">
            Explorar el catálogo público →
          </Link>
        </div>
      </aside>
    </div>
  );
}
