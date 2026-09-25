import type { Metadata } from 'next';
import './globals.css';
import { AuthProvider } from '@/lib/auth';
import { Navbar } from '@/components/Navbar';

export const metadata: Metadata = {
  title: 'Marketplace — Panel Multi-Rol',
  description:
    'Panel web del marketplace: catálogo, carrito y compras para compradores, gestión de productos y despachos para vendedores, moderación para administradores.',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="es">
      <body className="min-h-screen flex flex-col">
        <AuthProvider>
          <Navbar />
          <main className="flex-1 mx-auto w-full max-w-7xl px-4 py-6">{children}</main>
          <footer className="border-t border-ink-200 bg-white">
            <div className="mx-auto max-w-7xl px-4 py-4 text-xs text-ink-500 flex flex-wrap items-center justify-between gap-2">
              <span>
                Marketplace API · Spring Boot 3.3.4 + Java 21 + PostgreSQL (Google Cloud SQL)
              </span>
              <a
                href="/api/v1/products"
                className="hover:text-brand-600"
                target="_blank"
                rel="noreferrer"
              >
                API pública
              </a>
            </div>
          </footer>
        </AuthProvider>
      </body>
    </html>
  );
}
