'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';
import { useAuth } from '@/lib/auth';
import { CartCount } from './CartCount';

type NavLink = { href: string; label: string };

const PUBLIC_LINKS: NavLink[] = [{ href: '/productos', label: 'Catálogo' }];

const BUYER_LINKS: NavLink[] = [
  { href: '/productos', label: 'Catálogo' },
  { href: '/carrito', label: 'Carrito' },
  { href: '/mis-compras', label: 'Mis compras' },
];

const SELLER_LINKS: NavLink[] = [
  { href: '/vendedor', label: 'Mi catálogo' },
  { href: '/vendedor/inventario', label: 'Inventario' },
  { href: '/vendedor/despachos', label: 'Despachos' },
];

const ADMIN_LINKS: NavLink[] = [
  { href: '/admin', label: 'Moderación' },
  { href: '/admin/vendedores', label: 'Vendedores' },
  { href: '/admin/ordenes', label: 'Órdenes' },
  { href: '/admin/inventario', label: 'Inventario' },
];

export function Navbar() {
  const { session, logout, isAdmin, isSeller, isBuyer, loading } = useAuth();
  const pathname = usePathname();
  const router = useRouter();
  const [open, setOpen] = useState(false);

  const links = isAdmin ? ADMIN_LINKS : isSeller ? SELLER_LINKS : isBuyer ? BUYER_LINKS : PUBLIC_LINKS;

  function handleLogout() {
    logout();
    setOpen(false);
    router.push('/');
  }

  return (
    <header className="sticky top-0 z-20 border-b border-ink-200 bg-white/95 backdrop-blur">
      <div className="mx-auto flex max-w-7xl items-center gap-4 px-4 py-3">
        <Link href="/" className="flex items-center gap-2 font-bold text-ink-900">
          <span className="grid h-7 w-7 place-items-center rounded-lg bg-brand-600 text-sm text-white">
            M
          </span>
          <span className="hidden sm:inline">Marketplace</span>
        </Link>

        <nav className="hidden items-center gap-1 md:flex">
          {links.map((link) => {
            const active = pathname === link.href || pathname.startsWith(`${link.href}/`);
            return (
              <Link
                key={link.href}
                href={link.href}
                className={`rounded-lg px-3 py-1.5 text-sm font-medium transition ${
                  active
                    ? 'bg-brand-50 text-brand-700'
                    : 'text-ink-600 hover:bg-ink-100 hover:text-ink-900'
                }`}
              >
                {link.label}
              </Link>
            );
          })}
        </nav>

        <div className="ml-auto flex items-center gap-2">
          {isBuyer && <CartCount />}

          {loading ? (
            <span className="h-8 w-24 animate-pulse rounded-lg bg-ink-100" />
          ) : session ? (
            <div className="flex items-center gap-2">
              <div className="hidden text-right sm:block">
                <p className="text-xs font-semibold text-ink-800">{session.email}</p>
                <p className="text-[11px] text-ink-500">{roleLabel(session.role)}</p>
              </div>
              <button type="button" onClick={handleLogout} className="btn btn-secondary btn-sm">
                Salir
              </button>
            </div>
          ) : (
            <>
              <Link href="/login" className="btn btn-secondary btn-sm">
                Entrar
              </Link>
              <Link href="/registro" className="btn btn-primary btn-sm">
                Registrarse
              </Link>
            </>
          )}

          <button
            type="button"
            aria-label="Abrir menú"
            onClick={() => setOpen((value) => !value)}
            className="btn btn-secondary btn-sm md:hidden"
          >
            ☰
          </button>
        </div>
      </div>

      {open && (
        <nav className="border-t border-ink-200 bg-white px-4 py-2 md:hidden">
          {links.map((link) => (
            <Link
              key={link.href}
              href={link.href}
              onClick={() => setOpen(false)}
              className="block rounded-lg px-3 py-2 text-sm font-medium text-ink-700 hover:bg-ink-100"
            >
              {link.label}
            </Link>
          ))}
        </nav>
      )}
    </header>
  );
}

export function roleLabel(role: string): string {
  if (role === 'ROLE_ADMIN') return 'Administrador';
  if (role === 'ROLE_SELLER') return 'Vendedor';
  if (role === 'ROLE_BUYER') return 'Comprador';
  return role;
}
