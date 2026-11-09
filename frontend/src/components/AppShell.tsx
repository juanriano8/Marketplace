'use client';

import Link from 'next/link';
import { usePathname, useRouter } from 'next/navigation';
import { useState } from 'react';
import {
  Bell,
  Boxes,
  ClipboardList,
  Heart,
  LayoutDashboard,
  LogOut,
  Menu,
  Package,
  ReceiptText,
  Search,
  ShieldCheck,
  ShoppingCart,
  Store,
  Truck,
  Users,
  X,
} from 'lucide-react';
import { useAuth } from '@/lib/auth';
import type { UserRole } from '@/lib/types';
import { CartCount } from './CartCount';
import { NotificationBell } from './NotificationBell';

type NavItem = {
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
};

type NavSection = {
  title: string;
  items: NavItem[];
};

const BUYER_NAV: NavSection[] = [
  {
    title: 'Comprar',
    items: [
      { href: '/productos', label: 'Catálogo', icon: Search },
      { href: '/carrito', label: 'Carrito', icon: ShoppingCart },
      { href: '/favoritos', label: 'Favoritos', icon: Heart },
      { href: '/mis-compras', label: 'Mis compras', icon: ReceiptText },
    ],
  },
  {
    title: 'Cuenta',
    items: [{ href: '/notificaciones', label: 'Notificaciones', icon: Bell }],
  },
];

const SELLER_NAV: NavSection[] = [
  {
    title: 'Tienda',
    items: [
      { href: '/vendedor', label: 'Mi catálogo', icon: Store },
      { href: '/vendedor/inventario', label: 'Inventario', icon: Boxes },
      { href: '/vendedor/despachos', label: 'Despachos', icon: Truck },
    ],
  },
  {
    title: 'Cuenta',
    items: [{ href: '/notificaciones', label: 'Notificaciones', icon: Bell }],
  },
];

const ADMIN_NAV: NavSection[] = [
  {
    title: 'Operación',
    items: [
      { href: '/admin', label: 'Moderación', icon: ShieldCheck },
      { href: '/admin/vendedores', label: 'Vendedores', icon: Users },
      { href: '/admin/ordenes', label: 'Órdenes', icon: ClipboardList },
      { href: '/admin/inventario', label: 'Inventario', icon: Package },
    ],
  },
  {
    title: 'Cuenta',
    items: [{ href: '/notificaciones', label: 'Notificaciones', icon: Bell }],
  },
];

const PUBLIC_NAV: NavSection[] = [
  {
    title: 'Marketplace',
    items: [{ href: '/productos', label: 'Catálogo', icon: Search }],
  },
];

const PAGE_TITLES: Record<string, string> = {
  '/': 'Inicio',
  '/productos': 'Catálogo',
  '/login': 'Iniciar sesión',
  '/registro': 'Crear cuenta',
  '/carrito': 'Carrito',
  '/checkout': 'Checkout',
  '/favoritos': 'Favoritos',
  '/mis-compras': 'Mis compras',
  '/notificaciones': 'Notificaciones',
  '/vendedor': 'Mi catálogo',
  '/vendedor/inventario': 'Inventario',
  '/vendedor/despachos': 'Despachos',
  '/admin': 'Moderación',
  '/admin/vendedores': 'Vendedores',
  '/admin/ordenes': 'Órdenes',
  '/admin/inventario': 'Inventario',
};

function navForRole(role: UserRole | undefined): NavSection[] {
  if (role === 'ROLE_ADMIN') return ADMIN_NAV;
  if (role === 'ROLE_SELLER') return SELLER_NAV;
  if (role === 'ROLE_BUYER') return BUYER_NAV;
  return PUBLIC_NAV;
}

export function roleLabel(role: string): string {
  if (role === 'ROLE_ADMIN') return 'Administrador';
  if (role === 'ROLE_SELLER') return 'Vendedor';
  if (role === 'ROLE_BUYER') return 'Comprador';
  return 'Invitado';
}

function isActive(pathname: string, href: string): boolean {
  if (href === '/') return pathname === '/';
  return pathname === href || pathname.startsWith(`${href}/`);
}

function SidebarContent({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = usePathname();
  const router = useRouter();
  const { session, logout } = useAuth();
  const sections = navForRole(session?.role);

  function handleLogout() {
    logout();
    onNavigate?.();
    router.push('/');
  }

  return (
    <div className="flex h-full flex-col bg-white">
      <div className="flex h-16 items-center gap-2.5 border-b border-ink-100 px-5">
        <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-sm font-bold text-white">
          M
        </span>
        <div>
          <p className="text-sm font-bold tracking-tight text-ink-900">Marketplace</p>
          <p className="text-[10px] font-medium uppercase tracking-[0.12em] text-ink-400">
            Panel operativo
          </p>
        </div>
      </div>

      <div className="border-b border-ink-100 px-5 py-4">
        <div className="flex items-center gap-2 rounded-lg bg-brand-50 px-3 py-2">
          <span className="h-2 w-2 rounded-full bg-brand-600" />
          <div className="min-w-0">
            <p className="truncate text-xs font-semibold text-brand-800">
              {roleLabel(session?.role ?? '')}
            </p>
            <p className="truncate text-[10px] text-ink-500">
              {session ? 'Sesión activa' : 'Explorando el catálogo'}
            </p>
          </div>
        </div>
      </div>

      <nav className="flex-1 space-y-5 overflow-y-auto px-3 py-4">
        {sections.map((section) => (
          <div key={section.title}>
            <p className="px-3 pb-1.5 text-[10px] font-semibold uppercase tracking-[0.14em] text-ink-400">
              {section.title}
            </p>
            <div className="space-y-0.5">
              {section.items.map((item) => {
                const Icon = item.icon;
                const active = isActive(pathname, item.href);
                return (
                  <Link
                    key={item.href}
                    href={item.href}
                    onClick={onNavigate}
                    className={`flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium transition ${
                      active
                        ? 'bg-brand-50 text-brand-700'
                        : 'text-ink-600 hover:bg-ink-50 hover:text-ink-900'
                    }`}
                  >
                    <Icon className={`h-4 w-4 ${active ? 'text-brand-600' : 'text-ink-400'}`} />
                    {item.label}
                  </Link>
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      {session ? (
        <div className="border-t border-ink-100 p-3">
          <div className="flex items-center gap-2.5 rounded-lg px-2 py-2">
            <span className="grid h-9 w-9 shrink-0 place-items-center rounded-full bg-ink-100 text-xs font-bold text-ink-700">
              {session.email.slice(0, 2).toUpperCase()}
            </span>
            <div className="min-w-0 flex-1">
              <p className="truncate text-xs font-semibold text-ink-800">{session.email}</p>
              <p className="text-[10px] text-ink-400">{roleLabel(session.role)}</p>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="rounded-md p-1.5 text-ink-400 hover:bg-ink-100 hover:text-danger"
              aria-label="Cerrar sesión"
              title="Cerrar sesión"
            >
              <LogOut className="h-4 w-4" />
            </button>
          </div>
        </div>
      ) : (
        <div className="border-t border-ink-100 p-3">
          <Link href="/login" className="btn btn-secondary w-full" onClick={onNavigate}>
            Iniciar sesión
          </Link>
        </div>
      )}
    </div>
  );
}

export function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname();
  const { session, loading } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const title = PAGE_TITLES[pathname] ?? 'Marketplace';
  const hasSidebar = Boolean(session);

  return (
    <div className="min-h-screen bg-ink-50">
      {hasSidebar && (
        <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r border-ink-100 lg:block">
          <SidebarContent />
        </aside>
      )}

      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-ink-900/40"
            aria-label="Cerrar menú"
            onClick={() => setMobileOpen(false)}
          />
          <aside className="absolute inset-y-0 left-0 w-72 max-w-[85vw] shadow-2xl">
            <button
              type="button"
              className="absolute right-3 top-4 z-10 rounded-md p-1.5 text-ink-500 hover:bg-ink-100"
              onClick={() => setMobileOpen(false)}
              aria-label="Cerrar menú"
            >
              <X className="h-5 w-5" />
            </button>
            <SidebarContent onNavigate={() => setMobileOpen(false)} />
          </aside>
        </div>
      )}

      <div className={`${hasSidebar ? 'lg:pl-64' : ''} flex min-h-screen flex-col`}>
        <header className="sticky top-0 z-20 border-b border-ink-100 bg-white/95 backdrop-blur">
          <div className="flex h-14 items-center gap-3 px-4 lg:px-6">
            {hasSidebar && (
              <button
                type="button"
                className="rounded-md p-1.5 text-ink-500 hover:bg-ink-100 lg:hidden"
                onClick={() => setMobileOpen(true)}
                aria-label="Abrir menú"
              >
                <Menu className="h-5 w-5" />
              </button>
            )}

            {!hasSidebar && (
              <Link href="/" className="flex items-center gap-2">
                <span className="grid h-8 w-8 place-items-center rounded-lg bg-brand-600 text-sm font-bold text-white">
                  M
                </span>
                <span className="text-sm font-bold tracking-tight text-ink-900">Marketplace</span>
              </Link>
            )}

            <div className="hidden min-w-0 items-center gap-2 text-xs text-ink-400 sm:flex">
              <span className="font-medium text-ink-500">Panel</span>
              <span>/</span>
              <span className="truncate font-semibold text-ink-800">{title}</span>
            </div>

            <div className="ml-auto flex items-center gap-1.5">
              {!hasSidebar && (
                <nav className="mr-2 hidden items-center gap-1 sm:flex">
                  <Link
                    href="/productos"
                    className="rounded-lg px-3 py-1.5 text-sm font-medium text-ink-600 hover:bg-ink-100"
                  >
                    Catálogo
                  </Link>
                  <Link href="/login" className="btn btn-secondary btn-sm">
                    Entrar
                  </Link>
                  <Link href="/registro" className="btn btn-primary btn-sm">
                    Crear cuenta
                  </Link>
                </nav>
              )}

              {session?.role === 'ROLE_BUYER' && <CartCount />}
              {session && <NotificationBell />}

              {loading ? (
                <span className="h-8 w-8 animate-pulse rounded-full bg-ink-100" />
              ) : session ? (
                <div className="ml-1 hidden items-center gap-2 sm:flex">
                  <span className="grid h-8 w-8 place-items-center rounded-full bg-brand-600 text-[11px] font-bold text-white">
                    {session.email.slice(0, 2).toUpperCase()}
                  </span>
                </div>
              ) : null}
            </div>
          </div>

          {!hasSidebar && (
            <nav className="flex items-center gap-1 border-t border-ink-100 px-3 py-2 sm:hidden">
              <Link
                href="/productos"
                className="rounded-lg px-2.5 py-1.5 text-xs font-medium text-ink-600 hover:bg-ink-100"
              >
                Catálogo
              </Link>
              <Link
                href="/login"
                className="ml-auto rounded-lg px-2.5 py-1.5 text-xs font-medium text-ink-600 hover:bg-ink-100"
              >
                Entrar
              </Link>
              <Link href="/registro" className="btn btn-primary btn-sm">
                Crear cuenta
              </Link>
            </nav>
          )}

          <div className="hidden border-t border-ink-100 bg-brand-50/70 px-4 py-1.5 lg:px-6 lg:block">
            <div className="flex items-center justify-between gap-3 text-[11px]">
              <span className="font-medium text-brand-800">
                {session
                  ? `Estás operando como ${roleLabel(session.role)}`
                  : 'Catálogo público del marketplace'}
              </span>
              <span className="text-ink-500">
                Spring Boot · Next.js · PostgreSQL Neon
              </span>
            </div>
          </div>
        </header>

        <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6 lg:px-6">{children}</main>

        <footer className="border-t border-ink-100 bg-white">
          <div className="mx-auto flex max-w-7xl flex-wrap items-center justify-between gap-2 px-4 py-4 text-xs text-ink-500 lg:px-6">
            <span>Marketplace · Spring Boot 3.3.4 + Java 21 + PostgreSQL (Neon)</span>
            <a href="/api/v1/products" className="hover:text-brand-600" target="_blank" rel="noreferrer">
              API pública
            </a>
          </div>
        </footer>
      </div>
    </div>
  );
}
